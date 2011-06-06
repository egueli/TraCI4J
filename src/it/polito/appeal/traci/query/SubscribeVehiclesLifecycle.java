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
import java.util.List;

import de.uniluebeck.itm.tcpip.Socket;
import de.uniluebeck.itm.tcpip.Storage;

public class SubscribeVehiclesLifecycle extends TraCIQuery {

	private static final short COMMAND_SUBSCRIBE_INFO_RETRIEVAL_FROM_SIMULATION = 0xdb;
	private static final short RESPONSE_SUBSCRIBE_INFO_RETRIEVAL_FROM_SIMULATION = 0xeb;
	private static final short VAR_IDS_OF_DEPARTED_VEHICLES = 0x74;
	private static final short VAR_IDS_OF_ARRIVED_VEHICLES = 0x7a;

	private static final byte[] VARIABLES = new byte[] {
			VAR_IDS_OF_DEPARTED_VEHICLES, VAR_IDS_OF_ARRIVED_VEHICLES };

	private static final byte VARIABLES_TYPE = TraCIQuery.DATATYPE_STRINGLIST;

	public SubscribeVehiclesLifecycle(Socket sock) {
		super(sock);
	}

	public void doCommand() throws IOException {
		Storage stepCmd = new Storage();
		// see
		// http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Value_Retrieval_Subscription
		stepCmd.writeUnsignedByte(15 + VARIABLES.length);
		stepCmd.writeUnsignedByte(COMMAND_SUBSCRIBE_INFO_RETRIEVAL_FROM_SIMULATION);
		stepCmd.writeInt(0); // begin time
		stepCmd.writeInt(Integer.MAX_VALUE); // end time
		stepCmd.writeStringASCII(""); // simulation ID (ignored)
		stepCmd.writeByte(VARIABLES.length); // no. of variables
		for (byte var : VARIABLES)
			// list of variables to return
			stepCmd.writeByte(var);

		Storage response = queryAndGetResponse(stepCmd,
				COMMAND_SUBSCRIBE_INFO_RETRIEVAL_FROM_SIMULATION);

		/* the following response bytes is the message we're interested in. */
		readResponseLength(response);
		

		checkResponseByte(response, "response code", 
				RESPONSE_SUBSCRIBE_INFO_RETRIEVAL_FROM_SIMULATION);
		
		response.readStringASCII(); // simulation ID (ignored)

		checkResponseByte(response, "no. of variables", VARIABLES.length);

		for (int i = 0; i < VARIABLES.length; i++) {
			checkResponseByte(response, "ID of variable #" + i, VARIABLES[i]);

			short varStatus = response.readByte();
			if (varStatus == 0xff) // RTYPE_ERR
			{
				checkResponseByte(response, "Error description datatype",
						TraCIQuery.DATATYPE_STRING);
				String errorDesc = response.readStringASCII();
				throw new TraCIException("Error while retrieving variable "
						+ VARIABLES[i] + ": " + errorDesc);
			}

			checkResponseByte(response, "type of variable #" + i,
					VARIABLES_TYPE);

			// throw away current value of these variables
			int count = response.readInt();
			for (int j = 0; j < count; j++)
				response.readStringASCII();
		}
	}

	public static void readVehicleListFromResponse(Storage response,
			List<String> outDeparted, List<String> outArrived)
			throws TraCIException {

		for (int i = 0; i < VARIABLES.length; i++) {
			short varId = response.readByte();

			response.readStringASCII(); // ignored
			checkResponseByte(response, "type of response variable #" + varId,
					VARIABLES_TYPE);

			short count = response.readByte();

			switch (varId) {
			case VAR_IDS_OF_DEPARTED_VEHICLES:
				for (int j = 0; j < count; j++) {
					outDeparted.add(response.readStringASCII());
				}
				break;
			case VAR_IDS_OF_ARRIVED_VEHICLES:
				for (int j = 0; j < count; j++) {
					outArrived.add(response.readStringASCII());
				}
				break;
			default:
				throw new TraCIException("unexpected variable " + varId);
			}
		}
	}
}
