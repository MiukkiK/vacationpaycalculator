package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/** 
 * Main calculator class. Performs the vacation pay calculations.
 * custom output toString() for instructions on how to fill the vacation pay form.
 * @author Mia Kallio
 */
public class VacationPayCalculator {
	
	private final BigDecimal[] sovitutPaivat = new BigDecimal[12];
	private final BigDecimal[] sovitutTunnit = new BigDecimal[12];
	private boolean sovitutPaivatMuuttuneet;
	private VacationDayMethod lomaPaivaLaskuTapa;

	private final BigDecimal[] kuukaudenTyoTunnit = new BigDecimal[12];
	private final BigDecimal[] kuukaudenTyoPaivat = new BigDecimal[12];
	private final BigDecimal[] kuukaudenLomaPaivat = new BigDecimal[12];
	private final BigDecimal[] kuukaudenPalkka = new BigDecimal[12];

	private EmployeeRecord record;
	
	private int lomanMaaraytymisKuukaudet;
	private BigDecimal lomaPaivatPerMaaraytymisKuukausi;
	private int lomaPaivat;
	
	private BigDecimal lomaPaivatYhteensa;
	private BigDecimal paivaPalkkaKeskiarvo;

	private boolean lomaRahaOikeus;
	
	private Category lomaPalkkalaskuTapa;
	//category 1 fields
	private BigDecimal kuukausiPalkka;
	private BigDecimal kuukausiTyoPaivat;
	private BigDecimal paivaPalkka;
	private BigDecimal lomaPalkka;

	//category 2 fields
	private BigDecimal palkkaYhteensa;
	private BigDecimal tyoPaivatYhteensa;
	private BigDecimal tyoPaivatPerViikkoKeskiarvo;
	private BigDecimal lomaPalkkaKerroin;

	//category 4 fields
	private BigDecimal lomaKorvausYhteensa;
	private BigDecimal saamattaJaanytPalkka;
	private BigDecimal korvausProsentti;
	private BigDecimal lomaKorvaus;

	private BigDecimal lomaRaha;
	
	public enum VacationDayMethod {
		PAIVAT,
		TUNNIT;
	}
	
	public enum Category {
		KUUKAUSIPALKALLINEN,
		PAIVAKOHTAINEN;
	}

	public VacationPayCalculator (EmployeeRecord record, int year) {

		this.record = record;
		
		final LocalDate endDate = Rules.getLaskuKaudenLoppu(year);
		final LocalDate startDate = endDate.minusYears(1).plusDays(1); 

		/*
		 * Vuosilomalaki 18.3.2005/162: §6
		 * Jos työntekijä on sopimuksen mukaisesti työssä niin harvoina päivinä,
		 * että hänelle ei tästä syystä kerry ainoatakaan 14 työssäolopäivää sisältävää
		 * kalenterikuukautta tai vain osa kalenterikuukausista sisältää 14 työssäolopäivää,
		 * täydeksi lomanmääräytymiskuukaudeksi katsotaan sellainen kalenterikuukausi, jonka
		 * aikana työntekijälle on kertynyt vähintään 35 työtuntia tai 7 §:ssä tarkoitettua 
		 * työssäolon veroista tuntia.
		 */
		ChangeList dayChanges = record.getWorkDayChanges();
		if (!dayChanges.getDataBetween(startDate, endDate).isEmpty()) {
			sovitutPaivatMuuttuneet = true;
			// TODO work day changes not implemeneted yet

		} else {
			sovitutPaivatMuuttuneet = false;
			BigDecimal unchangedDays = dayChanges.getValueOn(endDate).multiply(new BigDecimal(4));
			for (int i=0; i < 12; i++) {
				sovitutPaivat[i] = unchangedDays;
			}
			if (unchangedDays.multiply(new BigDecimal(4)).compareTo(Rules.getKuukausiPaivaVaatimus()) != -1) {
				lomaPaivaLaskuTapa = VacationDayMethod.PAIVAT;
			}
		}
		
		ChangeList hourChanges = record.getWorkHourChanges();
		if (!hourChanges.getDataBetween(startDate, endDate).isEmpty()) {
			// TODO work hour changes not implemented yet

		} else {
			BigDecimal unchangedHours = hourChanges.getValueOn(endDate).multiply(new BigDecimal(4));
			for (int i=0; i < 12; i++) {
				sovitutTunnit[i] = unchangedHours;
			}
			if (unchangedHours.multiply(new BigDecimal(4)).compareTo(Rules.getKuukausiTuntiVaatimus()) != -1) {
				lomaPaivaLaskuTapa = VacationDayMethod.TUNNIT;
			}
		}

		// data processing to monthly totals
		if (record.isSalaried()) {
			for (int i=0 ; i < 12; i++) {
				kuukaudenTyoPaivat[i] = sovitutPaivat[i]; // workday tracking not implemented for salaraied employees
			}
			BigDecimal unchangedSalary = record.getSalaryChanges().getValueOn(endDate);
			ChangeList salaryChanges = record.getSalaryChanges();
			if (!salaryChanges.getDataBetween(startDate, endDate).isEmpty()) {
				// TODO salary changes not implememnted yet

			} else {
				for (int i=0; i < 12; i++) {
					kuukaudenPalkka[i] = unchangedSalary;
				}
			}
		} else {
			List<EmploymentData> filteredList = record.getEmploymentList().getDataBetween(startDate, endDate);
			for(EmploymentData data : filteredList) {
				int monthIndex = data.getDate().getMonthValue() - 1;

				/*
				 * Vuosilomalaki 18.3.2005/162: §7
				 * Työssäolon veroisena pidetään työstä poissaoloaikaa, jolta työnantaja on lain mukaan velvollinen maksamaan työntekijälle palkan.
				 *
				 * PAM Kaupan alan TES, §20 10.
				 * 10. Maksettuun palkkaan lisätään laskennallista palkkaa:
				 * ...
				 * raskaus- ja vanhempainvapaan vuosilomaa kerryttävältä ajalta
				 * tilapäisen hoitovapaan ajalta (työsopimuslain 4:6 §)
				 */
				if (!data.getInfo().equals("")) { // days with info are not added as regular workdays or add to vacation total hour count. (weekday holiday bonus)
					if (kuukaudenLomaPaivat[monthIndex] == null) kuukaudenLomaPaivat[monthIndex] = BigDecimal.ZERO;
					if (data.getHours() == BigDecimal.ZERO) kuukaudenLomaPaivat[monthIndex] = kuukaudenLomaPaivat[monthIndex].add(BigDecimal.ONE); // days with info and no hours are treated as valid leave days, limited leave such as sick leave not implememnted yet.
				} else  {	
					if (kuukaudenTyoPaivat[monthIndex] == null) kuukaudenTyoPaivat[monthIndex] = BigDecimal.ZERO;
					kuukaudenTyoPaivat[monthIndex] = kuukaudenTyoPaivat[monthIndex].add(BigDecimal.ONE);
					if (kuukaudenTyoTunnit[monthIndex] == null) kuukaudenTyoTunnit[monthIndex] = BigDecimal.ZERO;
					kuukaudenTyoTunnit[monthIndex] = kuukaudenTyoTunnit[monthIndex].add(data.getHours());
					/**
					 * PAM Kaupan alan TES: §20 6.
					 * Lomapalkka provision osalta lasketaan vuosilomalain mukaan.
					 */
					if (kuukaudenPalkka[monthIndex] == null) kuukaudenPalkka[monthIndex] = BigDecimal.ZERO;
					kuukaudenPalkka[monthIndex] = kuukaudenPalkka[monthIndex].add((data.getHours().multiply(data.getWage())).add(data.getBonus()));
				}
			}
		}
		palkkaYhteensa = BigDecimal.ZERO;
		for (BigDecimal thisMonthsPay : kuukaudenPalkka) {
			if (thisMonthsPay != null) palkkaYhteensa = palkkaYhteensa.add(thisMonthsPay);
		}
		tyoPaivatYhteensa = BigDecimal.ZERO;
		for (BigDecimal thisMonthsDays : kuukaudenTyoPaivat) {
			if (thisMonthsDays != null) tyoPaivatYhteensa = tyoPaivatYhteensa.add(thisMonthsDays);
		}
		paivaPalkkaKeskiarvo = palkkaYhteensa.divide(tyoPaivatYhteensa, 3, RoundingMode.HALF_UP);

		// vacation day calculation

		lomanMaaraytymisKuukaudet = 0;
		lomaKorvausYhteensa = BigDecimal.ZERO;
		for (int i = 0; i < 12; i++) {
			if (kuukaudenTyoTunnit[i] == null) kuukaudenTyoTunnit[i] = BigDecimal.ZERO;
			if (kuukaudenPalkka[i] == null) kuukaudenPalkka[i] = BigDecimal.ZERO;		
			if (kuukaudenTyoPaivat[i] == null) kuukaudenTyoPaivat[i] = BigDecimal.ZERO;
			if (kuukaudenLomaPaivat[i] == null) kuukaudenLomaPaivat[i] = BigDecimal.ZERO;
		
			/*
			 * Vuosilomalaki 18.3.2005/162: §6
			 * Jos työntekijä on sopimuksen mukaisesti työssä niin harvoina päivinä,
			 * että hänelle ei tästä syystä kerry ainoatakaan 14 työssäolopäivää sisältävää
			 * kalenterikuukautta tai vain osa kalenterikuukausista sisältää 14 työssäolopäivää,
			 * täydeksi lomanmääräytymiskuukaudeksi katsotaan sellainen kalenterikuukausi, jonka
			 * aikana työntekijälle on kertynyt vähintään 35 työtuntia tai 7 §:ssä tarkoitettua 
			 * työssäolon veroista tuntia.
			 */
			if ((sovitutPaivat[i].compareTo(Rules.getKuukausiPaivaVaatimus()) != -1) && 
					(kuukaudenTyoPaivat[i].add(kuukaudenLomaPaivat[i]).compareTo(Rules.getKuukausiPaivaVaatimus()) != -1))
				lomanMaaraytymisKuukaudet++;
			// Add leave days as proportionate hours as planned weekly hours / 5 per day
			else if ((sovitutTunnit[i].compareTo(Rules.getKuukausiTuntiVaatimus()) != -1) && 
					(kuukaudenLomaPaivat[i].divide(new BigDecimal(5)).multiply(sovitutTunnit[i]).add(kuukaudenTyoTunnit[i]).compareTo(Rules.getKuukausiTuntiVaatimus()) != -1))
				lomanMaaraytymisKuukaudet++;
			else {
				lomaKorvausYhteensa = lomaKorvausYhteensa.add(kuukaudenPalkka[i]);
				palkkaYhteensa = palkkaYhteensa.subtract(kuukaudenPalkka[i]);
			}
		}
		/* 
		 * Vuosilomalaki 18.3.2005/162: §5
		 * Työntekijällä on oikeus saada lomaa kaksi ja puoli arkipäivää kultakin täydeltä lomanmääräytymiskuukaudelta. 
		 * Jos työsuhde on lomanmääräytymisvuoden loppuun mennessä jatkunut yhdenjaksoisesti alle vuoden, 
		 * työntekijällä on kuitenkin oikeus saada lomaa kaksi arkipäivää kultakin täydeltä lomanmääräytymiskuukaudelta. 
		 * Loman pituutta laskettaessa päivän osa pyöristetään täyteen lomapäivään.
		 */	
		lomaPaivatPerMaaraytymisKuukausi = Rules.getLomanAnsainta(record.getStartDate(), endDate);
		BigDecimal lomaPaivatPyoristamaton = lomaPaivatPerMaaraytymisKuukausi.multiply(new BigDecimal(lomanMaaraytymisKuukaudet));	
		lomaPaivatPyoristamaton = lomaPaivatPyoristamaton.round(new MathContext(lomaPaivatPyoristamaton.precision() - lomaPaivatPyoristamaton.scale(), RoundingMode.CEILING));
		lomaPaivat = lomaPaivatPyoristamaton.intValue();

		if (lomaPaivat != 0) lomaRahaOikeus = true;
		else lomaRahaOikeus = false;
		
		// vacation day based pay calculation

		/**
		 * PAM Kaupan alan TES, §20 6.
		 * Jos työntekijän työaika ja vastaavasti palkka on muuttunut lomanmääräytymisvuoden aikana
		 * ja hän on kuukausipalkkainen lomanmääräytymisvuoden lopussa (31.3.), hänen lomapalkkansa 
		 * lasketaan tämän pykälän 8–11. kohdan mukaan.
		 */
		if (record.isSalaried() && !sovitutPaivatMuuttuneet) {
			if (lomaPaivat != 0) {
				lomaPalkkalaskuTapa = Category.KUUKAUSIPALKALLINEN;
				
				kuukausiPalkka = record.getSalaryChanges().getValueOn(endDate);
				kuukausiTyoPaivat = record.getWorkDayChanges().getValueOn(endDate).multiply(new BigDecimal(4));
				paivaPalkka = kuukausiPalkka.divide(kuukausiTyoPaivat);

				lomaPalkka = paivaPalkka.multiply(new BigDecimal(lomaPaivat));
			}
			/*
			 * Vuosilomalaki 18.3.2005/162: §11
			 * Muun kuin viikko- tai kuukausipalkalla työskentelevän sellaisen työntekijän vuosilomapalkka,
			 * joka sopimuksen mukaan työskentelee vähintään 14 päivänä kalenterikuukaudessa, lasketaan
			 * kertomalla hänen keskipäiväpalkkansa lomapäivien määrän perusteella määräytyvällä kertoimella
			 */
		} else {
			if (lomaPaivat != 0) lomaPalkkalaskuTapa = Category.PAIVAKOHTAINEN;
			lomaPalkkaKerroin = Rules.getLomaPalkkaKerroin(lomaPaivat);
			lomaPalkka = paivaPalkkaKeskiarvo.multiply(lomaPalkkaKerroin);
			
			//TODO Not implemented yet
			/**
			if (record.isSalaried() == true) {
				averageWeeklyWorkDays = new BigDecimal(totalDays).divide(new BigDecimal(12));
				vacationPay = vacationPay.multiply(averageWeeklyWorkDays.divide(new BigDecimal(5)));
			}
			 */
		}
		if (lomaRahaOikeus) lomaRaha = lomaPalkka.divide(new BigDecimal(2));
		// percentile vacation pay calculation

		lomaPaivatYhteensa = BigDecimal.ZERO;
		for (BigDecimal leaveThisMonth : kuukaudenLomaPaivat) {
			lomaPaivatYhteensa = lomaPaivatYhteensa.add(leaveThisMonth);
		}
		lomaKorvaus = BigDecimal.ZERO;
		if ((lomaKorvausYhteensa != BigDecimal.ZERO) || (lomaPaivatYhteensa != BigDecimal.ZERO)) {

			if (lomaPalkkalaskuTapa == Category.KUUKAUSIPALKALLINEN) saamattaJaanytPalkka = lomaPaivatYhteensa.multiply(paivaPalkka); 
			else saamattaJaanytPalkka = lomaPaivatYhteensa.multiply(paivaPalkkaKeskiarvo);

			korvausProsentti = Rules.getKorvausProsentti(record.getStartDate(), endDate);
			lomaKorvaus = lomaKorvausYhteensa.add(saamattaJaanytPalkka).multiply(korvausProsentti).movePointLeft(2);		
		}
	}

	public void printMonthlyInformation() {
		String[] months = new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
		for (int i=0; i < months.length; i++) {
			System.out.println(months[i] + ": Workdays - " + kuukaudenTyoPaivat[i] + ", Leave days - " + kuukaudenLomaPaivat[i] + ", Work hours - " + kuukaudenTyoTunnit[i] + ", Monthly pay - " + kuukaudenPalkka[i]);
		}
		System.out.println("Category is " + lomaPalkkalaskuTapa);
	}

	public BigDecimal[] getSovitutPaivat() {
		return sovitutPaivat;
	}

	public BigDecimal[] getSovitutTunnit() {
		return sovitutTunnit;
	}
	
	public VacationDayMethod getLomaPaivaLaskuTapa() {
		return lomaPaivaLaskuTapa;
	}
	
	public int getLomanMaaraytymisKuukaudet() {
		return lomanMaaraytymisKuukaudet;
	}

	public BigDecimal getLomaPaivatPerMaaraytymisKuukausi() {
		return lomaPaivatPerMaaraytymisKuukausi;
	}

	public int getLomaPaivat() {
		return lomaPaivat;
	}
	
	public BigDecimal[] getKuukaudenTyoTunnit() {
		return kuukaudenTyoTunnit;
	}

	public BigDecimal[] getKuukaudenTyoPaivat() {
		return kuukaudenTyoPaivat;
	}

	public BigDecimal[] getKuukaudenLomaPaivat() {
		return kuukaudenLomaPaivat;
	}

	public BigDecimal[] getKuukaudenPalkka() {
		return kuukaudenPalkka;
	}

	public BigDecimal getPaivaPalkkaKeskiarvo() {
		return paivaPalkkaKeskiarvo;
	}

	public Category getLomaPalkkaLaskuTapa() {
		return lomaPalkkalaskuTapa;
	}

	public BigDecimal getKuukausiPalkka() {
		return kuukausiPalkka;
	}

	public BigDecimal getKuukausiTyoPaivat() {
		return kuukausiTyoPaivat;
	}

	public BigDecimal getPaivaPalkka() {
		return paivaPalkka;
	}

	public BigDecimal getLomaPalkka() {
		return lomaPalkka;
	}

	public BigDecimal getPalkkaYhteensa() {
		return palkkaYhteensa;
	}

	public BigDecimal getTyoPaivatYhteensa() {
		return tyoPaivatYhteensa;
	}

	public BigDecimal getLomaPaivatYhteensa() {
		return lomaPaivatYhteensa;
	}
	
	public BigDecimal getTyoPaivatPerViikkoKeskiarvo() {
		return tyoPaivatPerViikkoKeskiarvo;
	}

	public BigDecimal getLomaPalkkaKerroin() {
		return lomaPalkkaKerroin;
	}

	public BigDecimal getLomaKorvausYhteensa() {
		return lomaKorvausYhteensa;
	}

	public BigDecimal getSaamattaJaanytPalkka() {
		return saamattaJaanytPalkka;
	}

	public BigDecimal getKorvausProsentti() {
		return korvausProsentti;
	}

	public BigDecimal getLomaKorvaus() {
		return lomaKorvaus;
	}

	public boolean oikeusLomaRahaan() {
		return lomaRahaOikeus;
	}
	
	public BigDecimal getLomaRaha() {
		return lomaRaha;
	}
	
	@Override
	public String toString() {
		String resultString = "";
		if (lomaPalkkalaskuTapa == Category.KUUKAUSIPALKALLINEN) {
			resultString = "Kohtaan 1:\n";
			resultString += "(" + kuukausiPalkka + " € : " + kuukausiTyoPaivat + " = " + String.format(Locale.ENGLISH, "%.2f", paivaPalkka) + " X " + lomaPaivat + " = " + String.format(Locale.ENGLISH, "%.2f", lomaPalkka) + " €\n";
		} else  if (lomaPalkkalaskuTapa == Category.PAIVAKOHTAINEN) {
			resultString = "Kohtaan 2:\n";
			resultString += palkkaYhteensa + " € : " + tyoPaivatYhteensa + " = " + String.format(Locale.ENGLISH, "%.2f", paivaPalkkaKeskiarvo) + " €/pv { X ";
			if (record.isSalaried()) resultString += tyoPaivatPerViikkoKeskiarvo;
			else resultString += "-";
			resultString += " : 5 } X " + lomaPalkkaKerroin + " = " + String.format(Locale.ENGLISH, "%.2f", lomaPalkka) + " €\n";
		}
		if (lomaKorvaus != BigDecimal.ZERO) {
			resultString += "Kohtaan 4:\n";
			resultString += lomaKorvausYhteensa + " € + " + String.format(Locale.ENGLISH, "%.2f", saamattaJaanytPalkka) + " € X " + String.format(Locale.ENGLISH, "%.1f", korvausProsentti.multiply(new BigDecimal(100))) + " % = " + String.format(Locale.ENGLISH, "%.2f", lomaKorvaus) + " €";
		}
		return resultString;
	}
}
