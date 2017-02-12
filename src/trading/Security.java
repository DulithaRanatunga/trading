package trading;

import java.util.LinkedList;
import java.util.List;

public class Security {
	private String code;
	private List<SecurityForDay> days;
	private Double averageTradingVolume;
	private int positiveCount;
	private int negativeCount;
	private int bothCount;
	private int streaklength;
	private String streakstart;

	public Security(String code) {
		this.code = code;
		this.days = new LinkedList<SecurityForDay>();
		this.positiveCount = 0;
		this.negativeCount = 0;
		this.bothCount = 0;
		this.streaklength = 0;
		this.streakstart = "Undefined";
	}

	public void addDataForNextDay(SecurityForDay data) {
		this.getDays().add(data);
	}

	public String getCode() {
		return code;
	}

	public List<SecurityForDay> getDays() {
		return days;
	}

	public double getAverageTradingVolume() {
		if (this.averageTradingVolume == null) {
			this.averageTradingVolume = this.days.stream().mapToDouble(day -> day.getTotalTradingValue()).average()
					.getAsDouble();
		}
		return this.averageTradingVolume;
	}

	public void process(int lookahead, double percentage) {
		this.positiveCount = 0;
		this.negativeCount = 0;
		this.bothCount = 0;
		this.streaklength = 0;
		int currentStreak = 0;
		String currentStreakStart = "No streak found";
		boolean restartStreak = true;
		List<SecurityForDay> sublist;
		for (int i = 0; i < days.size() - lookahead; i++) {
			sublist = days.subList(i, i + lookahead);
			boolean posFound = this.positiveFound(sublist, percentage);
			boolean negFound = this.negativeFound(sublist, percentage);
			if (posFound) {
				this.positiveCount++;
				if (restartStreak) {
					restartStreak = false;
					currentStreak = 1;
					currentStreakStart = sublist.get(0).getDate();
				} else {
					currentStreak++;
				}
			} else {
				restartStreak = true;
				if (currentStreak > this.streaklength) {
					this.streaklength = currentStreak;
					this.streakstart = currentStreakStart;
				}
			}
			if (negFound) {
				this.negativeCount++;
			}
			if (posFound && negFound) {
				this.bothCount++;
			}
		}
		if (currentStreak > this.streaklength) {
			this.streaklength = currentStreak;
			this.streakstart = currentStreakStart;
		}
	}

	private boolean negativeFound(List<SecurityForDay> sublist, double percentage) {
		double start = sublist.get(0).getOpen();
		for (SecurityForDay day : sublist) {
			// 100 - 10% = 90 = 100 - 100/100*10
			if (day.getClose() < (start - (start / (100 * (percentage - 1))))) {
				return true;
			}
		}
		return false;
	}

	private boolean positiveFound(List<SecurityForDay> sublist, double percentage) {
		double start = sublist.get(0).getOpen();
		for (SecurityForDay day : sublist) {
			if (day.getHigh() / start > percentage) {
				return true;
			}
		}
		return false;
	}

	public int getPositiveCount() {
		return this.positiveCount;
	}

	public int getBothCount() {
		return this.bothCount;
	}

	public int getNegativeCount() {
		return this.negativeCount;
	}

	public int getStreakLength() {
		return this.streaklength;
	}

	public String getStreakStart() {
		return this.streakstart;
	}

}
