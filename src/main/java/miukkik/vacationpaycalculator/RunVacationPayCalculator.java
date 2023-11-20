package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Vacation pay calculator main method for running the calculator. Input desired variables for calculating vacation pay according to set rules.
 * 
 * @author Mia Kallio
 */
public class RunVacationPayCalculator {

	public static void main(String[] args) {;
		/**  */
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2008, 6, 1), new BigDecimal(10)); // set employee start year and initial wage
		FileHandler.inputData("src/main/resources/raw_hours.txt", record.getEmploymentList()); // get raw data from a file
		
		// record.getWorkDayChanges().add(new ChangeData(LocalDate.of(2008, 6, 1), new BigDecimal(5))); // set planned work days per week starting from a date
		record.getWorkHourChanges().add(new ChangeData(LocalDate.of(2008, 6, 1), new BigDecimal(35))); // set planned work hours per week starting from a date
		
		// record.setSalariedStatus(true); // set employee as salaried
		// record.getSalaryChanges().add(new ChangeData(LocalDate.of(2008, 6,1), new BigDecimal("2400"))); // set monthly salary of a salaried worker starting from a date
		
		record.getEmploymentList().setWageFrom(LocalDate.of(2009, 10, 15), new BigDecimal(11)); // set a wage change starting from a date
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2010); // set ending year of vacation pay calculation

		System.out.println(calculator.toString()); // prints out information on how to fill the vacation pay form
		

	}
}
