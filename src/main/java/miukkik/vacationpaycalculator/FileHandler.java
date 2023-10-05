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
			while (scanner.hasNextLine()) {
				String[] data = scanner.nextLine().split("\\t", -1);		
				if ((data[1] != "") || (data[4] != "")){
					LocalDate date = LocalDate.parse(data[0], DateTimeFormatter.ofPattern("M/d/yyyy"));
					String info = data[1];
					double bonus = 0;
					double hours = 0;
					if (data[4] != "") hours = Double.parseDouble(data[4]);
					if (data[7] != "") bonus = Double.parseDouble(data[7].replace("€", ""));
					record.add(new EmploymentData(date, info, hours, bonus));
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("Could not find source file.");
			e.printStackTrace();
		}
	}
}
