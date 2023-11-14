/**
 * @author Mia Kallio
 */

package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Rules {
	/*
	 * PAM Kaupan alan TES: §20 2. 
	 * Lomaa ansaitaan täydeltä lomanmääräytymiskuukaudelta työsuhteen kestettyä
	 * lomanmääräytymisvuoden (1.4.–31.3.) loppuun mennessä:
	 * alle vuoden: 2 arkipäivää
	 * vähintään vuoden: 2,5 arkipäivää.
	 */
	private static int cutOffMonth = 3;
	private static int cutOffDay = 31;
	
	private static BigDecimal firstYearVacationDays = new BigDecimal(2);
	private static BigDecimal defaultVacationDays = new BigDecimal(2.5);
	
	/**
	 * PAM Kaupan alan TES: §20 2.
	 * Täysi lomanmääräytymiskuukausi on kalenterikuukausi, jonka aikana työntekijä on työskennellyt:
	 * vähintään 14 päivää
	 * vähintään 35 tuntia.
	 */
	private static BigDecimal vacationDayRquirement = new BigDecimal(14);
	private static BigDecimal vacationHourRequirement = new BigDecimal(35);
	
	/**
	 * PAM Kaupan alan TES: §20 7.
	 * Lomapalkkaan ja -korvaukseen lisätään lomanmääräytymisvuoden aikana maksetuista lisistä:
	 * 10 % työsuhteen kestettyä lomanmääräytymisvuoden loppuun (31.3.) mennessä alle 1 vuoden
	 * 12,5 % työsuhteen kestettyä lomanmääräytymisvuoden loppuun (31.3.) mennessä vähintään 1 vuoden.
	 */
	
	private static BigDecimal firstYearPercent = new BigDecimal(10);
	private static BigDecimal defaultPercent = new BigDecimal(12.5);
		
	public static BigDecimal getPercentileMultiplier(LocalDate startDate, LocalDate cutOffDate) {
		BigDecimal percent = new BigDecimal(100);
		if (startDate.isBefore(cutOffDate.minusYears(1))) return defaultPercent.divide(percent);
		else return firstYearPercent.divide(percent);
	}
	
	public static BigDecimal getVacationDayMultiplier(LocalDate startDate, LocalDate cutOffDate) {
		if (startDate.isBefore(cutOffDate.minusYears(1))) return defaultVacationDays;
		else return firstYearVacationDays;
	}
	
	public static BigDecimal getVacationDaysRequirement() {
		return vacationDayRquirement;
	}

	public static BigDecimal getVacationHoursRequirement() {
		return vacationHourRequirement;
	}	
	
	public static LocalDate getCutOffDate(int year) { 
		return LocalDate.of(year, cutOffMonth, cutOffDay);
	}

	/**
	 * Vuosilomalaki 18.3.2005/162 §11
	 * Muun kuin viikko- tai kuukausipalkalla työskentelevän sellaisen työntekijän vuosilomapalkka, 
	 * joka sopimuksen mukaan työskentelee vähintään 14 päivänä kalenterikuukaudessa, lasketaan kertomalla
	 * hänen keskipäiväpalkkansa lomapäivien määrän perusteella määräytyvällä kertoimella:
	 * 
	 * @param amount of vacation days
	 * @return corresponding vacation pay multiplier
	 */
	public static BigDecimal getVacationPayMultiplier(int vacationDays) {
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
		return BigDecimal.valueOf(multiplier);
	}
}
