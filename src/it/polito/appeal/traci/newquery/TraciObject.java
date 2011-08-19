/*   
    Copyright (C) 2011 ApPeAL Group, Politecnico di Torino

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

package it.polito.appeal.traci.newquery;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public abstract class TraciObject<E extends Enum<E>> {
	private final String id;
	
	private final EnumMap<E, ReadObjectVarQuery<?>> readQueries;
	
	protected TraciObject(String id, Class<E> enumClass) {
		this.id = id;
		readQueries = new EnumMap<E, ReadObjectVarQuery<?>>(enumClass);
	}
	
	protected void addReadQuery(E variable, ReadObjectVarQuery<?> query) {
		readQueries.put(variable, query);
	}
	
	public String getID() {
		return id;
	}
	
	public ReadObjectVarQuery<?> getReadQuery(E variable) {
		return readQueries.get(variable);
	}
	
	public Map<E, ReadObjectVarQuery<?>> getAllReadQueries() {
		return Collections.unmodifiableMap(readQueries);
	}
	
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
}
