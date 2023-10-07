package miukkik.vacationpaycalculator;

import java.time.LocalDate;

public class Change {
	double amount;
	LocalDate effectiveUntil;
	
	public Change (double amount) {
		this.amount = amount; 
		effectiveUntil = LocalDate.MAX;
	}
	
	public void setEffectiveUntil(LocalDate effectiveUntil) {
		this.effectiveUntil = effectiveUntil;
	}
}
