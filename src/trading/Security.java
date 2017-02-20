package trading;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class Security {
	private String code;
	private List<SecurityForDay> days;
	private Double averageTradingVolume;
	private int positiveCount;
	private int negativeCount;
	private int bothCount;
	private int streaklength;
	private String streakstart;
	private Double worst = 0.0;
	private Double best = 0.0;

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

	public void process(int lookahead, double percentage, Function<SecurityForDay, Double> metric) {
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
			boolean posFound = this.positiveFound(sublist, percentage, metric);
			boolean negFound = this.negativeFound(sublist, percentage, metric);
			checkBest(sublist, metric);
			checkWorst(sublist, metric);
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
		if (currentStreak >= this.streaklength) {
			this.streaklength = currentStreak;
			this.streakstart = currentStreakStart;
		}
	}

	private void checkWorst(List<SecurityForDay> sublist, Function<SecurityForDay, Double> metric) {
		double start = sublist.get(0).getOpen();
		double min = sublist.stream().mapToDouble(d -> metric.apply(d)).min().orElse(start);
		double loss = (start - min) / start;
		this.worst = Math.max(this.worst, loss);
	}

	private void checkBest(List<SecurityForDay> sublist, Function<SecurityForDay, Double> metric) {
		double start = sublist.get(0).getOpen();
		double max = sublist.stream().mapToDouble(d -> metric.apply(d)).max().orElse(0);
		this.best = Math.max(this.best, max / start);
	}

	private boolean negativeFound(List<SecurityForDay> sublist, double percentage,
			Function<SecurityForDay, Double> metric) {
		double target = sublist.get(0).getOpen() / percentage;
		return sublist.stream().map(d -> metric.apply(d) <= target).findFirst().orElse(false);
	}

	private boolean positiveFound(List<SecurityForDay> sublist, double percentage,
			Function<SecurityForDay, Double> metric) {
		double target = sublist.get(0).getOpen() * percentage;
		return sublist.stream().map(d -> metric.apply(d) >= target).findFirst().orElse(false);
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

	public Double getWorst() {
		return worst * -100;
	}

	public Double getBest() {
		return best * 100;
	}

}
