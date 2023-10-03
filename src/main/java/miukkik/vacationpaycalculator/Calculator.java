package miukkik.vacationpaycalculator;

import java.time.LocalDate;

public class Calculator {

	public static void main(String[] args) {
		Rules rules = new Rules();
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2008, 6, 1));
		FileHandler.inputData("src/main/resources/raw_hours.txt", record);
		
		// TODO
		
		System.out.println(rules);
		System.out.println(record.toString());
	}
}
