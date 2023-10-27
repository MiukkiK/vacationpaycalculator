/**
 * @author Mia Kallio
 */

package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

public class ChangeList {

	private ArrayList<Change> changes;
	
	public ChangeList(BigDecimal amount) {
		changes.add(new Change(amount));
	}
	
	public void addChange(BigDecimal amount, LocalDate effectiveFrom) {
		changes.get(changes.size()-1).setEffectiveUntil(effectiveFrom.minusDays(1));
		changes.add(new Change(amount));
	}
}
