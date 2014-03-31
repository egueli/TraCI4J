/*   
    Copyright (C) 2014 ApPeAL Group, Politecnico di Torino

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

package it.polito.appeal.traci.test;

import it.polito.appeal.traci.ReadObjectVarQuery;
import it.polito.appeal.traci.Vehicle;

import java.io.IOException;
import java.util.EnumMap;

import org.junit.Test;

public class ApiPerformanceTest extends SingleSimTraCITest {

	@Override
	protected String getSimConfigFileLocation() {
		return "test/sumo_maps/polito/test.sumo.cfg";
	}

	
	/**
	 * Verifies that the time taken to query a TraCI object, like the current
	 * edge of a vehicle, is not significatively slower than the time taken to
	 * query a simpler datatype (like the lane ID).
	 * 
	 * @throws IOException
	 */
	@Test
	public void testReadCurrentEdgeSpeed() throws IOException {
		getFirstVehicle();

		float readLaneIndexSpeed = getCallSpeed(Vehicle.Variable.LANE_INDEX);
		float readCurrentEdgeSpeed = getCallSpeed(Vehicle.Variable.CURRENT_EDGE);
		
		assert(readLaneIndexSpeed * 10 < readCurrentEdgeSpeed);
	}

	private final int N_READS = 1000;
	
	private float getCallSpeed(Vehicle.Variable var) throws IOException {
		ReadObjectVarQuery<?> q;
		q = firstVehicle.getAllReadQueries().get(var);

		long begin = System.currentTimeMillis();
		for (int i=0; i<N_READS; i++) {
			q.setObsolete();
			q.get();
		}
		long end = System.currentTimeMillis();
		
		float speed = (end - begin) / (float)N_READS;
		System.out.println("query speed for " + var + ": " + speed + "ms");
		return speed;
	}
}
