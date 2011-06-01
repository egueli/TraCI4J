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

import java.awt.geom.Point2D;
import java.io.IOException;

import de.uniluebeck.itm.tcpip.Socket;
import de.uniluebeck.itm.tcpip.Storage;

public class NetBoundariesQuery extends RoadmapQuery {

	private static final int VAR_NET_BOUNDARIES = 0x03;

	public NetBoundariesQuery(Socket sock) {
		super(sock, 0);
	}

	public Point2D[] doCommand() throws IOException {
		Storage scenarioCmd = makeCommand(VAR_NET_BOUNDARIES, DATATYPE_BOUNDARYBOX);
		
		Storage response = queryAndGetResponse(scenarioCmd, COMMAND_SCENARIO);
	
		readAndCheckResponse(response, VAR_NET_BOUNDARIES, DATATYPE_BOUNDARYBOX);
		
		float blX = response.readFloat();
		float blY = response.readFloat();
		float urX = response.readFloat();
		float urY = response.readFloat();
		
		return new Point2D[] {
				new Point2D.Float(blX, blY),
				new Point2D.Float(urX, urY),
		};
	}

}
