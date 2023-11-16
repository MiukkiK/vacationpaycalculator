/**
 * @author Mia Kallio
 */

package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ChangeData {

	BigDecimal value;
	LocalDate date;
	
	public ChangeData (LocalDate date, BigDecimal amount) {
		this.value = amount; 
		this.date = date;
	}
	
	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal amount) {
		this.value = amount;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}	
}
