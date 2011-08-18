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
import it.polito.appeal.traci.protocol.StringList;

import java.io.IOException;

import java.net.Socket;
import java.util.List;

public class ChangeVehicleStateQuery extends VehicleQuery {

	public ChangeVehicleStateQuery(Socket sock, String vehicleID) throws IOException {
		super(sock, vehicleID);
	}

	public void changeEdgeTravelTime(int beginTime, int endTime, String edgeID, double travelTime) throws IOException {
		Command cmd = 
			makeChangeStateCommand(Constants.VAR_EDGE_TRAVELTIME, Constants.TYPE_COMPOUND);
		cmd.content().writeInt(4);
		cmd.content().writeUnsignedByte(Constants.TYPE_INTEGER);
		cmd.content().writeInt(beginTime);
		cmd.content().writeUnsignedByte(Constants.TYPE_INTEGER);
		cmd.content().writeInt(endTime);
		cmd.content().writeUnsignedByte(Constants.TYPE_STRING);
		cmd.content().writeStringASCII(edgeID);
		cmd.content().writeUnsignedByte(Constants.TYPE_DOUBLE);
		cmd.content().writeDouble(travelTime);
		
		queryAndVerifySingle(cmd);
	}
	
	public void reroute() throws IOException {
		Command cmd = new Command(Constants.CMD_SET_VEHICLE_VARIABLE);
		cmd.content().writeUnsignedByte(Constants.CMD_REROUTE_TRAVELTIME);
		cmd.content().writeStringASCII(vehicleID);
		cmd.content().writeUnsignedByte(Constants.TYPE_COMPOUND);
		cmd.content().writeInt(0);
		
		queryAndVerifySingle(cmd);
	}
	
	public void setMaxSpeed(double speed) throws IOException
	{
		Command cmd = makeChangeStateCommand(Constants.VAR_MAXSPEED, 
				Constants.TYPE_DOUBLE);
		cmd.content().writeDouble(speed);

		queryAndVerifySingle(cmd);
	}
	
	public void changeTarget(String edgeID) throws IOException {
		Command cmd = makeChangeStateCommand(Constants.CMD_CHANGETARGET, 
				Constants.TYPE_STRING);
		cmd.content().writeStringASCII(edgeID);

		queryAndVerifySingle(cmd);
	}
	
	public void changeRoute(List<String> newRoute) throws IOException {
		Command cmd = makeChangeStateCommand(Constants.VAR_ROUTE, 
				Constants.TYPE_STRINGLIST);
		StringList sl = new StringList(newRoute);
		sl.writeTo(cmd.content(), false);

		queryAndVerifySingle(cmd);
	}
}
