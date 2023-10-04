package miukkik.vacationpaycalculator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public interface EmploymentDataInterface {
	
	public void add(EmploymentData data);
	public void mergeData(EmploymentData data);
	public ArrayList<EmploymentData> getRecord();
	public List<EmploymentData> getRecordBetween(LocalDate startDate, LocalDate endDate); 
	
}
