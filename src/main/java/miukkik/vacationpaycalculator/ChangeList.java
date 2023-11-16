/**
 * @author Mia Kallio
 */

package miukkik.vacationpaycalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

public class ChangeList implements DataInterface<ChangeData>{

	private ArrayList<ChangeData> changes;

	public ChangeList() {
		changes = new ArrayList<ChangeData>();
		changes.add(new ChangeData(LocalDate.MIN, BigDecimal.ZERO));
	}

	public void add(ChangeData change) {
		changes.add(change);
	}

	public ArrayList<ChangeData> getDataBetween(LocalDate startDate, LocalDate endDate) {
		ArrayList<ChangeData> result = new ArrayList<ChangeData>();
		for (ChangeData change : changes) {
			LocalDate date = change.getDate();
			if (date.isAfter(startDate)) {
				if (!date.isAfter(endDate)) result.add(change);
			} else break;
		}
		return result;
	}

	public BigDecimal getValueOn(LocalDate date) {		
		BigDecimal result = null;
		for (ChangeData change : changes) {
			if (!change.getDate().isAfter(date)) result = change.getValue();
			else break;
		}
		return result;
	}

}
