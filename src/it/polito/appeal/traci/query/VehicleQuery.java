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

import it.polito.appeal.traci.TraCIException;
import it.polito.appeal.traci.TraCIException.UnexpectedData;
import de.uniluebeck.itm.tcpip.Socket;
import de.uniluebeck.itm.tcpip.Storage;

public abstract class VehicleQuery extends TraCIQuery {

	private final short COMMAND_GET_VEHICLE_VARIABLE = 0xa4;
	private final short COMMAND_CHANGE_VEHICLE_STATE = 0xc4;
	
	protected final String vehicleID;

	public VehicleQuery(Socket sock, String nodeID) {
		super(sock);
		this.vehicleID = nodeID;
	}

	protected Storage makeReadVarCommand(int variable, int varType)
			throws TraCIException {
		
		Storage cmd = new Storage();
		cmd.writeUnsignedByte(1+1+1+4+vehicleID.length());
		cmd.writeUnsignedByte(COMMAND_GET_VEHICLE_VARIABLE);
		cmd.writeUnsignedByte(variable);
		cmd.writeStringASCII(vehicleID);
		
		return cmd;
	}
	
	protected Storage makeChangeStateCommand(int variable, int varType, int valueLength) {
		Storage cmd = new Storage();
		cmd.writeUnsignedByte(1+1+1+4+vehicleID.length()+1+valueLength);
		cmd.writeUnsignedByte(COMMAND_CHANGE_VEHICLE_STATE);
		cmd.writeUnsignedByte(variable);
		cmd.writeStringASCII(vehicleID);
		cmd.writeUnsignedByte(varType);
		
		return cmd;
	}
	

	protected void readAndCheckResponse(Storage response, int variable,
			int varType) throws UnexpectedData {
		readResponseLength(response);
		checkResponseByte(response, "variable id", variable);
		checkResponseByte(response, "variable type", varType);
	}

	protected Storage queryAndGetResponse(Storage cmd)
			throws IOException {
		return queryAndGetResponse(cmd, COMMAND_GET_VEHICLE_VARIABLE);
	}
}
