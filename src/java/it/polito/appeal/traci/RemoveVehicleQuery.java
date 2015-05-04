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

/**
 * Query for removing a vehicle from the simulation.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * @see <a href="http://sumo.sourceforge.net/doc/current/docs/userdoc/TraCI/Change_Vehicle_State.html">TraCI docs</a>
 */
public class RemoveVehicleQuery extends ChangeStateQuery {
	
	private Vehicle vehicleID;
	private int reason;
//	private int commandID;

	RemoveVehicleQuery(DataInputStream dis, DataOutputStream dos) {
		super(dis, dos, Constants.CMD_SET_VEHICLE_VARIABLE);
//		this.commandID = commandID;
	}

	@Override
	protected void writeRequestTo(Storage content) {
		content.writeUnsignedByte(Constants.REMOVE);
		content.writeStringASCII(vehicleID.getID());
		content.writeUnsignedByte(Constants.TYPE_BYTE);
		content.writeUnsignedByte(reason);

	}
	
	/**
	 * Set the removal parameters.
	 * 
	 * @param vehicle the vehicle to remove
	 * @param reason the reason of removal (see TraCI docs for a list)
	 * @see <a href="http://sumo.sourceforge.net/doc/current/docs/userdoc/TraCI/Change_Vehicle_State.html">TraCI docs</a>
	 * @throws IOException
	 * 
	 */
	public void setVehicleData(
			Vehicle vehicle, 
			int reason 
			) 
	throws IOException {
		
		if (vehicle == null)
			throw new IllegalArgumentException("Invalid vehicle");
		
		this.vehicleID = vehicle;
		this.reason = reason;
				
	}

}
