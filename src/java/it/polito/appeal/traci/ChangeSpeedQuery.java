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

import de.uniluebeck.itm.tcpip.Storage;

public class ChangeSpeedQuery extends ChangeObjectVarQuery<Double> {
		
	ChangeSpeedQuery(DataInputStream dis, DataOutputStream dos, String objectID) {
		super(dis, dos, Constants.CMD_SET_VEHICLE_VARIABLE, objectID, Constants.VAR_SPEED);
	}
	
	
	@Override
	protected void writeValueTo(Double speed, Storage content) {
		content.writeByte(Constants.TYPE_DOUBLE);
		content.writeDouble(speed);
	}
}
