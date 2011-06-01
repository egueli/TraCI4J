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
import it.polito.appeal.traci.TraCIException.UnexpectedData;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.uniluebeck.itm.tcpip.Socket;
import de.uniluebeck.itm.tcpip.Storage;

public class MultiVehiclePositionQuery extends TraCIQuery {

	private interface VehicleResponseProcessor {
		void process(Storage response, String vehicleID) throws TraCIException;
	}
	
	
	private static final short COMMAND_RETRIEVE_VEHICLE_VALUE = 0xA4;
	private static final short COMMAND_RETRIEVE_VEHICLE_VALUE_RESP = 0xB4;
	private static final short VAR_EDGE_ID = 0x50;
	private static final short VAR_POSITION = 0x42;
	

	public MultiVehiclePositionQuery(Socket sock) {
		super(sock);
	}
	
	

	public Map<String, String> getVehiclesEdge(Set<String> vehicleIDs) throws IOException {
		if (vehicleIDs.isEmpty())
			return Collections.emptyMap();
		
		short variable = VAR_EDGE_ID;
		sendCommand(vehicleIDs, variable);

		final Map<String, String> out = new HashMap<String, String>();
		
		VehicleResponseProcessor vrp = new VehicleResponseProcessor() {
			@Override
			public void process(Storage resp, String vehicleID) throws TraCIException {
				checkResponseByte(resp, "variable type", DATATYPE_STRING);
				
				String edgeID = resp.readStringASCII();
				
				out.put(vehicleID, edgeID);
			}
		};
		
		
		processResponse(vehicleIDs, variable, vrp);
		
		return out;
	}

	public Map<String, Point2D> getVehiclesPosition(Set<String> vehicleIDs) throws IOException {
		if (vehicleIDs.isEmpty())
			return Collections.emptyMap();
		
		short variable = VAR_EDGE_ID;
		sendCommand(vehicleIDs, variable);

		final Map<String, Point2D> out = new HashMap<String, Point2D>();
		
		VehicleResponseProcessor vrp = new VehicleResponseProcessor() {
			@Override
			public void process(Storage resp, String vehicleID) throws TraCIException {
				checkResponseByte(resp, "variable type", DATATYPE_3DPOSITION);
				
				float x = resp.readFloat();
				float y = resp.readFloat();
				resp.readFloat(); // Z axis will be ignored

				out.put(vehicleID, new Point2D.Float(x, y));
			}
		};
		
		
		processResponse(vehicleIDs, variable, vrp);
		
		return out;
	}


	private void processResponse(Set<String> vehicleIDs,
			short variable, VehicleResponseProcessor responseProcessor)
			throws IOException {
		
		Storage resp = sock.receiveExact();
		for (int i=0; i<vehicleIDs.size(); i++) {
			checkStatusResponse(resp, COMMAND_RETRIEVE_VEHICLE_VALUE);
			// skip 5 bytes, don't now why but it should work
			resp.readByte();
			resp.readInt();
			
			short responseID = resp.readUnsignedByte();
			if (responseID != COMMAND_RETRIEVE_VEHICLE_VALUE_RESP)
				throw new IOException("invalid response ID: " + responseID);
			
			short varID = resp.readUnsignedByte();
			if (varID != variable) {
				throw new IOException("invalid variable ID: " + varID);
			}
			
			String vehicleID = resp.readStringASCII();
			
			responseProcessor.process(resp, vehicleID);
		}
	}
	


	private void sendCommand(Set<String> vehicleIDs, short variable)
			throws IllegalArgumentException, IOException {
		Storage cmd = new Storage();
		for (String vehicleID : vehicleIDs) {
			cmd.writeUnsignedByte(1+1+1+4+vehicleID.length());
			cmd.writeUnsignedByte(COMMAND_RETRIEVE_VEHICLE_VALUE);
			cmd.writeUnsignedByte(variable);
			cmd.writeStringASCII(vehicleID);
		}
		sock.sendExact(cmd);
	}

}
