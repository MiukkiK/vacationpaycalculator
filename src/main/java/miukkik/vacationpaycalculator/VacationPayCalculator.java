package miukkik.vacationpaycalculator;

import java.time.LocalDate;
import java.util.List;

public class VacationPayCalculator {
	private final EmployeeRecord record;
	private final double[] monthlyHours = new double[12];
	private final double[] monthlyDays = new double[12];
	private final double[] monthlyPay = new double[12];

	private int category;
	private double salary;
	private int workDays;
	private double dailyPay;
	private double vacationDays;
	private double vacationPay;
	
	private double totalPay;
	private double totalDays;
	private double averagePay;
	
	public VacationPayCalculator (EmployeeRecord record, int year) {

		this.record = record;
		final LocalDate cutOffDate = Rules.getCutOffDate(year);
		List<EmploymentData> filteredList = record.getRecordBetween(cutOffDate.minusYears(1), cutOffDate);


		for(EmploymentData data : filteredList) {
			int monthIndex = data.getDate().getMonthValue() - 1;
			monthlyHours[monthIndex] += data.getHours();
			monthlyDays[monthIndex]++;
			monthlyPay[monthIndex] += data.getHours()*data.getWage() + data.getBonus();
		}
		vacationDays = 0;
		
		double dayModifier;	
		if (record.getStartDate().isAfter(cutOffDate.minusYears(1))) dayModifier = Rules.getFirstYearVacationDays();
		else dayModifier = Rules.getDefaultVacationDays();
		
		if (record.isSalaried()) {				
			for (double thisMonthsDays : monthlyDays) {
				if (thisMonthsDays >= Rules.getSalariedDayRequirement()) vacationDays += dayModifier;
			}
		} else {
			for (double thisMonthsHours : monthlyHours) {
				if (thisMonthsHours >= Rules.getNonSalariedHourRequirement()) vacationDays += dayModifier;
			}
		}
		
	if (record.isSalaried() && (filteredList.get(0).getWage() == filteredList.get(filteredList.size()-1).getWage())) {
			category = 1;
			salary = 37.5 * filteredList.get(0).getWage();
			workDays = 25;
			dailyPay = salary / workDays;
			vacationPay = dailyPay * vacationDays;
	} else {
		category = 2;
		for (double thisMonthsPay : monthlyPay) {
		totalPay += thisMonthsPay;
		}
		for (double thisMonthsDays : monthlyDays) {
			totalDays += thisMonthsDays;
		}
		averagePay = totalPay / totalDays;
		vacationPay = averagePay * vacationDays;
	}
	
	

	}
	@Override
	public String toString() {
		return "Result placeholder for " + record; // TODO
	}
}
