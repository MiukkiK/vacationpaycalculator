package miukkik.vacationpaycalculator;

import java.time.LocalDate;
import java.util.ArrayList;

public class EmployeeRecord implements EmploymentDataInterface{
	private boolean salariedStatus;
	private LocalDate startDate;
	private ArrayList<WageChange> wageChanges;
	private ArrayList<EmploymentData> record;
	
	
	
	public EmployeeRecord(LocalDate startDate) {
		salariedStatus = false;
		this.startDate = startDate;
		wageChanges = new ArrayList<WageChange>();
		record = new ArrayList<EmploymentData>();
	}
	
	public void add(WageChange change) {
		wageChanges.add(change);
	}
	
	public void add(EmploymentData data) {
		record.add(data);
	}
	
	public boolean isSalaried() {
		return salariedStatus;
	}

	public void setSalariedStatus(boolean salariedStatus) {
		this.salariedStatus = salariedStatus;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public ArrayList<WageChange> getWageChanges() {
		return wageChanges;
	}
	
	public ArrayList<EmploymentData> getRecord() {
		return record;
	}

	public ArrayList<EmploymentData> getRecordPeriod(LocalDate startDate, LocalDate endDate) {
		
		return record; //TODO
	}

	@Override
	public String toString() {
		return "EmployeeRecord [salariedStatus=" + salariedStatus + ", startDate=" + startDate + ", record size=" + record.size()
				+ "]";
	}
	
}
