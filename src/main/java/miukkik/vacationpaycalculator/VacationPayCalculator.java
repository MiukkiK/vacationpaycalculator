package miukkik.vacationpaycalculator;

import java.time.LocalDate;
import java.util.List;

public class VacationPayCalculator {
	private final double[] totalHours = new double[12];
	private final double[] totalDays = new double[12];
	private final double[] totalPay = new double[12];

	public VacationPayCalculator (EmployeeRecord record, int year) {
		
		final LocalDate cutOffDate = Rules.getCutOffDate(year);
		List<EmploymentData> filteredList = record.getRecordBetween(cutOffDate.minusYears(1), cutOffDate);
		
		for(EmploymentData data : filteredList) {
			int monthIndex = data.getDate().getMonthValue() - 1;
			totalHours[monthIndex] += data.getHours();
			totalDays[monthIndex]++;
			totalPay[monthIndex] += data.getHours()*data.getWage() + data.getBonus();
		}
	}
}
