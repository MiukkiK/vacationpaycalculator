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

	private EmployeeRecord record;

	private LomaPaivienAnsaintaSaanto lomaPaivienAnsaintaSaanto;

	private MonthlyData[] kuukausi;

	private int lomanMaaraytymisKuukaudet;
	private BigDecimal lomaPaivatPerMaaraytymisKuukausi;
	private int lomaPaivat;

	private BigDecimal palkkaYhteensa;
	private BigDecimal tyoPaivatYhteensa;
	private BigDecimal poissaOlotYhteensa;
	private BigDecimal tyoTunnitYhteensa;

	private BigDecimal paivaPalkkaKeskiarvo;

	private boolean lomaRahaOikeus;

	private LomaPalkkaKaava lomaPalkkaKaava;

	//category 1 fields
	private BigDecimal kuukausiPalkka;
	private BigDecimal kuukausiTyoPaivat;
	private BigDecimal paivaPalkka;

	//category 2 fields
	private BigDecimal tyoPaivatPerViikkoKeskiarvo;
	private BigDecimal lomaPalkkaKerroin;

	//category 4 fields
	private BigDecimal saamattaJaanytPalkka;
	private BigDecimal korvausProsentti;

	//common fields
	private BigDecimal lomaPalkka;
	private BigDecimal lomaRaha;

	public enum LomaPaivienAnsaintaSaanto {
		PAIVAT,
		TUNNIT;
	}

	public enum LomaPalkkaKaava {
		KUUKAUSIPALKKAISET,
		TUNTIPALKKAISET_VUOSILOMALAKI,
		TUNTIPALKKAISET_LOMAPALKKASOPIMUS,
		PROSENTTIPERUSTEINEN;
	}

	public VacationPayCalculator (EmployeeRecord record, int year) {

		this.record = record;

		final LocalDate endDate = Rules.getLaskuKaudenLoppu(year);
		final LocalDate startDate = endDate.minusYears(1).plusDays(1); 

		kuukausi = initData(record, startDate, endDate);
		calculateTotals(kuukausi);

		paivaPalkkaKeskiarvo = palkkaYhteensa.divide(tyoPaivatYhteensa, 3, RoundingMode.DOWN);

		lomaPaivienAnsaintaSaanto = calculateLomaPaivienAnsaintaSaanto(record.getWorkDayChanges().getValueOn(endDate), record.getWorkDayChanges().hasChangedBetween(startDate, endDate));

		lomanMaaraytymisKuukaudet = calculateMaaraytymisKuukaudet(lomaPaivienAnsaintaSaanto, kuukausi);

		/* 
		 * Vuosilomalaki 18.3.2005/162: §5
		 * Työntekijällä on oikeus saada lomaa kaksi ja puoli arkipäivää kultakin täydeltä lomanmääräytymiskuukaudelta. 
		 * Jos työsuhde on lomanmääräytymisvuoden loppuun mennessä jatkunut yhdenjaksoisesti alle vuoden, 
		 * työntekijällä on kuitenkin oikeus saada lomaa kaksi arkipäivää kultakin täydeltä lomanmääräytymiskuukaudelta. 
		 * Loman pituutta laskettaessa päivän osa pyöristetään täyteen lomapäivään.
		 */	
		lomaPaivatPerMaaraytymisKuukausi = Rules.getLomanAnsainta(record.getStartDate(), endDate);

		lomaPaivat = calculateLomaPaivat(lomanMaaraytymisKuukaudet, lomaPaivatPerMaaraytymisKuukausi);

		if (lomaPaivat != 0) lomaRahaOikeus = true; else lomaRahaOikeus = false;

		lomaPalkkaKaava = calculateLomaPalkkaKaava(lomaPaivat, record.isSalaried(), record.getWorkDayChanges().hasChangedBetween(startDate, endDate));

		// Lomapalkka calculation

		switch (lomaPalkkaKaava) {
		case KUUKAUSIPALKKAISET: 
			kuukausiPalkka = record.getSalaryChanges().getValueOn(endDate);
			kuukausiTyoPaivat = record.getWorkDayChanges().getValueOn(endDate).multiply(new BigDecimal(4));

			paivaPalkka = kuukausiPalkka.divide(kuukausiTyoPaivat);

			lomaPalkka = paivaPalkka.multiply(new BigDecimal(lomaPaivat));
			break;

		case TUNTIPALKKAISET_VUOSILOMALAKI:
			/**
			 * PAM Kaupan alan TES, §20 6.
			 * Jos työntekijän työaika ja vastaavasti palkka on muuttunut lomanmääräytymisvuoden aikana
			 * ja hän on kuukausipalkkainen lomanmääräytymisvuoden lopussa (31.3.), hänen lomapalkkansa 
			 * lasketaan tämän pykälän 8–11. kohdan mukaan.
			 *
			 * Vuosilomalaki 18.3.2005/162: §11
			 * Muun kuin viikko- tai kuukausipalkalla työskentelevän sellaisen työntekijän vuosilomapalkka,
			 * joka sopimuksen mukaan työskentelee vähintään 14 päivänä kalenterikuukaudessa, lasketaan
			 * kertomalla hänen keskipäiväpalkkansa lomapäivien määrän perusteella määräytyvällä kertoimella
			 */
			lomaPalkkaKerroin = Rules.getLomaPalkkaKerroin(lomaPaivat);
			lomaPalkka = paivaPalkkaKeskiarvo.multiply(lomaPalkkaKerroin);
			break;

		case TUNTIPALKKAISET_LOMAPALKKASOPIMUS:
			// not implemmented yet
			break;

		case PROSENTTIPERUSTEINEN:
			poissaOlotYhteensa = BigDecimal.ZERO;
			for (MonthlyData data : kuukausi) {
				poissaOlotYhteensa = poissaOlotYhteensa.add(data.getPoissaOlot());
			}
			saamattaJaanytPalkka = poissaOlotYhteensa.multiply(paivaPalkkaKeskiarvo);
			korvausProsentti = Rules.getKorvausProsentti(record.getStartDate(), endDate);
			lomaPalkka = palkkaYhteensa.add(saamattaJaanytPalkka).multiply(korvausProsentti).movePointLeft(2);
		}

		if (lomaRahaOikeus) lomaRaha = lomaPalkka.divide(new BigDecimal(2));

	}

	private MonthlyData[] initData(EmployeeRecord record, LocalDate startDate, LocalDate endDate) {
		MonthlyData[] monthlyData = new MonthlyData[12];
		for (int i = 0; i < 12; i++) {
			monthlyData[i] = new MonthlyData();
		}

		if (record.isSalaried()) {
			for (MonthlyData data : monthlyData) {
				data.setTyoPaivat(record.getWorkDayChanges().getValueOn(endDate).multiply(new BigDecimal(4))); 
				data.setPalkka(record.getSalaryChanges().getValueOn(endDate));
			}

		} else {
			List<EmploymentData> filteredList = record.getEmploymentList().getDataBetween(startDate, endDate);
			for(EmploymentData data : filteredList) {
				int monthIndex = data.getDate().getMonthValue() - 1;
				MonthlyData currentMonth = monthlyData[monthIndex];

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
					if (data.getHours() == BigDecimal.ZERO) currentMonth.setPoissaOlot(currentMonth.getPoissaOlot().add(BigDecimal.ONE)); // days with info and no hours are treated as valid leave days, limited leave such as sick leave not implememnted yet.
				} else  {
					currentMonth.setTyoPaivat(currentMonth.getTyoPaivat().add(BigDecimal.ONE));
					currentMonth.setTyoTunnit(currentMonth.getTyoTunnit().add(data.getHours()));
					/**
					 * PAM Kaupan alan TES: §20 6.
					 * Lomapalkka provision osalta lasketaan vuosilomalain mukaan.
					 */
					currentMonth.setPalkka(currentMonth.getPalkka().add((data.getHours().multiply(data.getWage())).add(data.getBonus())));
				}
			}
		}
		return monthlyData;
	}

	private void calculateTotals(MonthlyData[] monthlyData) {

		palkkaYhteensa = BigDecimal.ZERO;
		poissaOlotYhteensa = BigDecimal.ZERO;
		tyoPaivatYhteensa = BigDecimal.ZERO;
		tyoTunnitYhteensa = BigDecimal.ZERO;

		for (MonthlyData data : monthlyData) {
			palkkaYhteensa = palkkaYhteensa.add(data.getPalkka());
			tyoPaivatYhteensa = tyoPaivatYhteensa.add(data.getTyoPaivat());
			poissaOlotYhteensa = poissaOlotYhteensa.add(data.getPoissaOlot());
			tyoTunnitYhteensa = tyoTunnitYhteensa.add(data.getTyoTunnit());
		}
	}

		/**
		 * PAM Kaupan alan TES, §20 2.
		 * ...
		 * Lomaa ansaitaan joko 14 päivän tai 35 tunnin säännön perusteella.
		 * 
		 * Lomaa ansaitaan 35 tunnin säännön perusteella työntekijän työskennellessä työsopimuksen mukaan alle 14 päivää kuukaudessa.
		 */
		private LomaPaivienAnsaintaSaanto calculateLomaPaivienAnsaintaSaanto(BigDecimal sovitutTyoPaivat, boolean sovitutPaivatMuuttuneet) {
			if (!sovitutPaivatMuuttuneet && (sovitutTyoPaivat.multiply(new BigDecimal(4)).compareTo(Rules.getKuukausiPaivaVaatimus()) != -1))
				return LomaPaivienAnsaintaSaanto.PAIVAT;
			else return LomaPaivienAnsaintaSaanto.TUNNIT;
		}

		/*
		 * Vuosilomalaki 18.3.2005/162: §6
		 * Jos työntekijä on sopimuksen mukaisesti työssä niin harvoina päivinä,
		 * että hänelle ei tästä syystä kerry ainoatakaan 14 työssäolopäivää sisältävää
		 * kalenterikuukautta tai vain osa kalenterikuukausista sisältää 14 työssäolopäivää,
		 * täydeksi lomanmääräytymiskuukaudeksi katsotaan sellainen kalenterikuukausi, jonka
		 * aikana työntekijälle on kertynyt vähintään 35 työtuntia tai 7 §:ssä tarkoitettua 
		 * työssäolon veroista tuntia.
		 */
		private int calculateMaaraytymisKuukaudet(LomaPaivienAnsaintaSaanto ansaintaSaanto, MonthlyData[] monthlyData) {
			int months = 0;
			if (lomaPaivienAnsaintaSaanto == LomaPaivienAnsaintaSaanto.PAIVAT) {
				for (int i = 0; i < 12; i++) {
					if (monthlyData[i].getTyoPaivat().add(monthlyData[i].getPoissaOlot()).compareTo(Rules.getKuukausiPaivaVaatimus()) != -1) months++;
				}
			}
			else if (lomaPaivienAnsaintaSaanto == LomaPaivienAnsaintaSaanto.TUNNIT) {
				for (int i = 0; i < 12; i++) {
					if (monthlyData[i].getPoissaOlot().multiply(calculateTyoTuntiKeskiarvo(monthlyData)).add(monthlyData[i].getTyoTunnit()).compareTo(Rules.getKuukausiTuntiVaatimus()) != -1)
						months++;
				}		
			}
			return months;
		}

		private int calculateLomaPaivat(int kuukaudet, BigDecimal kerroin) {
			BigDecimal unrounded = new BigDecimal(kuukaudet).multiply(kerroin);
			return unrounded.round(new MathContext(unrounded.precision() - unrounded.scale(), RoundingMode.UP)).intValue();
		}

		private BigDecimal calculateTyoTuntiKeskiarvo(MonthlyData[] monthlyData) {
			BigDecimal hours = BigDecimal.ZERO;
			BigDecimal days = BigDecimal.ZERO;
			for (MonthlyData data : monthlyData) {
				hours = hours.add(data.getTyoTunnit());
				days = days.add(data.getTyoPaivat());
			}
			return hours.divide(days, 3, RoundingMode.DOWN);
		}

		private LomaPalkkaKaava calculateLomaPalkkaKaava(int lomaPaivat, boolean kuukausiPalkallinen, boolean tyoPaivatMuuttuneet) {
			if (lomaPaivat == 0) return LomaPalkkaKaava.PROSENTTIPERUSTEINEN;
			else if(kuukausiPalkallinen && !tyoPaivatMuuttuneet) return LomaPalkkaKaava.KUUKAUSIPALKKAISET;
			// LomaPalkkaKaava.TUNTIPALKKAISET_LOMAPALKKASOPIMUS not implemented yet
			else return LomaPalkkaKaava.TUNTIPALKKAISET_VUOSILOMALAKI;
		}

		public void printMonthlyInformation() {
			String[] months = new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
			for (int i=0; i < months.length; i++) {
				System.out.println(months[i] + ": Workdays - " + kuukausi[i].getTyoPaivat() + ", Leave days - " + kuukausi[i].getPoissaOlot() + ", Work hours - " + kuukausi[i].getTyoTunnit() + ", Monthly pay - " + kuukausi[i].getPalkka());
			}
			System.out.println("Category is " + lomaPalkkaKaava);
		}

		public LomaPaivienAnsaintaSaanto getLomaPaiivienAnsaintaSaanto() {
			return lomaPaivienAnsaintaSaanto;
		}

		public BigDecimal getPaivaPalkkaKeskiarvo() {
			return paivaPalkkaKeskiarvo;
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

		public LomaPalkkaKaava getLomaPalkkaKaava() {
			return lomaPalkkaKaava;
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

		public BigDecimal getPoissaOlotYhteensa() {
			return poissaOlotYhteensa;
		}

		public BigDecimal getTyoPaivatPerViikkoKeskiarvo() {
			return tyoPaivatPerViikkoKeskiarvo;
		}

		public BigDecimal getLomaPalkkaKerroin() {
			return lomaPalkkaKerroin;
		}

		public BigDecimal getSaamattaJaanytPalkka() {
			return saamattaJaanytPalkka;
		}

		public BigDecimal getKorvausProsentti() {
			return korvausProsentti;
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
			if (lomaPalkkaKaava == LomaPalkkaKaava.KUUKAUSIPALKKAISET) {
				resultString = "Kohtaan 1:\n";
				resultString += "(" + kuukausiPalkka + " € : " + kuukausiTyoPaivat + " = " + String.format(Locale.ENGLISH, "%.2f", paivaPalkka) + " X " + lomaPaivat + " = " + String.format(Locale.ENGLISH, "%.2f", lomaPalkka) + " €\n";
			} else  if (lomaPalkkaKaava == LomaPalkkaKaava.TUNTIPALKKAISET_VUOSILOMALAKI) {
				resultString = "Kohtaan 2:\n";
				resultString += palkkaYhteensa + " € : " + tyoPaivatYhteensa + " = " + String.format(Locale.ENGLISH, "%.2f", paivaPalkkaKeskiarvo) + " €/pv { X ";
				if (record.isSalaried()) resultString += tyoPaivatPerViikkoKeskiarvo;
				else resultString += "-";
				resultString += " : 5 } X " + lomaPalkkaKerroin + " = " + String.format(Locale.ENGLISH, "%.2f", lomaPalkka) + " €\n";
			}
			else if (lomaPalkkaKaava == LomaPalkkaKaava.PROSENTTIPERUSTEINEN) {
				resultString += "Kohtaan 4:\n";
				resultString += palkkaYhteensa + " € + " + String.format(Locale.ENGLISH, "%.2f", saamattaJaanytPalkka) + " € X " + String.format(Locale.ENGLISH, "%.1f", korvausProsentti) + " % = " + String.format(Locale.ENGLISH, "%.2f", lomaPalkka) + " €";
			}
			return resultString;
		}
	}
