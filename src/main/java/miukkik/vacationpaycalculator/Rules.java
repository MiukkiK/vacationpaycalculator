package miukkik.vacationpaycalculator;

import java.time.LocalDate;

public class Rules {
	//
	private static double firstYearPercent = 10;
	private static double defaultPercent = 12.5;
	//
	private static int cutOffMonth = 3;
	private static int cutOffDay = 31;
	
	public static double getFirstYearPercent() {
		return firstYearPercent;
	}

	public static double getDefaultPercent() {
		return defaultPercent;
	}

	public static LocalDate getCutOffDate(int year) { 
		return LocalDate.of(year, cutOffMonth, cutOffDay);
	}

	@Override
	public String toString() {
		return "Rules: First Year Percent = " + firstYearPercent + ", Default Percent = " + defaultPercent + "]";
	}
}
