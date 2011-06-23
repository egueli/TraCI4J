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
import it.polito.appeal.traci.TraCIException.UnexpectedData;
import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.protocol.ResponseContainer;

import java.io.IOException;
import java.net.Socket;

public abstract class VehicleQuery extends Query {

	protected final String vehicleID;

	public VehicleQuery(Socket sock, String nodeID) throws IOException {
		super(sock);
		this.vehicleID = nodeID;
	}

	protected Command makeReadVarCommand(int variable)
			throws TraCIException {
		return makeReadVarCommand(Constants.CMD_GET_VEHICLE_VARIABLE, variable,
				vehicleID);
	}
	
	protected Command makeChangeStateCommand(int variable, int varType) {
		return super.makeChangeStateCommand(
				Constants.CMD_SET_VEHICLE_VARIABLE,
				variable,
				vehicleID, 
				varType);
	}

	/**
	 * Like {@link #queryAndVerifyGetVarCommand(int)}, but verifies the variable
	 * type too.
	 * @param variable
	 * @param varType
	 * @throws IOException
	 * @see #queryAndVerifyGetVarCommand(int)
	 */
	protected Command queryAndVerifyGetVarCommand(int variable, int varType) throws IOException {
		Command req = makeReadVarCommand(variable);
		ResponseContainer respc = queryAndVerifySingle(req);
		Command resp = respc.getResponse();
		verifyGetVarResponse(resp, variable, varType);
		
		return resp;
	}

	/**
	 * Sends a "get vehicle variable" command and checks that the received
	 * response matches the response code, variable ID and vehicle ID.
	 * @param variable the variable ID
	 * @return the response, already pointing to the data to read
	 * @throws IOException 
	 */
	protected Command queryAndVerifyGetVarCommand(int variable) throws IOException {
		Command req = makeReadVarCommand(variable);
		ResponseContainer respc = queryAndVerifySingle(req);
		Command resp = respc.getResponse();
		verifyGetVarResponse(resp, variable);
		
		return resp;
	}

	protected void verifyGetVarResponse(Command resp, int variable)
			throws UnexpectedData, IllegalStateException,
			IllegalArgumentException {
		verifyGetVarResponse(resp, Constants.RESPONSE_GET_VEHICLE_VARIABLE,
				variable, vehicleID);
	}
	
	protected void verifyGetVarResponse(Command resp, int variable, int varType) throws UnexpectedData, IllegalStateException, IllegalArgumentException {
		verifyGetVarResponse(resp, variable);
		verify("variable type", varType, (int)resp.content().readUnsignedByte());
	}
}
