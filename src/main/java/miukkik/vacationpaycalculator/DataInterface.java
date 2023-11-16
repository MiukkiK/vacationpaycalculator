/**
 * @author Mia Kallio
 */

package miukkik.vacationpaycalculator;

import java.time.LocalDate;
import java.util.ArrayList;

public interface DataInterface<T> {
	
	public void add(T data);
	public ArrayList<T> getDataBetween(LocalDate startDate, LocalDate endDate); 	
}
