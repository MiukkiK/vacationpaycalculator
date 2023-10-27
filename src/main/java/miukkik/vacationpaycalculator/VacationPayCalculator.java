/*
 * @author Mia Kallio
 */

package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class VacationPayCalculator {
	private final BigDecimal[] monthlyHours = new BigDecimal[12];
	private final int[] monthlyDays = new int[12];
	private final int[] monthlyLeave = new int[12];
	private final BigDecimal[] monthlyPay = new BigDecimal[12];

	private int vacationDays;

	private BigDecimal averageDailyPay;
	
	private int category;
	//category 1 fields
	private BigDecimal monthlySalary;
	private BigDecimal monthlyWorkDays;
	private BigDecimal dailyPay;
	private BigDecimal vacationPay;
	
	//category 2 fields
	private BigDecimal totalPay;
	private int totalDays;
	private BigDecimal averageWeeklyWorkDays;
	private BigDecimal vacationPayMultiplier;

	//category 4 fields
	private BigDecimal percentilePayTotal;
	private BigDecimal missedPay;
	private BigDecimal percentileMultiplier;
	private BigDecimal percentileVacationPay;


	public VacationPayCalculator (EmployeeRecord record, int year) {

		final LocalDate cutOffDate = Rules.getCutOffDate(year);
		List<EmploymentData> filteredList = record.getRecordBetween(cutOffDate.minusYears(1), cutOffDate);

		// data processing to monthly totals
		
		for(EmploymentData data : filteredList) {

			int monthIndex = data.getDate().getMonthValue() - 1;
			/**
			 * Vuosilomalaki §7
			 * Työssäolon veroisena pidetään työstä poissaoloaikaa, jolta työnantaja on lain mukaan velvollinen maksamaan työntekijälle palkan.
			 *
			 * PAM Kaupan alan TES, §20 10.
			 * 10. Maksettuun palkkaan lisätään laskennallista palkkaa:
			 * ...
			 * raskaus- ja vanhempainvapaan vuosilomaa kerryttävältä ajalta
			 * tilapäisen hoitovapaan ajalta (työsopimuslain 4:6 §)
			 */

			if (!data.getInfo().equals("")) { // days with info are not added as regular workdays or add to vacation total hour count. (weekday holiday bonus)
				if (data.getHours() == BigDecimal.ZERO) monthlyLeave[monthIndex]++; // days with info and no hours are treated as valid leave days, limited leave such as sick leave not implememnted yet.
			} else  {		
				monthlyDays[monthIndex]++;
				if (monthlyHours[monthIndex] == null) monthlyHours[monthIndex] = BigDecimal.ZERO;
				monthlyHours[monthIndex] = monthlyHours[monthIndex].add(data.getHours());
				/**
				 * PAM Kaupan alan TES: §20 6.
				 * Lomapalkka provision osalta lasketaan vuosilomalain mukaan.
				 */
				if (monthlyPay[monthIndex] == null) monthlyPay[monthIndex] = BigDecimal.ZERO;
				monthlyPay[monthIndex] = monthlyPay[monthIndex].add((data.getHours().multiply(data.getWage())).add(data.getBonus()));
			}
		}

		
		totalPay = BigDecimal.ZERO;
		for (BigDecimal thisMonthsPay : monthlyPay) {
			if (thisMonthsPay != null) totalPay = totalPay.add(thisMonthsPay);
		}
		totalDays = 0;
		for (int thisMonthsDays : monthlyDays) {
			totalDays += thisMonthsDays;
		}
		averageDailyPay = totalPay.divide(new BigDecimal(totalDays), 3, RoundingMode.HALF_UP);
		// vacation day calculation
		BigDecimal tempVacationDays = BigDecimal.ZERO;
		percentilePayTotal = BigDecimal.ZERO;
		for (int i = 0; i < 12; i++) {
			if (monthlyHours[i] == null) monthlyHours[i] = BigDecimal.ZERO;
			if (monthlyPay[i] == null) monthlyPay[i] = BigDecimal.ZERO;
						
			/**
			 * Vuosilomalaki 18.3.2005/162: §6
			 * Jos työntekijä on sopimuksen mukaisesti työssä niin harvoina päivinä,
			 * että hänelle ei tästä syystä kerry ainoatakaan 14 työssäolopäivää sisältävää
			 * kalenterikuukautta tai vain osa kalenterikuukausista sisältää 14 työssäolopäivää,
			 * täydeksi lomanmääräytymiskuukaudeksi katsotaan sellainen kalenterikuukausi, jonka
			 * aikana työntekijälle on kertynyt vähintään 35 työtuntia tai 7 §:ssä tarkoitettua 
			 * työssäolon veroista tuntia.
			 */
			if (((record.getCurrentWorkDays()*4 >= Rules.getVacationDaysRequirement()) && ((monthlyDays[i] + monthlyLeave[i]) >= Rules.getVacationDaysRequirement()))
					|| ((record.getCurrentWorkHours().compareTo(Rules.getVacationHoursRequirement()) != -1) && (monthlyHours[i].compareTo(Rules.getVacationHoursRequirement()) != -1)))
				tempVacationDays = tempVacationDays.add(BigDecimal.ONE);
			else {
				percentilePayTotal = percentilePayTotal.add(monthlyPay[i]);
				totalPay = totalPay.subtract(monthlyPay[i]);
			}
		}
		tempVacationDays = tempVacationDays.multiply(Rules.getVacationDayMultiplier(record.getStartDate(), cutOffDate));

		/**
		 * Vuosilomalaki §5
		 * Loman pituutta laskettaessa päivän osa pyöristetään täyteen lomapäivään.
		 */
		tempVacationDays = tempVacationDays.round(new MathContext(tempVacationDays.precision()-1, RoundingMode.CEILING));
		vacationDays = tempVacationDays.intValue();
		
		// Average pay per day if not salaried
		
		// vacation day based pay calculation

		/**
		 * PAM Kaupan alan TES, §20 6.
		 * Jos työntekijän työaika ja vastaavasti palkka on muuttunut lomanmääräytymisvuoden aikana
		 * ja hän on kuukausipalkkainen lomanmääräytymisvuoden lopussa (31.3.), hänen lomapalkkansa 
		 * lasketaan tämän pykälän 8–11. kohdan mukaan.
		 */
		if (record.isSalaried() && (filteredList.get(0).getWage() == filteredList.get(filteredList.size()-1).getWage()) && filteredList.get(0).getWorkHours() == filteredList.get(filteredList.size()-1).getWorkHours()) {
			if (vacationDays != 0) category = 1;
			/** not implemented yet
				monthlySalary = filteredList.get(0).getWorkHours() * filteredList.get(0).getWage();
				monthlyWorkDays = 4*5; // workdays per week not implemented in this version, assumed 5 days per week
				dailyPay = monthlySalary / monthlyWorkDays;

				vacationPay = dailyPay * vacationDays;
			 */
		} else {
			if (vacationDays != 0) category = 2;
				// salaried employee with category 2 not implemented yet	
				averageWeeklyWorkDays = new BigDecimal(record.getCurrentWorkDays()); // changing workdays not implemented yet
				vacationPayMultiplier = Rules.getVacationPayMultiplier(vacationDays);
				if (averageWeeklyWorkDays.compareTo(BigDecimal.ZERO) == 0 ) vacationPay = averageDailyPay.multiply(vacationPayMultiplier);
				else vacationPay = averageDailyPay.multiply(averageWeeklyWorkDays.divide(new BigDecimal(5)).multiply(vacationPayMultiplier));
		}

		// percentile vacation pay calculation

		BigDecimal totalLeaveDays = BigDecimal.ZERO;
		for (int leaveThisMonth : monthlyLeave) {
			totalLeaveDays = totalLeaveDays.add(new BigDecimal(leaveThisMonth));
		}
		percentileVacationPay = BigDecimal.ZERO;
		if ((percentilePayTotal != BigDecimal.ZERO) || (totalLeaveDays != BigDecimal.ZERO)) {

			if (category == 1) missedPay = totalLeaveDays.multiply(dailyPay); else missedPay = totalLeaveDays.multiply(averageDailyPay);
			
			percentileMultiplier = Rules.getPercentileMultiplier(record.getStartDate(), cutOffDate);
			percentileVacationPay = (percentilePayTotal.add(missedPay)).multiply(percentileMultiplier);		
		}
	}



	public void printMonthlyInformation() {
		String[] months = new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
		for (int i=0; i < months.length; i++) {
			System.out.println(months[i] + ": Workdays - " + monthlyDays[i] + ", Leave days - " + monthlyLeave[i] + ", Work hours - " + monthlyHours[i] + ", Monthly pay - " + monthlyPay[i]);
		}
		System.out.println("Category is " + category);
	}

	public BigDecimal[] getMonthlyHours() {
		return monthlyHours;
	}

	public int[] getMonthlyDays() {
		return monthlyDays;
	}

	public int[] getMonthlyLeave() {
		return monthlyLeave;
	}

	public BigDecimal[] getMonthlyPay() {
		return monthlyPay;
	}

	public int getVacationDays() {
		return vacationDays;
	}

	public BigDecimal getAverageDailyPay() {
		return averageDailyPay;
	}

	public int getCategory() {
		return category;
	}

	public BigDecimal getMonthlySalary() {
		return monthlySalary;
	}

	public BigDecimal getMonthlyWorkDays() {
		return monthlyWorkDays;
	}

	public BigDecimal getDailyPay() {
		return dailyPay;
	}

	public BigDecimal getVacationPay() {
		return vacationPay;
	}

	public BigDecimal getTotalPay() {
		return totalPay;
	}

	public int getTotalDays() {
		return totalDays;
	}

	public BigDecimal getAverageWeeklyWorkDays() {
		return averageWeeklyWorkDays;
	}

	public BigDecimal getVacationPayMultiplier() {
		return vacationPayMultiplier;
	}

	public BigDecimal getPercentilePayTotal() {
		return percentilePayTotal;
	}

	public BigDecimal getMissedPay() {
		return missedPay;
	}

	public BigDecimal getPercentileMultiplier() {
		return percentileMultiplier;
	}

	public BigDecimal getPercentileVacationPay() {
		return percentileVacationPay;
	}

	@Override
	public String toString() {
		String resultString = "";
		if (category == 1) {
			resultString = "Kohtaan 1:\n";
			resultString += "(" + monthlySalary + " € : " + monthlyWorkDays + " = " + String.format(Locale.ENGLISH, "%.2f", dailyPay) + " X " + vacationDays + " = " + String.format(Locale.ENGLISH, "%.2f", vacationPay) + " €\n";
		} else  if (category == 2) {
			resultString = "Kohtaan 2:\n";
			if (averageWeeklyWorkDays == BigDecimal.ZERO) resultString += totalPay + " € : " + totalDays + " = " + String.format(Locale.ENGLISH, "%.2f", averageDailyPay) + " €/pv { X  - : 5 } X " + vacationPayMultiplier + " = " + String.format(Locale.ENGLISH, "%.2f", vacationPay) + " €\n";
			else resultString += totalPay + " € : " + totalDays + " = " + String.format(Locale.ENGLISH, "%.2f", averageDailyPay) + " €/pv { X " + averageWeeklyWorkDays + " : 5 } X " + vacationPayMultiplier + " = " + String.format(Locale.ENGLISH, "%.2f", vacationPay) + " €\n";
		}
		if (percentileVacationPay != BigDecimal.ZERO) {
			resultString += "Kohtaan 4:\n";
			resultString += percentilePayTotal + " € + " + String.format(Locale.ENGLISH, "%.2f", missedPay) + " € X " + String.format(Locale.ENGLISH, "%.1f", percentileMultiplier.multiply(new BigDecimal(100))) + " % = " + String.format(Locale.ENGLISH, "%.2f", percentileVacationPay) + " €";
		}
		return resultString;
	}
}
