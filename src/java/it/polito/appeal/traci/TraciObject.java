/*   
    Copyright (C) 2013 ApPeAL Group, Politecnico di Torino

    This file is part of TraCI4J.

    TraCI4J is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TraCI4J is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TraCI4J.  If not, see <http://www.gnu.org/licenses/>.
*/

package it.polito.appeal.traci;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * This class represents a generic SUMO Object, i.e. a vehicle, a lane, an
 * edge etc. It has a string ID that uniquely identifies it in the simulation
 * and may have one or more {@link ReadObjectVarQuery}es that refer to
 * corresponding variables that can be read. Queries for those variables can be
 * obtained via {@link #getReadQuery(Enum)} or {@link #getAllReadQueries()}.
 * <p>
 * If a subclass has at least one read query, it will have an own {@link Enum} 
 * (either nested or in a separate file) that lists all its read queries.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 * @param <E> the enum type that lists all the read queries
 */
public abstract class TraciObject<E extends Enum<E>> {
	private final String id;
	
	private final EnumMap<E, ReadObjectVarQuery<?>> readQueries;
	
	/**
	 * Constructor for the SUMO object.
	 * <p>
	 * If the subclass has one or more read queries, its constructor must call
	 * {@link #addReadQuery(Enum, ReadObjectVarQuery)} for each query,
	 * passing an instance of {@link ReadObjectVarQuery}. To avoid
	 * duplicated data, that instance should be used nowhere else.
	 * <p>
	 * Optionally, a subclass may have these elements:
	 * <ul>
	 * <li>a getter method for each query (named in the form of
	 * <code>queryReadXXXX()</code>), where XXXX is the variable name. Each
	 * getter may use {@link #getReadQuery(Enum)} and casts it to the
	 * variable-specific read query, e.g. ReadObjectVarQuery&lt;Double&gt;.</li>
	 * <li>a getter method for each query (named in the form of
	 * <code>queryXXXX()</code>) that changes the state of the object. If
	 * possible, such queries may be instantiated at construction time to
	 * increase performance.
	 * </ul>
	 * 
	 * <p>
	 * It is advised that the subclass constructor has package visibility as
	 * well as this one.
	 * 
	 * @param id
	 *            the string ID of the object, retrieved from SUMO
	 * @param enumClass
	 *            the class type of the variable list enum
	 */
	protected TraciObject(String id, Class<E> enumClass) {
		this.id = id;
		readQueries = new EnumMap<E, ReadObjectVarQuery<?>>(enumClass);
	}
	
	/**
	 * Adds a read query to the list of readable queries. It should be called
	 * by the subclass's constructor.
	 * 
	 * @param variable the enum instance of the variable
	 * @param query the corresponding {@link ReadObjectVarQuery} instance
	 */
	protected void addReadQuery(E variable, ReadObjectVarQuery<?> query) {
		readQueries.put(variable, query);
	}
	
	/**
	 * @return the string ID of the SUMO object this instance refers to.
	 */
	public String getID() {
		return id;
	}
	
	/**
	 * @param variable
	 * @return a "read variable" query for the corresponding variable.
	 */
	public ReadObjectVarQuery<?> getReadQuery(E variable) {
		return readQueries.get(variable);
	}
	
	/**
	 * @return a map of all the available read variables, along with their
	 *         query object.
	 */
	public Map<E, ReadObjectVarQuery<?>> getAllReadQueries() {
		return Collections.unmodifiableMap(readQueries);
	}
	
	/**
	 * Returns the output of {@link #getID()}.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getID();
	}
	
	@Override
	public int hashCode() {
		return getID().hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) 
			return false;
		if (other == this) 
			return true;
		if (!(other instanceof TraciObject<?>)) 
			return false;
		TraciObject<?> that = (TraciObject<?>) other;
		return getID().equals(that.getID());
	}

	/**
	 * Clears all the read queries' stored values, therefore forcing all
	 * subsequent queries to make a TraCI transaction. 
	 */
	public void clearCache() {
		for (ReadObjectVarQuery<?> q : getAllReadQueries().values())
			q.setObsolete();
	}
}
