/**
 * Generic Interface for data list objects. Requires methods for adding data to the list and returning a partial list of data between given start and end dates.
 * @author Mia Kallio
 */

package miukkik.vacationpaycalculator;

import java.time.LocalDate;
import java.util.ArrayList;

public interface DataInterface<T> {
	
	public void add(T data);
	public ArrayList<T> getDataBetween(LocalDate startDate, LocalDate endDate); 	
}
