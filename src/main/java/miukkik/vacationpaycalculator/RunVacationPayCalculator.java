package miukkik.vacationpaycalculator;

import java.time.LocalDate;

public class RunVacationPayCalculator {

	public static void main(String[] args) {;
		EmployeeRecord testCaseA = new EmployeeRecord(LocalDate.of(2008, 6, 1), 10);
		FileHandler.inputData("src/main/resources/raw_hours.txt", testCaseA);
		testCaseA.setWageFrom(LocalDate.of(2009, 10, 15), 11);

		VacationPayCalculator calculatorA = new VacationPayCalculator(testCaseA, 2010);
		//calculatorA.printMonthlyInformation(); // for checking monthly totals of calculated data
		System.out.println("Test case A:");
		System.out.println(calculatorA.toString() + "\n");
		
		EmployeeRecord testCaseB = new EmployeeRecord(LocalDate.of(2008, 6, 1), 10);
		FileHandler.inputData("src/main/resources/raw_hours.txt", testCaseB);
		testCaseB.setSalariedStatus(true);
		testCaseB.setWorkHoursFrom(LocalDate.MIN, 37.5);
		testCaseB.setWageFrom(LocalDate.of(2009, 10, 15), 11);
		
		VacationPayCalculator calculatorB = new VacationPayCalculator(testCaseB, 2010);
		//calculatorB.printMonthlyInformation(); // for checking monthly totals of calculated data
		System.out.println("Test case B:");
		System.out.println(calculatorB.toString());
	}
}
