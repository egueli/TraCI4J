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

public class SubscribeVehiclesLifecycle extends Query {

	// TODO make this more generalized (SubscribeSimulationVars)

	private static final byte[] VARIABLES = new byte[] {
			Constants.VAR_DEPARTED_VEHICLES_IDS, 
			Constants.VAR_ARRIVED_VEHICLES_IDS,
			Constants.VAR_TELEPORT_STARTING_VEHICLES_IDS,
			Constants.VAR_TELEPORT_ENDING_VEHICLES_IDS,
			};

	public SubscribeVehiclesLifecycle(Socket sock) throws IOException {
		super(sock);
	}

	public void doCommand() throws IOException {
		Command cmd = new Command(Constants.CMD_SUBSCRIBE_SIM_VARIABLE);
		// see
		// http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Value_Retrieval_Subscription
		cmd.content().writeInt(0); // begin time
		cmd.content().writeInt(Integer.MAX_VALUE); // end time
		cmd.content().writeStringASCII(""); // simulation ID (ignored)
		cmd.content().writeByte(VARIABLES.length); // no. of variables
		for (byte var : VARIABLES)
			// list of variables to return
			cmd.content().writeByte(var);

		queryAndVerifySingle(cmd);
	}
}
