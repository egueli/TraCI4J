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

import it.polito.appeal.traci.protocol.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import de.uniluebeck.itm.tcpip.Storage;

/**
 * Represents a query for changing a single-value variable.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 * @param <V>
 */
public abstract class ChangeObjectVarQuery<V> extends ChangeObjectStateQuery {

	private V value;
	
	ChangeObjectVarQuery(DataInputStream dis, DataOutputStream dos,
			int commandID, String objectID, int variableID) {
		super(dis, dos, commandID, objectID, variableID);
	}

	/**
	 * @return the new value to set to this object's variable.
	 */
	public V getValue() {
		return value;
	}

	/**
	 * Sets the new value to set to this object's variable.
	 * @param value
	 */
	public void setValue(V value) {
		this.value = value;
	}

	@Override
	protected final void writeParamsTo(Storage content) {
		if (value == null)
			throw new IllegalStateException("value wasn't set");
		
		writeValueTo(value, content);
	}

	protected abstract void writeValueTo(V val, Storage content);
	
	
	/**
	 * Specialization of {@link ChangeObjectVarQuery} for a {@link String} variable.
	 * 
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 *
	 */
	public static class ChangeStringQ extends ChangeObjectVarQuery<String> {
		ChangeStringQ(DataInputStream dis, DataOutputStream dos, int commandID,
				String objectID, int variableID) {
			super(dis, dos, commandID, objectID, variableID);
		}

		@Override
		protected void writeValueTo(String val, Storage content) {
			content.writeByte(Constants.TYPE_STRING);
			content.writeStringASCII(val);
		}
	}
	
	/**
	 * Specialization of {@link ChangeObjectVarQuery} for an {@link Integer} variable.
	 * 
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 *
	 */
	public static class ChangeIntegerQ extends ChangeObjectVarQuery<Integer> {

		ChangeIntegerQ(DataInputStream dis, DataOutputStream dos, int commandID,
				String objectID, int variableID) {
			super(dis, dos, commandID, objectID, variableID);
		}

		@Override
		protected void writeValueTo(Integer val, Storage content) {
			content.writeByte(Constants.TYPE_INTEGER);
			content.writeInt(val);
		}
	}
	
	/**
	 * Specialization of {@link ChangeObjectVarQuery} for a {@link Double} variable.
	 * 
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 *
	 */
	public static class ChangeDoubleQ extends ChangeObjectVarQuery<Double> {

		ChangeDoubleQ(DataInputStream dis, DataOutputStream dos,
				int commandID, String objectID, int variableID) {
			super(dis, dos, commandID, objectID, variableID);
		}

		@Override
		protected void writeValueTo(Double val, Storage content) {
			content.writeByte(Constants.TYPE_DOUBLE);
			content.writeDouble(val);
		}
	}
}
