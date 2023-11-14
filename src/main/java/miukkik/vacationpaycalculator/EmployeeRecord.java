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
	private BigDecimal currentSalary;
	private BigDecimal currentWorkHours;
	private BigDecimal currentWorkDays;

	private ArrayList<EmploymentData> record;
	
	public EmployeeRecord(LocalDate startDate, BigDecimal startWage) {		
		salariedStatus = false;
		this.startDate = startDate;
		currentWage = startWage; 
		currentSalary = BigDecimal.ZERO;
		currentWorkHours = BigDecimal.ZERO;
		currentWorkDays  = BigDecimal.ZERO;

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
	
	public BigDecimal getWage(LocalDate date) {
		return currentWage; 
	}
	
	public void setWageFrom(LocalDate changeDate, BigDecimal newWage) {
		for (EmploymentData data : record) {
			if(!data.getDate().isBefore(changeDate)) data.setWage(newWage);
		}
		currentWage = newWage;

	}	
	
	public boolean hasSalaryChanged(LocalDate startDate, LocalDate endDate) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasDaysChanged(LocalDate startDate, LocalDate endDate) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean hasHoursChanged(LocalDate startDate, LocalDate endDate) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public BigDecimal getSalary(LocalDate date) {
		return currentSalary;
		// TODO Auto-generated method stub

	}

	public BigDecimal getWorkDays(LocalDate date) {
		return currentWorkDays;
		// TODO Auto-generated method stub

	}

	public BigDecimal getWorkHours(LocalDate date) {
		return currentWorkHours;
		// TODO Auto-generated method stub
		
	}
	
	public void setWorkHours(LocalDate changeDate, BigDecimal newWorkHours) {
		currentWorkHours = newWorkHours;
		// TODO
	}
	
	public void setWorkDays(LocalDate changeDate, BigDecimal newWorkDays) {
		currentWorkDays = newWorkDays;
		// TODO
	}
	
	public void setSalary(LocalDate changeDate, BigDecimal newSalary) {
		currentSalary = newSalary;
		// TODO
	}
	
	public void add(EmploymentData data) {
		data.setWage(currentWage);
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
