package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import junit.framework.TestCase;

/** 
 * Test class for the vacation pay calculator. Checks for outputs with given test cases A and B.
 * Also tests basic vacation pay calculations for salaried and general categories and percentile calculations.
 * @author Mia Kallio
 */
public class VacationPayCalculatorTest extends TestCase {
	
	public void bigDecimalAssert(String variableName, BigDecimal assertValue, BigDecimal testValue) {
		assertEquals(variableName + " should be " + assertValue + ", was " + testValue + ".", 0, testValue.compareTo(assertValue));
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
		assertEquals("Lomapäivälaskutapa", VacationPayCalculator.VacationDayMethod.TUNNIT, calculator.getLomaPaivaLaskuTapa());
		
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
		 * Työntekijällä on työssäolon veroisiksi laskettavia poissaoloja, mutta koska sopimuksessa
		 * ei ole määritetty tunteja, niitä ei huomioida. Ilman poissaoloja työntekijällä on 10
		 * lomapäivien kertymään oikeuttavaa kuukautta.
		 * 
		 * Expected: 10
		 */		
		assertEquals("Lomanmääräytymiskuukaudet", 10, calculator.getLomanMaaraytymisKuukaudet());
		
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
		 * Lomapäivät - 25
		 */				
		BigDecimal assertLomaPaivaKerroin = new BigDecimal("2.5");
		BigDecimal testLomaPaivaKerroin = calculator.getLomaPaivatPerMaaraytymisKuukausi();
		bigDecimalAssert("Lomapäivät per määräytymiskuukausi", assertLomaPaivaKerroin, testLomaPaivaKerroin);
		
		assertEquals("Lomapäivät", 25, calculator.getLomaPaivat());
		
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
		assertEquals("Lomapalkkalaskutapa", VacationPayCalculator.Category.PAIVAKOHTAINEN, calculator.getLomaPalkkaLaskuTapa());

		/*
		 * Testisyötteessä on 195 työpäivää. Niihin on laskettu päivät joilla on työtunteja, mutta ei merkintöjä "arkipyhäkorvauksista"
		 * Esimerkkisyötteessä ei ole yli- tai hätätyötunteja, joten ne eivät koske lopputulosta.
		 * 
		 * Expected: 195
		 */
		BigDecimal assertTyoPaivat = new BigDecimal(195);
		BigDecimal testTyoPaivat = calculator.getTyoPaivatYhteensa();
		bigDecimalAssert("Työpäivät", assertTyoPaivat, testTyoPaivat);
				
		BigDecimal assertPoissaOloPaivat = calculator.getPoissaOloPaivatYhteensa();
		BigDecimal testPoissaOloPaivat = new BigDecimal(36);
		bigDecimalAssert("Poissaolopäivät", assertPoissaOloPaivat, testPoissaOloPaivat);

		
		/*
		 * Vuosilomalaki 18.3.2005/162: §12
		 * ...
		 * 
		 * Jos työntekijä on lomanmääräytymisvuoden aikana ollut estynyt tekemästä työtä 7 §:n 2 momentin 1–4 tai 
		 * 7 kohdassa tarkoitetusta syystä, vuosilomapalkan perusteena olevaan palkkaan lisätään laskennallisesti poissaoloajalta 
		 * saamatta jäänyt palkka enintään 7 §:n 3 momentissa säädetyltä ajalta.
		 * 
		 * -----
		 * Testitapauksessa on 36 poissaolopäivää, joilta maksetaan keskipäiväpalkan verran prosentuaalista korvausta.
		 * 
		 * Expected: 72.631 * 36 (2650.72(716)
		 */			
		BigDecimal assertSaamattaJaanytPalkka = calculator.getPaivaPalkkaKeskiarvo().multiply(calculator.getPoissaOloPaivatYhteensa());
		BigDecimal testSaamattaJaanytPalkka = calculator.getSaamattaJaanytPalkka();
		bigDecimalAssert("Saamatta jäänyt palkka", assertSaamattaJaanytPalkka, testSaamattaJaanytPalkka);		

		/* PAM Kaupan alan TES, §20 8.
		 * Lomapalkka tai -korvaus on sekä tuntipalkkaisella että suhteutettua
		 * kuukausipalkkaa saavalla jäljempänä esitetystä lomanmääräytymisvuoden ansiosta:
		 * 10 % työsuhteen kestettyä lomanmääräytymisvuoden loppuun (31.3.) mennessä alle vuoden
		 * 12,5 % työsuhteen kestettyä lomanmääräytymisvuoden loppuun (31.3.) mennessä vähintään vuoden.
		 * 
		 * -----
		 * Työntekijä on ollut työsuhteessa yli 1 vuoden, joten kerroin on 12.5%.
		 * Lomakorvausta tulee kahdelta kuukaudelta joista ei kertynyt lomapäiviä.
		 * 
		 * Expected:
		 * Korvausprosentti - 12.5
		 * Lomakorvaus - 150
		 */
		BigDecimal assertKorvausProsentti = new BigDecimal("12.5");
		BigDecimal testKorvausProsentti = calculator.getKorvausProsentti();
		bigDecimalAssert("Korvausprosentti", assertKorvausProsentti, testKorvausProsentti);	
		
		BigDecimal assertLomaKorvaus = new BigDecimal(150);
		BigDecimal testLomaKorvaus = calculator.getLomaKorvausYhteensa();
		bigDecimalAssert("Lomakorvaus", assertLomaKorvaus, testLomaKorvaus);		
		
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
		 * Expected: VacationDayMethod.TUNNIT
		 */
		assertEquals("Lomapäivälaskutapa", VacationPayCalculator.VacationDayMethod.TUNNIT, calculator.getLomaPaivaLaskuTapa());

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
		assertEquals("Palkanlaskutapa", VacationPayCalculator.Category.PAIVAKOHTAINEN, calculator.getLomaPalkkaLaskuTapa());

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
		bigDecimalAssert("Lomapäivät per määräytymiskuukausi", assertLomaPaivaKerroin, testLomaPaivaKerroin);
		
		assertEquals("Lomapäivät", 30, calculator.getLomaPaivat());
		
		/*
		 * Testisyötteessä on 195 työpäivää. Niihin on laskettu päivät joilla on työtunteja, mutta ei merkintöjä "arkipyhäkorvauksista"
		 * Esimerkkisyötteessä ei ole yli- tai hätätyötunteja, joten ne eivät koske lopputulosta.
		 * 
		 * Expected: 195
		 */		
		BigDecimal assertTyoPaivat = new BigDecimal(195);
		BigDecimal testTyoPaivat = calculator.getTyoPaivatYhteensa();
		bigDecimalAssert("Työpäivät", assertTyoPaivat, testTyoPaivat);
		

		/*
		 * Keskimääräinen päiväpalkka saadaan jakamalla ansaittu tulo työpäivien määrällä.
		 * 
		 * Expected: 14358 / 195 (~73.63(631))
		 */
		BigDecimal assertPalkkaKeskiarvo = new BigDecimal(14358).divide(new BigDecimal(195), 3, RoundingMode.HALF_UP);
		BigDecimal testPalkkaKeskiarvo = calculator.getPaivaPalkkaKeskiarvo();
		bigDecimalAssert("Päiväpalkkakeskiarvo", assertPalkkaKeskiarvo, testPalkkaKeskiarvo);

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
		bigDecimalAssert("Lomapalkkakerroin", assertLomaPalkkaKerroin, testLomaPalkkaKerroin);
		
		/*
		 * Vuosilomalaki 18.3.2005/162: §12
		 * 
		 * ...
		 * Jos työntekijä on lomanmääräytymisvuoden aikana ollut estynyt tekemästä työtä 7 §:n 2 momentin 1–4 tai 
		 * 7 kohdassa tarkoitetusta syystä, vuosilomapalkan perusteena olevaan palkkaan lisätään laskennallisesti poissaoloajalta 
		 * saamatta jäänyt palkka enintään 7 §:n 3 momentissa säädetyltä ajalta.
		 * 
		 * -----
		 * Testitapauksessa on 36 poissaolopäivää, joilta maksetaan keskipäiväpalkan verran prosentuaalista korvausta.
		 * 
		 * Expected: 72.631 * 36 (2650.72(716)
		 */
		BigDecimal assertSaamattaJaanytPalkka = calculator.getPaivaPalkkaKeskiarvo().multiply(calculator.getPoissaOloPaivatYhteensa());
		BigDecimal testSaamattaJaanytPalkka = calculator.getSaamattaJaanytPalkka();
		bigDecimalAssert("Saamatta jäänyt palkka", assertSaamattaJaanytPalkka, testSaamattaJaanytPalkka);
		
		/*
		 * PAM Kaupan alan TES: §20 8.
		 * Alle 37,5 tuntia tekevät
		 * Lomapalkka tai -korvaus on sekä tuntipalkkaisella että suhteutettua kuukausipalkkaa saavalla jäljempänä esitetystä lomanmääräytymisvuoden ansiosta:
		 * 10 % työsuhteen kestettyä lomanmääräytymisvuoden loppuun (31.3.) mennessä alle vuoden
		 * 12,5 % työsuhteen kestettyä lomanmääräytymisvuoden loppuun (31.3.) mennessä vähintään vuoden.
		 * 
		 * Vuosilomalaki 18.3.2005/162: §16
		 * ...
		 * Jos työntekijä on ollut estynyt tekemästä työtä raskaus-, erityisraskaus- tai vanhempainvapaan vuoksi,
		 * lomakorvauksen perusteena olevaan palkkaan lisätään poissaolon ajalta saamatta jäänyt palkka noudattaen 12 §:n 2 momenttia.
		 * 
		 * ------
		 * Testitapauksessa työntekijä on ollut kirjoilla vuodesta 2008. Isompaan prosenttiin riittää 1.4.2009 lähtien kirjoilla oleminen.
		 * Tässä tapauksessa työssäolon veroisista poissaoloista maksetaan vastaava palkka ja se vaikuttaa lomapalkkaan prosenttikertoimella.
		 * 
		 * Expected: 12.5 (12.5%)
		 */
		
		BigDecimal assertKorvausProsentti = new BigDecimal("12.5");
		BigDecimal testKorvausProsentti = calculator.getKorvausProsentti();
		bigDecimalAssert("Korvausprosentti", assertKorvausProsentti, testKorvausProsentti);

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
		 *  Lomakorvaus (kuukausilta joista ei saatu lomapäiviä) - 0
		 *  Lomarahaoikeus - true
		 *  Lomaraha - 2046.9418) / 2 (1023.4709)
		 */

		assertEquals("Lomarahaoikeus", true, calculator.oikeusLomaRahaan());
	
		BigDecimal assertLomaKorvaus = BigDecimal.ZERO;
		BigDecimal testLomaKorvaus = calculator.getLomaKorvausYhteensa();
		bigDecimalAssert("Lomakorvaus", assertLomaKorvaus, testLomaKorvaus);
		
		BigDecimal assertLomaRaha = calculator.getLomaPalkka().divide(new BigDecimal(2));
		BigDecimal testLomaRaha = calculator.getLomaRaha();
		bigDecimalAssert("Lomaraha", assertLomaRaha, testLomaRaha);
		
		BigDecimal assertPalkka = new BigDecimal(14358);
		BigDecimal testPalkka = calculator.getPalkkaYhteensa();
		bigDecimalAssert("Palkka", assertPalkka, testPalkka);
		
		
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
		assertEquals("Lomapalkkalaskutapa", VacationPayCalculator.Category.PAIVAKOHTAINEN, calculator.getLomaPalkkaLaskuTapa());
		
		BigDecimal assertPalkka = new BigDecimal(500);
		BigDecimal testPalkka = calculator.getPalkkaYhteensa();
		bigDecimalAssert("Palkka", assertPalkka, testPalkka);
		
		BigDecimal assertPalkkaKeskiarvo = new BigDecimal(100);
		BigDecimal testPalkkaKeskiarvo = calculator.getPaivaPalkkaKeskiarvo();
		bigDecimalAssert("Päiväpalkkakeskiarvo", assertPalkkaKeskiarvo, testPalkkaKeskiarvo);

		BigDecimal assertLomaPalkka = new BigDecimal(180);
		BigDecimal testLomaPalkka = calculator.getLomaPalkka();
		bigDecimalAssert("Lomapalkka", assertLomaPalkka, testLomaPalkka);
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
		
		BigDecimal assertLomaPalkka = BigDecimal.ZERO;
		BigDecimal testLomaPalkka = calculator.getLomaPalkka();
		bigDecimalAssert("LomaPalkka", assertLomaPalkka, testLomaPalkka);
		
		BigDecimal assertLomaKorvaus = new BigDecimal(32);
		BigDecimal testLomaKorvaus = calculator.getLomaKorvaus();
		bigDecimalAssert("Lomakorvaus", assertLomaKorvaus, testLomaKorvaus);
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
		
		assertEquals("Lomapalkkalaskutapa", VacationPayCalculator.Category.PAIVAKOHTAINEN, calculator.getLomaPalkkaLaskuTapa());
		
		BigDecimal assertLomaPalkka = new BigDecimal(144);
		BigDecimal testLomaPalkka = calculator.getLomaPalkka();
		bigDecimalAssert("LomaPalkka", assertLomaPalkka, testLomaPalkka);
		
		BigDecimal assertLomaKorvaus = BigDecimal.ZERO;
		BigDecimal testLomaKorvaus = calculator.getLomaKorvaus();
		bigDecimalAssert("Lomakorvaus", assertLomaKorvaus, testLomaKorvaus);
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
		
		BigDecimal assertLomaPalkka = BigDecimal.ZERO;
		BigDecimal testLomaPalkka = calculator.getLomaPalkka();
		bigDecimalAssert("LomaPalkka", assertLomaPalkka, testLomaPalkka);
		
		BigDecimal assertLomaKorvaus = new BigDecimal(96);
		BigDecimal testLomaKorvaus = calculator.getLomaKorvaus();
		bigDecimalAssert("Lomakorvaus", assertLomaKorvaus, testLomaKorvaus);
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

		assertEquals("Lomapalkkalaskutapa", VacationPayCalculator.Category.PAIVAKOHTAINEN, calculator.getLomaPalkkaLaskuTapa());
		
		BigDecimal assertLomaPalkka = new BigDecimal(144);
		BigDecimal testLomaPalkka = calculator.getLomaPalkka();
		bigDecimalAssert("LomaPalkka", assertLomaPalkka, testLomaPalkka);
		
		BigDecimal assertLomaKorvaus = new BigDecimal(32);
		BigDecimal testLomaKorvaus = calculator.getLomaKorvaus();
		bigDecimalAssert("Lomakorvaus", assertLomaKorvaus, testLomaKorvaus);
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
		bigDecimalAssert("LomaPalkka", assertLomaPalkka, testLomaPalkka);
	}

	public void testBasicSalariedVacationPay() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000,1,1), BigDecimal.ZERO);
		record.setSalariedStatus(true);
		record.getSalaryChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(1000)));
		record.getWorkDayChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(5)));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2001);
		
		BigDecimal assertTyoPaivat = new BigDecimal(20);
		BigDecimal testTyoPaivat = calculator.getKuukausiTyoPaivat();
		bigDecimalAssert("Sopimuksen työpäivät kuukaudessa", assertTyoPaivat, testTyoPaivat);
		
		assertEquals("Lomapäivät", 30, calculator.getLomaPaivat());
		assertEquals("Lomapalkkalaskutapa", VacationPayCalculator.Category.KUUKAUSIPALKALLINEN, calculator.getLomaPalkkaLaskuTapa());
		
		BigDecimal assertLomaPalkka = new BigDecimal(1500);
		BigDecimal testLomaPalkka = calculator.getLomaPalkka();
		bigDecimalAssert("LomaPalkka", assertLomaPalkka, testLomaPalkka);
		
		BigDecimal assertLomaKorvaus = BigDecimal.ZERO;
		BigDecimal testLomaKorvaus = calculator.getLomaKorvaus();
		bigDecimalAssert("Lomakorvaus", assertLomaKorvaus, testLomaKorvaus);
	}
	
	public void testSalariedNotEnoughDays() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000,1,1), BigDecimal.ZERO);
		record.setSalariedStatus(true);
		record.getSalaryChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(600)));
		record.getWorkDayChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(3)));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2001);
		
		assertEquals("Lomapalkkalaskutapa", null, calculator.getLomaPalkkaLaskuTapa());
		assertEquals("Lomapäivät", 0, calculator.getLomaPaivat());
		assertEquals("Lomapalkka", null, calculator.getLomaPalkka());
		
		BigDecimal assertLomaKorvaus = new BigDecimal(900);
		BigDecimal testLomaKorvaus = calculator.getLomaKorvaus();
		bigDecimalAssert("Lomakorvaus", assertLomaKorvaus, testLomaKorvaus);
	}
}
