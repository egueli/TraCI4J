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

public class VehicleCountMaxQuery extends DomainQuery {

	private static final int DOMAIN_VEHICLE = 0x01;
	private static final int VAR_COUNTMAX = 0x0A;
	
	public VehicleCountMaxQuery(Socket sock) {
		super(sock);
	}
	
	public int doCommand() throws IOException {
		
		Storage scenarioCmd = makeCommand(DOMAIN_VEHICLE, -1, VAR_COUNTMAX,
				DATATYPE_INTEGER);
		
		Storage response = queryAndGetResponse(scenarioCmd);
	
		readAndCheckResponse(response, DOMAIN_VEHICLE, -1, VAR_COUNTMAX,
				DATATYPE_INTEGER);
		
		return response.readInt();
	}
}
