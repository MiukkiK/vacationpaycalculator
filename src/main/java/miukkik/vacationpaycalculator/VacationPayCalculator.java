package miukkik.vacationpaycalculator;

import java.time.LocalDate;
import java.util.List;

public class VacationPayCalculator {
	private final EmployeeRecord record;
	private final double[] monthlyHours = new double[12];
	private final int[] monthlyDays = new int[12];
	private final int[] monthlyLeave = new int[12];
	private final double[] monthlyPay = new double[12];


	private double percentilePayTotal;
	private double percentileLeavePayTotal;

	public VacationPayCalculator (EmployeeRecord record, int year) {

		this.record = record;

		final LocalDate cutOffDate = Rules.getCutOffDate(year);
		List<EmploymentData> filteredList = record.getRecordBetween(cutOffDate.minusYears(1), cutOffDate);

		// data processing to monthly totals
		LocalDate previousDate = LocalDate.MIN;

		for(EmploymentData data : filteredList) {

			int monthIndex = data.getDate().getMonthValue() - 1;
			if ((data.getHours() == 0) && (!data.getInfo().equals(""))) monthlyLeave[monthIndex]++; // days with info are treated as valid leave days, limited leave such as sick leave not implememnted yet.
			else {
				monthlyHours[monthIndex] += data.getHours();
				if (previousDate != data.getDate()) monthlyDays[monthIndex]++;
				monthlyPay[monthIndex] += (data.getHours()*data.getWage()) + data.getBonus();
			}
			previousDate = data.getDate();
		}

		// vacation day calculation
		double vacationDays = 0;
		for (int i = 0; i < 12; i++) {
			if ((monthlyDays[i] + monthlyLeave[i] >= Rules.getVacationDaysRequirement()) || (monthlyHours[i]) >= Rules.getVacationHoursRequirement()) vacationDays++;
			else {
				percentilePayTotal += monthlyPay[i];
				percentileLeavePayTotal += monthlyLeave[i] * (monthlyPay[i] / monthlyDays[i]);
			}
		}
		if (vacationDays != 0) vacationDays = vacationDays * Rules.getVacationDayMultiplier(record.getStartDate(), cutOffDate);

		// vacation pay calculation
		
		
		
		
		double percentileMultiplier = Rules.getPercentileMultiplier(record.getStartDate(), cutOffDate);
		
	}
	


	public void printMonthlyInformation() {
		String[] months = new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
		for (int i=0; i < months.length; i++) {
			System.out.println(months[i] + ": Workdays - " + monthlyDays[i] + ", Leave days - " + monthlyLeave[i] + ", Work hours - " + monthlyHours[i] + ", Monthly pay - " + monthlyPay[i]);
		}
	}

	@Override
	public String toString() {
		return "Result placeholder for " + record; // TODO
	}
}
