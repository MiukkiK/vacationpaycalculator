package miukkik.vacationpaycalculator;

import java.time.LocalDate;

public class Rules {
	//
	private static double firstYearPercent = 10;
	private static double defaultPercent = 12.5;
	
	private static double firstYearVacationDays = 2;
	private static double defaultVacationDays = 2.5;
	
	//
	private static int cutOffMonth = 3;
	private static int cutOffDay = 31;
	
	private static int salariedDayRquirement = 14;
	private static double nonSalariedHourRequirement = 35;
	
	public static double getFirstYearPercent() {
		return firstYearPercent;
	}

	public static double getDefaultPercent() {
		return defaultPercent;
	}
	
	public static int getSalariedDayRequirement() {
		return salariedDayRquirement;
	}

	public static double getNonSalariedHourRequirement() {
		return nonSalariedHourRequirement;
	}	
	
	public static double getFirstYearVacationDays() {
		return firstYearVacationDays;
	}

	public static double getDefaultVacationDays() {
		return defaultVacationDays;
	}

	public static LocalDate getCutOffDate(int year) { 
		return LocalDate.of(year, cutOffMonth, cutOffDay);
	}

	@Override
	public String toString() {
		return "Rules: First Year Percent = " + firstYearPercent + ", Default Percent = " + defaultPercent + "]";
	}

}
