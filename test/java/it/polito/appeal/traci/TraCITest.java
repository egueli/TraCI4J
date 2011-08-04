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

import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
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

	private static final double DELTA = 1e-6;
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
			public void vehicleArrived(String id) { }
			
			@Override
			public void vehicleDeparted(String id) {
				departed.add(id);
			}

			@Override
			public void vehicleTeleportEnding(String id) { }

			@Override
			public void vehicleTeleportStarting(String id) { }
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
		v0.setEdgeTravelTime(edgeToSlowDown, 10000);
		
		List<String> routeAfter = v0.getCurrentRoute();
		System.out.println("Route after:          " + routeAfter);
		
		assertFalse(routeBefore.equals(routeAfter));
	}

	public void getFirstVehicleID()
			throws IOException {
		conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {
			@Override public void vehicleDeparted(String id) {
				firstVehicleID = id;
			}
			@Override public void vehicleArrived(String id) { }
			
			@Override
			public void vehicleTeleportEnding(String id) { }
			@Override
			public void vehicleTeleportStarting(String id) { }
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
		double newTravelTime = conn.getEdgeTravelTime(edgeToSlowDown);
		assertEquals(10000, newTravelTime, DELTA);

		v0.reroute();

		List<String> routeAfter = v0.getCurrentRoute();
		System.out.println("Route after:          " + routeAfter);

		assertFalse(routeBefore.equals(routeAfter));
	}
	
	@Test
	public void testGetShape() throws IOException {
		Lane r = conn.getLane("beg_0");
		PathIterator it = r.shape.getPathIterator(null);
		assertFalse(it.isDone());
		double[] coords = new double[2];
		assertEquals(PathIterator.SEG_MOVETO, it.currentSegment(coords));
		assertEquals(  0   , coords[0], DELTA);
		assertEquals( -1.65, coords[1], DELTA);
		it.next();
		assertEquals(PathIterator.SEG_LINETO, it.currentSegment(coords));
		assertEquals(498.55, coords[0], DELTA);
		assertEquals( -1.65, coords[1], DELTA);
		it.next();
		assertTrue(it.isDone());
	}
	
	@Test
	public void testQueryBounds() throws IOException {
		Rectangle2D bounds = conn.queryBounds();
		assertEquals(0.0, bounds.getMinX(), DELTA);
		assertEquals(-1.65, bounds.getMinY(), DELTA);
		assertEquals(2500.0, bounds.getMaxX(), DELTA);
		assertEquals(498.35, bounds.getMaxY(), DELTA);
	}
	
	@Test
	public void testCloseInObserverBody() throws IOException {
		conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {
			@Override public void vehicleArrived(String id) {
				try {
					conn.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			@Override public void vehicleDeparted(String id) { }
			@Override
			public void vehicleTeleportEnding(String id) { }
			@Override
			public void vehicleTeleportStarting(String id) { }
		});
		getFirstVehicleID();
	}
	
	@Test
	public void testWhoDepartsArrives() throws IOException {
		
		final Set<String> traveling = new HashSet<String>();
		
		conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {
			
			@Override
			public void vehicleArrived(String id) {
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
			public void vehicleDeparted(String id) {
				assertFalse(traveling.contains(id));
				traveling.add(id);
			}

			@Override
			public void vehicleTeleportEnding(String id) { }

			@Override
			public void vehicleTeleportStarting(String id) { }
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
	
	@Test
	public void testChangeRoute() throws IOException, NotActiveException {
		getFirstVehicleID();
		Vehicle v = conn.getVehicle(firstVehicleID);
		List<String> newRoute = new ArrayList<String>();
		newRoute.add("beg");
		newRoute.add("beg2left");
		newRoute.add("left");
		newRoute.add("left2end");
		v.changeRoute(newRoute);
		assertEquals(newRoute, v.getCurrentRoute());
	}
}
