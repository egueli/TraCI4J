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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uniluebeck.itm.tcpip.Socket;
import de.uniluebeck.itm.tcpip.Storage;

public class SimStepQuery extends TraCIQuery {

	private static final int COMMAND_SIMSTEP = 0x02;
	
	private static final int RESPONSE_SIMULATION_VARIABLE = 0xbb;

	
	private final int step;
	
	private Set<String> createdVehicles;
	private Set<String> destroyedVehicles;

	public SimStepQuery(Socket sock, int step)
	{
		super(sock);
		this.step = step;
	}
	
	public void doCommand() throws IOException {
		Storage stepCmd = new Storage();
		stepCmd.writeUnsignedByte(5);
		stepCmd.writeUnsignedByte(COMMAND_SIMSTEP);
		stepCmd.writeInt(step * 1000);

		Storage response = queryAndGetResponse(stepCmd, COMMAND_SIMSTEP);
		
		readResponseLength(response);
		
		checkResponseByte(response, "response type", RESPONSE_SIMULATION_VARIABLE);

		List<String> departed = new ArrayList<String>();
		List<String> arrived = new ArrayList<String>();
		SubscribeVehiclesLifecycle.readVehicleListFromResponse(response, departed, arrived);

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
