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

import it.polito.appeal.traci.Road;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uniluebeck.itm.tcpip.Socket;
import de.uniluebeck.itm.tcpip.Storage;

public class RoadsQuery extends RoadmapQuery {

	private static final int VAR_COUNT = 0x01;
	private static final int VAR_NAME = 0x0D;
	private static final int VAR_SHAPE = 0x09;

	public RoadsQuery(Socket sock) {
		super(sock, 0);
	}

	public Set<Road> queryRoads(boolean alsoInternal) throws IOException {
		int count = queryCount();

		Set<Road> out = new HashSet<Road>();

		for (int i = 0; i < count; i++) {
			String externalID = queryExternalID(i);

			if (!alsoInternal && externalID.startsWith(":"))
				continue;

			List<Point2D> shape = queryShape(i);
			Road newLane = new Road(i, externalID, shape);
			out.add(newLane);
			// System.err.println(newLane);
		}

		return out;
	}

	private int queryCount() throws IOException {
		Storage scenarioCmd = makeCommand(VAR_COUNT, DATATYPE_INTEGER);
		Storage response = queryAndGetResponse(scenarioCmd, COMMAND_SCENARIO);
		readAndCheckResponse(response, VAR_COUNT, DATATYPE_INTEGER);
		return response.readInt();
	}

	private String queryExternalID(int id) throws IOException {
		Storage scenarioCmd = makeCommand(DOMAIN_ROADMAP, id, VAR_NAME,
				DATATYPE_STRING);
		Storage response = queryAndGetResponse(scenarioCmd, COMMAND_SCENARIO);
		readAndCheckResponse(response, DOMAIN_ROADMAP, id, VAR_NAME,
				DATATYPE_STRING);
		return response.readStringASCII();
	}

	private List<Point2D> queryShape(int id) throws IOException {
		Storage scenarioCmd = makeCommand(DOMAIN_ROADMAP, id, VAR_SHAPE,
				DATATYPE_POLYGON);
		Storage response = queryAndGetResponse(scenarioCmd, COMMAND_SCENARIO);
		readAndCheckResponse(response, DOMAIN_ROADMAP, id, VAR_SHAPE,
				DATATYPE_POLYGON);

		int count = response.readByte();

		List<Point2D> out = new ArrayList<Point2D>();
		for (int i = 0; i < count; i++) {
			out.add(new Point2D.Float(response.readFloat(), response
					.readFloat()));
		}

		return out;
	}

}
