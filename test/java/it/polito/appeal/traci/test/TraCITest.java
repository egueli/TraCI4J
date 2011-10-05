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

package it.polito.appeal.traci.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import it.polito.appeal.traci.ChangeEdgeTravelTimeQuery;
import it.polito.appeal.traci.ChangeObjectVarQuery.ChangeStringQ;
import it.polito.appeal.traci.ChangeGlobalTravelTimeQuery;
import it.polito.appeal.traci.ChangeRouteQuery;
import it.polito.appeal.traci.ChangeTargetQuery;
import it.polito.appeal.traci.Edge;
import it.polito.appeal.traci.Lane;
import it.polito.appeal.traci.MultiQuery;
import it.polito.appeal.traci.POI;
import it.polito.appeal.traci.ReadGlobalTravelTimeQuery;
import it.polito.appeal.traci.POI.ChangeColorQuery;
import it.polito.appeal.traci.POI.ChangePositionQuery;
import it.polito.appeal.traci.Repository;
import it.polito.appeal.traci.SumoTraciConnection;
import it.polito.appeal.traci.ValueReadQuery;
import it.polito.appeal.traci.Vehicle;
import it.polito.appeal.traci.VehicleLifecycleObserver;
import it.polito.appeal.traci.Lane.Link;

import java.awt.Color;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
public class TraCITest {

	protected static final double DELTA = 1e-6;
	private static final String SIM_CONFIG_LOCATION = "test/sumo_maps/variable_speed_signs/test.sumo.cfg";
	protected SumoTraciConnection conn;
	
	static {
		// this need to be done only once
		BasicConfigurator.configure();
	}

	@Before
	public void setUp() throws Exception {
		printSeparator();
		conn = startSumoConn(SIM_CONFIG_LOCATION);
	}

	public static SumoTraciConnection startSumoConn(String simConfigLocation) throws Exception {
		try {
			SumoTraciConnection newConn = new SumoTraciConnection(simConfigLocation, 0, false);
			newConn.runServer();
			return newConn;
		}
		catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static void printSeparator() {
		System.out.println();
		System.out.println("=======================================");
		System.out.println();
	}

	@After
	public void tearDown() throws IOException, InterruptedException {
		stopSumoConn(conn);
	}

	public static void stopSumoConn(SumoTraciConnection conn) throws IOException, InterruptedException {
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
			Map<String, Vehicle> vehicles = conn.getVehicleRepository().getAll();
			System.out.println(t + "\t" + vehicles.keySet());
			assertTrue(vehicles.size() > 0);
		}
	}
	
	@Test
	public void testRefreshedValues() throws IllegalStateException, IOException {
		conn.nextSimStep();
		Vehicle v = conn.getVehicleRepository().getAll().values().iterator().next();
		ValueReadQuery<Double> readSpeedQuery = v.queryReadSpeed();
		Double speedFirst = readSpeedQuery.get();
		
		for (int i=0; i<10; i++) {
			conn.nextSimStep();
			Double speedNow = readSpeedQuery.get();
			System.out.println(speedNow);
			assertTrue(Math.abs(speedFirst - speedNow) > DELTA);
		}
	}

	@Test
	public void testNoVehiclesAtStepZero() throws IOException {
		assertTrue(conn.getVehicleRepository().getIDs().isEmpty());
	}
	
	@Test
	public void testOneVehicleAtStepOne() throws IOException {
		conn.nextSimStep();
		final Repository<Vehicle> repo = conn.getVehicleRepository();
		assertThat(repo.getIDs().size(), equalTo(1));
	}
	
	@Test
	public void testVehicleIDAtStepOne() throws IOException {
		conn.nextSimStep();
		final Repository<Vehicle> repo = conn.getVehicleRepository();
		assertThat(repo.getIDs(), equalTo(Collections.singleton("0.0")));	
	}
	
	@Test
	public void testVehiclePositionAtStepOne() throws IOException {
		conn.nextSimStep();
		final Repository<Vehicle> repo = conn.getVehicleRepository();
		Vehicle v0 = repo.getByID("0.0");
		assertEquals(0, v0.queryReadLanePosition().get(), DELTA);
	}
	
	@Test
	public void testVehiclePositionAtStepTwo() throws IOException {
		conn.nextSimStep();
		conn.nextSimStep();
		final Repository<Vehicle> repo = conn.getVehicleRepository();
		Vehicle v0 = repo.getByID("0.0");
		assertEquals(1.886542, v0.queryReadLanePosition().get(), DELTA);
	}
	
	private Vehicle firstVehicle = null;
	
	@Test
	public void testRoute() throws IOException {
		getFirstVehicle();
		
		ValueReadQuery<List<Edge>> routeQuery = firstVehicle.queryReadCurrentRoute();
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
			throws IOException {
		
		
		getFirstVehicle();
		
		ValueReadQuery<List<Edge>> routeQuery = firstVehicle.queryReadCurrentRoute();
		
		List<Edge> routeBefore = routeQuery.get();
		System.out.println("Route before:         " + routeBefore);

		String edgeID = "middle";
		Edge edge = conn.getEdgeRepository().getByID(edgeID);
		ChangeEdgeTravelTimeQuery settq = firstVehicle.querySetEdgeTravelTime();
		settq.setEdge(edge);
		settq.setTravelTime(10000);
		settq.run();
		
		firstVehicle.queryReroute().run();
		
		List<Edge> routeAfter = routeQuery.get();
		System.out.println("Route after:          " + routeAfter);
		
		assertFalse(routeBefore.equals(routeAfter));
	}

	public void getFirstVehicle() throws IOException {
		Repository<Vehicle> repo = conn.getVehicleRepository();
		while(repo.getAll().isEmpty())
			conn.nextSimStep();
		
		firstVehicle = repo.getAll().values().iterator().next();
	}

	@Test
	public void testChangeGlobalTravelTime()
			throws IOException {

		getFirstVehicle();
		
		ValueReadQuery<List<Edge>> routeQuery = firstVehicle.queryReadCurrentRoute();
		List<Edge> routeBefore = routeQuery.get();
		System.out.println("Route before:         " + routeBefore);

		String edgeID = "middle";
		Edge edge = conn.getEdgeRepository().getByID(edgeID);
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
		Lane lane = conn.getLaneRepository().getByID("beg_0");
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
		Lane lane = conn.getLaneRepository().getByID("beg_0");
		Edge edge = lane.queryReadParentEdge().get();
		assertEquals("beg", edge.getID());
	}
	
	@Test
//	@Ignore // its duration may be annoying; feel free to comment this
	public void testMultiQueryPerformance() throws IllegalStateException, IOException {
		final int RETRIES = 5;
		
		while (conn.getCurrentSimStep() < 300)
			conn.nextSimStep();
		
		
		long start = System.currentTimeMillis();
		for (int r = 0; r < RETRIES; r++) {
			Map<String, Vehicle> vehicles = conn.getVehicleRepository().getAll();
			for (Vehicle vehicle : vehicles.values()) {
				vehicle.queryReadPosition().get();
			}
			conn.nextSimStep();
		}
		long elapsedSingle = System.currentTimeMillis() - start;
		System.out.println("Individual queries: " + elapsedSingle + " ms");
		
		conn.nextSimStep(); // to clear already read values
		
		start = System.currentTimeMillis();
		for (int r = 0; r < RETRIES; r++) {
			Map<String, Vehicle> vehicles = conn.getVehicleRepository().getAll();
			MultiQuery multi = conn.makeMultiQuery();
			for (Vehicle vehicle : vehicles.values()) {
				multi.add(vehicle.queryReadPosition());
			}
			multi.add(conn.getVehicleRepository().getQuery());
			multi.run();
			conn.nextSimStep();
		}
		long elapsedMulti = System.currentTimeMillis() - start;
		System.out.println("MultiQuery queries: " + elapsedMulti + " ms");
		
		assertTrue(elapsedMulti < elapsedSingle);
	}
	
	@Test
	public void testQueryBounds() throws IOException {
		Rectangle2D bounds = conn.getSimulationData().queryNetBoundaries().get();
//		assertEquals(0.0, bounds.getMinX(), DELTA);
//		assertEquals(-1.65, bounds.getMinY(), DELTA);
//		assertEquals(2500.0, bounds.getMaxX(), DELTA);
//		assertEquals(498.35, bounds.getMaxY(), DELTA);
		assertEquals(0.0, bounds.getMinX(), DELTA);
		assertEquals(0.0, bounds.getMinY(), DELTA);
		assertEquals(2500.0, bounds.getMaxX(), DELTA);
		assertEquals(500.0, bounds.getMaxY(), DELTA);	}
	
	@Test
	public void testQueryRoads() throws IOException, InterruptedException {
		Set<String> expectedLaneIDs = new HashSet<String>();
		expectedLaneIDs.add("beg_0");
		expectedLaneIDs.add(":beg_0_0");
		expectedLaneIDs.add(":beg_1_0");
		expectedLaneIDs.add("beg2left_0");
		expectedLaneIDs.add(":begleft_0_0");
		expectedLaneIDs.add("middle_0");
		expectedLaneIDs.add("left_0");
		expectedLaneIDs.add(":endleft_0_0");
		expectedLaneIDs.add("left2end_0");
		expectedLaneIDs.add(":end_0_0");
		expectedLaneIDs.add(":end_1_0");
		expectedLaneIDs.add("end_0");
		expectedLaneIDs.add(":absEnd_0_0");
		expectedLaneIDs.add("rend_0");
		
		
		Collection<Lane> lanes = conn.getLaneRepository().getAll().values();
		Set<String> laneIDs = new HashSet<String>();
		for (Lane lane : lanes) {
			laneIDs.add(lane.getID());
		}
		
		assertEquals(expectedLaneIDs, laneIDs);
	}
	
	@Test
//	@Ignore // its duration may be annoying; feel free to comment this
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
		getFirstVehicle();
		Vehicle v = firstVehicle;

		ChangeTargetQuery ctq = v.queryChangeTarget();
		ctq.setValue(conn.getEdgeRepository().getByID("end"));
		ctq.run();
		
		Edge lastEdge = null;
		while (conn.getVehicleRepository().getByID(v.getID()) != null) {
			lastEdge = v.queryReadCurrentEdge().get();
			assertFalse(lastEdge.getID().equals("end"));

			conn.nextSimStep();
		}
	}
	
	@Test
	public void testChangeTargetAlsoAffectsRouteList() throws IOException {
		getFirstVehicle();
		Vehicle v = firstVehicle;
		ChangeTargetQuery ctq = v.queryChangeTarget();
		ctq.setValue(conn.getEdgeRepository().getByID("end"));
		ctq.run();
		List<Edge> route = v.queryReadCurrentRoute().get();
		assertEquals("end", route.get(route.size()-1).getID());
	}
	
	@Test
	public void testChangeRoute() throws IOException {
		getFirstVehicle();
		Vehicle v = firstVehicle;
		List<Edge> newRoute = new ArrayList<Edge>();
		newRoute.add(conn.getEdgeRepository().getByID("beg"));
		newRoute.add(conn.getEdgeRepository().getByID("beg2left"));
		newRoute.add(conn.getEdgeRepository().getByID("left"));
		newRoute.add(conn.getEdgeRepository().getByID("left2end"));
		ChangeRouteQuery crq = v.queryChangeRoute();
		crq.setValue(newRoute);
		crq.run();
		assertEquals(newRoute, v.queryReadCurrentRoute().get());
	}
	
	@Test
	public void testLaneLinks() throws IOException {
		Lane begLane = conn.getLaneRepository().getByID("beg_0");
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

	@Test
	public void testVehiclePositionIsInBounds() throws IOException {
		getFirstVehicle();
		final ValueReadQuery<Point2D> queryReadPosition = firstVehicle.queryReadPosition();
		while (conn.getVehicleRepository().getByID(firstVehicle.getID()) != null) {
			Point2D pos = queryReadPosition.get();
			assertTrue(pos.getX() >= 0);
			assertTrue(pos.getX() < 2000);
			assertEquals(-1.65, pos.getY(), DELTA);
			conn.nextSimStep();
		}
	}
	
	@Test
	public void testUsingInactiveVehicle() throws IOException {
		getFirstVehicle();
		conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {
			@Override
			public void vehicleTeleportStarting(Vehicle vehicle) {}
			@Override
			public void vehicleTeleportEnding(Vehicle vehicle) { }
			@Override
			public void vehicleDeparted(Vehicle vehicle) { }
			@Override
			public void vehicleArrived(Vehicle vehicle) {
				if (vehicle.equals(firstVehicle)) {
					try {
						System.out.println(firstVehicle.queryReadPosition().get());
						fail("it should throw an exception");
					} catch (IOException e) {
					}
				}
			}
		});
		
		for (int t=0; t<500; t++) {
			conn.nextSimStep();
		}
	}
	
	@Test
	public void testPOIExistence() throws IOException {
		Repository<POI> poiRepo = conn.getPOIRepository();
		assertNotNull(poiRepo.getByID("0"));
	}
	
	@Test
	public void testPOIType() throws IOException {
		Repository<POI> poiRepo = conn.getPOIRepository();
		POI poi = poiRepo.getByID("0");
		assertEquals("TEST_TYPE", poi.getReadTypeQuery().get());
	}
	
	@Test
	public void testPOIColor() throws IOException {
		Repository<POI> poiRepo = conn.getPOIRepository();
		POI poi = poiRepo.getByID("0");
		Color c = new Color(255, 128, 0);
		assertEquals(c, poi.getReadColorQuery().get());
	}
	
	@Test
	public void testPOIPosition() throws IOException {
		Repository<POI> poiRepo = conn.getPOIRepository();
		POI poi = poiRepo.getByID("0");
		Point2D pos = new Point2D.Double(100, 50);
		Point2D poiPos = poi.getReadPositionQuery().get();
		assertEquals(pos.getX(), poiPos.getX(), DELTA);
		assertEquals(pos.getY(), poiPos.getY(), DELTA);
	}
	
	@Test
	public void testSetPOIType() throws IOException {
		Repository<POI> poiRepo = conn.getPOIRepository();
		POI poi = poiRepo.getByID("0");
		ChangeStringQ q = poi.getChangeTypeQuery();
		final String newType = "NEW_TYPE";
		q.setValue(newType);
		q.run();
		assertEquals(newType, poi.getReadTypeQuery().get());
	}
	
	@Test
	public void testSetPOIPosition() throws IOException {
		Repository<POI> poiRepo = conn.getPOIRepository();
		POI poi = poiRepo.getByID("0");
		final Point2D newPos = new Point2D.Double(0, 0);
		ChangePositionQuery q = poi.getChangePositionQuery();
		q.setValue(newPos);
		q.run();
		final Point2D pos = poi.getReadPositionQuery().get();
		assertEquals(newPos.getX(), pos.getX(), DELTA);
		assertEquals(newPos.getY(), pos.getY(), DELTA);
	}
	
	@Test
	public void testSetPOIColor() throws IOException {
		Repository<POI> poiRepo = conn.getPOIRepository();
		POI poi = poiRepo.getByID("0");
		final Color newColor = Color.cyan;
		ChangeColorQuery q = poi.getChangeColorQuery();
		q.setValue(newColor);
		q.run();
		assertEquals(newColor, poi.getReadColorQuery().get());
	}
	
	// TODO add induction loop tests
}
