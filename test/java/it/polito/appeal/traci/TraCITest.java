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

import static org.junit.Assert.*;
import it.polito.appeal.traci.Vehicle.NotActiveException;
import it.polito.appeal.traci.protocol.BoundingBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for TraCI4J.
 * <p>
 * To run tests, please set the following system variable:
 * <dl>
 * <dt>it.polito.appeal.traci.sumo_exe</dt>
 * <dd>set this to the SUMO executable, e.g. &lt;SUMO_BASE&gt;/bin/sumo; see
 * {@link SumoTraciConnection#SUMO_EXE_PROPERTY}</dd>
 * </dl>
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * 
 */
public class TraCITest {

	private static final String SIM_CONFIG_LOCATION = "test/sumo_maps/variable_speed_signs/test.sumo.cfg";
	private SumoTraciConnection conn;
	
	static {
		// this need to be done only once
		BasicConfigurator.configure();
	}

	@Before
	public void setUp() throws Exception {
		System.out.println();
		System.out.println("=======================================");
		System.out.println();

		try {
			conn = new SumoTraciConnection(SIM_CONFIG_LOCATION, 0, false);


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

	/**
	 * The sim step at startup must be zero.
	 */
	@Test
	public void testFirstStepIsZero() {
		assertEquals(0, conn.getCurrentSimStep());
	}
	
	/**
	 * Calling {@link SumoTraciConnection#nextSimStep()} should move to
	 * simulation step one.
	 * @throws IOException
	 */
	@Test
	public void testNextSimStepGoesToOne() throws IOException {
		conn.nextSimStep();
		assertEquals(1, conn.getCurrentSimStep());
	}
	
	@Test
	public void testGetSubscriptionResponses() throws IOException {
		final List<String> departed = new ArrayList<String>();
		
		conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {
			@Override
			public void vehicleDestroyed(String id) {
			}
			
			@Override
			public void vehicleCreated(String id) {
				departed.add(id);
			}
		});
		conn.nextSimStep();
		conn.nextSimStep();
		conn.nextSimStep();
		
		assertFalse(departed.isEmpty());
	}
	
	private String firstVehicleID = null;
	
	@Test
	public void testRerouting()
			throws IOException, NotActiveException {
		
		
		getFirstVehicleID();
		
		Vehicle v0 = conn.getVehicle(firstVehicleID);
		
		List<String> routeBefore = v0.getCurrentRoute();
		System.out.println("Route before:         " + routeBefore);

		String edgeToSlowDown = "middle";
		v0.changeRoute(edgeToSlowDown, 10000);
		
		List<String> routeAfter = v0.getCurrentRoute();
		System.out.println("Route after:          " + routeAfter);
		
		assertFalse(routeBefore.equals(routeAfter));
	}

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

		String edgeToSlowDown = "middle";
		conn.changeEdgeTravelTime(0, 1000, edgeToSlowDown, 10000);
		float newTravelTime = conn.getEdgeTravelTime(edgeToSlowDown);
		assertEquals(10000, newTravelTime, 1e-6);

		v0.reroute();

		List<String> routeAfter = v0.getCurrentRoute();
		System.out.println("Route after:          " + routeAfter);

		assertFalse(routeBefore.equals(routeAfter));
	}
	
	@Test
	public void testQueryBounds() throws IOException {
		BoundingBox bounds = conn.queryBounds();
		assertEquals(0.0, bounds.getMinX(), 1e-6);
		assertEquals(-1.65, bounds.getMinY(), 1e-6);
		assertEquals(2500.0, bounds.getMaxX(), 1e-6);
		assertEquals(498.35, bounds.getMaxY(), 1e-6);
	}
	
	@Test
	public void testCloseInObserverBody() throws IOException {
		conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {
			@Override public void vehicleDestroyed(String id) {
				try {
					conn.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			@Override public void vehicleCreated(String id) { }
		});
		getFirstVehicleID();
	}
	
	@Test
	public void testWhoDepartsArrives() throws IOException {
		
		final Set<String> traveling = new HashSet<String>();
		
		conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {
			
			@Override
			public void vehicleDestroyed(String id) {
				assertTrue(traveling.contains(id));
				traveling.remove(id);
				if (traveling.isEmpty()) {
					try {
						conn.close();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
			
			@Override
			public void vehicleCreated(String id) {
				assertFalse(traveling.contains(id));
				traveling.add(id);
			}
		});
		
		while(!conn.isClosed()) {
			conn.nextSimStep();
			System.out.println(conn.getCurrentSimStep());
		}
			
	}

	@Test
	public void testChangeTarget() throws IOException {
		getFirstVehicleID();
		Vehicle v = conn.getVehicle(firstVehicleID);
		try {
			v.changeTarget("end");
			
			String lastEdge = null;
			while (v.isAlive()) {
				lastEdge = v.queryCurrentEdge();
				assertFalse(lastEdge.equals("end"));

				conn.nextSimStep();
			}
			
		} catch (NotActiveException e) {
			throw new RuntimeException("should never happen");
		}
	}
	
	@Test
	public void testChangeTargetAlsoAffectsRouteList() throws IOException, NotActiveException {
		getFirstVehicleID();
		Vehicle v = conn.getVehicle(firstVehicleID);
		v.changeTarget("end");
		List<String> route = v.getCurrentRoute();
		assertEquals("end", route.get(route.size()-1));
	}
}
