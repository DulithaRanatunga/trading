package trading;

public class SecurityForDay {

	private String code;
	private String date; // Dont parse for now
	private double open;
	private double high;
	private double low;
	private double close;
	private int volume;

	public SecurityForDay(String[] row) {
		if (row.length != 7) {
			System.err.println("Invalid Data");
			System.exit(-1);
		}
		this.code = row[0];
		this.date = row[1];
		this.open = Double.valueOf(row[2]);
		this.high = Double.valueOf(row[3]);
		this.low = Double.valueOf(row[4]);
		this.close = Double.valueOf(row[5]);
		this.volume = Integer.valueOf(row[6]);
	}

	public String getCode() {
		return code;
	}

	public String getDate() {
		return date;
	}

	public double getOpen() {
		return open;
	}

	public double getHigh() {
		return high;
	}

	public double getLow() {
		return low;
	}

	public double getAverage() {
		return (this.close + this.open) / 2.0;
	}

	public double getClose() {
		return close;
	}

	public int getVolume() {
		return volume;
	}

	public Double getTotalTradingValue() {
		return (this.high + this.low) / 2 * this.volume;
	}
}
