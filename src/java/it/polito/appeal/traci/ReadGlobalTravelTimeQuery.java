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

/**
 * 
 */
package it.polito.appeal.traci;

import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;

public class ReadGlobalTravelTimeQuery extends ReadObjectVarQuery.DoubleQ {

	private int time = -1;
	
	ReadGlobalTravelTimeQuery(DataInputStream dis, DataOutputStream dos,
			int commandID, String objectID, int varID) {
		super(dis, dos, commandID, objectID, varID);
	}
	
	public void setTime(int time) {
		/*
		 * if the time is modified, forget the old value
		 */
		if (this.time != time)
			setObsolete();
		
		this.time = time;
	}

	@Override
	List<Command> getRequests() {
		if (time == -1)
			throw new IllegalStateException("time must be set first");
		
		List<Command> reqs = super.getRequests();
		Command req = reqs.iterator().next();
		req.content().writeByte(Constants.TYPE_INTEGER);
		req.content().writeInt(time);
		return reqs;
	}
	
}