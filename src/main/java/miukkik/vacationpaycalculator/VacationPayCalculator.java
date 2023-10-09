package miukkik.vacationpaycalculator;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class VacationPayCalculator {
	private final double[] monthlyHours = new double[12];
	private final int[] monthlyDays = new int[12];
	private final int[] monthlyLeave = new int[12];
	private final double[] monthlyPay = new double[12];

	private int vacationDays;

	private double averageDailyPay;
	
	private int category;
	//category 1 fields
	private double monthlySalary;
	private double monthlyWorkDays;
	private double dailyPay;
	private double vacationPay;
	
	//category 2 fields
	private double totalPay;
	private int totalDays;
	private double averageWeeklyWorkDays;
	private double vacationPayMultiplier;

	//category 4 fields
	private double percentilePayTotal;
	private double missedPay;
	private double percentileMultiplier;
	private double percentileVacationPay;


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
				if (data.getHours() == 0) monthlyLeave[monthIndex]++; // days with info and no hours are treated as valid leave days, limited leave such as sick leave not implememnted yet.
			} else  {
				monthlyDays[monthIndex]++;
				monthlyHours[monthIndex] += data.getHours();
				/**
				 * PAM Kaupan alan TES: §20 6.
				 * Lomapalkka provision osalta lasketaan vuosilomalain mukaan.
				 */
				monthlyPay[monthIndex] += (data.getHours()*data.getWage()) + data.getBonus();
			}
		}
		
		totalPay = 0;
		for (double thisMonthsPay : monthlyPay) {
			totalPay += thisMonthsPay;
		}
		totalDays = 0;
		for (int thisMonthsDays : monthlyDays) {
			totalDays += thisMonthsDays;
		}
		averageDailyPay = totalPay / totalDays;
		
		// vacation day calculation
		double tempVacationDays = 0;
		for (int i = 0; i < 12; i++) {
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
					|| ((record.getCurrentWorkHours()*4 >= Rules.getVacationHoursRequirement()) && (monthlyHours[i]) >= Rules.getVacationHoursRequirement()))
				tempVacationDays++;
			else {
				percentilePayTotal += monthlyPay[i];
				totalPay -= monthlyPay[i];
			}
		}
		tempVacationDays *= Rules.getVacationDayMultiplier(record.getStartDate(), cutOffDate);
		/**
		 * Vuosilomalaki §5
		 * Loman pituutta laskettaessa päivän osa pyöristetään täyteen lomapäivään.
		 */
		vacationDays = (int)Math.round(tempVacationDays);

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
				averageWeeklyWorkDays = record.getCurrentWorkDays(); // changing workdays not implemented yet
				vacationPayMultiplier = Rules.getVacationPayMultiplier(vacationDays);
				if (averageWeeklyWorkDays == 0) vacationPay = averageDailyPay * Rules.getVacationPayMultiplier(vacationDays);
				else vacationPay = averageDailyPay * (averageWeeklyWorkDays / 5) * Rules.getVacationPayMultiplier(vacationDays);
		}

		// percentile vacation pay calculation

		int totalLeaveDays = 0;
		for (int leaveThisMonth : monthlyLeave) {
			totalLeaveDays += leaveThisMonth;
		}
		percentileVacationPay = 0;
		if ((percentilePayTotal != 0) || (totalLeaveDays != 0)) {

			if (category == 1) missedPay = totalLeaveDays * dailyPay; else missedPay = totalLeaveDays * averageDailyPay;
			
			percentileMultiplier = Rules.getPercentileMultiplier(record.getStartDate(), cutOffDate);
			percentileVacationPay = (percentilePayTotal + missedPay) * percentileMultiplier;		
		}
	}



	public void printMonthlyInformation() {
		String[] months = new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
		for (int i=0; i < months.length; i++) {
			System.out.println(months[i] + ": Workdays - " + monthlyDays[i] + ", Leave days - " + monthlyLeave[i] + ", Work hours - " + monthlyHours[i] + ", Monthly pay - " + monthlyPay[i]);
		}
		System.out.println("Category is " + category);
	}

		public double[] getMonthlyHours() {
		return monthlyHours;
	}

	public int[] getMonthlyDays() {
		return monthlyDays;
	}

	public int[] getMonthlyLeave() {
		return monthlyLeave;
	}

	public double[] getMonthlyPay() {
		return monthlyPay;
	}

	public int getVacationDays() {
		return vacationDays;
	}

	public double getAverageDailyPay() {
		return averageDailyPay;
	}

	public int getCategory() {
		return category;
	}

	public double getMonthlySalary() {
		return monthlySalary;
	}

	public double getMonthlyWorkDays() {
		return monthlyWorkDays;
	}

	public double getDailyPay() {
		return dailyPay;
	}

	public double getVacationPay() {
		return vacationPay;
	}

	public double getTotalPay() {
		return totalPay;
	}

	public int getTotalDays() {
		return totalDays;
	}

	public double getAverageWeeklyWorkDays() {
		return averageWeeklyWorkDays;
	}

	public double getVacationPayMultiplier() {
		return vacationPayMultiplier;
	}

	public double getPercentilePayTotal() {
		return percentilePayTotal;
	}

	public double getMissedPay() {
		return missedPay;
	}

	public double getPercentileMultiplier() {
		return percentileMultiplier;
	}

	public double getPercentileVacationPay() {
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
			if (averageWeeklyWorkDays == 0) resultString += totalPay + " € : " + totalDays + " = " + String.format(Locale.ENGLISH, "%.2f", averageDailyPay) + " €/pv { X  - : 5 } X " + vacationPayMultiplier + " = " + String.format(Locale.ENGLISH, "%.2f", vacationPay) + " €\n";
			else resultString += totalPay + " € : " + totalDays + " = " + String.format(Locale.ENGLISH, "%.2f", averageDailyPay) + " €/pv { X " + averageWeeklyWorkDays + " : 5 } X " + vacationPayMultiplier + " = " + String.format(Locale.ENGLISH, "%.2f", vacationPay) + " €\n";
		}
		if (percentileVacationPay != 0) {
			resultString += "Kohtaan 4:\n";
			resultString += percentilePayTotal + " € + " + String.format(Locale.ENGLISH, "%.2f", missedPay) + " € X " + percentileMultiplier*100 + " % = " + String.format(Locale.ENGLISH, "%.2f", percentileVacationPay) + " €";
		}
		return resultString;
	}
}
