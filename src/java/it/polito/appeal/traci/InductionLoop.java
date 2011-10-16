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

package it.polito.appeal.traci;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import it.polito.appeal.traci.InductionLoop.Variable;
import it.polito.appeal.traci.ReadObjectVarQuery.LaneQ;
import it.polito.appeal.traci.ReadObjectVarQuery.IntegerQ;
import it.polito.appeal.traci.ReadObjectVarQuery.DoubleQ;
import it.polito.appeal.traci.protocol.Constants;

public class InductionLoop extends TraciObject<Variable> {
	public enum Variable {
		POSITION,
		LANE_ID,
		VEHICLE_NUMBER,
		MEAN_SPEED,
		VEHICLES,
		OCCUPANCY,
		MEAN_VEHICLE_LENGTH,
		LAST_DETECTION_TIME
	}
	
	protected InductionLoop(String id, Repository<Lane> lanesRepo,
			Repository<Vehicle> vehicles, DataInputStream dis, DataOutputStream dos) {
		super(id, Variable.class);
		
		final int cmd = Constants.CMD_GET_INDUCTIONLOOP_VARIABLE;
		addReadQuery(Variable.POSITION, new DoubleQ(dis, dos, cmd, id,
				Constants.VAR_POSITION));

		addReadQuery(Variable.LANE_ID, new LaneQ(dis, dos, cmd, id,
				Constants.VAR_LANE_ID, lanesRepo));

		addReadQuery(Variable.VEHICLE_NUMBER, new IntegerQ(dis, dos, cmd, id,
				Constants.LAST_STEP_VEHICLE_NUMBER));

		addReadQuery(Variable.MEAN_SPEED, new IntegerQ(dis, dos, cmd, id,
				Constants.LAST_STEP_MEAN_SPEED));

		addReadQuery(Variable.VEHICLES, new VehicleSetQuery(dis, dos, cmd,
				id, Constants.LAST_STEP_VEHICLE_ID_LIST, vehicles));

		addReadQuery(Variable.OCCUPANCY, new DoubleQ(dis, dos, cmd, id,
				Constants.LAST_STEP_OCCUPANCY));
		
		addReadQuery(Variable.MEAN_VEHICLE_LENGTH, new DoubleQ(dis, dos, cmd,
				id, Constants.LAST_STEP_LENGTH));
		
		addReadQuery(Variable.LAST_DETECTION_TIME, new DoubleQ(dis, dos, cmd,
				id, Constants.LAST_STEP_TIME_SINCE_DETECTION));
	}

	/*
	 * TODO getters for read queries (with casts)
	 */
}
