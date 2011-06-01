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

public class ChangeDestinationQuery extends TraCIQuery {

	private static final short COMMAND_CHANGE_DESTINATION = 0x31;

	private final int id;
	
	public ChangeDestinationQuery(Socket sock, int id) {
		super(sock);
		this.id = id;
	}
	
	public void changeDestination(String edgeID) throws IOException
	{
		Storage cmd = new Storage();
		changeDestCommand(cmd, edgeID);
		queryAndGetResponse(cmd, COMMAND_CHANGE_DESTINATION);
	}

	private void changeDestCommand(Storage cmd, String edgeID) {
		cmd.writeUnsignedByte(10 + edgeID.length());
		cmd.writeUnsignedByte(COMMAND_CHANGE_DESTINATION);
		cmd.writeInt(id);
		cmd.writeStringASCII(edgeID);
	}
}
