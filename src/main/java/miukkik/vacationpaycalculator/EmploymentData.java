package miukkik.vacationpaycalculator;

import java.time.LocalDate;

public class EmploymentData {
	private LocalDate date;
	private double hours;
	private double bonus;
	private double wage;
	private String info;
	
	public EmploymentData(LocalDate date, String info, double hours, double bonus) {
		this.date = date;
		this.info = info;
		this.hours = hours;
		this.bonus = bonus;
	}

	public LocalDate getDate() {
		return date;
	}

	public String getInfo() {
		return info;
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
