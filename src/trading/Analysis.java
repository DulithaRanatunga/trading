package trading;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;

/**
 * Overall goals::
 * 1. Parse asx CSV data.
 * Out:: How many stocks exist
 * 2. Aggregate CSV data for a week: Stock, Avg Total trade
 * How many of these trade > $1 mill/ day for a week?
 * Out:: List of stocks, Total trade.
 * Out:: How many were culled?
 * 3. If less than 100, Commsec ReST => Get data
 * 4. Pass through and find
 * (for range 2016 + Jan 2017)
 * -- Num days that +3% in next week.
 * -- Num days that -3% in next week.
 * -- Longest streak (and start date)
 * 
 * @author dulitha
 *
 */
public class Analysis {
	private static final double LIMIT = 1000000;
	private static final int LOOKAHEAD_PERIOD = 5; // num trading days.

	public static void main(String[] args) throws IOException {
		System.out.println("Analysis Start");
		Map<String, List<Double>> codeToTotalsMap = readAllFiles();
		System.out.println("Total codes: " + codeToTotalsMap.size());
		Map<String, Double> filteredCodes = filterStocksWithoutEnoughTradingVolume(codeToTotalsMap);
		System.out.println("Total codes after filtering:" + filteredCodes.size());
		System.out.println("Analysis Complete!");
	}

	private static Map<String, Double> filterStocksWithoutEnoughTradingVolume(
			Map<String, List<Double>> codeToTotalsMap) {
		Map<String, Double> codeToAverageMap = new HashMap<String, Double>();
		codeToTotalsMap.forEach((code, list) -> {
			Double average = list.stream().mapToDouble(i -> i).average().getAsDouble();
			if (average > LIMIT) {
				codeToAverageMap.put(code, average);
			}
		});
		return codeToAverageMap;
	}

	private static Map<String, List<Double>> readAllFiles() throws IOException {
		Map<String, List<Double>> codeToAverageMap = new HashMap<>();
		Files.walk(Paths.get("src/resources/data")).filter(Files::isRegularFile).forEach(path -> {
			try {
				Analysis.readAllSecurityFile(path.toFile(), codeToAverageMap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		return codeToAverageMap;
	}

	/**
	 * Populates the averages map with code and $ data.
	 * 
	 * @param f
	 *            - csv file with format (code, date, open, high, low, close,
	 *            volume)
	 * @param averages
	 *            - Map<code, [$ in day]
	 * @throws IOException
	 */
	public static void readAllSecurityFile(File f, Map<String, List<Double>> averages) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(f));
		List<String[]> rows = reader.readAll();
		reader.close();
		rows.forEach(row -> {
			SecurityForDay sec = new SecurityForDay(row);
			List<Double> list = averages.get(sec.getCode());
			if (list == null) {
				List<Double> newList = new LinkedList<Double>();
				newList.add(sec.getTotalTradingValue());
				averages.put(sec.getCode(), newList);
			} else {
				list.add(sec.getTotalTradingValue());
			}
		});
	}
}
