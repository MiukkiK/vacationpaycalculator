package miukkik.vacationpaycalculator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeRecord implements EmploymentDataInterface{
	private boolean salariedStatus;
	private LocalDate startDate;
	private double wage;
	private ArrayList<EmploymentData> record;
	
	
	public EmployeeRecord(LocalDate startDate, double startWage) {
		salariedStatus = false;
		this.startDate = startDate;
		wage = startWage;
		record = new ArrayList<EmploymentData>();
	}
	
	public void add(EmploymentData data) {
		data.setWage(wage);
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
	
	public void setWageFrom(LocalDate changeDate, double newWage) {
		for (EmploymentData data : record) {
			if(!data.getDate().isBefore(changeDate)) data.setWage(newWage);
		}
		wage = newWage;
	}
	
	public ArrayList<EmploymentData> getRecord() {
		return record;
	}

	public List<EmploymentData> getRecordBetween(LocalDate startDate, LocalDate endDate) {
		int startIndex=0;
		for (int i=0; i<record.size(); i++) {
			if (!record.get(i).getDate().isBefore(startDate)) {
				startIndex = i;
				break;
			}
		}
		int endIndex = record.size();
		for (int j=startIndex; j<endIndex; j++) {
			if(record.get(j).getDate().isAfter(endDate)) {
				endIndex = j;
			}
		}
		return record.subList(startIndex, endIndex);
	}
	
	@Override
	public String toString() {
		return "EmployeeRecord [salariedStatus=" + salariedStatus + ", startDate=" + startDate + ", record size=" + record.size()
				+ "]";
	}
	
}
