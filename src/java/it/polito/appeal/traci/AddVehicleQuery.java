/*   
    Copyright (C) 2013 ApPeAL Group, Politecnico di Torino

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

package it.polito.appeal.traci;

import it.polito.appeal.traci.protocol.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.uniluebeck.itm.tcpip.Storage;

public class AddVehicleQuery extends ChangeStateQuery {

	private String id;
	private VehicleType vehicleType;
	private Route route;
	private int lane;
	private double insertionPosition;
	private double insertionSpeed;
	private final Repository<Vehicle> vehicles;

	AddVehicleQuery(DataInputStream dis, DataOutputStream dos,
			Repository<Vehicle> vehicles) {
		super(dis, dos, Constants.CMD_SET_VEHICLE_VARIABLE);
		this.vehicles = vehicles;
	}

	public void setVehicleData(
			String id, 
			VehicleType vehicleType, 
			Route route,
			int lane,
			double insertionPosition, 
			double insertionSpeed) 
	throws IOException {
		
		if (vehicles.getByID(id) != null)
			throw new IllegalArgumentException("vehicle already exists");
		
		this.id = id;
		this.vehicleType = vehicleType;
		this.route = route;
		this.lane = lane;
		this.insertionPosition = insertionPosition;
		this.insertionSpeed = insertionSpeed;
		
	}
	
	@Override
	protected void writeRequestTo(Storage content) {
		content.writeUnsignedByte(Constants.ADD);
		
		content.writeStringASCII(id);
		content.writeUnsignedByte(Constants.TYPE_COMPOUND);
		content.writeInt(6);
		
		content.writeUnsignedByte(Constants.TYPE_STRING);
		content.writeStringASCII(vehicleType.getID());
		
		content.writeUnsignedByte(Constants.TYPE_STRING);
		content.writeStringASCII(route.getID());

		content.writeUnsignedByte(Constants.TYPE_INTEGER);
		content.writeInt(0); // departure time - TODO add departure times in the future
		
		content.writeUnsignedByte(Constants.TYPE_DOUBLE);
		content.writeDouble(insertionPosition);

		content.writeUnsignedByte(Constants.TYPE_DOUBLE);
		content.writeDouble(insertionSpeed);
		
		content.writeUnsignedByte(Constants.TYPE_BYTE);
		content.writeByte(lane);
	}
	
}