package miukkik.vacationpaycalculator;

import java.time.LocalDate;

public class RunVacationPayCalculator {

	public static void main(String[] args) {;
		EmployeeRecord testCaseA = new EmployeeRecord(LocalDate.of(2008, 6, 1), 10);
		FileHandler.inputData("src/main/resources/raw_hours.txt", testCaseA);
		testCaseA.setWageFrom(LocalDate.of(2009, 10, 15), 11);

		VacationPayCalculator calculatorA = new VacationPayCalculator(testCaseA, 2010);
		System.out.println(calculatorA.toString());
		
		EmployeeRecord testCaseB = new EmployeeRecord(LocalDate.of(2008, 6, 1), 10);
		FileHandler.inputData("src/main/resources/raw_hours.txt", testCaseB);
		testCaseB.setSalariedStatus(true);
		testCaseB.setWageFrom(LocalDate.of(2009, 10, 15), 11);
		
		VacationPayCalculator calculatorB = new VacationPayCalculator(testCaseB, 2010);
		System.out.println(calculatorB.toString());
	}
}
