package miukkik.vacationpaycalculator;

import java.math.BigDecimal;

/** 
 * Basic data element for monthly totals. Default getters and setters.
 * 
 * @author Mia Kallio
 */
public class MonthlyData {
	private BigDecimal tyoPaivat;
	private BigDecimal poissaOlot;
	private BigDecimal tyoTunnit;
	private BigDecimal palkka;
	
	public MonthlyData() {
		tyoPaivat = BigDecimal.ZERO;
		poissaOlot = BigDecimal.ZERO;
		tyoTunnit = BigDecimal.ZERO;
		palkka = BigDecimal.ZERO;
	}

	public BigDecimal getTyoPaivat() {
		return tyoPaivat;
	}

	public void setTyoPaivat(BigDecimal tyoPaivat) {
		this.tyoPaivat = tyoPaivat;
	}

	public BigDecimal getPoissaOlot() {
		return poissaOlot;
	}

	public void setPoissaOlot(BigDecimal poissaOlot) {
		this.poissaOlot = poissaOlot;
	}

	public BigDecimal getTyoTunnit() {
		return tyoTunnit;
	}

	public void setTyoTunnit(BigDecimal tyoTunnit) {
		this.tyoTunnit = tyoTunnit;
	}

	public BigDecimal getPalkka() {
		return palkka;
	}

	public void setPalkka(BigDecimal palkka) {
		this.palkka = palkka;
	}
	
	
}