/**
 * @author Mia Kallio
 */

package miukkik.vacationpaycalculator;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class FileHandler {
	public static void inputData(String source, EmploymentList list) {
		try {
			File file = new File(source);
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				String[] data = scanner.nextLine().split("\\t", -1);		
				if ((data[1] != "") || (data[4] != "")){
					LocalDate date = LocalDate.parse(data[0], DateTimeFormatter.ofPattern("M/d/yyyy"));
					String info = data[1];
					BigDecimal bonus;
					BigDecimal hours;
					if (data[4] != "") hours = new BigDecimal(data[4]); else hours = BigDecimal.ZERO;
					if (data[7] != "") bonus = new BigDecimal(data[7].replace("€", "").strip()); else bonus = BigDecimal.ZERO;
					list.add(new EmploymentData(date, info, hours, bonus));
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("Could not find source file.");
			e.printStackTrace();
		}
	}
}
