package miukkik.vacationpaycalculator;

import java.time.LocalDate;

import junit.framework.TestCase;

public class VacationPayCalculatorTest extends TestCase {

	public void testBasicVacationPay() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), 10);
		record.setWorkHoursFrom(record.getStartDate(), 35);
		record.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 6), "", 8, 100));
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);
		System.out.println(calculator.toString());
		assertEquals("Category does not match", 2, calculator.getCategory());
		assertEquals("Total pay does not match",500.0, calculator.getTotalPay());
		assertEquals("Vacation days do not match", 2, calculator.getVacationDays());
		assertEquals("Average daily pay does not match", 100.0, calculator.getAverageDailyPay());
		assertEquals("Vacation pay incorrect", 180.0, calculator.getVacationPay());
	}
	
	public void testNotEnoughWorkHours() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), 10);
		record.setWorkHoursFrom(record.getStartDate(), 8);
		record.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 6), "", 8, 100));	
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);
		System.out.println(calculator.toString());
		assertEquals("Category does not match", 0, calculator.getCategory());
		assertEquals("Vacation pay in incorrect category", 0.0, calculator.getVacationPay());
		assertEquals("Incorrect percentile pay", 50.0, calculator.getPercentileVacationPay());
	}
	
	public void testNotEnoughActualHours() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), 10);
		record.setWorkHoursFrom(record.getStartDate(), 35);
		record.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", 8, 0));
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);
		System.out.println(calculator.toString());
		assertEquals("Vacation pay in incorrect category", 0.0, calculator.getVacationPay());
		assertEquals("Incorrect percentile pay", 32.0, calculator.getPercentileVacationPay());
	}
	
	public void testNotEnoughWorkDays() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), 10);
		record.setWorkDaysFrom(record.getStartDate(), 3);
		record.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 6), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 7), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 8), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 9), "", 8, 0));	
		record.add(new EmploymentData(LocalDate.of(2000, 1, 10), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 11), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 12), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 13), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 14), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 15), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 16), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 17), "", 8, 0));
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);
		System.out.println(calculator.toString());
		assertEquals("Category does not match", 0, calculator.getCategory());
		assertEquals("Vacation pay in incorrect category", 0.0, calculator.getVacationPay());
		assertEquals("Incorrect percentile pay", 128.0, calculator.getPercentileVacationPay());
	}
	
	public void testNotEnoughActualDays() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), 10);
		record.setWorkDaysFrom(record.getStartDate(), 4);
		record.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 6), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 7), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 8), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 9), "", 8, 0));	
		record.add(new EmploymentData(LocalDate.of(2000, 1, 10), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 11), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 12), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 13), "", 8, 0));

		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);
		System.out.println(calculator.toString());
		assertEquals("Vacation pay in incorrect category", 0.0, calculator.getVacationPay());
		assertEquals("Incorrect percentile pay", 96.0, calculator.getPercentileVacationPay());
	}
	
	public void testWorkDaysWithPaidLeave() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), 10);
		record.setWorkDaysFrom(record.getStartDate(), 4);
		record.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 6), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 7), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 8), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 9), "", 8, 0));	
		record.add(new EmploymentData(LocalDate.of(2000, 1, 10), "Paid Leave", 0, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 11), "Paid Leave", 0, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 12), "Paid Leave", 0, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 13), "Paid Leave", 0, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 14), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 15), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 16), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 17), "", 8, 0));
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);
		System.out.println(calculator.toString());
		assertEquals("Category does not match", 2, calculator.getCategory());
		assertEquals("Incorrect vacation pay", 115.2, calculator.getVacationPay());
		assertEquals("Incorrect percentile pay", 32.0, calculator.getPercentileVacationPay());
	}
	
	
	public void testNonintegerVacationDays() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), 10);
		record.setWorkHoursFrom(record.getStartDate(), 35);
		record.add(new EmploymentData(LocalDate.of(2001, 1, 2), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2001, 1, 3), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2001, 1, 4), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2001, 1, 5), "", 8, 0));
		record.add(new EmploymentData(LocalDate.of(2001, 1, 6), "", 8, 100));	
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2001);
		System.out.println(calculator.toString());
		assertEquals("Incorrect amount of vacation days", 3, calculator.getVacationDays());
		assertEquals("Vacation pay incorrect", 270.0, calculator.getVacationPay());
	}
}
