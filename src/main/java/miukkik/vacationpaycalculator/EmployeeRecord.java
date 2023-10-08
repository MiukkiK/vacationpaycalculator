package miukkik.vacationpaycalculator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeRecord implements EmploymentDataInterface{
	private boolean salariedStatus;
	private LocalDate startDate;
	private double currentWage;
	private double currentWorkHours;
	private double currentWorkDays;
//	private ChangeList wages;
//	private ChangeList workHours;
	private ArrayList<EmploymentData> record;
	
	public EmployeeRecord(LocalDate startDate, double startWage) {		
		salariedStatus = false;
		this.startDate = startDate;
		currentWage = startWage; 
		currentWorkHours = 0;
		currentWorkDays  = 0;
//		wages = new ChangeList(startWage);
//		workHours = new ChangeList(0);
		record = new ArrayList<EmploymentData>();
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
	
	public double getCurrentWage() {
		return currentWage;
	}
	
	public double getWageOn(LocalDate date) {
		return currentWage; // not implemented
	}
	
	public void setWageFrom(LocalDate changeDate, double newWage) {
		for (EmploymentData data : record) {
			if(!data.getDate().isBefore(changeDate)) data.setWage(newWage);
		}
		currentWage = newWage;
//		wages.addChange(newWage, changeDate);
	}	
	
	public double getCurrentWorkHours() {
		return currentWorkHours;
	}
	
	public void setWorkHoursFrom(LocalDate changeDate, double newWorkHours) {
		for (EmploymentData data : record) {
			if(!data.getDate().isBefore(changeDate)) data.setWorkHours(newWorkHours);
		}
		currentWorkHours = newWorkHours;
//		workHours.addChange(newWorkHours, changeDate);
	}
	
	public double getCurrentWorkDays() {
		return currentWorkDays;
	}
	
	public void setWorkDaysFrom(LocalDate changeDate, double newWorkHours) {
		// not implemented
	}
	
	public void add(EmploymentData data) {
		data.setWage(currentWage);
		data.setWorkHours(currentWorkHours);
		record.add(data);
		
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
}
