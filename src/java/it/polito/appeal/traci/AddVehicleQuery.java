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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uniluebeck.itm.tcpip.Storage;

/**
 * Query for adding a new vehicle in the simulation.
 * <p>
 * For the moment, the new vehicle must follow an already known route.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * @see <a
 *      href="http://sumo.sourceforge.net/doc/current/docs/userdoc/TraCI/Change_Vehicle_State.html">TraCI
 *      docs</a>
 */
public class AddVehicleQuery extends ChangeStateQuery {

	private static Pattern pattern = Pattern.compile("\\d+$");

	private String id;
	private VehicleType vehicleType;
	private Route route;
	private Lane lane;
	private double insertionPosition;
	private double insertionSpeed;
	private final Repository<Vehicle> vehicles;
	private int departureTime;

	AddVehicleQuery(DataInputStream dis, DataOutputStream dos,
			Repository<Vehicle> vehicles) {
		super(dis, dos, Constants.CMD_SET_VEHICLE_VARIABLE);
		this.vehicles = vehicles;
	}

	/**
	 * Sets the parameters for the new vehicle.
	 * 
	 * @see <a
	 *      href="http://sumo.sourceforge.net/doc/current/docs/userdoc/TraCI/Change_Vehicle_State.html">TraCI
	 *      doc</a>
	 * @param id
	 * @param vehicleType
	 * @param route
	 * @param lane
	 * @param insertionPosition
	 * @param insertionSpeed
	 * @throws IOException
	 */
	public void setVehicleData(String id, VehicleType vehicleType, Route route,
			Lane lane, int departureTime, double insertionPosition,
			double insertionSpeed) throws IOException {

		if (vehicles.getByID(id) != null)
			throw new IllegalArgumentException("vehicle already exists");

		this.id = id;
		this.vehicleType = vehicleType;
		this.route = route;
		this.lane = lane;
		this.insertionPosition = insertionPosition;
		this.insertionSpeed = insertionSpeed;
		this.departureTime = departureTime;
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
		content.writeInt(departureTime);

		content.writeUnsignedByte(Constants.TYPE_DOUBLE);
		content.writeDouble(insertionPosition);

		content.writeUnsignedByte(Constants.TYPE_DOUBLE);
		content.writeDouble(insertionSpeed);

		try {
			content.writeUnsignedByte(Constants.TYPE_BYTE);
			content.writeByte(getIdFromLane(lane));
		} catch (IllegalLaneException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private int getIdFromLane(Lane lane) throws IllegalLaneException {
		Matcher m = pattern.matcher(lane.getID());
		if (m.find()) {
			return Integer.parseInt(m.group());
		} else {
			throw new IllegalLaneException();
		}
	}

	public class IllegalLaneException extends Exception {
		private static final long serialVersionUID = 511943021293498823L;
	}
}