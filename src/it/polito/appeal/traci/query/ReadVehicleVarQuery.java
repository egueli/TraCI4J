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

import it.polito.appeal.traci.TraCIException.UnexpectedData;
import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.protocol.RequestMessage;
import it.polito.appeal.traci.protocol.ResponseContainer;
import it.polito.appeal.traci.protocol.ResponseMessage;
import it.polito.appeal.traci.protocol.RoadmapPosition;
import it.polito.appeal.traci.protocol.StringList;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import java.net.Socket;

public class ReadVehicleVarQuery extends VehicleQuery {

	public ReadVehicleVarQuery(Socket sock, String vehicleID) throws IOException {
		super(sock, vehicleID);
	}

	public List<String> queryRoute() throws IOException {
		Command resp = 
			queryAndVerifyGetVarCommand(Constants.VAR_EDGES, Constants.TYPE_STRINGLIST);
		
		return new StringList(resp.content(), false);
	}
	
	public Point2D queryPosition2D() throws IOException {
		Command resp =
			queryAndVerifyGetVarCommand(Constants.VAR_POSITION);
		
		float x = resp.content().readFloat();
		float y = resp.content().readFloat();
		
		return new Point2D.Float(x, y);
	}

	public RoadmapPosition queryPositionRoadmap() throws IOException {
		
		RequestMessage reqm = new RequestMessage();
		addGetRoadmapPositionCommands(vehicleID, reqm);
		
		ResponseMessage respm = queryAndVerify(reqm);
		
		Iterator<ResponseContainer> it = respm.responses().iterator();
		return getRoadmapPositionFromResponse(vehicleID, it);
	}

	static void addGetRoadmapPositionCommands(String vehicleID,
			RequestMessage reqm) {
		reqm.append(makeReadVarCommand(Constants.CMD_GET_VEHICLE_VARIABLE,
				Constants.VAR_ROAD_ID, vehicleID));
		reqm.append(makeReadVarCommand(Constants.CMD_GET_VEHICLE_VARIABLE,
				Constants.VAR_LANE_INDEX, vehicleID));
		reqm.append(makeReadVarCommand(Constants.CMD_GET_VEHICLE_VARIABLE,
				Constants.VAR_LANEPOSITION, vehicleID));
	}
	
	static RoadmapPosition getRoadmapPositionFromResponse(String vehicleID, Iterator<ResponseContainer> it) throws UnexpectedData {
		Command respGetEdge =      it.next().getResponse();
		Command respGetLaneIndex = it.next().getResponse();
		Command respGetLanePos =   it.next().getResponse();
		
		verifyGetVarResponse(respGetEdge,
				Constants.RESPONSE_GET_VEHICLE_VARIABLE, Constants.VAR_ROAD_ID,
				vehicleID);
		verify("variable type", Constants.TYPE_STRING, (int) respGetEdge
				.content().readUnsignedByte());

		verifyGetVarResponse(respGetLaneIndex,
				Constants.RESPONSE_GET_VEHICLE_VARIABLE,
				Constants.VAR_LANE_INDEX, vehicleID);
		verify("variable type", Constants.TYPE_INTEGER, (int) respGetLaneIndex
				.content().readUnsignedByte());
		
		verifyGetVarResponse(respGetLanePos,
				Constants.RESPONSE_GET_VEHICLE_VARIABLE,
				Constants.VAR_LANEPOSITION, vehicleID);		
		verify("variable type", Constants.TYPE_DOUBLE, (int) respGetLanePos
				.content().readUnsignedByte());

		return new RoadmapPosition(
				respGetEdge.content().readStringASCII(),
				respGetLanePos.content().readDouble(),
				respGetLaneIndex.content().readInt()
				);
	}
}
