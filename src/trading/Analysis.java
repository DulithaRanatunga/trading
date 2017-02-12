package trading;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

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
	// how much daily trading volume should a stock have in $.
	private static final double TRADING_LIMIT = 1000000;
	// num trading days.
	private static final int LOOKAHEAD_PERIOD = 5;
	// how much change do we want.
	private static final double PERCENTAGE_DEVIATION_LIMIT = 1.05;

	public static void main(String[] args) throws IOException {
		System.out.println("Analysis Start");
		Map<String, Security> securities = readAllFiles();
		System.out.println("Total codes: " + securities.size());
		List<Security> filteredSecurities = getStocksWithMinimumTradingVolume(securities, TRADING_LIMIT);
		System.out.println("Total codes after filtering:" + filteredSecurities.size());
		filteredSecurities.forEach(sec -> sec.process(LOOKAHEAD_PERIOD, PERCENTAGE_DEVIATION_LIMIT));
		outputResultsToCsv(filteredSecurities);
		System.out.println("Analysis Complete!");
	}

	private static void outputResultsToCsv(List<Security> securities) throws IOException {
		File results = new File("results.csv");
		CSVWriter writer = new CSVWriter(new FileWriter(results));
		String[] headers = new String[] {
				"Code",
				"Num days with positive deviation",
				"Num days with negative deviation",
				"Num days with both",
				"Longest Streak",
				"Streak start",
				"Average Trading $" };

		writer.writeNext(headers);
		securities.forEach(sec -> {
			writer.writeNext(new String[] {
					sec.getCode(),
					Integer.toString(sec.getPositiveCount()),
					Integer.toString(sec.getNegativeCount()),
					Integer.toString(sec.getBothCount()),
					Integer.toString(sec.getStreakLength()),
					sec.getStreakStart(),
					Double.toString(sec.getAverageTradingVolume()) });
		});
		writer.close();
	}

	private static List<Security> getStocksWithMinimumTradingVolume(Map<String, Security> securities, double minimum) {
		List<Security> secs = new LinkedList<Security>();
		securities.forEach((String code, Security sec) -> {
			if (sec.getAverageTradingVolume() > minimum) {
				secs.add(sec);
			}
		});
		return secs;
	}

	private static Map<String, Security> readAllFiles() throws IOException {
		Map<String, Security> codeToSecuritiesMap = new HashMap<>();
		Files.walk(Paths.get("src/resources/data")).filter(Files::isRegularFile).forEach(path -> {
			try {
				Analysis.readFileWithDataForOneDay(path.toFile(), codeToSecuritiesMap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		return codeToSecuritiesMap;
	}

	/**
	 * Reads all the security data into memory
	 * 
	 * @param f
	 *            - csv file with format (code, date, open, high, low, close,
	 *            volume)
	 * @param securities
	 *            - Map<code, security>
	 * @throws IOException
	 */
	public static void readFileWithDataForOneDay(File f, Map<String, Security> securities) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(f));
		List<String[]> rows = reader.readAll();
		reader.close();
		rows.forEach(row -> {
			SecurityForDay secForDay = new SecurityForDay(row);
			Security sec = securities.get(secForDay.getCode());
			if (sec == null) {
				sec = new Security(secForDay.getCode());
			}
			sec.addDataForNextDay(secForDay);
			securities.put(sec.getCode(), sec);
		});
	}
}
