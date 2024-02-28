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
public class TestVacationPayCalculator extends TestCase {
	
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
		 * PAM Kaupan alan TES: §2
		 * ...
		 * Täysi lomanmääräytymiskuukausi on kalenterikuukausi, jonka aikana työntekijä on työskennellyt:
		 * -vähintään 14 päivää
		 * -vähintään 35 tuntia.
		 * Lomaa ansaitaan joko 14 päivän tai 35 tunnin säännön perusteella.
		 * Lomaa ansaitaan 35 tunnin säännön perusteella työntekijän työskennellessä työsopimuksen mukaan alle 14 päivää kuukaudessa.
		 * 
		 * -----
		 * Jos työsopimuksessa ei ole sovittu vähintään 14 päivää kuukaudessa, lomapäivät lasketaan 35 tunnin säännön perusteella.
		 * 
		 * Expected: TUNNIT
		 */		
		assertEquals("Lomapäivälaskutapa", LomaPaivienAnsaintaSaanto.TUNNIT, calculator.getLomaPaiivienAnsaintaSaanto());
		
		/* 
		 * Vuosilomalaki 15.3.2019/346 §7a 
		 *
		 * Työntekijällä on oikeus vuosilomaa täydentäviin lisävapaapäiviin, jos hänen täydeltä
		 * lomanmääräytymisvuodelta ansaitsemansa vuosiloma alittaa 24 päivää 7 §:n 2 momentin 2 tai 3 
		 * kohdassa tarkoitetun poissaolon vuoksi. 
		 * ...
		 * 
		 * -----
		 * Työntekijällä on työssäolon veroisiksi laskettavia poissaoloja. koska sopimuksessa
		 * ei ole määritetty tunteja, poissaolot eivät kerrytä työtunteja.
		 * 
		 * Jos lomapäiviä olisi alle 24, työntekijällä olisi oikeus lisäpäiviin poissaolojen pohjalta.
		 * Tällöin ne laskettaisiin keskimääräisten työtuntien perusteella.
		 * 
		 * Expected: 10
		 */		
		assertEquals("Lomanmääräytymiskuukaudet", 10, calculator.getLomanMaaraytymisKuukaudet());
		
		/* 
		 * PAM Kaupan alan TES: §2
		 * Lomaa ansaitaan täydeltä lomanmääräytymiskuukaudelta työsuhteen kestettyä lomanmääräytymisvuoden (1.4.–31.3.) loppuun mennessä:
		 * -alle vuoden: 2 arkipäivää
		 * -vähintään vuoden: 2,5 arkipäivää.
		 * ...
		 * 
		 * -----
		 * Työntekijä on ollut työsuhteessa vuodesta 2008, ja isompi kerroin täyttyy maaliskuussa 2009.
		 * 
		 * Expected:
		 * Lomapäiväkerroin - 2.5
		 * Lomapäivät - 25
		 */				
		assertEquals("Lomapäivät per määräytymiskuukausi", new BigDecimal("2.5"), calculator.getLomaPaivatPerMaaraytymisKuukausi());	
		
		assertEquals("Lomapäivät", 25, calculator.getLomaPaivat());
		
		/*
		 * Testisyötteessä on 195 työpäivää. Niihin on laskettu päivät joilla on työtunteja, mutta ei merkintöjä "arkipyhäkorvauksista"
		 * Esimerkkisyötteessä ei ole yli- tai hätätyötunteja, joten ne eivät koske lopputulosta.
		 * 
		 * Expected: 195
		 */
		assertEquals("Työpäivät", new BigDecimal(195), calculator.getTyoPaivatYhteensa());
		
		/*
		 * Testisyötteessä on 36 lomapäivää. Lomapäiviksi Niihin laskettiin päivät joissa ei ole työtunteja ja merkinnöissä on 
		 * kirjoitettu joku kuvaus vapaapäivän luonteesta (esim. "vanhempainvapaa").
		 */
		assertEquals("Poissaolopäivät", new BigDecimal(36), calculator.getPoissaOlotYhteensa());
		
		/*
		 * Vuosilomalaki 18.3.2005/162: §11
		 * Muun kuin viikko- tai kuukausipalkalla työskentelevän sellaisen työntekijän vuosilomapalkka,
		 * joka sopimuksen mukaan työskentelee vähintään 14 päivänä kalenterikuukaudessa, lasketaan
		 * kertomalla hänen keskipäiväpalkkansa lomapäivien määrän perusteella määräytyvällä kertoimella.
		 * 
		 * -----
		 * Jos työntekijä ei ole kuukausipalkkainen ja hänellä on lomapäiviä, lomapalkka lasketaan keskipäiväpalkan pohjalta.
		 * 
		 * Expected: TUNTIPALKKAISET_VUOSILOMALAKI
		 */		
		assertEquals("Lomapalkkalaskutapa", LomaPalkkaKaava.TUNTIPALKKAISET_VUOSILOMALAKI, calculator.getLomaPalkkaKaava());

		/*
		 * Keskimääräinen päiväpalkka saadaan jakamalla ansaittu tulo työpäivien määrällä.
		 * 
		 * Expected: 14358 / 195 (~73.630)
		 */
		assertEquals("Palkka", new BigDecimal(14358), calculator.getPalkkaYhteensa());
		
		assertEquals("Päiväpalkkakeskiarvo", new BigDecimal("73.630"), calculator.getPaivaPalkkaKeskiarvo());
		
		/*
		 * Vuosilomalaki 18.3.2005/162: §11
		 * Muun kuin viikko- tai kuukausipalkalla työskentelevän sellaisen työntekijän vuosilomapalkka,
		 * joka sopimuksen mukaan työskentelee vähintään 14 päivänä kalenterikuukaudessa, lasketaan kertomalla 
		 * hänen keskipäiväpalkkansa lomapäivien määrän perusteella määräytyvällä kertoimella.
		 * 
		 * -----
		 * Kerroin on määritelty vuosilomalaissa, se on epälineaarinen ~0.9 per päivä asteikko. 25 päivää vastaava luku on 23.2
		 * 
		 * Expected: 23.2
		 */
		assertEquals("Lomapalkkakerroin", new BigDecimal("23.2"), calculator.getLomaPalkkaKerroin());
		
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
		 *  Lomaraha - 1708.218 / 2 (854.108)
		 */
		
		assertEquals("Lomarahaoikeus", true, calculator.oikeusLomaRahaan());		
		
		assertEquals("Lomapalkka", new BigDecimal("1708.216"), calculator.getLomaPalkka().round(new MathContext(7, RoundingMode.DOWN)));	
		
		assertEquals("Lomaraha", new BigDecimal("854.108"), calculator.getLomaRaha().round(new MathContext(6, RoundingMode.DOWN)));
	}
	
	public void testCaseB() {
		EmployeeRecord testCase = new EmployeeRecord(LocalDate.of(2008, 6, 1), new BigDecimal(10));
		FileHandler.inputData("src/test/resources/raw_hours.txt", testCase.getEmploymentList());
		testCase.getWorkHourChanges().add(new ChangeData(testCase.getStartDate(), new BigDecimal(37.5)));
		testCase.getEmploymentList().setWageFrom(LocalDate.of(2009, 10, 15), new BigDecimal(11));
		
		VacationPayCalculator calculator = new VacationPayCalculator(testCase, 2010);
		
		// System.out.println(calculator);
		
		/*
		 * PAM Kaupan alan TES: §2
		 * ...
		 * Täysi lomanmääräytymiskuukausi on kalenterikuukausi, jonka aikana työntekijä on työskennellyt:
		 * -vähintään 14 päivää
		 * -vähintään 35 tuntia.
		 * Lomaa ansaitaan joko 14 päivän tai 35 tunnin säännön perusteella.
		 * Lomaa ansaitaan 35 tunnin säännön perusteella työntekijän työskennellessä työsopimuksen mukaan alle 14 päivää kuukaudessa.
		 * 
		 * -----
		 * Jos työsopimuksessa ei ole sovittu vähintään 14 päivää kuukaudessa, lomapäivät lasketaan 35 tunnin säännön perusteella.
		 * 
		 * Expected: TUNNIT
		 */		
		assertEquals("Lomapäivälaskutapa", LomaPaivienAnsaintaSaanto.TUNNIT, calculator.getLomaPaiivienAnsaintaSaanto());

		/*
		 * Testisyötteessä on kaksi kuukautta jossa ei ole 35 työtuntia, 7/2009 ja 12/2009. Niissä on kuitenkin työssäolon veroisia päiviä korvaamaan vaaditut työtunnit.
		 * 
		 * Expected: 12
		 */
		assertEquals("Lomanmääräytymiskuukaudet", 12, calculator.getLomanMaaraytymisKuukaudet());
		
		/* 
		 * PAM Kaupan alan TES: §2
		 * Lomaa ansaitaan täydeltä lomanmääräytymiskuukaudelta työsuhteen kestettyä lomanmääräytymisvuoden (1.4.–31.3.) loppuun mennessä:
		 * -alle vuoden: 2 arkipäivää
		 * -vähintään vuoden: 2,5 arkipäivää.
		 * ...
		 * 
		 * -----
		 * Työntekijä on ollut työsuhteessa vuodesta 2008, ja isompi kerroin täyttyy maaliskuussa 2009.
		 * 
		 * Expected:
		 * Lomapäiväkerroin - 2.5
		 * Lomapäivät - 30
		 */				
		assertEquals("Lomapäivät per määräytymiskuukausi", new BigDecimal("2.5"), calculator.getLomaPaivatPerMaaraytymisKuukausi());
		
		assertEquals("Lomapäivät", 30, calculator.getLomaPaivat());
		
		/*
		 * Testisyötteessä on 195 työpäivää. Niihin on laskettu päivät joilla on työtunteja, mutta ei merkintöjä "arkipyhäkorvauksista"
		 * Esimerkkisyötteessä ei ole yli- tai hätätyötunteja, joten ne eivät koske lopputulosta.
		 * 
		 * Expected: 195
		 */
		assertEquals("Työpäivät", new BigDecimal(195), calculator.getTyoPaivatYhteensa());
		
		/*
		 * Testisyötteessä on 36 lomapäivää. Lomapäiviksi Niihin laskettiin päivät joissa ei ole työtunteja ja merkinnöissä on 
		 * kirjoitettu joku kuvaus vapaapäivän luonteesta (esim. "vanhempainvapaa").
		 */
		assertEquals("Poissaolopäivät", new BigDecimal(36), calculator.getPoissaOlotYhteensa());
		
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
		assertEquals("Palkanlaskutapa", LomaPalkkaKaava.TUNTIPALKKAISET_VUOSILOMALAKI, calculator.getLomaPalkkaKaava());
		
		/*
		 * Keskimääräinen päiväpalkka saadaan jakamalla ansaittu tulo työpäivien määrällä.
		 * 
		 * Expected: 14358 / 195 (~73.630)
		 */
		assertEquals("Palkka", new BigDecimal(14358), calculator.getPalkkaYhteensa());
		
		assertEquals("Päiväpalkkakeskiarvo", new BigDecimal("73.630"), calculator.getPaivaPalkkaKeskiarvo());

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
		assertEquals("Lomapalkkakerroin", new BigDecimal("27.8"), calculator.getLomaPalkkaKerroin());
		
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
		
		assertEquals("Lomapalkka", new BigDecimal("2046.914"), calculator.getLomaPalkka().round(new MathContext(7, RoundingMode.DOWN)));	
		
		assertEquals("Lomaraha", new BigDecimal("1023.457"), calculator.getLomaRaha().round(new MathContext(7, RoundingMode.DOWN)));		
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

		assertEquals("Lomapalkkakaava", LomaPalkkaKaava.TUNTIPALKKAISET_VUOSILOMALAKI, calculator.getLomaPalkkaKaava());

		assertEquals("Palkka", new BigDecimal(500), calculator.getPalkkaYhteensa());

		assertEquals("Päiväpalkan keskiarvo", new BigDecimal(100), calculator.getPaivaPalkkaKeskiarvo());

		assertEquals("Lomapalkka", new BigDecimal(180), calculator.getLomaPalkka());
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

		assertEquals("Lomapalkan laskukaava", LomaPalkkaKaava.PROSENTTIPERUSTEINEN, calculator.getLomaPalkkaKaava());
		
		assertEquals("LomaPalkka", new BigDecimal(32), calculator.getLomaPalkka());
		
	}
	
	public void testNotEnoughPlannedWorkDays() {
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
		
		assertEquals("Lomapalkkakaava", LomaPalkkaKaava.TUNTIPALKKAISET_VUOSILOMALAKI, calculator.getLomaPalkkaKaava());
		
		assertEquals("LomaPalkka", new BigDecimal(144), calculator.getLomaPalkka());
		
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
		
		assertEquals("Määräytymiskuukaudet", 0, calculator.getLomanMaaraytymisKuukaudet());
		assertEquals("Lomapalkkakaava", LomaPalkkaKaava.PROSENTTIPERUSTEINEN, calculator.getLomaPalkkaKaava());

		assertEquals("LomaPalkka", new BigDecimal(96), calculator.getLomaPalkka());
		
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

		assertEquals("Lomapalkkakaava", LomaPalkkaKaava.TUNTIPALKKAISET_VUOSILOMALAKI, calculator.getLomaPalkkaKaava());
		
		assertEquals("LomaPalkka", new BigDecimal(144), calculator.getLomaPalkka());
		
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

		assertEquals("LomaPalkka", new BigDecimal(270), calculator.getLomaPalkka());
	}

	public void testBasicSalariedVacationPay() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000,1,1), BigDecimal.ZERO);
		record.setSalariedStatus(true);
		record.getSalaryChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(1000)));
		record.getWorkDayChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(5)));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2001);
	
		assertEquals("Lomapäivien ansaintasääntö", LomaPaivienAnsaintaSaanto.PAIVAT, calculator.getLomaPaiivienAnsaintaSaanto());
		assertEquals("Lomapäivät", 30, calculator.getLomaPaivat());
		
		assertEquals("Lomapalkkakaava", LomaPalkkaKaava.KUUKAUSIPALKKAISET, calculator.getLomaPalkkaKaava());

		assertEquals("Sopimuksen työpäivät kuukaudessa", new BigDecimal(20), calculator.getKuukausiTyoPaivat());
		
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
		
		assertEquals("Lomapalkkakaava", LomaPalkkaKaava.PROSENTTIPERUSTEINEN, calculator.getLomaPalkkaKaava());
		assertEquals("Lomapäivät", 0, calculator.getLomaPaivat());

		assertEquals("Lomapalkka", new BigDecimal(900), calculator.getLomaPalkka());
	}
}
