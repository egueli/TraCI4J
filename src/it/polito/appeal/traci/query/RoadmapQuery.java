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

import it.polito.appeal.traci.Lane;
import it.polito.appeal.traci.protocol.BoundingBox;
import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.protocol.Polygon;
import it.polito.appeal.traci.protocol.RequestMessage;
import it.polito.appeal.traci.protocol.ResponseContainer;
import it.polito.appeal.traci.protocol.ResponseMessage;
import it.polito.appeal.traci.protocol.StringList;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

public class RoadmapQuery extends DomainQuery {

	private static final Logger log = Logger.getLogger(RoadmapQuery.class);

	
	public RoadmapQuery(Socket sock) throws IOException {
		super(sock);
	}
	
	public Set<Lane> queryLanes(boolean alsoInternal) throws IOException {
		Set<Lane> out = new HashSet<Lane>();

		Command req = new Command(Constants.CMD_GET_LANE_VARIABLE);
		req.content().writeUnsignedByte(Constants.ID_LIST);
		req.content().writeStringASCII("");
		
		ResponseContainer respc = queryAndVerifySingle(req);
		Command resp = respc.getResponse();
		verify("variable ID", Constants.ID_LIST, resp.content().readUnsignedByte());
		resp.content().readStringASCII(); // ignored
		
		StringList laneIDs = new StringList(respc.getResponse().content(), true);
		
		if (log.isDebugEnabled()) {
			log.debug("laneIDs: ");
			int i=0;
			for (String laneID : laneIDs) {
				log.debug("#" + i + ": " + laneID);
				i++;
			}
		}
		
		RequestMessage reqm = new RequestMessage();
		
		for (String laneID : laneIDs) {
			if (!alsoInternal && laneID.startsWith(":"))
				continue;

			Command getShapeCmd = new Command(Constants.CMD_GET_LANE_VARIABLE);
			getShapeCmd.content().writeUnsignedByte(Constants.VAR_SHAPE);
			getShapeCmd.content().writeStringASCII(laneID);
			
			reqm.append(getShapeCmd);
		}

		ResponseMessage respm = queryAndVerify(reqm);
		
		for (ResponseContainer laneRespC : respm.responses()) {
			Command laneResp = laneRespC.getResponse();
			verify("lane response id", Constants.RESPONSE_GET_LANE_VARIABLE,
					laneResp.id());
			verify("lane variable", Constants.VAR_SHAPE, laneResp.content()
					.readUnsignedByte());
			
			String laneID = laneResp.content().readStringASCII();
			Polygon shape = new Polygon(laneResp.content(), true);
			
			out.add(new Lane(laneID, shape.getShape()));
		}
		
		return out;
	}
}
