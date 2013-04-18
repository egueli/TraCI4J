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

import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.StringList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import de.uniluebeck.itm.tcpip.Storage;

/**
 * Represents a query for a collection of values, represented in a StringList
 * that contains their IDs. Its output is a collection of TraCI objects,
 * gathered from a {@link Repository}.
 * <p>
 * It provides an abstract method to create a specific {@link Collection} and
 * requires a reference to a {@link Repository} to retrieve each element in the
 * collection given its ID.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * 
 * @param <V>
 * @param <C>
 */
public abstract class ObjectCollectionQuery<V extends TraciObject<?>, C extends Collection<V>> extends ReadObjectVarQuery<C> {

	private final Repository<V> repository;

	ObjectCollectionQuery(DataInputStream dis, DataOutputStream dos,
			int commandID, Repository<V> repository, String objectID, int varID) {
		super(dis, dos, commandID, objectID, varID);
		this.repository = repository;
	}

	protected abstract C makeCollection();
	
	@Override
	protected C readValue(Command resp) throws TraCIException {
		Storage content = resp.content();
		List<String> ids = new StringList(content, true);
		C out = makeCollection();
		for (String id : ids) {
			try {
				out.add(repository.getByID(id));
			} catch (IOException e) {
				throw new TraCIException(e.toString());
			}
		}
		return out;
	}
}
