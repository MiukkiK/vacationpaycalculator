package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.time.LocalDate;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import java.math.MathContext;
import java.math.RoundingMode;

/** 
 * Test class for the vacation pay calculator. Checks for outputs with given test cases A and B.
 * Also tests basic vacation pay calculations for salaried and general categories and percentile calculations.
 * @author Mia Kallio
 */
public class VacationPayCalculatorTest extends TestCase {
	
	// Overloaded assert for BigDecimals to show the expected values instead of result of compare (-1, 0, 1).
	public void assertEquals(String variableName, BigDecimal assertValue, BigDecimal testValue) {
		try {
		assertEquals(variableName + " should be " + assertValue + ", was " + testValue + ".", 0, testValue.compareTo(assertValue));
		} catch (AssertionFailedError e) {
			throw (new AssertionFailedError(variableName + " expected:<" + assertValue +"> but was:<" + testValue +">"));
		}
	}
	
	public void testCaseA() {
		EmployeeRecord testCase = new EmployeeRecord(LocalDate.of(2008, 6, 1), new BigDecimal(10));
		FileHandler.inputData("src/test/resources/raw_hours.txt", testCase.getEmploymentList());
		testCase.getEmploymentList().setWageFrom(LocalDate.of(2009, 10, 15), new BigDecimal(11));
		int vacationYear = 2010;
		VacationPayCalculator calculator = new VacationPayCalculator(testCase, vacationYear);
		// System.out.println(calculator);
		
		/*
		 * Vuosilomalaki 18.3.2005/162: §6
		 * Jos työntekijä on sopimuksen mukaisesti työssä niin harvoina päivinä,
		 * että hänelle ei tästä syystä kerry ainoatakaan 14 työssäolopäivää sisältävää
		 * kalenterikuukautta tai vain osa kalenterikuukausista sisältää 14 työssäolopäivää,
		 * täydeksi lomanmääräytymiskuukaudeksi katsotaan sellainen kalenterikuukausi, jonka
		 * aikana työntekijälle on kertynyt vähintään 35 työtuntia tai 7 §:ssä tarkoitettua 
		 * työssäolon veroista tuntia.
		 * 
		 * -----
		 * Jos työsopimuksessa ei ole sovittu päiviä, lasketaan tuntisäännön perusteella.
		 * 
		 * Expected: TUNNIT
		 */		
		assertEquals("Lomapäivälaskutapa", VacationPayCalculator.LomaPaivienAnsaintaSaanto.TUNNIT, calculator.getLomaPaivaLaskuTapa());
		
		/* Vuosilomalaki 18.3.2005/162: §7
		 * Työssäolon veroisena pidetään työstä poissaoloaikaa, jolta työnantaja on lain mukaan 
		 * velvollinen maksamaan työntekijälle palkan. Työssäolon veroisena pidetään myös aikaa, 
		 * jolloin työntekijä on poissa työstä sellaisen työajan tasaamiseksi annetun vapaan vuoksi, 
		 * jolla hänen keskimääräinen viikkotyöaikansa tasataan laissa säädettyyn enimmäismäärään. 
		 * Saman kalenterikuukauden aikana viikkotyöajan tasaamiseksi annetuista vapaapäivistä 
		 * työssäolon veroisina pidetään kuitenkin vain neljä päivää ylittäviä vapaapäiviä, 
		 * jollei vapaata ole annettu yli kuuden arkipäivän pituisena yhtenäisenä vapaana.
		 * 
		 * -----
		 * Työntekijällä on työssäolon veroisiksi laskettavia poissaoloja. koska sopimuksessa
		 * ei ole määritetty tunteja, ne lasketaan keskimääräisen työpäivän tuntien mukaan.
		 * 
		 * Expected: 12
		 */		
		assertEquals("Lomanmääräytymiskuukaudet", 12, calculator.getLomanMaaraytymisKuukaudet());
		
		/* 
		 * Vuosilomalaki 18.3.2005/162: §5
		 * Työntekijällä on oikeus saada lomaa kaksi ja puoli arkipäivää kultakin täydeltä lomanmääräytymiskuukaudelta. 
		 * Jos työsuhde on lomanmääräytymisvuoden loppuun mennessä jatkunut yhdenjaksoisesti alle vuoden, 
		 * työntekijällä on kuitenkin oikeus saada lomaa kaksi arkipäivää kultakin täydeltä lomanmääräytymiskuukaudelta. 
		 * 
		 * -----
		 * Työntekijä on ollut työsuhteessa vuodesta 2008, ja isompi kerroin täyttyy maaliskuussa 2009.
		 * 
		 * Expected:
		 * Lomapäiväkerroin - 2.5
		 * Lomapäivät - 30
		 */				
		BigDecimal assertLomaPaivaKerroin = new BigDecimal("2.5");
		BigDecimal testLomaPaivaKerroin = calculator.getLomaPaivatPerMaaraytymisKuukausi();
		assertEquals("Lomapäivät per määräytymiskuukausi", assertLomaPaivaKerroin, testLomaPaivaKerroin);
		
		assertEquals("Lomapäivät", 30, calculator.getLomaPaivat());
		
		/*
		 * Vuosilomalaki 18.3.2005/162: §11
		 * Muun kuin viikko- tai kuukausipalkalla työskentelevän sellaisen työntekijän vuosilomapalkka,
		 * joka sopimuksen mukaan työskentelee vähintään 14 päivänä kalenterikuukaudessa, lasketaan
		 * kertomalla hänen keskipäiväpalkkansa lomapäivien määrän perusteella määräytyvällä kertoimella.
		 * 
		 * -----
		 * Jos työntekijä ei ole kuukausipalkkainen ja hänellä on lomapäiviä, lomapalkka lasketaan keskipäiväpalkan pohjalta.
		 * 
		 * Expected: Category.PAIVAKOHTAINEN
		 */		
		assertEquals("Lomapalkkalaskutapa", VacationPayCalculator.LomaPalkkaKaava.TUNTIPALKKAISET_VUOSILOMALAKI, calculator.getLomaPalkkaLaskuTapa());

		/*
		 * Testisyötteessä on 195 työpäivää. Niihin on laskettu päivät joilla on työtunteja, mutta ei merkintöjä "arkipyhäkorvauksista"
		 * Esimerkkisyötteessä ei ole yli- tai hätätyötunteja, joten ne eivät koske lopputulosta.
		 * 
		 * Expected: 195
		 */
		BigDecimal assertTyoPaivat = new BigDecimal(195);
		BigDecimal testTyoPaivat = calculator.getTyoPaivatYhteensa();
		assertEquals("Työpäivät", assertTyoPaivat, testTyoPaivat);
				
		BigDecimal assertPoissaOloPaivat = new BigDecimal(36);
		BigDecimal testPoissaOloPaivat = calculator.getPoissaOloPaivatYhteensa();
		assertEquals("Poissaolopäivät", assertPoissaOloPaivat, testPoissaOloPaivat);

		
		/* PAM Kaupan alan TES: §21
		 * Lomaraha on 50 % vuosilomalain mukaan ansaittua lomaa vastaavasta lomapalkasta.
		 * 
		 * Lomaraha maksetaan työntekijän:
		 *  -aloittaessa loman ilmoitettuna tai sovittuna aikana ja
		 *  -palatessa työhön heti loman päätyttyä.
		 *  
		 *  
		 *  -----
		 *  Työntekijä saa lomapalkkaa, joten hänellä on oikeus lomarahaan.
		 *  
		 *  expected: true
		 */
		assertEquals("Lomarahaoikeus", true, calculator.oikeusLomaRahaan());					
	}
	
	public void testCaseB() {
		EmployeeRecord testCase = new EmployeeRecord(LocalDate.of(2008, 6, 1), new BigDecimal(10));
		FileHandler.inputData("src/test/resources/raw_hours.txt", testCase.getEmploymentList());
		testCase.getWorkHourChanges().add(new ChangeData(testCase.getStartDate(), new BigDecimal(37.5)));
		testCase.getEmploymentList().setWageFrom(LocalDate.of(2009, 10, 15), new BigDecimal(11));
		
		VacationPayCalculator calculator = new VacationPayCalculator(testCase, 2010);
		// System.out.println(calculator);
		
		/*
		 * Vuosilomalaki 18.3.2005/162: §6
		 * Jos työntekijä on sopimuksen mukaisesti työssä niin harvoina päivinä,
		 * että hänelle ei tästä syystä kerry ainoatakaan 14 työssäolopäivää sisältävää
		 * kalenterikuukautta tai vain osa kalenterikuukausista sisältää 14 työssäolopäivää,
		 * täydeksi lomanmääräytymiskuukaudeksi katsotaan sellainen kalenterikuukausi, jonka
		 * aikana työntekijälle on kertynyt vähintään 35 työtuntia tai 7 §:ssä tarkoitettua 
		 * työssäolon veroista tuntia.
		 *
		 * -----
		 * Jos työsopimuksessa ei ole sovittu vähintään 14 päivää kuukaudessa, lomapäivät lasketaan 35 tunnin säännön perusteella.
		 * 
		 * Expected: LomaPaivienAnsaintaSaanto.TUNNIT
		 */
		assertEquals("Lomapäivälaskutapa", VacationPayCalculator.LomaPaivienAnsaintaSaanto.TUNNIT, calculator.getLomaPaivaLaskuTapa());

		/*
		 * Vuosilomalaki 18.3.2005/162: §11
		 * Muun kuin viikko- tai kuukausipalkalla työskentelevän sellaisen työntekijän vuosilomapalkka,
		 * joka sopimuksen mukaan työskentelee vähintään 14 päivänä kalenterikuukaudessa, lasketaan
		 * kertomalla hänen keskipäiväpalkkansa lomapäivien määrän perusteella määräytyvällä kertoimella.
		 * 
		 * -----
		 * Jos työntekijä ei ole kuukausipalkkainen ja hänellä on lomapäiviä, lomapalkka lasketaan keskipäiväpalkan pohjalta.
		 * 
		 * Expected: LomaPalkkaKaava.TUNTIPALKKAISET_VUOSILOMALAKI
		 */		
		assertEquals("Palkanlaskutapa", VacationPayCalculator.LomaPalkkaKaava.TUNTIPALKKAISET_VUOSILOMALAKI, calculator.getLomaPalkkaLaskuTapa());

		/*
		 * Vuosilomalaki 18.3.2005/162: §6
		 * Jos työntekijä on sopimuksen mukaisesti työssä niin harvoina päivinä,
		 * että hänelle ei tästä syystä kerry ainoatakaan 14 työssäolopäivää sisältävää
		 * kalenterikuukautta tai vain osa kalenterikuukausista sisältää 14 työssäolopäivää,
		 * täydeksi lomanmääräytymiskuukaudeksi katsotaan sellainen kalenterikuukausi, jonka
		 * aikana työntekijälle on kertynyt vähintään 35 työtuntia tai 7 §:ssä tarkoitettua 
		 * työssäolon veroista tuntia.
		 * 
		 * §7
		 * Työssäolon veroisena pidetään työstä poissaoloaikaa, jolta työnantaja on lain mukaan velvollinen maksamaan työntekijälle palkan.
		 *
		 * -----
		 * Testisyötteessä on kaksi kuukautta jossa ei ole 35 työtuntia, 7/2009 ja 12/2009. Niissä on kuitenkin työssäolon veroisia päiviä korvaamaan vaaditut työtunnit.
		 * 
		 * Expected: 12
		 */
		assertEquals("Lomanmääräytymiskuukaudet", 12, calculator.getLomanMaaraytymisKuukaudet());
		
		/* 
		 * Vuosilomalaki 18.3.2005/162: §5
		 * Työntekijällä on oikeus saada lomaa kaksi ja puoli arkipäivää kultakin täydeltä lomanmääräytymiskuukaudelta. 
		 * Jos työsuhde on lomanmääräytymisvuoden loppuun mennessä jatkunut yhdenjaksoisesti alle vuoden, 
		 * työntekijällä on kuitenkin oikeus saada lomaa kaksi arkipäivää kultakin täydeltä lomanmääräytymiskuukaudelta. 
		 * 
		 * -----
		 * Työntekijä on ollut työsuhteessa vuodesta 2008, ja isompi kerroin täyttyy maaliskuussa 2009.
		 * 
		 * Expected: 
		 * Lomapäiväkerroin - 2.5
		 * Lomapäivät - 30
		 */
		BigDecimal assertLomaPaivaKerroin = new BigDecimal("2.5");
		BigDecimal testLomaPaivaKerroin = calculator.getLomaPaivatPerMaaraytymisKuukausi();
		assertEquals("Lomapäivät per määräytymiskuukausi", assertLomaPaivaKerroin, testLomaPaivaKerroin);
		
		assertEquals("Lomapäivät", 30, calculator.getLomaPaivat());
		
		/*
		 * Testisyötteessä on 195 työpäivää. Niihin on laskettu päivät joilla on työtunteja, mutta ei merkintöjä "arkipyhäkorvauksista"
		 * Esimerkkisyötteessä ei ole yli- tai hätätyötunteja, joten ne eivät koske lopputulosta.
		 * 
		 * Expected: 195
		 */		
		BigDecimal assertTyoPaivat = new BigDecimal(195);
		BigDecimal testTyoPaivat = calculator.getTyoPaivatYhteensa();
		assertEquals("Työpäivät", assertTyoPaivat, testTyoPaivat);
		

		/*
		 * Keskimääräinen päiväpalkka saadaan jakamalla ansaittu tulo työpäivien määrällä.
		 * 
		 * Expected: 14358 / 195 (~73.630)
		 */
		BigDecimal assertPalkkaKeskiarvo = new BigDecimal("73.630");
		BigDecimal testPalkkaKeskiarvo = calculator.getPaivaPalkkaKeskiarvo();
		assertEquals("Päiväpalkkakeskiarvo", assertPalkkaKeskiarvo, testPalkkaKeskiarvo);

		/*
		 * Vuosilomalaki 18.3.2005/162: 
		 * Jos työntekijän viikoittaisten työpäivien määrä on sopimuksen mukaan pienempi tai suurempi kuin viisi,
		 * keskipäiväpalkka kerrotaan viikoittaisten työpäivien määrällä ja jaetaan viidellä.
		 * 
		 * -----
		 * Testitapauksessa ei ole määritetty työpäiviä joten tämä kohta ei koske testitapausta.
		 */
		
		
		/*
		 * Vuosilomalaki 18.3.2005/162: §11
		 * Muun kuin viikko- tai kuukausipalkalla työskentelevän sellaisen työntekijän vuosilomapalkka,
		 * joka sopimuksen mukaan työskentelee vähintään 14 päivänä kalenterikuukaudessa, lasketaan kertomalla 
		 * hänen keskipäiväpalkkansa lomapäivien määrän perusteella määräytyvällä kertoimella.
		 * 
		 * -----
		 * Kerroin on määritelty vuosilomalaissa, se on epälineaarinen ~0.9 per päivä asteikko. 30 päivää vastaava luku on 27.8
		 * 
		 * Expected: 27.8
		 */
		BigDecimal assertLomaPalkkaKerroin = new BigDecimal("27.8");
		BigDecimal testLomaPalkkaKerroin = calculator.getLomaPalkkaKerroin();
		assertEquals("Lomapalkkakerroin", assertLomaPalkkaKerroin, testLomaPalkkaKerroin);
		
		/* PAM Kaupan alan TES: §21
		 * Lomaraha on 50 % vuosilomalain mukaan ansaittua lomaa vastaavasta lomapalkasta.
		 * 
		 * Lomaraha maksetaan työntekijän:
		 *  -aloittaessa loman ilmoitettuna tai sovittuna aikana ja
		 *  -palatessa työhön heti loman päätyttyä.
		 *  
		 *  -----
		 *  Testidata ei ota kantaa lomien aloitusaikaan, joten voimme päätellä vain lomarahan suuruuden, 50% lomapäivistä saadusta lomapalkasta.
		 *  
		 *  Expected: 
		 *  Lomarahaoikeus - true
		 *  Lomaraha - 2046.9418 / 2 (1023.4709)
		 */
		
		
		assertEquals("Lomarahaoikeus", true, calculator.oikeusLomaRahaan());
	
		BigDecimal assertLomaRaha = new BigDecimal("1023.457");
		BigDecimal testLomaRaha = calculator.getLomaRaha().round(new MathContext(7, RoundingMode.DOWN));
		assertEquals("Lomaraha", assertLomaRaha, testLomaRaha);
		
		BigDecimal assertPalkka = new BigDecimal(14358);
		BigDecimal testPalkka = calculator.getPalkkaYhteensa();
		assertEquals("Palkka", assertPalkka, testPalkka);
		
		BigDecimal assertLomaPalkka = new BigDecimal("2046.914");
		BigDecimal testLomaPalkka = calculator.getLomaPalkka().round(new MathContext(7, RoundingMode.DOWN));
		assertEquals("Lomapalkka", assertLomaPalkka, testLomaPalkka);	
	}
	
	public void testBasicVacationPay() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), new BigDecimal(10));
		record.getWorkHourChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(35)));
		EmploymentList list = record.getEmploymentList();
		
		list.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 6), "", new BigDecimal(8), new BigDecimal(100)));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);
		
		assertEquals("Lomapäivät", 2, calculator.getLomaPaivat());
		assertEquals("Lomapalkkakaava", VacationPayCalculator.LomaPalkkaKaava.TUNTIPALKKAISET_VUOSILOMALAKI, calculator.getLomaPalkkaLaskuTapa());
		
		BigDecimal assertPalkka = new BigDecimal(500);
		BigDecimal testPalkka = calculator.getPalkkaYhteensa();
		assertEquals("Palkka", assertPalkka, testPalkka);
		
		BigDecimal assertPalkkaKeskiarvo = new BigDecimal(100);
		BigDecimal testPalkkaKeskiarvo = calculator.getPaivaPalkkaKeskiarvo();
		assertEquals("Päiväpalkan keskiarvo", assertPalkkaKeskiarvo, testPalkkaKeskiarvo);

		BigDecimal assertLomaPalkka = new BigDecimal(180);
		BigDecimal testLomaPalkka = calculator.getLomaPalkka();
		assertEquals("Lomapalkka", assertLomaPalkka, testLomaPalkka);
	}

	public void testNotEnoughHours() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), new BigDecimal(10));
		record.getWorkHourChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(35)));
		
		EmploymentList list = record.getEmploymentList();
		
		list.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", new BigDecimal(8), BigDecimal.ZERO));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);
		
		BigDecimal assertLomaPalkka = new BigDecimal(32);
		BigDecimal testLomaPalkka = calculator.getLomaPalkka();
		assertEquals("LomaPalkka", assertLomaPalkka, testLomaPalkka);
		
	}
	
	public void testNotEnoughWorkDays() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), new BigDecimal(10));
		record.getWorkDayChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(3)));
		
		EmploymentList list = record.getEmploymentList();
		
		list.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 6), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 7), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 8), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 9), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 10), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 11), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 12), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 13), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 14), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 15), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 16), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 17), "", new BigDecimal(8), BigDecimal.ZERO));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);
		
		assertEquals("Lomapalkkakaava", VacationPayCalculator.LomaPalkkaKaava.TUNTIPALKKAISET_VUOSILOMALAKI, calculator.getLomaPalkkaLaskuTapa());
		
		BigDecimal assertLomaPalkka = new BigDecimal(144);
		BigDecimal testLomaPalkka = calculator.getLomaPalkka();
		assertEquals("LomaPalkka", assertLomaPalkka, testLomaPalkka);
		
	}
	
	public void testNotEnoughActualDays() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), new BigDecimal(10));
		record.getWorkDayChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(4)));
		
		EmploymentList list = record.getEmploymentList();
		
		list.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 6), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 7), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 8), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 9), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 10), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 11), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 12), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 13), "", new BigDecimal(8), BigDecimal.ZERO));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);
		
		BigDecimal assertLomaPalkka = new BigDecimal(96);
		BigDecimal testLomaPalkka = calculator.getLomaPalkka();
		assertEquals("LomaPalkka", assertLomaPalkka, testLomaPalkka);
		
	}
	
	public void testWorkDaysWithPaidLeave() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), new BigDecimal(10));
		record.getWorkDayChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(4)));
		
		EmploymentList list = record.getEmploymentList();
		
		list.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 6), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 7), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 8), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 9), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 10), "Paid leave", BigDecimal.ZERO, BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 11), "Paid leave", BigDecimal.ZERO, BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 12), "Paid leave", BigDecimal.ZERO, BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 13), "Paid leave", BigDecimal.ZERO, BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 14), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 15), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 16), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 17), "", new BigDecimal(8), BigDecimal.ZERO));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);

		assertEquals("Lomapalkkakaava", VacationPayCalculator.LomaPalkkaKaava.TUNTIPALKKAISET_VUOSILOMALAKI, calculator.getLomaPalkkaLaskuTapa());
		
		BigDecimal assertLomaPalkka = new BigDecimal(144);
		BigDecimal testLomaPalkka = calculator.getLomaPalkka();
		assertEquals("LomaPalkka", assertLomaPalkka, testLomaPalkka);
		
	}
	
	public void testNonintegerVacationDays() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), new BigDecimal(10));
		record.getWorkHourChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(35)));
		
		EmploymentList list = record.getEmploymentList();
		
		list.add(new EmploymentData(LocalDate.of(2001, 1, 2), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2001, 1, 3), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2001, 1, 4), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2001, 1, 5), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2001, 1, 6), "", new BigDecimal(8), new BigDecimal(100)));	
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2001);

		assertEquals("Lomapäivät", 3, calculator.getLomaPaivat());
		
		BigDecimal assertLomaPalkka = new BigDecimal(270);
		BigDecimal testLomaPalkka = calculator.getLomaPalkka();
		assertEquals("LomaPalkka", assertLomaPalkka, testLomaPalkka);
	}

	public void testBasicSalariedVacationPay() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000,1,1), BigDecimal.ZERO);
		record.setSalariedStatus(true);
		record.getSalaryChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(1000)));
		record.getWorkDayChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(5)));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2001);
	
		assertEquals("Lomapäivien ansaintasääntö", VacationPayCalculator.LomaPaivienAnsaintaSaanto.PAIVAT, calculator.getLomaPaivaLaskuTapa());
		assertEquals("Lomapäivät", 30, calculator.getLomaPaivat());
		
		assertEquals("Lomapalkkakaava", VacationPayCalculator.LomaPalkkaKaava.KUUKAUSIPALKKAISET, calculator.getLomaPalkkaLaskuTapa());
		
		BigDecimal assertTyoPaivat = new BigDecimal(20);
		BigDecimal testTyoPaivat = calculator.getKuukausiTyoPaivat();
		assertEquals("Sopimuksen työpäivät kuukaudessa", assertTyoPaivat, testTyoPaivat);
		
		BigDecimal assertLomaPalkka = new BigDecimal(1500);
		BigDecimal testLomaPalkka = calculator.getLomaPalkka();
		assertEquals("LomaPalkka", assertLomaPalkka, testLomaPalkka);			
	}
	
	public void testSalariedNotEnoughDays() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000,1,1), BigDecimal.ZERO);
		record.setSalariedStatus(true);
		record.getSalaryChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(600)));
		record.getWorkDayChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(3)));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2001);
		
		assertEquals("Lomapalkkakaava", VacationPayCalculator.LomaPalkkaKaava.PROSENTTIPERUSTEINEN, calculator.getLomaPalkkaLaskuTapa());
		assertEquals("Lomapäivät", 0, calculator.getLomaPaivat());
		
		BigDecimal assertLomaPalkka = new BigDecimal(900);
		BigDecimal testLomaPalkka = calculator.getLomaPalkka();
		assertEquals("Lomapalkka", assertLomaPalkka, testLomaPalkka);
	}
}
