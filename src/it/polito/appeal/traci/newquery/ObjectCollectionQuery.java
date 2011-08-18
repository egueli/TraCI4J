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

import it.polito.appeal.traci.TraCIException;
import it.polito.appeal.traci.protocol.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Collection;
import java.util.List;

import de.uniluebeck.itm.tcpip.Storage;

/**
 * Represents a query for a collection of values. The collection is read and provided
 * atomically, i.e. is not possible to read only a subset of values ({@link Repository} 
 * can be used for that).
 * <p>
 * It provides an abstract method to create a specific {@link Collection} and
 * requires a reference to an {@link ObjectFactory} to make each element in the
 * collection.
 *   
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 * @param <V>
 * @param <C>
 */
public abstract class ObjectCollectionQuery<V, C extends Collection<V>> extends ReadObjectVarQuery<C> {

	private final ObjectFactory<V> factory;

	ObjectCollectionQuery(DataInputStream dis, DataOutputStream dos,
			int commandID, ObjectFactory<V> factory, String objectID, int varID) {
		super(dis, dos, commandID, objectID, varID);
		this.factory = factory;
	}

	protected abstract C makeCollection();
	
	@Override
	protected C readValue(Command resp) throws TraCIException {
		Storage content = resp.content();
		List<String> ids = new it.polito.appeal.traci.protocol.StringList(content, true);
		C out = makeCollection();
		for (String id : ids) {
			out.add(factory.newObject(id));
		}
		return out;
	}
}
