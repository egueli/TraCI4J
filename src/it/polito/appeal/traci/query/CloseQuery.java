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

import java.io.IOException;

import de.uniluebeck.itm.tcpip.Socket;
import de.uniluebeck.itm.tcpip.Storage;

public class CloseQuery extends TraCIQuery {

	private static final short COMMAND_CLOSE = 0x7F;

	public CloseQuery(Socket sock) {
		super(sock);
	}

	public void doCommand() throws IOException {
		Storage stepCmd = new Storage();
		stepCmd.writeUnsignedByte(2);
		stepCmd.writeUnsignedByte(COMMAND_CLOSE);

		queryAndGetResponse(stepCmd, COMMAND_CLOSE);
	}
}
