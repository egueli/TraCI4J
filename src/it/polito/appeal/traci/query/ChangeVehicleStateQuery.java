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

public class ChangeVehicleStateQuery extends TraCIQuery {
	private static final short COMMAND_CHANGE_VEHICLE_STATE = 0xC4;	
	private static final short VAR_REROUTE = 0x90;
	private static final short VAR_CHANGE_EDGE_TRAVEL_TIME = 0x58;
	private String vehicleID;

	public ChangeVehicleStateQuery(Socket sock, String vehicleID) {
		super(sock);
		this.vehicleID = vehicleID;
	}

	public void changeEdgeTravelTime(int beginTime, int endTime, String edgeID, float travelTime) throws IOException {
		Storage cmd = new Storage();
		cmd.writeUnsignedByte(1+(4+vehicleID.length())+1+1+1+4+1+4+1+4+1+(4+edgeID.length())+1+4);
		cmd.writeUnsignedByte(COMMAND_CHANGE_VEHICLE_STATE);
		cmd.writeUnsignedByte(VAR_CHANGE_EDGE_TRAVEL_TIME);
		cmd.writeStringASCII(vehicleID);
		cmd.writeUnsignedByte(DATATYPE_COMPOUND);
		cmd.writeInt(4);
		cmd.writeUnsignedByte(DATATYPE_INTEGER);
		cmd.writeInt(beginTime);
		cmd.writeUnsignedByte(DATATYPE_INTEGER);
		cmd.writeInt(endTime);
		cmd.writeUnsignedByte(DATATYPE_STRING);
		cmd.writeStringASCII(edgeID);
		cmd.writeUnsignedByte(DATATYPE_FLOAT);
		cmd.writeFloat(travelTime);
		
		queryAndGetResponse(cmd, COMMAND_CHANGE_VEHICLE_STATE);
	}
	
	public void reroute() throws IOException {
		Storage cmd = new Storage();
		cmd.writeUnsignedByte(1+1+1+4+vehicleID.length()+1+4);
		cmd.writeUnsignedByte(COMMAND_CHANGE_VEHICLE_STATE);
		cmd.writeUnsignedByte(VAR_REROUTE);
		cmd.writeStringASCII(vehicleID);
		cmd.writeUnsignedByte(DATATYPE_COMPOUND);
		cmd.writeInt(0);
		
		queryAndGetResponse(cmd, COMMAND_CHANGE_VEHICLE_STATE);
	}
}
