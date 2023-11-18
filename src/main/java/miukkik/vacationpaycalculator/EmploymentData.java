/**
 * Simple data object for storing work events.
 * Stores date of event, additional info, work hours, current wage and possible bonus pay.
 * Constructor sets all fields except wage. Default getters, and setter for wage.
 * Custom toString() for work event output.
 * @author Mia Kallio
 */

package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EmploymentData {
	private LocalDate date;
	private BigDecimal hours;
	private BigDecimal bonus;
	private BigDecimal wage;
	private String info;
	
	public EmploymentData(LocalDate date, String info, BigDecimal hours, BigDecimal bonus) {
		this.date = date;
		this.info = info;
		this.hours = hours;
		this.bonus = bonus;
		this.wage = BigDecimal.ZERO;
	}

	public LocalDate getDate() {
		return date;
	}

	public String getInfo() {
		return info;
	}
	
	public BigDecimal getHours() {
		return hours;
	}

	public BigDecimal getBonus() {
		return bonus;
	}
	
	public BigDecimal getWage() {
		return wage;
	}
	
	public void setWage(BigDecimal wage) {
		this.wage = wage;
	}
	
	@Override
	public String toString() {
		return "EmploymentData [date=" + date + ", hours=" + hours + ", bonus=" + bonus + "]";
	}
	
}
