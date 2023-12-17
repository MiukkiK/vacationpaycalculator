package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import junit.framework.TestCase;

/** 
 * Test class for the vacation pay calculator. Checks for outputs with given test cases A and B.
 * Also tests basic vacation pay calculations for salaried and general categories and percentile calculations.
 * @author Mia Kallio
 */
public class VacationPayCalculatorTest extends TestCase {
		
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
		 * §12
		 * Muun kuin viikko- tai kuukausipalkalla alle 14 päivänä kalenterikuukaudessa työtä tekevän
		 * työntekijän vuosilomapalkka on 9 prosenttia taikka työsuhteen jatkuttua lomakautta edeltävän
		 * lomanmääräytymisvuoden loppuun mennessä vähintään vuoden 11,5 prosenttia lomanmääräytymisvuoden
		 * aikana työssäolon ajalta maksetusta tai maksettavaksi erääntyneestä palkasta lukuun ottamatta 
		 * hätätyöstä ja lain tai sopimuksen mukaisesta ylityöstä maksettavaa korotusta.
		 *
		 * Jos työsopimuksessa ei ole sovittu työtunteja tai -päiviä, ei kerry lomapäiviä.
		 * Ilman sovittuja työtunteja ja -päiviä ei kerry lomapäiviä, ja lomapalkka kertyy pelkästään prosenttiperusteisesti.
		 * 
		 * Expected: null
		 */
		assertEquals("Lomapäivälaskutapa should be null", null, calculator.getLomaPaivaLaskuTapa());
		assertEquals("Lomapalkkalaskutapa should be null", null, calculator.getLomaPalkkaLaskuTapa());
		assertEquals("Lomanmääräytymiskuukaudet should be zero", 0, calculator.getLomanMaaraytymisKuukaudet());
		assertEquals("Lomapäivät should be zero", 0, calculator.getLomaPaivat());
		
		/*
		 * Testisyötteessä on 195 työpäivää. Niihin on laskettu päivät joilla on työtunteja, mutta ei merkintöjä "arkipyhäkorvauksista"
		 * 
		 * Expected: 195
		 */
		
		assertEquals("Incorrect amount of work days", 0, calculator.getTyoPaivatYhteensa().compareTo(new BigDecimal(195)));
		
		/*
		 * Vuosilomalaki 18.3.2005/162: §12
		 * Muun kuin viikko- tai kuukausipalkalla alle 14 päivänä kalenterikuukaudessa työtä tekevän työntekijän vuosilomapalkka
		 * on 9 prosenttia taikka työsuhteen jatkuttua lomakautta edeltävän lomanmääräytymisvuoden loppuun mennessä vähintään vuoden
		 * 11,5 prosenttia lomanmääräytymisvuoden aikana työssäolon ajalta maksetusta tai maksettavaksi erääntyneestä palkasta lukuun
		 * ottamatta hätätyöstä ja lain tai sopimuksen mukaisesta ylityöstä maksettavaa korotusta. 
		 * 
		 * Jos työntekijä on lomanmääräytymisvuoden aikana ollut estynyt tekemästä työtä 7 §:n 2 momentin 1–4 tai 
		 * 7 kohdassa tarkoitetusta syystä, vuosilomapalkan perusteena olevaan palkkaan lisätään laskennallisesti poissaoloajalta 
		 * saamatta jäänyt palkka enintään 7 §:n 3 momentissa säädetyltä ajalta.
		 * 
		 * Testitapauksessa on 36 poissaolopäivää, joilta maksetaan keskipäiväpalkan verran prosentuaalista korvausta.
		 * Expected: 72.631 * 36 (2650.72(716)
		 */
		assertEquals("Total missed pay does not match", 0, calculator.getSaamattaJaanytPalkka().compareTo(calculator.getPaivaPalkkaKeskiarvo().multiply(calculator.getLomaPaivatYhteensa())));
		
		/*
		 * PAM Kaupan alan TES: §20 8.
		 * Alle 37,5 tuntia tekevät
		 * Lomapalkka tai -korvaus on sekä tuntipalkkaisella että suhteutettua kuukausipalkkaa saavalla jäljempänä esitetystä lomanmääräytymisvuoden ansiosta:
		 * 10 % työsuhteen kestettyä lomanmääräytymisvuoden loppuun (31.3.) mennessä alle vuoden
		 * 12,5 % työsuhteen kestettyä lomanmääräytymisvuoden loppuun (31.3.) mennessä vähintään vuoden.
		 * 
		 * Testitapauksessa työntekijä on ollut kirjoilla vuodesta 2008. Isompaan prosenttiin riittää 1.4.2009 lähtien kirjoilla oleminen.
		 * Expected: 0.125 (12.5%)
		 */
		assertEquals("Incorrect percentile multiplier", 0, calculator.getKorvausProsentti().compareTo(new BigDecimal("12.5")));
		
		/* PAM Kaupan alan TES: §21
		 * Lomaraha on 50 % vuosilomalain mukaan ansaittua lomaa vastaavasta lomapalkasta.
		 * 
		 * Lomaraha maksetaan työntekijän:
		 *  -aloittaessa loman ilmoitettuna tai sovittuna aikana ja
		 *  -palatessa työhön heti loman päätyttyä.
		 *  
		 *  Jos työntekijällä ei ole lomapäiviä, ei työntekijällä ole oikeutta lomarahaan.
		 *  
		 */
		assertEquals("Incorrect state for vacation bonus eligibility", false, calculator.oikeusLomaRahaan());
		
		assertEquals("Total percentile pay does not match", 0, calculator.getLomaKorvausYhteensa().compareTo(new BigDecimal(14358)));
		assertEquals("Incorrect amount of leave days", 0, calculator.getLomaPaivatYhteensa().compareTo(new BigDecimal(36)));		
	}
	
	public void testCaseB() {
		EmployeeRecord testCase = new EmployeeRecord(LocalDate.of(2008, 6, 1), new BigDecimal(10));
		FileHandler.inputData("src/test/resources/raw_hours.txt", testCase.getEmploymentList());
		testCase.getWorkHourChanges().add(new ChangeData(testCase.getStartDate(), new BigDecimal(37.5)));
		testCase.getEmploymentList().setWageFrom(LocalDate.of(2009, 10, 15), new BigDecimal(11));
		
		VacationPayCalculator calculator = new VacationPayCalculator(testCase, 2010);
		System.out.println(calculator);
		
		/*
		 * Vuosilomalaki 18.3.2005/162: §6
		 * Jos työntekijä on sopimuksen mukaisesti työssä niin harvoina päivinä,
		 * että hänelle ei tästä syystä kerry ainoatakaan 14 työssäolopäivää sisältävää
		 * kalenterikuukautta tai vain osa kalenterikuukausista sisältää 14 työssäolopäivää,
		 * täydeksi lomanmääräytymiskuukaudeksi katsotaan sellainen kalenterikuukausi, jonka
		 * aikana työntekijälle on kertynyt vähintään 35 työtuntia tai 7 §:ssä tarkoitettua 
		 * työssäolon veroista tuntia.
		 *
		 * Jos työsopimuksessa ei ole sovittu päiviä ja työtunteja on sovittu vähintään 35 tuntia kuukaudessa lomapäivät lasketaan 35 tunnin säännön perusteella.
		 * 
		 * Expected: VacationDayMethod.HOURS
		 */
		assertEquals("Incorrect vacation day calculation method", VacationPayCalculator.VacationDayMethod.TUNNIT, calculator.getLomaPaivaLaskuTapa());

		/*
		 * Vuosilomalaki 18.3.2005/162: §11
		 * Muun kuin viikko- tai kuukausipalkalla työskentelevän sellaisen työntekijän vuosilomapalkka,
		 * joka sopimuksen mukaan työskentelee vähintään 14 päivänä kalenterikuukaudessa, lasketaan
		 * kertomalla hänen keskipäiväpalkkansa lomapäivien määrän perusteella määräytyvällä kertoimella.
		 * 
		 * Jos työntekijä ei ole kuukausipalkkainen ja hänellä on lomapäiviä, lomapalkka lasketaan keskipäiväpalkan pohjalta.
		 * 
		 * Exptected: Category.DAILY_PAY
		 */		
		assertEquals("Incorrect category", VacationPayCalculator.Category.PAIVAKOHTAINEN, calculator.getLomaPalkkaLaskuTapa());

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
		 * Testisyötteessä on kaksi kuukautta jossa ei ole 35 työtuntia, 7/2009 ja 12/2009. Niissä on kuitenkin työssäolon veroisia päiviä korvaamaan vaaditut työtunnit.
		 * 
		 * Expected: 12
		 */
		assertEquals("Incorrect amount of vacation applicable months", 12, calculator.getLomanMaaraytymisKuukaudet());
		
		/* 
		 * Vuosilomalaki 18.3.2005/162: §5
		 * Työntekijällä on oikeus saada lomaa kaksi ja puoli arkipäivää kultakin täydeltä lomanmääräytymiskuukaudelta. 
		 * Jos työsuhde on lomanmääräytymisvuoden loppuun mennessä jatkunut yhdenjaksoisesti alle vuoden, 
		 * työntekijällä on kuitenkin oikeus saada lomaa kaksi arkipäivää kultakin täydeltä lomanmääräytymiskuukaudelta. 
		 * 
		 * Työntekijä on ollut työsuhteessa vuodesta 2008, ja isompi kerroin täyttyy jo maaliskuussa 2009.
		 * 
		 * Expected: 2.5
		 */
		assertEquals("Incorrect vacation day modifier", new BigDecimal("2.5"), calculator.getLomaPaivatPerMaaraytymisKuukausi());
		assertEquals("Incorrect amount of vacation days", 30, calculator.getLomaPaivat());
		
		/*
		 * Testisyötteessä on 195 työpäivää. Niihin on laskettu päivät joilla on työtunteja, mutta ei merkintöjä "arkipyhäkorvauksista"
		 * 
		 * Expected: 195
		 */		
		assertEquals("Incorrect amount of work days", 0, calculator.getTyoPaivatYhteensa().compareTo(new BigDecimal(195)));
		
		/*
		 * Vuosilomalaki 18.3.2005/162: 
		 * Jos työntekijän viikoittaisten työpäivien määrä on sopimuksen mukaan pienempi tai suurempi kuin viisi,
		 * keskipäiväpalkka kerrotaan viikoittaisten työpäivien määrällä ja jaetaan viidellä.
		 * 
		 * Testitapauksessa ei ole määritetty työpäiviä joten tämä kohta ei koske testitapausta.
		 */
		
		/*
		 * Keskimääräinen päiväpalkka saadaan jakamalla ansaittu tulo työpäivien määrällä.
		 * Expected: 14358 / 195 (~73.63(631))
		 */
		assertEquals("Average daily pay does not match", 0, calculator.getPaivaPalkkaKeskiarvo().compareTo(new BigDecimal(14358).divide(new BigDecimal(195), 3, RoundingMode.HALF_UP)));

		/*
		 * Vuosilomalaki 18.3.2005/162: §11
		 * ...
		 * Jos työntekijän viikoittaisten työpäivien määrä on sopimuksen mukaan pienempi tai suurempi kuin viisi,
		 * keskipäiväpalkka kerrotaan viikoittaisten työpäivien määrällä ja jaetaan viidellä.
		 * 
		 * Testitapauksessa ei ole määritetty työpäiviä joten tämä kohta ei koske testitapausta.
		 */
		
		/*
		 * Vuosilomalaki 18.3.2005/162: §11
		 * Muun kuin viikko- tai kuukausipalkalla työskentelevän sellaisen työntekijän vuosilomapalkka,
		 * joka sopimuksen mukaan työskentelee vähintään 14 päivänä kalenterikuukaudessa, lasketaan kertomalla 
		 * hänen keskipäiväpalkkansa lomapäivien määrän perusteella määräytyvällä kertoimella.
		 * 
		 * Kerroin on määritelty vuosilomalaissa, se on epälineaarinen ~0.9 per päivä asteikko. 30 päivää vastaava luku on 27.8
		 * Expected: 27.8
		 */
		assertEquals("Incorrect vacation pay multiplier", 0, calculator.getLomaPalkkaKerroin().compareTo(new BigDecimal("27.8")));
		
		/*
		 * Vuosilomalaki 18.3.2005/162: §12
		 * Muun kuin viikko- tai kuukausipalkalla alle 14 päivänä kalenterikuukaudessa työtä tekevän työntekijän vuosilomapalkka
		 * on 9 prosenttia taikka työsuhteen jatkuttua lomakautta edeltävän lomanmääräytymisvuoden loppuun mennessä vähintään vuoden
		 * 11,5 prosenttia lomanmääräytymisvuoden aikana työssäolon ajalta maksetusta tai maksettavaksi erääntyneestä palkasta lukuun
		 * ottamatta hätätyöstä ja lain tai sopimuksen mukaisesta ylityöstä maksettavaa korotusta. 
		 * 
		 * Jos työntekijä on lomanmääräytymisvuoden aikana ollut estynyt tekemästä työtä 7 §:n 2 momentin 1–4 tai 
		 * 7 kohdassa tarkoitetusta syystä, vuosilomapalkan perusteena olevaan palkkaan lisätään laskennallisesti poissaoloajalta 
		 * saamatta jäänyt palkka enintään 7 §:n 3 momentissa säädetyltä ajalta.
		 * 
		 * Testitapauksessa on 36 poissaolopäivää, joilta maksetaan keskipäiväpalkan verran prosentuaalista korvausta.
		 * Expected: 72.631 * 36 (2650.72(716)
		 */
		assertEquals("Total missed pay does not match", 0, calculator.getSaamattaJaanytPalkka().compareTo(calculator.getPaivaPalkkaKeskiarvo().multiply(calculator.getLomaPaivatYhteensa())));
		
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
		 * Testitapauksessa työntekijä on ollut kirjoilla vuodesta 2008. Isompaan prosenttiin riittää 1.4.2009 lähtien kirjoilla oleminen.
		 * Tässä tapauksessa työssäolon veroisista poissaoloista maksetaan vastaava palkka ja se vaikuttaa lomapalkkaan prosenttikertoimella.
		 * Expected: 0.125 (12.5%)
		 */
		assertEquals("Incorrect percentile multiplier", 0, calculator.getKorvausProsentti().compareTo(new BigDecimal("12.5")));
		
		/* PAM Kaupan alan TES: §21
		 * Lomaraha on 50 % vuosilomalain mukaan ansaittua lomaa vastaavasta lomapalkasta.
		 * 
		 * Lomaraha maksetaan työntekijän:
		 *  -aloittaessa loman ilmoitettuna tai sovittuna aikana ja
		 *  -palatessa työhön heti loman päätyttyä.
		 *  
		 *  Testidata ei ota kantaa lomien aloitusaikaan, joten voimme päätellä vain lomarahan suuruuden, 50% lomapäivistä saadusta lomapalkasta.
		 *  Expected: 2046.9418) / 2 (1023.4709)
		 */
		System.out.println(calculator.getLomaPalkka());
		assertEquals("Incorrect state for vacation bonus eligibility", true, calculator.oikeusLomaRahaan());
		assertEquals("Vacation bonus does not match", 0, calculator.getLomaRaha().compareTo(new BigDecimal("2046.9418").divide(new BigDecimal(2))));
		
		assertEquals("Total pay does not match", 0, calculator.getPalkkaYhteensa().compareTo(new BigDecimal(14358)));
		assertEquals("Total percentile pay does not match", 0, calculator.getLomaKorvausYhteensa().compareTo(new BigDecimal(0)));
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

		assertEquals("Total pay does not match", 0, calculator.getPalkkaYhteensa().compareTo(new BigDecimal(500)));
		assertEquals("Incorrect amount of vacation days", 2, calculator.getLomaPaivat());
		assertEquals("Incorrect category", VacationPayCalculator.Category.PAIVAKOHTAINEN, calculator.getLomaPalkkaLaskuTapa());
		assertEquals("Average daily pay does not match", 0, calculator.getPaivaPalkkaKeskiarvo().compareTo(new BigDecimal(100)));
		assertEquals("Vacation pay does not match", 0, calculator.getLomaPalkka().compareTo(new BigDecimal(180)));
	}

	public void testNotEnoughWorkHours() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), new BigDecimal(10));
		record.getWorkHourChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(8)));
		
		EmploymentList list = record.getEmploymentList();
		
		list.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 6), "", new BigDecimal(8), new BigDecimal(100)));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);
		
		assertEquals("Incorrect category", null, calculator.getLomaPalkkaLaskuTapa());
		assertEquals("Vacation pay in incorrect category (daily pay)", 0, calculator.getLomaPalkka().compareTo(BigDecimal.ZERO));
		assertEquals("Percentile pay does not match", 0, calculator.getLomaKorvaus().compareTo(new BigDecimal(50)));
	}

	public void testNotEnoughActualHours() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000, 1, 1), new BigDecimal(10));
		record.getWorkHourChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(35)));
		
		EmploymentList list = record.getEmploymentList();
		
		list.add(new EmploymentData(LocalDate.of(2000, 1, 2), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 3), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 4), "", new BigDecimal(8), BigDecimal.ZERO));
		list.add(new EmploymentData(LocalDate.of(2000, 1, 5), "", new BigDecimal(8), BigDecimal.ZERO));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2000);
		
		assertEquals("Vacation pay in incorrect category (daily pay)", 0, calculator.getLomaPalkka().compareTo(BigDecimal.ZERO));
		assertEquals("Percentile pay does not match", 0, calculator.getLomaKorvaus().compareTo(new BigDecimal(32)));
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
		assertEquals("Category does not match", null, calculator.getLomaPalkkaLaskuTapa());
		
		assertEquals("Vacation pay in incorrect category", 0, calculator.getLomaPalkka().compareTo(BigDecimal.ZERO));
		assertEquals("Percentile pay does not match", 0, calculator.getLomaKorvaus().compareTo(new BigDecimal(128)));
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
		
		assertEquals("Vacation pay in incorrect category", 0, calculator.getLomaPalkka().compareTo(BigDecimal.ZERO));
		assertEquals("Percentile pay does not match", 0, calculator.getLomaKorvaus().compareTo(new BigDecimal(96)));
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

		assertEquals("Incorrect category", VacationPayCalculator.Category.PAIVAKOHTAINEN, calculator.getLomaPalkkaLaskuTapa());
		assertEquals("Vacation pay does not match", 0, calculator.getLomaPalkka().compareTo(new BigDecimal("144")));
		assertEquals("Percentile pay does not match", 0, calculator.getLomaKorvaus().compareTo(new BigDecimal(32)));
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

		assertEquals("Incorrect amount of vacation days", 3, calculator.getLomaPaivat());
		assertEquals("Vacation pay does not match", 0, calculator.getLomaPalkka().compareTo(new BigDecimal(270)));
	}

	public void testBasicSalariedVacationPay() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000,1,1), BigDecimal.ZERO);
		record.setSalariedStatus(true);
		record.getSalaryChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(1000)));
		record.getWorkDayChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(5)));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2001);
		
		assertEquals("Incorrect amount of weekly workdays", 0, calculator.getKuukausiTyoPaivat().compareTo(new BigDecimal(5).multiply(new BigDecimal(4))));
		assertEquals("Incorrect amount of vacation days", 30, calculator.getLomaPaivat());
		assertEquals("Incorrect category", VacationPayCalculator.Category.KUUKAUSIPALKALLINEN, calculator.getLomaPalkkaLaskuTapa());
		assertEquals("Vacation pay does not match", 0, calculator.getLomaPalkka().compareTo(new BigDecimal(1500)));
		assertEquals("Vacation pay in incorrect category (percentile)", 0, calculator.getLomaKorvaus().compareTo(BigDecimal.ZERO));
	}
	
	public void testSalariedNotEnoughDays() {
		EmployeeRecord record = new EmployeeRecord(LocalDate.of(2000,1,1), BigDecimal.ZERO);
		record.setSalariedStatus(true);
		record.getSalaryChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(600)));
		record.getWorkDayChanges().add(new ChangeData(record.getStartDate(), new BigDecimal(3)));
		
		VacationPayCalculator calculator = new VacationPayCalculator(record, 2001);
		
		assertEquals("Incorrect category", null, calculator.getLomaPalkkaLaskuTapa());
		assertEquals("Incorrect amount of vacation days", 0, calculator.getLomaPaivat());
		assertEquals("Vacation pay in incorrect category (salaried)", null, calculator.getLomaPalkka());
		assertEquals("Vacation pay does not match", 0, calculator.getLomaKorvaus().compareTo(new BigDecimal(900)));
	}
}
