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
		this.setCode(row[0]);
		this.setDate(row[1]);
		this.setOpen(Double.valueOf(row[2]));
		this.setHigh(Double.valueOf(row[3]));
		this.setLow(Double.valueOf(row[4]));
		this.setClose(Double.valueOf(row[5]));
		this.setVolume(Integer.valueOf(row[6]));
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getClose() {
		return close;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public Double getTotalTradingValue() {
		return (this.high + this.low) / 2 * this.volume;
	}
}
