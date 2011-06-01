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

import de.uniluebeck.itm.tcpip.Socket;
import de.uniluebeck.itm.tcpip.Storage;

public class ChangeEdgeStateQuery extends TraCIQuery {
	private static final short COMMAND_CHANGE_EDGE_STATE = 0xCA;	
	private static final short VAR_TRAVEL_TIME = 0x58;
	private String edgeID;

	public ChangeEdgeStateQuery(Socket sock, String edgeID) {
		super(sock);
		this.edgeID = edgeID;
	}

	public void changeGlobalTravelTime (int begin, int end, float travelTime) throws IOException {
		Storage cmd = new Storage();
		cmd.writeUnsignedByte(1+1+1+4+edgeID.length()+1+4+1+4+1+4+1+4);
		cmd.writeUnsignedByte(COMMAND_CHANGE_EDGE_STATE);
		cmd.writeUnsignedByte(VAR_TRAVEL_TIME);
		cmd.writeStringASCII(edgeID);
		cmd.writeUnsignedByte(DATATYPE_COMPOUND);
		cmd.writeInt(3);
		cmd.writeUnsignedByte(DATATYPE_INTEGER);
		cmd.writeInt(begin);
		cmd.writeUnsignedByte(DATATYPE_INTEGER);
		cmd.writeInt(end);
		cmd.writeUnsignedByte(DATATYPE_FLOAT);
		cmd.writeFloat(travelTime);
		
		queryAndGetResponse(cmd, COMMAND_CHANGE_EDGE_STATE);
		
	}
}
