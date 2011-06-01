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

public class RetrieveEdgeStateQuery extends TraCIQuery {
	private static final short COMMAND_RETRIEVE_EDGE_STATE = 0xAA;	
//	private static final short VAR_TRAVEL_TIME = 0x5A;
	private static final short VAR_TRAVEL_TIME = 0x58;
	private static final short COMMAND_RETRIEVE_EDGE_STATE_RESP = 0xBA;
	private String edgeID;

	public RetrieveEdgeStateQuery(Socket sock, String edgeID) {
		super(sock);
		this.edgeID = edgeID;
	}

//	public float getGlobalTravelTime() throws IOException {
	public float getGlobalTravelTime(int time) throws IOException {
		Storage cmd = new Storage();
//		cmd.writeUnsignedByte(1+1+1+(4+edgeID.length()));
		cmd.writeUnsignedByte(1+1+1+(4+edgeID.length()+1+4));
		cmd.writeUnsignedByte(COMMAND_RETRIEVE_EDGE_STATE);
		cmd.writeUnsignedByte(VAR_TRAVEL_TIME);
		cmd.writeStringASCII(edgeID);
		cmd.writeUnsignedByte(DATATYPE_INTEGER);
		cmd.writeInt(time);
		
		Storage resp = queryAndGetResponse(cmd, COMMAND_RETRIEVE_EDGE_STATE);
		// skip 5 bytes, don't now why but it should work
		resp.readByte();
		resp.readInt();
		short responseID = resp.readUnsignedByte();
		if (responseID != COMMAND_RETRIEVE_EDGE_STATE_RESP)
			throw new IOException("invalid response ID: " + responseID);
		
		short varID = resp.readUnsignedByte();
		if (varID != VAR_TRAVEL_TIME)
			throw new IOException("invalid variable response: " + varID);
		
		String respEdgeID = resp.readStringASCII();
		if (!respEdgeID.equals(edgeID))
			throw new IOException("invalid edge ID response: " + respEdgeID);
		
		short varType = resp.readUnsignedByte();
		if (varType != DATATYPE_FLOAT) 
			throw new IOException("invalid variable type response: " + varType);
	
		return resp.readFloat();
	}
}
