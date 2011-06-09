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

package it.polito.appeal.traci.protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uniluebeck.itm.tcpip.Storage;

public class ResponseMessage {

	private static final int[] STATUS_ONLY_RESPONSES = new int[] {
		Constants.CMD_CLOSE,
		Constants.CMD_SET_LANE_VARIABLE,
		Constants.CMD_SET_TL_VARIABLE,
		Constants.CMD_SET_VEHICLE_VARIABLE,
		Constants.CMD_SET_POI_VARIABLE,
		Constants.CMD_SET_POLYGON_VARIABLE,
		Constants.CMD_SET_EDGE_VARIABLE,
		Constants.CMD_SET_GUI_VARIABLE
	};
	
	private List<ResponseContainer> pairs = new ArrayList<ResponseContainer>();
	
	public ResponseMessage(DataInputStream dis) throws IOException {
		int totalLen = dis.readInt() - Integer.SIZE/8;
		
		byte[] buffer = new byte[totalLen];
		dis.readFully(buffer);
		
		Storage s = new Storage(buffer);
		
		while (s.validPos()) {
			StatusResponse sr = new StatusResponse(s);
			ResponseContainer responseContainer;
			
			if (sr.id() == Constants.CMD_SIMSTEP2) {
				int nSubResponses = s.readInt();
				List<Command> subResponses = new ArrayList<Command>(
						nSubResponses);
				for (int i = 0; i < nSubResponses; i++) {
					subResponses.add(new Command(s));
				}

				responseContainer = new ResponseContainer(sr, null, subResponses);
				
			}
			else if (isStatusOnlyResponse(sr.id())) {
				responseContainer = new ResponseContainer(sr, null);
			}
			else
				responseContainer = new ResponseContainer(sr, new Command(s));
			
			pairs.add(responseContainer);
		}
	}
	
	private boolean isStatusOnlyResponse(int statusResponseID) {
		for (int id : STATUS_ONLY_RESPONSES)
			if (id == statusResponseID)
				return true;
		return false;
	}

	public List<ResponseContainer> responses() {
		return Collections.unmodifiableList(pairs);
	}
	

}
