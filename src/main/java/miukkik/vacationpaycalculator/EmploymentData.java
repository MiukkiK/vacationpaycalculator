package miukkik.vacationpaycalculator;

import java.time.LocalDate;

public class EmploymentData {
	private LocalDate date;
	private double hours;
	private double bonus;
	private double wage;
	
	public EmploymentData(LocalDate date, double hours, double bonus) {
		this.date = date;
		this.hours = hours;
		this.bonus = bonus;
	}

	public LocalDate getDate() {
		return date;
	}

	public double getHours() {
		return hours;
	}

	public double getBonus() {
		return bonus;
	}
	
	public double getWage() {
		return wage;
	}

	public void setWage(double wage) {
		this.wage = wage;
	}

	@Override
	public String toString() {
		return "EmploymentData [date=" + date + ", hours=" + hours + ", bonus=" + bonus + "]";
	}
	
}
