/**
 * @author Mia Kallio
 */

package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Change {
	BigDecimal amount;
	LocalDate effectiveUntil;
	
	public Change (BigDecimal amount) {
		this.amount = amount; 
		effectiveUntil = LocalDate.MAX;
	}
	
	public void setEffectiveUntil(LocalDate effectiveUntil) {
		this.effectiveUntil = effectiveUntil;
	}
}
