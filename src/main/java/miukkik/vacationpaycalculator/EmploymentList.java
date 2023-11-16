package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

public class EmploymentList implements DataInterface<EmploymentData> {

	private ArrayList<EmploymentData> record;
	private BigDecimal currentWage;

	public EmploymentList(BigDecimal initialWage) {
		record = new ArrayList<EmploymentData>();
		currentWage = initialWage;
	}

	public void add(EmploymentData data) {
		data.setWage(currentWage);
		record.add(data);
	}

	public ArrayList<EmploymentData> getDataBetween(LocalDate startDate, LocalDate endDate) {
		ArrayList<EmploymentData> result = new ArrayList<EmploymentData>();
		for (EmploymentData data : record) {
			LocalDate date = data.getDate();
			if (date.isAfter(startDate)) {
				if (!date.isAfter(endDate)) result.add(data);
				else break;
			}
		}
		return result;
	}

	public void setWageFrom(LocalDate changeDate, BigDecimal newWage) {

		for (EmploymentData data : record) {
			if(!data.getDate().isBefore(changeDate)) data.setWage(newWage);
		}
	}
}
