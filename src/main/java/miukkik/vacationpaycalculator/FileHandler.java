package miukkik.vacationpaycalculator;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class FileHandler {
	public static void inputData(String source, EmployeeRecord record) {
		try {
			File file = new File(source);
			Scanner scanner = new Scanner(file);
			LocalDate previousDate = LocalDate.MIN;
			while (scanner.hasNextLine()) {
				String[] data = scanner.nextLine().split("\\t", -1);		
				if (data[4] != "") {
					LocalDate date = LocalDate.parse(data[0], DateTimeFormatter.ofPattern("M/d/yyyy"));
					double bonus = 0;
					double hours = Double.parseDouble(data[4]);
					if (data[7] != "") bonus = Double.parseDouble(data[7].replace("€", ""));
					if (date.isEqual(previousDate)) {
						record.mergeData(new EmploymentData(date, hours, bonus));
						} else {
						record.add(new EmploymentData(date, hours, bonus));
						previousDate = date;
					}
					
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("Could not find source file.");
			e.printStackTrace();
		}
	}
}
