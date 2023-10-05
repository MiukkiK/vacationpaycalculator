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
	
	private static int vacationDayRquirement = 14;
	private static double vacationHourRequirement = 35;
	
	public static double getPercentileMultiplier(LocalDate startDate, LocalDate cutOffDate) {
		if (startDate.isBefore(cutOffDate.minusYears(2))) return defaultPercent / 100;
		else return firstYearPercent / 100;
	}
	
	public static double getVacationDayMultiplier(LocalDate startDate, LocalDate cutOffDate) {
		if (startDate.isBefore(cutOffDate.minusYears(2))) return defaultVacationDays;
		else return firstYearVacationDays;
	}
	
	public static int getVacationDaysRequirement() {
		return vacationDayRquirement;
	}

	public static double getVacationHoursRequirement() {
		return vacationHourRequirement;
	}	
	
	public static LocalDate getCutOffDate(int year) { 
		return LocalDate.of(year, cutOffMonth, cutOffDay);
	}

	@Override
	public String toString() {
		return "Rules: First Year Percent = " + firstYearPercent + ", Default Percent = " + defaultPercent + "]";
	}

}
