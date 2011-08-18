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
import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.protocol.ResponseContainer;
import it.polito.appeal.traci.protocol.StatusResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.uniluebeck.itm.tcpip.Storage;

public abstract class ChangeStateQuery extends Query {

	private final int commandID;
	
	public ChangeStateQuery(DataInputStream dis, DataOutputStream dos, int commandID) {
		super(dis, dos);
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
		if (statusResp.id() != commandID)
			throw new TraCIException("command and status IDs must match");
		if (statusResp.result() != Constants.RTYPE_OK)
			throw new TraCIException("SUMO error for command "
					+ statusResp.id() + ": " + statusResp.description());
		
		
	}
	
	public void run() throws IOException {
		MultiQuery multi = new MultiQuery(dos, dis);
		multi.add(this);
		multi.sendRequestsAndDispatchResponses();
	}

}
