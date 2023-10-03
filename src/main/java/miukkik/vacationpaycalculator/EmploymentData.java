package miukkik.vacationpaycalculator;

import java.time.LocalDate;

public class EmploymentData {
	private LocalDate date;
	private String info;
	private double hours;
	private double bonus;
	
	public EmploymentData(LocalDate date, String info, double hours, double bonus) {
		super();
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

	@Override
	public String toString() {
		return "EmploymentData [date=" + date + ", info=" + info + ", hours=" + hours + ", bonus=" + bonus + "]";
	}
	
}
