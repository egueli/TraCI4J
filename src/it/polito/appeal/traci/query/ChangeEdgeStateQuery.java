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

import java.io.IOException;

import java.net.Socket;

public class ChangeEdgeStateQuery extends Query {
	private String edgeID;

	public ChangeEdgeStateQuery(Socket sock, String edgeID) throws IOException {
		super(sock);
		this.edgeID = edgeID;
	}

	public void changeGlobalTravelTime (int begin, int end, double travelTime) throws IOException {
		Command cmd = makeChangeStateCommand(
				Constants.CMD_SET_EDGE_VARIABLE, 
				Constants.VAR_EDGE_TRAVELTIME, 
				edgeID, Constants.TYPE_COMPOUND);
		
		cmd.content().writeInt(3);
		cmd.content().writeUnsignedByte(Constants.TYPE_INTEGER);
		cmd.content().writeInt(begin);
		cmd.content().writeUnsignedByte(Constants.TYPE_INTEGER);
		cmd.content().writeInt(end);
		cmd.content().writeUnsignedByte(Constants.TYPE_DOUBLE);
		cmd.content().writeDouble(travelTime);
		
		queryAndVerifySingle(cmd);
	}
}
