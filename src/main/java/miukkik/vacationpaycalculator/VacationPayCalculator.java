package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/** 
 * Main calculator class. Performs the vacation pay calculations.
 * custom output toString() for instructions on how to fill the vacation pay form.
 * @author Mia Kallio
 */
public class VacationPayCalculator {
	
	private final BigDecimal[] plannedDays = new BigDecimal[12];
	private final BigDecimal[] plannedHours = new BigDecimal[12];
	private boolean hasDaysChanged;
	private VacationDayMethod vacationDayMethod;

	private final BigDecimal[] monthlyHours = new BigDecimal[12];
	private final BigDecimal[] monthlyDays = new BigDecimal[12];
	private final BigDecimal[] monthlyLeave = new BigDecimal[12];
	private final BigDecimal[] monthlyPay = new BigDecimal[12];

	private EmployeeRecord record;
	
	private int applicableMonths;
	private BigDecimal vacationDaysPerApplicableMonth;
	private int vacationDays;
	
	private BigDecimal totalLeaveDays;
	private BigDecimal averageDailyPay;

	private boolean eligibleForVacationBonus;
	
	private Category category;
	//category 1 fields
	private BigDecimal monthlySalary;
	private BigDecimal monthlyWorkDays;
	private BigDecimal dailyPay;
	private BigDecimal vacationPay;

	//category 2 fields
	private BigDecimal totalPay;
	private BigDecimal totalWorkDays;
	private BigDecimal averageWeeklyWorkDays;
	private BigDecimal vacationPayMultiplier;

	//category 4 fields
	private BigDecimal totalPercentilePay;
	private BigDecimal missedPay;
	private BigDecimal percentileMultiplier;
	private BigDecimal percentileVacationPay;

	private BigDecimal vacationBonus;
	
	public enum VacationDayMethod {
		DAYS,
		HOURS;
	}
	
	public enum Category {
		SALARIED,
		DAILY_PAY;
	}

	public VacationPayCalculator (EmployeeRecord record, int year) {

		this.record = record;
		
		final LocalDate endDate = Rules.getCutOffDate(year);
		final LocalDate startDate = endDate.minusYears(1).plusDays(1); 

		/*
		 * Vuosilomalaki 18.3.2005/162: §6
		 * Jos työntekijä on sopimuksen mukaisesti työssä niin harvoina päivinä,
		 * että hänelle ei tästä syystä kerry ainoatakaan 14 työssäolopäivää sisältävää
		 * kalenterikuukautta tai vain osa kalenterikuukausista sisältää 14 työssäolopäivää,
		 * täydeksi lomanmääräytymiskuukaudeksi katsotaan sellainen kalenterikuukausi, jonka
		 * aikana työntekijälle on kertynyt vähintään 35 työtuntia tai 7 §:ssä tarkoitettua 
		 * työssäolon veroista tuntia.
		 */
		ChangeList dayChanges = record.getWorkDayChanges();
		if (!dayChanges.getDataBetween(startDate, endDate).isEmpty()) {
			hasDaysChanged = true;
			// TODO work day changes not implemeneted yet

		} else {
			hasDaysChanged = false;
			BigDecimal unchangedDays = dayChanges.getValueOn(endDate).multiply(new BigDecimal(4));
			for (int i=0; i < 12; i++) {
				plannedDays[i] = unchangedDays;
			}
			if (unchangedDays.multiply(new BigDecimal(4)).compareTo(Rules.getVacationDaysRequirement()) != -1) {
				vacationDayMethod = VacationDayMethod.DAYS;
			}
		}
		
		ChangeList hourChanges = record.getWorkHourChanges();
		if (!hourChanges.getDataBetween(startDate, endDate).isEmpty()) {
			// TODO work hour changes not implemented yet

		} else {
			BigDecimal unchangedHours = hourChanges.getValueOn(endDate).multiply(new BigDecimal(4));
			for (int i=0; i < 12; i++) {
				plannedHours[i] = unchangedHours;
			}
			if (unchangedHours.multiply(new BigDecimal(4)).compareTo(Rules.getVacationHoursRequirement()) != -1) {
				vacationDayMethod = VacationDayMethod.HOURS;
			}
		}

		// data processing to monthly totals
		if (record.isSalaried()) {
			for (int i=0 ; i < 12; i++) {
				monthlyDays[i] = plannedDays[i]; // workday tracking not implemented for salaraied employees
			}
			BigDecimal unchangedSalary = record.getSalaryChanges().getValueOn(endDate);
			ChangeList salaryChanges = record.getSalaryChanges();
			if (!salaryChanges.getDataBetween(startDate, endDate).isEmpty()) {
				// TODO salary changes not implememnted yet

			} else {
				for (int i=0; i < 12; i++) {
					monthlyPay[i] = unchangedSalary;
				}
			}
		} else {
			List<EmploymentData> filteredList = record.getEmploymentList().getDataBetween(startDate, endDate);
			for(EmploymentData data : filteredList) {
				int monthIndex = data.getDate().getMonthValue() - 1;

				/*
				 * Vuosilomalaki 18.3.2005/162: §7
				 * Työssäolon veroisena pidetään työstä poissaoloaikaa, jolta työnantaja on lain mukaan velvollinen maksamaan työntekijälle palkan.
				 *
				 * PAM Kaupan alan TES, §20 10.
				 * 10. Maksettuun palkkaan lisätään laskennallista palkkaa:
				 * ...
				 * raskaus- ja vanhempainvapaan vuosilomaa kerryttävältä ajalta
				 * tilapäisen hoitovapaan ajalta (työsopimuslain 4:6 §)
				 */
				if (!data.getInfo().equals("")) { // days with info are not added as regular workdays or add to vacation total hour count. (weekday holiday bonus)
					if (monthlyLeave[monthIndex] == null) monthlyLeave[monthIndex] = BigDecimal.ZERO;
					if (data.getHours() == BigDecimal.ZERO) monthlyLeave[monthIndex] = monthlyLeave[monthIndex].add(BigDecimal.ONE); // days with info and no hours are treated as valid leave days, limited leave such as sick leave not implememnted yet.
				} else  {	
					if (monthlyDays[monthIndex] == null) monthlyDays[monthIndex] = BigDecimal.ZERO;
					monthlyDays[monthIndex] = monthlyDays[monthIndex].add(BigDecimal.ONE);
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
		}
		totalPay = BigDecimal.ZERO;
		for (BigDecimal thisMonthsPay : monthlyPay) {
			if (thisMonthsPay != null) totalPay = totalPay.add(thisMonthsPay);
		}
		totalWorkDays = BigDecimal.ZERO;
		for (BigDecimal thisMonthsDays : monthlyDays) {
			if (thisMonthsDays != null) totalWorkDays = totalWorkDays.add(thisMonthsDays);
		}
		averageDailyPay = totalPay.divide(totalWorkDays, 3, RoundingMode.HALF_UP);

		// vacation day calculation

		applicableMonths = 0;
		totalPercentilePay = BigDecimal.ZERO;
		for (int i = 0; i < 12; i++) {
			if (monthlyHours[i] == null) monthlyHours[i] = BigDecimal.ZERO;
			if (monthlyPay[i] == null) monthlyPay[i] = BigDecimal.ZERO;		
			if (monthlyDays[i] == null) monthlyDays[i] = BigDecimal.ZERO;
			if (monthlyLeave[i] == null) monthlyLeave[i] = BigDecimal.ZERO;
		
			/*
			 * Vuosilomalaki 18.3.2005/162: §6
			 * Jos työntekijä on sopimuksen mukaisesti työssä niin harvoina päivinä,
			 * että hänelle ei tästä syystä kerry ainoatakaan 14 työssäolopäivää sisältävää
			 * kalenterikuukautta tai vain osa kalenterikuukausista sisältää 14 työssäolopäivää,
			 * täydeksi lomanmääräytymiskuukaudeksi katsotaan sellainen kalenterikuukausi, jonka
			 * aikana työntekijälle on kertynyt vähintään 35 työtuntia tai 7 §:ssä tarkoitettua 
			 * työssäolon veroista tuntia.
			 */
			if ((plannedDays[i].compareTo(Rules.getVacationDaysRequirement()) != -1) && 
					(monthlyDays[i].add(monthlyLeave[i]).compareTo(Rules.getVacationDaysRequirement()) != -1))
				applicableMonths++;
			// Add leave days as proportionate hours as planned weekly hours / 5 per day
			else if ((plannedHours[i].compareTo(Rules.getVacationHoursRequirement()) != -1) && 
					(monthlyLeave[i].divide(new BigDecimal(5)).multiply(plannedHours[i]).add(monthlyHours[i]).compareTo(Rules.getVacationHoursRequirement()) != -1))
				applicableMonths++;
			else {
				totalPercentilePay = totalPercentilePay.add(monthlyPay[i]);
				totalPay = totalPay.subtract(monthlyPay[i]);
			}
		}
		/* 
		 * Vuosilomalaki 18.3.2005/162: §5
		 * Työntekijällä on oikeus saada lomaa kaksi ja puoli arkipäivää kultakin täydeltä lomanmääräytymiskuukaudelta. 
		 * Jos työsuhde on lomanmääräytymisvuoden loppuun mennessä jatkunut yhdenjaksoisesti alle vuoden, 
		 * työntekijällä on kuitenkin oikeus saada lomaa kaksi arkipäivää kultakin täydeltä lomanmääräytymiskuukaudelta. 
		 * Loman pituutta laskettaessa päivän osa pyöristetään täyteen lomapäivään.
		 */	
		vacationDaysPerApplicableMonth = Rules.getVacationDayMultiplier(record.getStartDate(), endDate);
		BigDecimal unroundedVacationDays = vacationDaysPerApplicableMonth.multiply(new BigDecimal(applicableMonths));	
		unroundedVacationDays = unroundedVacationDays.round(new MathContext(unroundedVacationDays.precision() - unroundedVacationDays.scale(), RoundingMode.CEILING));
		vacationDays = unroundedVacationDays.intValue();

		if (vacationDays != 0) eligibleForVacationBonus = true;
		else eligibleForVacationBonus = false;
		
		// vacation day based pay calculation

		/**
		 * PAM Kaupan alan TES, §20 6.
		 * Jos työntekijän työaika ja vastaavasti palkka on muuttunut lomanmääräytymisvuoden aikana
		 * ja hän on kuukausipalkkainen lomanmääräytymisvuoden lopussa (31.3.), hänen lomapalkkansa 
		 * lasketaan tämän pykälän 8–11. kohdan mukaan.
		 */
		if (record.isSalaried() && !hasDaysChanged) {
			if (vacationDays != 0) {
				category = Category.SALARIED;
				
				monthlySalary = record.getSalaryChanges().getValueOn(endDate);
				monthlyWorkDays = record.getWorkDayChanges().getValueOn(endDate).multiply(new BigDecimal(4));
				dailyPay = monthlySalary.divide(monthlyWorkDays);

				vacationPay = dailyPay.multiply(new BigDecimal(vacationDays));
			}
			/*
			 * Vuosilomalaki 18.3.2005/162: §11
			 * Muun kuin viikko- tai kuukausipalkalla työskentelevän sellaisen työntekijän vuosilomapalkka,
			 * joka sopimuksen mukaan työskentelee vähintään 14 päivänä kalenterikuukaudessa, lasketaan
			 * kertomalla hänen keskipäiväpalkkansa lomapäivien määrän perusteella määräytyvällä kertoimella
			 */
		} else {
			if (vacationDays != 0) category = Category.DAILY_PAY;
			vacationPayMultiplier = Rules.getVacationPayMultiplier(vacationDays);
			vacationPay = averageDailyPay.multiply(vacationPayMultiplier);
			
			//TODO Not implemented yet
			/**
			if (record.isSalaried() == true) {
				averageWeeklyWorkDays = new BigDecimal(totalDays).divide(new BigDecimal(12));
				vacationPay = vacationPay.multiply(averageWeeklyWorkDays.divide(new BigDecimal(5)));
			}
			 */
		}
		if (eligibleForVacationBonus) vacationBonus = vacationPay.divide(new BigDecimal(2));
		// percentile vacation pay calculation

		totalLeaveDays = BigDecimal.ZERO;
		for (BigDecimal leaveThisMonth : monthlyLeave) {
			totalLeaveDays = totalLeaveDays.add(leaveThisMonth);
		}
		percentileVacationPay = BigDecimal.ZERO;
		if ((totalPercentilePay != BigDecimal.ZERO) || (totalLeaveDays != BigDecimal.ZERO)) {

			if (category == Category.SALARIED) missedPay = totalLeaveDays.multiply(dailyPay); 
			else missedPay = totalLeaveDays.multiply(averageDailyPay);

			percentileMultiplier = Rules.getPercentileMultiplier(record.getStartDate(), endDate);
			percentileVacationPay = totalPercentilePay.add(missedPay).multiply(percentileMultiplier);		
		}
	}

	public void printMonthlyInformation() {
		String[] months = new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
		for (int i=0; i < months.length; i++) {
			System.out.println(months[i] + ": Workdays - " + monthlyDays[i] + ", Leave days - " + monthlyLeave[i] + ", Work hours - " + monthlyHours[i] + ", Monthly pay - " + monthlyPay[i]);
		}
		System.out.println("Category is " + category);
	}

	public BigDecimal[] getPlannedDays() {
		return plannedDays;
	}

	public BigDecimal[] getPlannedHours() {
		return plannedHours;
	}
	
	public VacationDayMethod getVacationDayMethod() {
		return vacationDayMethod;
	}
	
	public int getApplicableMonths() {
		return applicableMonths;
	}

	public BigDecimal getVacationDaysPerApplicableMonth() {
		return vacationDaysPerApplicableMonth;
	}

	public int getVacationDays() {
		return vacationDays;
	}
	
	public BigDecimal[] getMonthlyHours() {
		return monthlyHours;
	}

	public BigDecimal[] getMonthlyDays() {
		return monthlyDays;
	}

	public BigDecimal[] getMonthlyLeave() {
		return monthlyLeave;
	}

	public BigDecimal[] getMonthlyPay() {
		return monthlyPay;
	}

	public BigDecimal getAverageDailyPay() {
		return averageDailyPay;
	}

	public Category getCategory() {
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

	public BigDecimal getTotalWorkDays() {
		return totalWorkDays;
	}

	public BigDecimal getTotalLeaveDays() {
		return totalLeaveDays;
	}
	
	public BigDecimal getAverageWeeklyWorkDays() {
		return averageWeeklyWorkDays;
	}

	public BigDecimal getVacationPayMultiplier() {
		return vacationPayMultiplier;
	}

	public BigDecimal getPercentilePayTotal() {
		return totalPercentilePay;
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

	public boolean isEligibleForVacationBonus() {
		return eligibleForVacationBonus;
	}
	
	public BigDecimal getVacationBonus() {
		return vacationBonus;
	}
	
	@Override
	public String toString() {
		String resultString = "";
		if (category == Category.SALARIED) {
			resultString = "Kohtaan 1:\n";
			resultString += "(" + monthlySalary + " € : " + monthlyWorkDays + " = " + String.format(Locale.ENGLISH, "%.2f", dailyPay) + " X " + vacationDays + " = " + String.format(Locale.ENGLISH, "%.2f", vacationPay) + " €\n";
		} else  if (category == Category.DAILY_PAY) {
			resultString = "Kohtaan 2:\n";
			resultString += totalPay + " € : " + totalWorkDays + " = " + String.format(Locale.ENGLISH, "%.2f", averageDailyPay) + " €/pv { X ";
			if (record.isSalaried()) resultString += averageWeeklyWorkDays;
			else resultString += "-";
			resultString += " : 5 } X " + vacationPayMultiplier + " = " + String.format(Locale.ENGLISH, "%.2f", vacationPay) + " €\n";
		}
		if (percentileVacationPay != BigDecimal.ZERO) {
			resultString += "Kohtaan 4:\n";
			resultString += totalPercentilePay + " € + " + String.format(Locale.ENGLISH, "%.2f", missedPay) + " € X " + String.format(Locale.ENGLISH, "%.1f", percentileMultiplier.multiply(new BigDecimal(100))) + " % = " + String.format(Locale.ENGLISH, "%.2f", percentileVacationPay) + " €";
		}
		return resultString;
	}
}
