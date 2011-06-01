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

import java.awt.geom.Point2D;
import java.io.IOException;

import de.uniluebeck.itm.tcpip.Socket;
import de.uniluebeck.itm.tcpip.Storage;

public class VehiclePositionQuery extends VehicleQuery {

	public static class RoadmapPosition {
		public final String edgeID;
		public final float pos;
		public final byte laneID;
		private RoadmapPosition(String edgeID, float pos, byte laneID) {
			this.edgeID = edgeID;
			this.pos = pos;
			this.laneID = laneID;
		}
	}
	
	private static final int VAR_POSITION = 0x02;

	public VehiclePositionQuery(Socket sock, String nodeID) {
		super(sock, nodeID);
	}
	
	public Point2D.Float getPosition2D() throws IOException {
		Storage scenarioCmd = makeReadVarCommand(VAR_POSITION, DATATYPE_3DPOSITION);
		Storage response = queryAndGetResponse(scenarioCmd);
		readAndCheckResponse(response, VAR_POSITION, DATATYPE_3DPOSITION);
		
		float x = response.readFloat();
		float y = response.readFloat();
		/* float z = */ response.readFloat(); // don't need any Z
		
		return new Point2D.Float(x, y);
	}

	public RoadmapPosition getPositionRoadmap() throws IOException {
		Storage scenarioCmd = makeReadVarCommand(VAR_POSITION, DATATYPE_ROADMAP);
		Storage response = queryAndGetResponse(scenarioCmd);
		readAndCheckResponse(response, VAR_POSITION, DATATYPE_ROADMAP);
		
		return new RoadmapPosition(
				response.readStringASCII(),
				response.readFloat(),
				(byte) response.readUnsignedByte()
				);
	}

}
