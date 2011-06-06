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
import java.util.Map;

import de.uniluebeck.itm.tcpip.Socket;
import de.uniluebeck.itm.tcpip.Storage;

public class ChangeRouteQuery extends TraCIQuery {

	private static final short COMMAND_CHANGE_ROUTE = 0x30;

	private final int id;
	
	public ChangeRouteQuery(Socket sock, int id) {
		super(sock);
		this.id = id;
	}
	
	public void changeRoute(String edgeID, double travelTime) throws IOException
	{
		Storage cmd = new Storage();
		addRerouteCommand(cmd, edgeID, travelTime);

		queryAndGetResponse(cmd, COMMAND_CHANGE_ROUTE);
	}

	private void addRerouteCommand(Storage cmd, String edgeID, double travelTime) {
		cmd.writeUnsignedByte(18 + edgeID.length());
		cmd.writeUnsignedByte(COMMAND_CHANGE_ROUTE);
		cmd.writeInt(id);
		cmd.writeStringASCII(edgeID);
		cmd.writeDouble(travelTime);
	}

	public void changeRoute(Map<String, Double> travelTimes) throws IOException {
		Storage cmd = new Storage();
		for(Map.Entry<String, Double> entry : travelTimes.entrySet())
		{
			addRerouteCommand(cmd, entry.getKey(), entry.getValue());			
		}

		queryAndGetResponse(cmd, COMMAND_CHANGE_ROUTE);
	}
}
