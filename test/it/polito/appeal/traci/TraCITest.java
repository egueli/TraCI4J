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

import it.polito.appeal.traci.Vehicle.NotActiveException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TraCITest {

	private static final String SIM_CONFIG_LOCATION_PARAM = "sim.config.location";
	
	private SumoTraciConnection conn;
	
	static {
		BasicConfigurator.configure();
	}

	@Before
	public void setUp() throws Exception {
		try {
			String simConfigLocation = System.getProperty(SIM_CONFIG_LOCATION_PARAM);
			if (simConfigLocation == null)
				throw new IllegalArgumentException("please set "
						+ SIM_CONFIG_LOCATION_PARAM
						+ " to the path of a SUMO config file");

			conn = new SumoTraciConnection(simConfigLocation, 0, false);


			conn.runServer();
		}
		catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@After
	public void tearDown() throws IOException, InterruptedException {
		if (conn != null)
			conn.close();
	}
	
	// TODO either add assertXXX() to most of these tests, or delete them. 
	
	@Test
	public void testFirstStepIsZero() {
		assertEquals(0, conn.getCurrentSimStep());
	}
	
	@Test
	public void testNextSimStepGoesToOne() throws IOException {
		conn.nextSimStep();
		assertEquals(1, conn.getCurrentSimStep());
	}
	
	@Test
	public void testStrings() throws IOException, NotActiveException {
		for (int t=0; t<100; t++) {
			conn.nextSimStep();
		}
		
		Set<String> vehicles = conn.getActiveVehicles();
		Map<String, List<String>> dups = new HashMap<String, List<String>>();
		
		for (String id : vehicles) {
			String edge = conn.getVehicle(id).queryCurrentEdge();
			List<String> dupList;
			if (!dups.containsKey(edge)) {
				dupList = new ArrayList<String>();
				dups.put(edge, dupList);
			}
			else
				dupList = dups.get(edge);
			dupList.add(edge);
		}
		
		for (Map.Entry<String, List<String>> entry : dups.entrySet()) {
			System.out.println(entry.getKey() + ":");
			for (String dup : entry.getValue()) {
				System.out.println("\t" + dup);
			}
		}
	}
	
	@Test
	public void testFastStoragePerformance() throws IOException {
		System.out.println("Start reading edges");

		double startTime = System.currentTimeMillis();
		conn.setReadInternalLinks(true);
		conn.queryRoads();
		double endTime = System.currentTimeMillis();
		
		System.out.println("End reading edges");
		System.out.println("Time elapsed: " + (endTime-startTime)/1000 + " s");
	}

	private String firstVehicleID = null;
	
	@Test
	public void testRerouting()
			throws IOException, NotActiveException {
		
		
		getFirstVehicleID();
		
		Vehicle v0 = conn.getVehicle(firstVehicleID);
		
		List<String> routeBefore = v0.getCurrentRoute();
		System.out.println("Route before:         " + routeBefore);

		String edgeToSlowDown = routeBefore.get(5);
		v0.changeRoute(edgeToSlowDown, 10000);
		
		System.out.println("Route after:          " + v0.getCurrentRoute());
		conn.nextSimStep();
		System.out.println("Route after sim step: " + v0.getCurrentRoute());
	}

	@Test
	public void getFirstVehicleID()
			throws IOException {
		conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {
			@Override public void vehicleCreated(String id) {
				firstVehicleID = id;
			}
			@Override public void vehicleDestroyed(String id) {
			}
		});
		
		while(firstVehicleID == null)
			conn.nextSimStep();
	}

	@Test
	public void testChangeGlobalTravelTime()
			throws IOException, NotActiveException {

		getFirstVehicleID();
		Vehicle v0 = conn.getVehicle(firstVehicleID);
		
		List<String> routeBefore = v0.getCurrentRoute();
		System.out.println("Route before:         " + routeBefore);

		String edgeToSlowDown = routeBefore.get(5);
		conn.changeEdgeTravelTime(0, 1000, edgeToSlowDown, 50.0f);
		float newTravelTime = conn.getEdgeTravelTime(edgeToSlowDown);
		System.out.println("New travel time: " + newTravelTime);

		v0.reroute();

		String prevEdge = null;
		while(true) {
			String edge = v0.queryCurrentEdge();
			System.out.println("Route before:         " + v0.getCurrentRoute());
			if (prevEdge != null && !prevEdge.equals(edge)) {
				System.out.println("Now walking on " + edge);
			}
			prevEdge = edge;
			conn.nextSimStep();
		}
	}
}
