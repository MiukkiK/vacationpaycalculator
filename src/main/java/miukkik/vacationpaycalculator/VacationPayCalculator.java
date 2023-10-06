package miukkik.vacationpaycalculator;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class VacationPayCalculator {
	private final EmployeeRecord record;
	private final double[] monthlyHours = new double[12];
	private final int[] monthlyDays = new int[12];
	private final int[] monthlyLeave = new int[12];
	private final double[] monthlyPay = new double[12];

	 private int vacationDays;

	private int category;
	//category 1 fields
	private double monthlySalary;
	private double monthlyWorkDays;
	private double dailyPay;
	private double vacationPay;

	//category 2 fields
	private double totalPay;
	private int totalDays;
	private double averageDailyPay;
	private double averageWeeklyWorkDays;
	private double vacationPayMultiplier;

	//category 4 fields
	private double percentilePayTotal;
	private double missedPay;
	private double percentileMultiplier;
	private double percentileVacationPay;

	
	public VacationPayCalculator (EmployeeRecord record, int year) {

		this.record = record;

		final LocalDate cutOffDate = Rules.getCutOffDate(year);
		List<EmploymentData> filteredList = record.getRecordBetween(cutOffDate.minusYears(1), cutOffDate);

		// data processing to monthly totals

		for(EmploymentData data : filteredList) {

			int monthIndex = data.getDate().getMonthValue() - 1;

			if (!data.getInfo().equals("")) { // days with info are not added as regular workdays or add to vacation total hour count. (weekday holiday bonus)
				if (data.getHours() == 0) monthlyLeave[monthIndex]++; // days with info and no hours are treated as valid leave days, limited leave such as sick leave not implememnted yet.
			} else  {
				monthlyDays[monthIndex]++;
				monthlyHours[monthIndex] += data.getHours();
				monthlyPay[monthIndex] += (data.getHours()*data.getWage()) + data.getBonus();
			}
		}

		// vacation day calculation
		double tempVacationDays = 0;
		for (int i = 0; i < 12; i++) {
			if ((monthlyDays[i] + monthlyLeave[i] >= Rules.getVacationDaysRequirement()) || (monthlyHours[i]) >= Rules.getVacationHoursRequirement()) tempVacationDays++;
			else {
				percentilePayTotal += monthlyPay[i];
			}
		}
		tempVacationDays *= Rules.getVacationDayMultiplier(record.getStartDate(), cutOffDate);
		vacationDays = (int)Math.round(tempVacationDays);

		// vacation day based pay calculation

		if (vacationDays != 0) {
			if (record.isSalaried() && (filteredList.get(0).getWage() == filteredList.get(filteredList.size()-1).getWage()) && filteredList.get(0).getWorkHours() == filteredList.get(filteredList.size()-1).getWorkHours()) {
				category = 1;
				monthlySalary = filteredList.get(0).getWorkHours() * filteredList.get(0).getWage();
				monthlyWorkDays = 4*5; // workdays per week not implemented in this version, assumed 5 days per week
				dailyPay = monthlySalary / monthlyWorkDays;

				vacationPay = dailyPay * vacationDays;
			} else {
				category = 2;
				totalPay = 0;
				for (double thisMonthsPay : monthlyPay) {
					totalPay += thisMonthsPay;
				}
				totalDays = 0;
				for (int thisMonthsDays : monthlyDays) {
					totalDays += thisMonthsDays;
				}
				averageDailyPay = totalPay / totalDays;
				averageWeeklyWorkDays = 5; // workdays per week not implemented in this version, assumed 5 days per week
				vacationPayMultiplier = Rules.getVacationPayMultiplier(vacationDays);
				vacationPay = averageDailyPay * (averageWeeklyWorkDays / 5) * Rules.getVacationPayMultiplier(vacationDays);
			}
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

	@Override
	public String toString() {
		String resultString = "";
		if (category == 1) {
			resultString = "Kohtaan 1:\n";
			resultString += "(" + monthlySalary + " € : " + monthlyWorkDays + " = " + String.format(Locale.ENGLISH, "%.2f", dailyPay) + " X " + vacationDays + " = " + String.format(Locale.ENGLISH, "%.2f", vacationPay) + " €\n";
		} else  if (category == 2) {
			resultString = "Kohtaan 2:\n";
			resultString += totalPay + " € : " + totalDays + " = " + String.format(Locale.ENGLISH, "%.2f", averageDailyPay) + " €/pv { X " + averageWeeklyWorkDays + " : 5 } X " + vacationPayMultiplier + " = " + String.format(Locale.ENGLISH, "%.2f", vacationPay) + " €\n";
		}
		if (percentileVacationPay != 0) {
			resultString += "Kohtaan 4:\n";
			resultString += percentilePayTotal + " € + " + String.format(Locale.ENGLISH, "%.2f", missedPay) + " € X " + percentileMultiplier*100 + " % " + String.format(Locale.ENGLISH, "%.2f", percentileVacationPay) + " €";
			}
		return resultString;
	}
}
