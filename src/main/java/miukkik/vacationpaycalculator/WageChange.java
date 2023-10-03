package miukkik.vacationpaycalculator;

import java.time.LocalDate;

public class WageChange {
	private LocalDate effectiveDate;
	private double wage;
	
	public WageChange(LocalDate effectiveDate, double wage) {
		this.effectiveDate = effectiveDate;
		this.wage = wage;
	}

	public LocalDate getEffectiveDate() {
		return effectiveDate;
	}

	public double getWage() {
		return wage;
	}
}
