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
import it.polito.appeal.traci.protocol.ResponseContainer;
import it.polito.appeal.traci.protocol.StatusResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.uniluebeck.itm.tcpip.Storage;

/**
 * Represents a query that changes the state of the simulation in some way.
 * <p>
 * It can be executed immediately using the {@link #run()} method, or it
 * can be added to a {@link MultiQuery} together with other queries to make a single
 * request to SUMO and increase performance.
 * <p>
 * If a subclass needs parameters, these can be entered via setter methods, so that
 * the general usage is like this:
 * <ol>
 * <li>get a query from {@link SumoTraciConnection} or a {@link TraciObject} subclass</li>
 * <li>set the query's data</li>
 * <li>run the query</li>
 * </ol>
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * 
 */
public abstract class ChangeStateQuery extends Query {

	private final int commandID;
	private final DataInputStream dis;
	private final DataOutputStream dos;
	
	ChangeStateQuery(DataInputStream dis, DataOutputStream dos, int commandID) {
		this.dis = dis;
		this.dos = dos;
		this.commandID = commandID;
	}

	@Override
	List<Command> getRequests() {
		Command cmd = new Command(commandID);
		writeRequestTo(cmd.content());
		return Collections.singletonList(cmd);
	}
	
	protected abstract void writeRequestTo(Storage content);

	/**
	 * Checks that the response matches the command and was successful.
	 */
	@Override
	void pickResponses(Iterator<ResponseContainer> responseIterator)
			throws TraCIException {
		
		ResponseContainer respc = responseIterator.next();
		StatusResponse statusResp = respc.getStatus();
		Utils.checkStatusResponse(statusResp, commandID);
		
	}
	
	/**
	 * Sends the query to SUMO and get the response.
	 * @throws IOException
	 */
	public void run() throws IOException {
		MultiQuery multi = new MultiQuery(dos, dis);
		multi.add(this);
		multi.run();
	}
}
