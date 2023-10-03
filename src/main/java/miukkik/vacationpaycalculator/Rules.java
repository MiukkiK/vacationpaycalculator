package miukkik.vacationpaycalculator;

import java.time.LocalDate;

public class Rules {
	private double firstYearPercent;
	private double defaultPercent;
	LocalDate cutOffDate;
	
	public Rules() {
		firstYearPercent = 10;
		defaultPercent = 12.5;
	}
	
	public double getFirstYearPercent() {
		return (firstYearPercent / 100);
	}
	public void setFirstYearPercent(double firstYearPercent) {
		this.firstYearPercent = firstYearPercent;
	}
	public double getDefaultPercent() {
		return (defaultPercent / 100);
	}
	public void setDefaultPercent(double defaultPercent) {
		this.defaultPercent = defaultPercent;
	}
	
	@Override
	public String toString() {
		return "Conditions [firstYearPercent=" + firstYearPercent + ", defaultPercent=" + defaultPercent
				+ "]";
	}
	
}
