/**
 * @author Mia Kallio
 */

package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeRecord implements EmploymentDataInterface{
	private boolean salariedStatus;
	private LocalDate startDate;
	private BigDecimal currentWage;
	private BigDecimal currentWorkHours;
	private int currentWorkDays;
//	private ChangeList wages;
//	private ChangeList workHours;
	private ArrayList<EmploymentData> record;
	
	public EmployeeRecord(LocalDate startDate, BigDecimal startWage) {		
		salariedStatus = false;
		this.startDate = startDate;
		currentWage = startWage; 
		currentWorkHours = BigDecimal.ZERO;
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
	
	public BigDecimal getCurrentWage() {
		return currentWage;
	}
	
	public BigDecimal getWageOn(LocalDate date) {
		return currentWage; // not implemented
	}
	
	public void setWageFrom(LocalDate changeDate, BigDecimal newWage) {
		for (EmploymentData data : record) {
			if(!data.getDate().isBefore(changeDate)) data.setWage(newWage);
		}
		currentWage = newWage;
//		wages.addChange(newWage, changeDate);
	}	
	
	public BigDecimal getCurrentWorkHours() {
		return currentWorkHours;
	}
	
	public void setWorkHoursFrom(LocalDate changeDate, BigDecimal newWorkHours) {
		for (EmploymentData data : record) {
			if(!data.getDate().isBefore(changeDate)) data.setWorkHours(newWorkHours);
		}
		currentWorkHours = newWorkHours;
//		workHours.addChange(newWorkHours, changeDate);
	}
	
	public int getCurrentWorkDays() {
		return currentWorkDays;
	}
	
	public void setWorkDaysFrom(LocalDate changeDate, int newWorkDays) {
		currentWorkDays = newWorkDays;
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
