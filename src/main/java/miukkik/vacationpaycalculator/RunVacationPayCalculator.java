/**
 * @author Mia Kallio
 */

package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RunVacationPayCalculator {

	public static void main(String[] args) {;
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2008, 6, 1), new BigDecimal(10));
		FileHandler.inputData("src/main/resources/raw_hours.txt", record.getEmploymentList());
		
		record.getEmploymentList().setWageFrom(LocalDate.of(2009, 10, 15), new BigDecimal(11));

		VacationPayCalculator calculator = new VacationPayCalculator(record, 2010);
		//calculatorA.printMonthlyInformation(); // for checking monthly totals of calculated data
		System.out.println(calculator.toString());
		

	}
}
