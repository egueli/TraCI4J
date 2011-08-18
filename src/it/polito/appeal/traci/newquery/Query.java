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
import it.polito.appeal.traci.protocol.ResponseContainer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Iterator;
import java.util.List;

public abstract class Query {

	protected final DataInputStream dis;
	protected final DataOutputStream dos;

	Query(DataInputStream dis, DataOutputStream dos) {
		this.dis = dis;
		this.dos = dos;
	}
	
	/**
	 * Returns a list of Command needed to make the request.
	 * <p>
	 * This method must call the {@link #setPending()} method in order to update
	 * the query's state.
	 * @return
	 */
	abstract List<Command> getRequests();

	/**
	 * Reads one or more responses from the given {@link Iterator}, in order to
	 * get the requested data.
	 * <p>
	 * @param responseIterator
	 * @throws TraCIException 
	 */
	abstract void pickResponses(Iterator<ResponseContainer> responseIterator) throws TraCIException;

}
