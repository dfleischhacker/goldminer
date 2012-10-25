package de.uni_mannheim.informatik.dws.goldminer.sparql;

import de.uni_mannheim.informatik.dws.goldminer.util.Parameter;
import de.uni_mannheim.informatik.dws.goldminer.util.Settings;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Contains the different types of filters.
 * @author Jakob
 *
 */
public class Filter {
	
	/** filter that is used for the creation of class tables. */
	private final String classesFilter;
	
	/** filter that is used for the creation of the individuals tables. */
	private final String individualsFilter;
	
	/**
	 * the parameterless constructor takes the values for the filters
	 * from the properties file.
	 * 
	 * @throws java.io.FileNotFoundException if the prop. file wasn't found.
	 * @throws java.io.IOException if an problem occurs while reading the prop. file.
	 */
	public Filter() throws FileNotFoundException, IOException {
		if(!Settings.loaded()) {
			Settings.load();
		}
		classesFilter = Settings.getString(Parameter.CLASSES_FILTER);
		individualsFilter = Settings.getString(Parameter.INDIVIDUALS_FILTER);
	}
	
	/**
	 * constructor that enables the user to submit the parameters
	 * for the filters (ignors the properties file).
	 * 
	 * @param classesFilter
	 * @param individualsFilter
	 */
	public Filter(String classesFilter, String individualsFilter) {
		this.classesFilter = classesFilter;
		this.individualsFilter = individualsFilter;
	}
	
	/**
	 * getter for the classes filter.
	 * 
	 * @return classes filter string.
	 */
	public String getClassesFilter() {
		return this.classesFilter;
	}
	
	/**
	 * getter for the individuals filter.
	 * 
	 * @return individuals filter string.
	 */
	public String getIndividualsFilter() {
		return this.individualsFilter;
	}

}
