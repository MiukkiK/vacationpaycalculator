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
		if (startDate.isBefore(cutOffDate.minusYears(1))) return defaultPercent / 100;
		else return firstYearPercent / 100;
	}
	
	public static double getVacationDayMultiplier(LocalDate startDate, LocalDate cutOffDate) {
		if (startDate.isBefore(cutOffDate.minusYears(1))) return defaultVacationDays;
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

	public static double getVacationPayMultiplier(int vacationDays) {
		double multiplier;
		switch (vacationDays) {
		case 2:
			multiplier = 1.8;
			break;
		case 3:
			multiplier = 2.7;
			break;
		case 4:
			multiplier = 3.6;
			break;
		case 5:
			multiplier = 4.5;
			break;
		case 6:
			multiplier = 5.4;
			break;
		case 7:
			multiplier = 6.3;
			break;
		case 8:
			multiplier = 7.2;
			break;
		case 9:
			multiplier = 8.1;
			break;
		case 10:
			multiplier = 9.0;
			break;
		case 11:
			multiplier = 9.9;
			break;
		case 12:
			multiplier = 10.8;
			break;
		case 13:
			multiplier = 11.8;
			break;
		case 14:
			multiplier = 12.7;
			break;
		case 15:
			multiplier = 13.6;
			break;
		case 16:
			multiplier = 14.5;
			break;
		case 17:
			multiplier = 15.5;
			break;
		case 18:
			multiplier = 16.4;
			break;
		case 19:
			multiplier = 17.4;
			break;
		case 20:
			multiplier = 18.3;
			break;
		case 21:
			multiplier = 19.3;
			break;
		case 22:
			multiplier = 20.3;
			break;
		case 23:
			multiplier = 21.3;
			break;
		case 24:
			multiplier = 22.2;
			break;
		case 25:
			multiplier = 23.2;
			break;
		case 26:
			multiplier = 24.1;
			break;
		case 27:
			multiplier = 25.0;
			break;
		case 28:
			multiplier = 25.9;
			break;
		case 29:
			multiplier = 26.9;
			break;
		case 30:
			multiplier = 27.8;
			break;
		default:
			multiplier = 0;
		}
		return multiplier;
	}
	
	@Override
	public String toString() {
		return "Rules: First Year Percent = " + firstYearPercent + ", Default Percent = " + defaultPercent + "]";
	}

}
