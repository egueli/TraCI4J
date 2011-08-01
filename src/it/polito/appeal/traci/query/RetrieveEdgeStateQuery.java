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

import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.protocol.ResponseContainer;

import java.io.IOException;

import java.net.Socket;

public class RetrieveEdgeStateQuery extends Query {
	private String edgeID;

	public RetrieveEdgeStateQuery(Socket sock, String edgeID) throws IOException {
		super(sock);
		this.edgeID = edgeID;
	}

	public double getGlobalTravelTime(int time) throws IOException {
		Command cmd = makeReadVarCommand(Constants.CMD_GET_EDGE_VARIABLE, 
				Constants.VAR_EDGE_TRAVELTIME, edgeID);
		cmd.content().writeUnsignedByte(Constants.TYPE_INTEGER);
		cmd.content().writeInt(time);
		
		ResponseContainer respc = queryAndVerifySingle(cmd);
		Command resp = respc.getResponse();
		
		verifyGetVarResponse(resp, Constants.RESPONSE_GET_EDGE_VARIABLE,
				Constants.VAR_EDGE_TRAVELTIME, edgeID);
		
		verify("edge travel time data type", Constants.TYPE_DOUBLE, (int)resp
				.content().readUnsignedByte());
		
		return resp.content().readDouble();
	}
}
