package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.time.LocalDate;

import junit.framework.TestCase;

public class VacationPayCalculatorTest extends TestCase {
	
	public void testBasicVacationPay() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), new BigDecimal(10));
		record.setWorkHours(record.getStartDate(), new BigDecimal(35));
		
		record.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 6), "", new BigDecimal(8), new BigDecimal(100)));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);
		assertEquals("Category does not match", 2, calculator.getCategory());
		assertEquals("Total pay does not match", 0, calculator.getTotalPay().compareTo(new BigDecimal(500)));
		assertEquals("Vacation days do not match", 2, calculator.getVacationDays());
		assertEquals("Average daily pay does not match", 0, calculator.getAverageDailyPay().compareTo(new BigDecimal(100)));
		assertEquals("Vacation pay does not match", 0, calculator.getVacationPay().compareTo(new BigDecimal(180)));
	}

	public void testNotEnoughWorkHours() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), new BigDecimal(10));
		record.setWorkHours(record.getStartDate(), new BigDecimal(8));
		
		record.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 6), "", new BigDecimal(8), new BigDecimal(100)));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);
		
		assertEquals("Category does not match", 0, calculator.getCategory());
		assertEquals("Vacation pay in incorrect category", 0, calculator.getVacationPay().compareTo(BigDecimal.ZERO));
		assertEquals("Percentile pay does not match", 0, calculator.getPercentileVacationPay().compareTo(new BigDecimal(50)));
	}

	public void testNotEnoughActualHours() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), new BigDecimal(10));
		record.setWorkHours(record.getStartDate(), new BigDecimal(35));
		
		record.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", new BigDecimal(8), BigDecimal.ZERO));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);
		
		assertEquals("Vacation pay in incorrect category", 0, calculator.getVacationPay().compareTo(BigDecimal.ZERO));
		assertEquals("Percentile pay does not match", 0, calculator.getPercentileVacationPay().compareTo(new BigDecimal(32)));
	}
	
	public void testNotEnoughWorkDays() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), new BigDecimal(10));
		record.setWorkDays(record.getStartDate(), new BigDecimal(3));
		
		record.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 6), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 7), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 8), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 9), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 10), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 11), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 12), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 13), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 14), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 15), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 16), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 17), "", new BigDecimal(8), BigDecimal.ZERO));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);
		assertEquals("Category does not match", 0, calculator.getCategory());
		
		assertEquals("Vacation pay in incorrect category", 0, calculator.getVacationPay().compareTo(BigDecimal.ZERO));
		assertEquals("Percentile pay does not match", 0, calculator.getPercentileVacationPay().compareTo(new BigDecimal(128)));
	}
	
	public void testNotEnoughActualDays() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), new BigDecimal(10));
		record.setWorkDays(record.getStartDate(), new BigDecimal(4));
		
		record.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 6), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 7), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 8), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 9), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 10), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 11), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 12), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 13), "", new BigDecimal(8), BigDecimal.ZERO));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);
		
		assertEquals("Vacation pay in incorrect category", 0, calculator.getVacationPay().compareTo(BigDecimal.ZERO));
		assertEquals("Percentile pay does not match", 0, calculator.getPercentileVacationPay().compareTo(new BigDecimal(96)));
	}
	
	public void testWorkDaysWithPaidLeave() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), new BigDecimal(10));
		record.setWorkDays(record.getStartDate(), new BigDecimal(4));
		
		record.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 6), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 7), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 8), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 9), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 10), "Paid leave", BigDecimal.ZERO, BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 11), "Paid leave", BigDecimal.ZERO, BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 12), "Paid leave", BigDecimal.ZERO, BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 13), "Paid leave", BigDecimal.ZERO, BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 14), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 15), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 16), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2000, 1, 17), "", new BigDecimal(8), BigDecimal.ZERO));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);

		assertEquals("Category does not match", 2, calculator.getCategory());
		assertEquals("Vacation pay does not match", 0, calculator.getVacationPay().compareTo(new BigDecimal("144")));
		assertEquals("Percentile pay does not match", 0, calculator.getPercentileVacationPay().compareTo(new BigDecimal(32)));
	}
	
	public void testNonintegerVacationDays() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), new BigDecimal(10));
		record.setWorkHours(record.getStartDate(), new BigDecimal(35));
		
		record.add(new EmploymentData(LocalDate.of(2001, 1, 2), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2001, 1, 3), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2001, 1, 4), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2001, 1, 5), "", new BigDecimal(8), BigDecimal.ZERO));
		record.add(new EmploymentData(LocalDate.of(2001, 1, 6), "", new BigDecimal(8), new BigDecimal(100)));	
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2001);

		assertEquals("Incorrect amount of vacation days", 3, calculator.getVacationDays());
		assertEquals("Vacation pay does not match", 0, calculator.getVacationPay().compareTo(new BigDecimal(270)));
	}

}
