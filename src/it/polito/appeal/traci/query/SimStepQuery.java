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

package it.polito.appeal.traci.query;

import it.polito.appeal.traci.TraCIException;
import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.protocol.ResponseContainer;
import it.polito.appeal.traci.protocol.StringList;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uniluebeck.itm.tcpip.Storage;

public class SimStepQuery extends Query {

	private final int step;
	
	private Set<String> createdVehicles;
	private Set<String> destroyedVehicles;

	public SimStepQuery(Socket sock, int step) throws IOException
	{
		super(sock);
		this.step = step;
	}
	
	public void doCommand() throws IOException {
		Command req = new Command(Constants.CMD_SIMSTEP2);
		req.content().writeInt(step * 1000);

		ResponseContainer respc = queryAndVerifySingle(req);
		
		List<String> departed = null;
		List<String> arrived = null;
		
		for (Command subResp : respc.getSubResponses()) {
			if (subResp.id() == Constants.RESPONSE_SUBSCRIBE_SIM_VARIABLE) {
				Storage content = subResp.content();
			
				content.readStringASCII(); // ignored for sim variables
				int varCount = content.readByte();
				
				for (int i=0; i<varCount; i++) {
					int var = content.readUnsignedByte();
					int status = content.readUnsignedByte();
					if (status == Constants.RTYPE_ERR) {
						verify("error description type", 
								Constants.TYPE_STRING, content.readUnsignedByte());
						throw new TraCIException("error in getting variable "
								+ var + "subscription response: "
								+ content.readStringASCII());
					}
					
					if (var == Constants.VAR_DEPARTED_VEHICLES_IDS)
						departed = new StringList(content, true);
					else if (var == Constants.VAR_ARRIVED_VEHICLES_IDS)
						arrived = new StringList(content, true);
				}
			}
		}
		
		createdVehicles = new HashSet<String>(departed);
		destroyedVehicles = new HashSet<String>(arrived);
	}

	/**
	 * @return the createdVehicles
	 */
	public Set<String> getCreatedVehicles() {
		return createdVehicles;
	}

	/**
	 * @return the destroyedVehicles
	 */
	public Set<String> getDestroyedVehicles() {
		return destroyedVehicles;
	}
}
