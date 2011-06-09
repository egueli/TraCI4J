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
import it.polito.appeal.traci.protocol.RequestMessage;
import it.polito.appeal.traci.protocol.ResponseContainer;
import it.polito.appeal.traci.protocol.ResponseMessage;
import it.polito.appeal.traci.protocol.RoadmapPosition;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MultiVehiclePositionQuery extends Query {

	private final Set<String> vehicleIDs;
	
	
	public MultiVehiclePositionQuery(Socket sock, Set<String> vehicleIDs) throws IOException {
		super(sock);
		this.vehicleIDs = vehicleIDs;
	}
	
	

	public Map<String, RoadmapPosition> getVehiclesPositionRoadmap() throws IOException {
		if (vehicleIDs.isEmpty())
			return Collections.emptyMap();
		
		RequestMessage reqm = new RequestMessage();
		
		/*
		 * Copy the set into a list, whose order is guaranteed to be kept.
		 * This is needed because we must verify that each response matches
		 * a vehicleID; if we assume that SUMO gives responses in the same order
		 * as the requests, we can traverse the vehicleID list and the
		 * responses list in parallel and always expect matches.
		 */
		List<String> vehicleIDList = new ArrayList<String>(vehicleIDs);
		
		for (String vehicleID : vehicleIDList) {
			ReadVehicleVarQuery.addGetRoadmapPositionCommands(vehicleID, reqm);
		}
		
		ResponseMessage respm = queryAndVerify(reqm);
		
		final Map<String, RoadmapPosition> out = new HashMap<String, RoadmapPosition>();
		
		Iterator<ResponseContainer> respcIt = respm.responses().iterator();
		Iterator<String> vehiclesIt = vehicleIDList.iterator();
		while (respcIt.hasNext() || vehiclesIt.hasNext()) {
			String vehicleID = vehiclesIt.next();
			out.put(vehicleID, 
					ReadVehicleVarQuery.getRoadmapPositionFromResponse(vehicleID, respcIt));
		}
		
		return out;
	}

	public Map<String, Point2D> getVehiclesPosition2D() throws IOException {
		if (vehicleIDs.isEmpty())
			return Collections.emptyMap();
		
		RequestMessage reqm = new RequestMessage();
		
		for (String vehicleID : vehicleIDs) {
			Command cmd = makeReadVarCommand(
					Constants.CMD_GET_VEHICLE_VARIABLE, Constants.VAR_POSITION,
					vehicleID);
			reqm.append(cmd);
		}
		
		ResponseMessage respm = queryAndVerify(reqm);
		
		final Map<String, Point2D> out = new HashMap<String, Point2D>();
		
		for (ResponseContainer respc : respm.responses()) {
			
			Command resp = respc.getResponse();
			String vehicleID = verifyGetVarResponse(resp,
					Constants.RESPONSE_GET_VEHICLE_VARIABLE,
					Constants.VAR_POSITION, null);
			verify("position data type", 1 /* 2DPosition */, (int)resp.content()
					.readUnsignedByte());
			float x = resp.content().readFloat();
			float y = resp.content().readFloat();
			
			out.put(vehicleID, new Point2D.Float(x, y));
		}
		
		return out;
	}
}
