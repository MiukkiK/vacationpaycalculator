/**
 * @author Mia Kallio
 */

package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeRecord {
	private boolean salariedStatus;
	private LocalDate startDate;

	
	private ChangeList salaryChanges;
	private ChangeList workHourChanges;
	private ChangeList workDayChanges;

	private EmploymentList employmentList;
	
	public EmployeeRecord(LocalDate startDate, BigDecimal startWage) {		
		salariedStatus = false;
		this.startDate = startDate;
		
		salaryChanges = new ChangeList();
		workHourChanges = new ChangeList();
		workDayChanges = new ChangeList();
		employmentList = new EmploymentList(startWage);
		
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
	
	public ChangeList getSalaryChanges() {
		return salaryChanges;
	}

	public ChangeList getWorkDayChanges() {
		return workDayChanges;
	}

	public ChangeList getWorkHourChanges() {
		return workHourChanges;
	}

	public EmploymentList getEmploymentList() {
		return employmentList;
	}	
}
