package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 
 * Employee object for vacation pay calculator.
 * Stores employee start date and whether employee is salaried or not.
 * Also has data lists for changes and work events.
 * Getters for variables and lists, and setter for salaried status.
 * @author Mia Kallio
 */
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
	
	/**
	 * PAM Kaupan alan TES, §20 2.
	 * ...
	 * Lomaa ansaitaan joko 14 päivän tai 35 tunnin säännön perusteella.
	 * 
	 * Lomaa ansaitaan 35 tunnin säännön perusteella työntekijän työskennellessä työsopimuksen mukaan alle 14 päivää kuukaudessa.
	 */
	public LomaPaivienAnsaintaSaanto getLomaPaivienAnsaintaSaanto(LocalDate startDate, LocalDate endDate) {
		if (!getWorkDayChanges().hasChangedBetween(startDate, endDate) && (getWorkDayChanges().getValueOn(endDate).multiply(new BigDecimal(4)).compareTo(Rules.getKuukausiPaivaVaatimus()) != -1))
			return LomaPaivienAnsaintaSaanto.PAIVAT;
		else return LomaPaivienAnsaintaSaanto.TUNNIT;
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
