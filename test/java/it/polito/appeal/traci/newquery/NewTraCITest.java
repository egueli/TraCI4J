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

package it.polito.appeal.traci.newquery;

import static org.junit.Assert.*;
import it.polito.appeal.traci.TraCITest;
import it.polito.appeal.traci.Vehicle.NotActiveException;
import it.polito.appeal.traci.newquery.Edge.ChangeGlobalTravelTimeQuery;
import it.polito.appeal.traci.newquery.Edge.ReadGlobalTravelTimeQuery;
import it.polito.appeal.traci.newquery.Lane.Link;
import it.polito.appeal.traci.newquery.Vehicle.ChangeEdgeTravelTimeQuery;
import it.polito.appeal.traci.newquery.Vehicle.ChangeRouteQuery;
import it.polito.appeal.traci.newquery.Vehicle.ChangeTargetQuery;

import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test case for TraCI4J. This is similar to {@link TraCITest}, but uses new
 * API instead. Many tests have the same name and do the same things, therefore
 * they can be used as a usage comparison between old and new APIs.
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
public class NewTraCITest {

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
	
	@Ignore
	public void ignore() {
		// do nothing, it's here just to avoid an unused import warning
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
		final List<Vehicle> departed = new ArrayList<Vehicle>();
		
		conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {
			@Override
			public void vehicleArrived(Vehicle v) { }
			
			@Override
			public void vehicleDeparted(Vehicle v) {
				departed.add(v);
			}

			@Override
			public void vehicleTeleportEnding(Vehicle v) { }

			@Override
			public void vehicleTeleportStarting(Vehicle v) { }
		});
		conn.nextSimStep();
		conn.nextSimStep();
		conn.nextSimStep();
		
		assertFalse(departed.isEmpty());
	}

	@Test
	public void testVehicleSet() throws IllegalStateException, IOException {
		for (int i=0; i<10; i++) {
			conn.nextSimStep();
			int t = conn.getCurrentSimStep();
			Collection<Vehicle> vehicles = conn.getVehicles();
			System.out.println(t + "\t" + vehicles);
			assertTrue(vehicles.size() > 0);
		}
	}
	
	@Test
	public void testRefreshedValues() throws IllegalStateException, IOException {
		conn.nextSimStep();
		Vehicle v = conn.getVehicles().iterator().next();
		ValueReadQuery<Double> readSpeedQuery = v.queryReadSpeed();
		Double speedFirst = readSpeedQuery.get();
		
		for (int i=0; i<10; i++) {
			conn.nextSimStep();
			Double speedNow = readSpeedQuery.get();
			System.out.println(speedNow);
			assertTrue(Math.abs(speedFirst - speedNow) > DELTA);
		}
	}

	
	private Vehicle firstVehicle = null;
	
	@Test
	public void testRoute() throws IOException {
		getFirstVehicleID();
		
		ValueReadQuery<List<Edge>> routeQuery = firstVehicle.queryReadRoute();
		List<Edge> route = routeQuery.get();
		assertEquals(4, route.size());

		Iterator<Edge> it = route.iterator();
		assertEquals("beg",    it.next().getID());
		assertEquals("middle", it.next().getID());
		assertEquals("end",    it.next().getID());
		assertEquals("rend",   it.next().getID());
	}
	
	@Test
	public void testRerouting()
			throws IOException, NotActiveException {
		
		
		getFirstVehicleID();
		
		ValueReadQuery<List<Edge>> routeQuery = firstVehicle.queryReadRoute();
		
		List<Edge> routeBefore = routeQuery.get();
		System.out.println("Route before:         " + routeBefore);

		String edgeID = "middle";
		Edge edge = conn.getEdgeByID(edgeID);
		ChangeEdgeTravelTimeQuery settq = firstVehicle.querySetEdgeTravelTime();
		settq.setEdge(edge);
		settq.setTravelTime(10000);
		settq.run();
		
		firstVehicle.queryReroute().run();
		
		List<Edge> routeAfter = routeQuery.get();
		System.out.println("Route after:          " + routeAfter);
		
		assertFalse(routeBefore.equals(routeAfter));
	}

	public void getFirstVehicleID()
			throws IOException {
		conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {
			@Override public void vehicleDeparted(Vehicle v) {
				firstVehicle = v;
			}
			@Override public void vehicleArrived(Vehicle v) { }
			
			@Override
			public void vehicleTeleportEnding(Vehicle v) { }
			@Override
			public void vehicleTeleportStarting(Vehicle v) { }
		});
		
		while(firstVehicle == null)
			conn.nextSimStep();
	}

	@Test
	public void testChangeGlobalTravelTime()
			throws IOException, NotActiveException {

		getFirstVehicleID();
		
		ValueReadQuery<List<Edge>> routeQuery = firstVehicle.queryReadRoute();
		List<Edge> routeBefore = routeQuery.get();
		System.out.println("Route before:         " + routeBefore);

		String edgeID = "middle";
		Edge edge = conn.getEdgeByID(edgeID);
		ChangeGlobalTravelTimeQuery cttq = edge.queryChangeTravelTime();
		cttq.setBeginTime(0);
		cttq.setEndTime(1000);
		cttq.setTravelTime(10000);
		cttq.run();
		
		ReadGlobalTravelTimeQuery rgttq = edge.queryReadGlobalTravelTime();
		rgttq.setTime(conn.getCurrentSimStep());
		double newTravelTime = rgttq.get();
		assertEquals(10000, newTravelTime, DELTA);

		firstVehicle.queryReroute().run();

		List<Edge> routeAfter = routeQuery.get();
		System.out.println("Route after:          " + routeAfter);

		assertFalse(routeBefore.equals(routeAfter));
	}
	
	@Test
	public void testGetShape() throws IOException {
		Lane lane = conn.getLaneByID("beg_0");
		PathIterator it = lane.queryReadShape().get().getPathIterator(null);
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
	public void testGetBelongingEdge() throws IOException {
		Lane lane = conn.getLaneByID("beg_0");
		Edge edge = lane.queryReadParentEdge().get();
		assertEquals("beg", edge.getID());
	}
	
	@Test
	@Ignore // its duration may be annoying; feel free to comment this
	public void testMultiQueryPerformance() throws IllegalStateException, IOException {
		final int RETRIES = 5;
		
		while (conn.getCurrentSimStep() < 300)
			conn.nextSimStep();
		
		Collection<Vehicle> vehicles = conn.getVehicles();
		
		long start = System.currentTimeMillis();
		for (int r = 0; r < RETRIES; r++) {
			for (Vehicle vehicle : vehicles) {
				vehicle.queryReadPosition().get();
			}
			conn.nextSimStep();
		}
		long elapsedSingle = System.currentTimeMillis() - start;
		System.out.println("Individual queries: " + elapsedSingle + " ms");
		
		conn.nextSimStep(); // to clear already read values
		
		start = System.currentTimeMillis();
		for (int r = 0; r < RETRIES; r++) {
			MultiQuery multi = conn.makeMultiQuery();
			for (Vehicle vehicle : vehicles) {
				multi.add(vehicle.queryReadPosition());
			}
			multi.run();
			conn.nextSimStep();
		}
		long elapsedMulti = System.currentTimeMillis() - start;
		System.out.println("MultiQuery queries: " + elapsedMulti + " ms");
		
		assertTrue(elapsedMulti < elapsedSingle);
	}
	
	@Test
	public void testQueryBounds() throws IOException {
		Rectangle2D bounds = conn.getSimValues().queryNetBoundaries().get();
//		assertEquals(0.0, bounds.getMinX(), DELTA);
//		assertEquals(-1.65, bounds.getMinY(), DELTA);
//		assertEquals(2500.0, bounds.getMaxX(), DELTA);
//		assertEquals(498.35, bounds.getMaxY(), DELTA);
		assertEquals(0.0, bounds.getMinX(), DELTA);
		assertEquals(0.0, bounds.getMinY(), DELTA);
		assertEquals(2500.0, bounds.getMaxX(), DELTA);
		assertEquals(500.0, bounds.getMaxY(), DELTA);	}
	
	@Test
	public void testCloseInObserverBody() throws IOException {
		conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {
			@Override public void vehicleArrived(Vehicle v) {
			}
			@Override public void vehicleDeparted(Vehicle v) {
				try {
					conn.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			@Override
			public void vehicleTeleportEnding(Vehicle v) { }
			@Override
			public void vehicleTeleportStarting(Vehicle v) { }
		});
		getFirstVehicleID();
	}
	
	@Test
	@Ignore // its duration may be annoying; feel free to comment this
	public void testWhoDepartsArrives() throws IOException {
		
		final Set<Vehicle> traveling = new HashSet<Vehicle>();
		
		conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {
			
			@Override
			public void vehicleArrived(Vehicle v) {
				assertTrue(traveling.contains(v));
				traveling.remove(v);
				if (traveling.isEmpty()) {
					try {
						conn.close();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
			
			@Override
			public void vehicleDeparted(Vehicle v) {
				assertFalse(traveling.contains(v));
				traveling.add(v);
			}

			@Override
			public void vehicleTeleportEnding(Vehicle v) { }

			@Override
			public void vehicleTeleportStarting(Vehicle v) { }
		});
		
		while(!conn.isClosed()) {
			conn.nextSimStep();
			System.out.println(conn.getCurrentSimStep());
		}
			
	}

	@Test
	public void testChangeTarget() throws IOException {
		getFirstVehicleID();
		Vehicle v = firstVehicle;

		ChangeTargetQuery ctq = v.queryChangeTarget();
		ctq.setNewTarget(conn.getEdgeByID("end"));
		ctq.run();
		
		Edge lastEdge = null;
		while (conn.getVehicles().contains(v)) {
			lastEdge = v.queryReadCurrentEdge().get();
			assertFalse(lastEdge.getID().equals("end"));

			conn.nextSimStep();
		}
	}
	
	@Test
	public void testChangeTargetAlsoAffectsRouteList() throws IOException {
		getFirstVehicleID();
		Vehicle v = firstVehicle;
		ChangeTargetQuery ctq = v.queryChangeTarget();
		ctq.setNewTarget(conn.getEdgeByID("end"));
		ctq.run();
		List<Edge> route = v.queryReadRoute().get();
		assertEquals("end", route.get(route.size()-1).getID());
	}
	
	@Test
	public void testChangeRoute() throws IOException, NotActiveException {
		getFirstVehicleID();
		Vehicle v = firstVehicle;
		List<Edge> newRoute = new ArrayList<Edge>();
		newRoute.add(conn.getEdgeByID("beg"));
		newRoute.add(conn.getEdgeByID("beg2left"));
		newRoute.add(conn.getEdgeByID("left"));
		newRoute.add(conn.getEdgeByID("left2end"));
		ChangeRouteQuery crq = v.queryChangeRoute();
		crq.setNewRoute(newRoute);
		crq.run();
		assertEquals(newRoute, v.queryReadRoute().get());
	}
	
	@Test
	public void testLaneLinks() throws IOException {
		Lane begLane = conn.getLaneByID("beg_0");
		List<Link> links = begLane.queryReadLinks().get();
		Set<String> linkIDs = new HashSet<String>();
		Set<String> intLinkIDs = new HashSet<String>();
		for (Link link : links) {
			linkIDs.add(link.getNextNonInternalLane().getID());
			intLinkIDs.add(link.getNextInternalLane().getID());
		}
		
		assertEquals(2, linkIDs.size());
		assertTrue(linkIDs.contains("middle_0"));
		assertTrue(intLinkIDs.contains(":beg_0_0"));
		assertTrue(linkIDs.contains("beg2left_0"));
		assertTrue(intLinkIDs.contains(":beg_1_0"));
	}
}
