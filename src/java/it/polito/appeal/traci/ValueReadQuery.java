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


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Represents a query to retrieve a value. This class is able to
 * communicate directly with SUMO to run the query.
 * <p>
 * Calling the {@link #get()} method runs the query immediately: the query's
 * requests are sent to SUMO; responses from SUMO are received and processed.
 * The returned value is cached internally and any subsequent calls to
 * {@link #get()} will return that value and avoid another dialog with SUMO,
 * unless the subclass calls {@link #setObsolete()};
 * <p>
 * Please note that using the immediate execution can be slow; to increase
 * performance, it is recommended to use a {@link MultiQuery}.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * 
 * @param <V>
 *            the type of the value to read
 */
public abstract class ValueReadQuery<V> extends Query {
	private V value = null;

	protected final DataInputStream dis;
	protected final DataOutputStream dos;

	ValueReadQuery(DataInputStream dis, DataOutputStream dos) {
		this.dis = dis;
		this.dos = dos;
	}
	
	protected void setObsolete() {
		value = null;
	}

	protected void setDone(V value) {
		this.value = value;
	}
	
	/**
	 * If the output value of this query is available, returns that value,
	 * otherwise makes an immediate request to SUMO.
	 *  
	 * @return
	 * @throws IOException
	 */
	public V get() throws IOException {
		if (hasValue())
			return value;
		else {
			MultiQuery mq = new MultiQuery(dos, dis);
			mq.add(this);
			mq.run();
			if (!hasValue())
				throw new IllegalStateException("incorrect state after pickResponses()");
			return value;
		}
	}
	
	public boolean hasValue() {
		return value != null;
	}
}
