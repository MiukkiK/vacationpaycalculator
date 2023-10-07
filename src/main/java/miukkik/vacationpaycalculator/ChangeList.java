package miukkik.vacationpaycalculator;

import java.time.LocalDate;
import java.util.ArrayList;

public class ChangeList {

	private ArrayList<Change> changes;
	
	public ChangeList(double amount) {
		changes.add(new Change(amount));
	}
	
	public void addChange(double amount, LocalDate effectiveFrom) {
		changes.get(changes.size()-1).setEffectiveUntil(effectiveFrom.minusDays(1));
		changes.add(new Change(amount));
	}
}
