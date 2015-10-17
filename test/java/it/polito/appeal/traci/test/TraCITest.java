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

package it.polito.appeal.traci.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import it.polito.appeal.traci.AddRouteQuery;
import it.polito.appeal.traci.AddVehicleQuery;
import it.polito.appeal.traci.ChangeEdgeTravelTimeQuery;
import it.polito.appeal.traci.ChangeGlobalTravelTimeQuery;
import it.polito.appeal.traci.Edge;
import it.polito.appeal.traci.InductionLoop;
import it.polito.appeal.traci.LaArDetector;
import it.polito.appeal.traci.Lane;
import it.polito.appeal.traci.Link;
import it.polito.appeal.traci.MeMeDetector;
import it.polito.appeal.traci.MultiQuery;
import it.polito.appeal.traci.POI;
import it.polito.appeal.traci.ReadGlobalTravelTimeQuery;
import it.polito.appeal.traci.RemoveVehicleQuery;
import it.polito.appeal.traci.Repository;
import it.polito.appeal.traci.Route;
import it.polito.appeal.traci.SumoTraciConnection;
import it.polito.appeal.traci.Vehicle;
import it.polito.appeal.traci.VehicleLifecycleObserver;
import it.polito.appeal.traci.VehicleType;

/**
 * Main test case for TraCI4J. This class tries to test and describe all the
 * basic features of the library.
 * <p>
 * Each test is run on an existing SUMO simulation, initialized with the
 * configuration file specified in {@link #SIM_CONFIG_LOCATION}. The simulation
 * is reset for each test.
 * <p>
 * The tests assume that the SUMO binary directory is in the system PATH. If
 * not, please set the Java system variable
 * <code>it.polito.appeal.traci.sumo_exe</code> to the full path of the "sumo"
 * executable, e.g. <code>/usr/bin/sumo</code>; see
 * {@link SumoTraciConnection#SUMO_EXE_PROPERTY}</dd>
 * </dl>
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * 
 */
@SuppressWarnings("javadoc")
public class TraCITest extends SingleSimTraCITest {

	private static final Logger log = LogManager.getLogger();

	protected static final double DELTA = 1e-6;

	@Override
	protected String getSimConfigFileLocation() {
		return "test/resources/sumo_maps/variable_speed_signs/test.sumo.cfg";
	}

	@Before
	public void setUp() throws Exception {
		printSeparator();
	}

	public static void printSeparator() {
		log.info("=======================================");
	}

	@Ignore
	public void ignore() {
		// do nothing, it's here just to avoid an unused import warning
	}

	/**
	 * The sim step at startup must be zero (this should match the "begin"
	 * parameter in the SUMO configuration).
	 */
	@Test
	public void testFirstStepIsZero() {
		assertEquals(0, conn.getCurrentSimTime());
	}

	/**
	 * Calling {@link SumoTraciConnection#nextSimStep()} should move to
	 * simulation step one.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testNextSimStepGoesToOne() throws IOException {
		conn.nextSimStep();
		assertEquals(1000, conn.getCurrentSimTime());
	}

	/**
	 * This test shows how a vehicle lifecycle listener can be attached to the
	 * simulation, and how its callbacks are called by TraCI4J when something
	 * about a vehicle happens.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetSubscriptionResponses() throws IOException {
		final List<Vehicle> departed = new ArrayList<Vehicle>();

		conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {
			public void vehicleArrived(Vehicle v) {
			}

			public void vehicleDeparted(Vehicle v) {
				departed.add(v);
			}

			public void vehicleTeleportEnding(Vehicle v) {
			}

			public void vehicleTeleportStarting(Vehicle v) {
			}
		});

		// In this simulation, the first vehicles are been seen at step 2.
		conn.nextSimStep();
		conn.nextSimStep();
		conn.nextSimStep();

		assertFalse(departed.isEmpty());
	}

	/**
	 * This test shows a basic usage of
	 * {@link SumoTraciConnection#getVehicleRepository()}. The method
	 * {@link Repository#getAll()} will return all vehicles in the simulation.
	 * 
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@Test
	public void testVehicleSet() throws IllegalStateException, IOException {
		for (int i = 0; i < 10; i++) {
			conn.nextSimStep();
			int t = conn.getCurrentSimTime() / 1000;
			Map<String, Vehicle> vehicles = conn.getVehicleRepository().getAll();
			log.info(t + "\t" + vehicles.keySet());
			assertTrue(vehicles.size() > 0);
		}
	}

	/**
	 * This test shows how a TraCI object is updated; calling
	 * {@link Vehicle#getSpeed()} in two different simulation steps may give
	 * different results.
	 * 
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@Test
	public void testRefreshedValues() throws IllegalStateException, IOException {
		conn.nextSimStep();
		Vehicle v = conn.getVehicleRepository().getAll().values().iterator().next();
		Double speedFirst = v.getSpeed();

		for (int i = 0; i < 10; i++) {
			conn.nextSimStep();
			Double speedNow = v.getSpeed();
			log.info(speedNow.toString());
			assertTrue(Math.abs(speedFirst - speedNow) > DELTA);
		}
	}

	/**
	 * Normally, the simulation at step zero contains no vehicles.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testNoVehiclesAtStepZero() throws IOException {
		assertTrue(conn.getVehicleRepository().getIDs().isEmpty());
	}

	/**
	 * In this simulation, there should be exactly one vehicle at step one.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testOneVehicleAtStepOne() throws IOException {
		conn.nextSimStep();
		final Repository<Vehicle> repo = conn.getVehicleRepository();
		assertThat(repo.getIDs().size(), equalTo(1));
	}

	/**
	 * Tests that the vehicle's ID of the first vehicle is correct.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testVehicleIDAtStepOne() throws IOException {
		conn.nextSimStep();
		final Repository<Vehicle> repo = conn.getVehicleRepository();
		assertThat(repo.getIDs(), equalTo(Collections.singleton("0.0")));
	}

	/**
	 * Tests that the vehicle at step 1 is at beginning of its departure lane.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testVehiclePositionAtStepOne() throws IOException {
		conn.nextSimStep();
		final Repository<Vehicle> repo = conn.getVehicleRepository();
		Vehicle v0 = repo.getByID("0.0");
		assertEquals(0, v0.getLanePosition(), DELTA);
	}

	/**
	 * Tests that the vehicle at step 2 is about 1.9m at beginning of its
	 * departure lane. Note: this may change if SUMO's internal mobility model
	 * is changed.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testVehiclePositionAtStepTwo() throws IOException {
		conn.nextSimStep();
		conn.nextSimStep();
		final Repository<Vehicle> repo = conn.getVehicleRepository();
		Vehicle v0 = repo.getByID("0.0");
		assertEquals(1.886542, v0.getLanePosition(), DELTA);
	}

	private Vehicle firstVehicle = null;

	/**
	 * This test reads a vehicle's route and checks for its correctness. (they
	 * all have the same route)
	 * 
	 * @throws IOException
	 */
	@Test
	public void testRoute() throws IOException {
		getFirstVehicle();

		List<Edge> route = firstVehicle.getCurrentRoute();
		assertEquals(4, route.size());

		Iterator<Edge> it = route.iterator();
		assertEquals("beg", it.next().getID());
		assertEquals("middle", it.next().getID());
		assertEquals("end", it.next().getID());
		assertEquals("rend", it.next().getID());
	}

	/**
	 * This test increases the travel time of a road "perceived" by the first
	 * vehicle. This will make the vehicle look for an alternative route.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testRerouting() throws IOException {

		getFirstVehicle();

		List<Edge> routeBefore = firstVehicle.getCurrentRoute();
		log.info("Route before:         " + routeBefore);

		String edgeID = "middle";
		Edge edge = conn.getEdgeRepository().getByID(edgeID);
		ChangeEdgeTravelTimeQuery settq = firstVehicle.querySetEdgeTravelTime();
		settq.setEdge(edge);
		settq.setTravelTime(10000);
		settq.run();

		firstVehicle.queryReroute().run();

		List<Edge> routeAfter = firstVehicle.getCurrentRoute();
		log.info("Route after:          " + routeAfter);

		assertFalse(routeBefore.equals(routeAfter));
	}

	/**
	 * Returns the first vehicle entered in the simulation. Since all vehicles
	 * depart from the same road, and SUMO lets at most one vehicle to depart
	 * from a given road at each step, the vehicle returned from this function
	 * will always be the same.
	 * 
	 * @throws IOException
	 */
	public void getFirstVehicle() throws IOException {
		Repository<Vehicle> repo = conn.getVehicleRepository();
		while (repo.getAll().isEmpty())
			conn.nextSimStep();

		firstVehicle = repo.getAll().values().iterator().next();
	}

	/**
	 * This test increases the travel time of a road, checks that the new travel
	 * time is accepted by SUMO, and verifies that the first vehicle changed its
	 * route. In contrast with {@link #testRerouting()}, the specified travel
	 * time applies to all vehicles in the simulation.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testChangeGlobalTravelTime() throws IOException {

		getFirstVehicle();

		List<Edge> routeBefore = firstVehicle.getCurrentRoute();
		log.info("Route before:         " + routeBefore);

		String edgeID = "middle";
		Edge edge = conn.getEdgeRepository().getByID(edgeID);
		ChangeGlobalTravelTimeQuery cttq = edge.queryChangeTravelTime();
		cttq.setBeginTime(0);
		cttq.setEndTime(1000);
		cttq.setTravelTime(10000);
		cttq.run();

		ReadGlobalTravelTimeQuery rgttq = edge.queryReadGlobalTravelTime();
		rgttq.setTime(conn.getCurrentSimTime() / 1000);
		double newTravelTime = rgttq.get();
		assertEquals(10000, newTravelTime, DELTA);

		firstVehicle.queryReroute().run();

		List<Edge> routeAfter = firstVehicle.getCurrentRoute();
		log.info("Route after:          " + routeAfter);

		assertFalse(routeBefore.equals(routeAfter));
	}

	/**
	 * This test demonstrates the usage of the {@link Lane} object to get
	 * geometric information.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetShape() throws IOException {
		Lane lane = conn.getLaneRepository().getByID("beg_0");
		PathIterator it = lane.getShape().getPathIterator(null);
		assertFalse(it.isDone());
		double[] coords = new double[2];
		assertEquals(PathIterator.SEG_MOVETO, it.currentSegment(coords));
		assertEquals(0, coords[0], DELTA);
		assertEquals(-1.65, coords[1], DELTA);
		it.next();
		assertEquals(PathIterator.SEG_LINETO, it.currentSegment(coords));
		assertEquals(498.55, coords[0], DELTA);
		assertEquals(-1.65, coords[1], DELTA);
		it.next();
		assertTrue(it.isDone());
	}

	/**
	 * This test demonstrates the usage of the {@link Lane} object to get
	 * topological information.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetBelongingEdge() throws IOException {
		Lane lane = conn.getLaneRepository().getByID("beg_0");
		Edge edge = lane.getParentEdge();
		assertEquals("beg", edge.getID());
	}

	/**
	 * This test demonstrates how the execution speed can be increased by the
	 * usage of a {@link MultiQuery}. First, the simulation is advanced to
	 * populate the roads. Then, the position of all vehicles is queried for a
	 * given number of steps. The query is made in two methods: in the first
	 * method, a network request is made for each vehicle; in the second method,
	 * the queries for all vehicles are put into a MultiQuery, and only one
	 * network request is made. This test verifies that the performance of the
	 * second method is higher than the first. The difference can be up to 10x
	 * on a Linux machine, less on a Windows machine.
	 * 
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@Test
	// @Ignore // its duration may be annoying; feel free to comment this
	public void testMultiQueryPerformance() throws IllegalStateException, IOException {
		final int RETRIES = 5;

		while (conn.getCurrentSimTime() < 300000)
			conn.nextSimStep();

		long start = System.currentTimeMillis();
		for (int r = 0; r < RETRIES; r++) {
			Map<String, Vehicle> vehicles = conn.getVehicleRepository().getAll();
			for (Vehicle vehicle : vehicles.values()) {
				vehicle.getPosition();
			}
			conn.nextSimStep();
		}
		long elapsedSingle = System.currentTimeMillis() - start;
		log.info("Individual queries: " + elapsedSingle + " ms");

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
		log.info("MultiQuery queries: " + elapsedMulti + " ms");

		assertTrue(elapsedMulti < elapsedSingle);
	}

	/**
	 * This test demonstrates the read of the network's physical bounds.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetBounds() throws IOException {
		Rectangle2D bounds = conn.getSimulationData().queryNetBoundaries().get();
		assertEquals(0.0, bounds.getMinX(), DELTA);
		assertEquals(0.0, bounds.getMinY(), DELTA);
		assertEquals(2500.0, bounds.getMaxX(), DELTA);
		assertEquals(500.0, bounds.getMaxY(), DELTA);
	}

	/**
	 * Ensures that the set of roads in the network matches a predefined set.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetRoads() throws IOException, InterruptedException {
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

	/**
	 * Checks that the reported max speed of a lane is correct.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetLaneMaxSpeed() throws IOException {
		assertEquals(27.8, conn.getLaneRepository().getByID("beg_0").getMaxSpeed(), DELTA);
	}

	/**
	 * This test verifies that all the vehicles entered in the simulation will
	 * leave it sooner or later.
	 * 
	 * @throws IOException
	 */
	@Test
	// @Ignore // its duration may be annoying; feel free to comment this
	public void testWhoDepartsArrives() throws IOException {

		final Set<Vehicle> traveling = new HashSet<Vehicle>();

		conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {

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

			public void vehicleDeparted(Vehicle v) {
				assertFalse(traveling.contains(v));
				traveling.add(v);
			}

			public void vehicleTeleportEnding(Vehicle v) {
			}

			public void vehicleTeleportStarting(Vehicle v) {
			}
		});

		while (!conn.isClosed()) {
			conn.nextSimStep();
			log.info("step " + conn.getCurrentSimTime() / 1000);
		}

	}

	/**
	 * This test shows how to change a vehicle's destination road to "end",
	 * which is just before the default "rend", then advances the simulation to
	 * check that "rend" is never traversed by the vehicle until it exits.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testChangeTarget() throws IOException {
		getFirstVehicle();
		Vehicle v = firstVehicle;

		Edge endEdge = conn.getEdgeRepository().getByID("end");
		v.changeTarget(endEdge);

		Edge lastEdge = null;
		while (conn.getVehicleRepository().getByID(v.getID()) != null) {
			lastEdge = v.getCurrentEdge();
			assertFalse(lastEdge.getID().equals("rend"));

			conn.nextSimStep();
		}
	}

	/**
	 * This test checks ensures that changing the destination road also changes
	 * the vehicle's current route list such that the last road is the new
	 * destination road.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testChangeTargetAlsoAffectsRouteList() throws IOException {
		getFirstVehicle();
		Vehicle v = firstVehicle;
		Edge endEdge = conn.getEdgeRepository().getByID("end");
		v.changeTarget(endEdge);
		List<Edge> route = v.getCurrentRoute();
		assertEquals("end", route.get(route.size() - 1).getID());
	}

	/**
	 * This test tries to explicitly set a vehicle's route, and verifies that
	 * SUMO accepts it.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testChangeRoute() throws IOException {
		getFirstVehicle();
		Vehicle v = firstVehicle;
		List<Edge> newRoute = new ArrayList<Edge>();
		newRoute.add(conn.getEdgeRepository().getByID("beg"));
		newRoute.add(conn.getEdgeRepository().getByID("beg2left"));
		newRoute.add(conn.getEdgeRepository().getByID("left"));
		newRoute.add(conn.getEdgeRepository().getByID("left2end"));
		v.changeRoute(newRoute);
		assertEquals(newRoute, v.getCurrentRoute());
	}

	/**
	 * This test demonstrates the usage of the {@link Link} object by testing
	 * the links between a lane and the lanes a vehicle can go through.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testLaneLinks() throws IOException {
		Lane begLane = conn.getLaneRepository().getByID("beg_0");
		List<Link> links = begLane.getLinks();
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

	/**
	 * This test ensures that a vehicle's X/Y position never goes outside the
	 * road bounds.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testVehiclePositionIsInBounds() throws IOException {
		getFirstVehicle();
		while (conn.getVehicleRepository().getByID(firstVehicle.getID()) != null) {
			Point2D pos = firstVehicle.getPosition();
			assertTrue(pos.getX() >= 0);
			assertTrue(pos.getX() < 2500);
			assertEquals(-1.65, pos.getY(), DELTA);
			conn.nextSimStep();
		}
	}

	/**
	 * This test demonstrates that getting info from an invalid vehicle (e.g. an
	 * exited vehicle) will cause an exception. It also shows that the vehicle
	 * in {@link VehicleLifecycleObserver#vehicleArrived(Vehicle)} can't be
	 * queried anymore.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testUsingInactiveVehicle() throws IOException {
		getFirstVehicle();
		conn.addVehicleLifecycleObserver(new VehicleLifecycleObserver() {
			public void vehicleTeleportStarting(Vehicle vehicle) {
			}

			public void vehicleTeleportEnding(Vehicle vehicle) {
			}

			public void vehicleDeparted(Vehicle vehicle) {
			}

			public void vehicleArrived(Vehicle vehicle) {
				if (vehicle.equals(firstVehicle)) {
					try {
						log.info("pos: " + firstVehicle.getPosition());
						fail("it should throw an exception");
					} catch (IOException e) {
						log.trace(e);
					}
				}
			}
		});

		for (int t = 0; t < 500; t++) {
			conn.nextSimStep();
		}
	}

	/**
	 * Checks for presence of a Point of Interest.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testPOIExistence() throws IOException {
		Repository<POI> poiRepo = conn.getPOIRepository();
		assertNotNull(poiRepo.getByID("0"));
	}

	/**
	 * Checks the correct reading of a POI's type.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testPOIType() throws IOException {
		Repository<POI> poiRepo = conn.getPOIRepository();
		POI poi = poiRepo.getByID("0");
		assertEquals("TEST_TYPE", poi.getType());
	}

	/**
	 * Checks the correct reading of a POI's color.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testPOIColor() throws IOException {
		Repository<POI> poiRepo = conn.getPOIRepository();
		POI poi = poiRepo.getByID("0");
		Color c = new Color(255, 128, 0);
		assertEquals(c, poi.getColor());
	}

	/**
	 * Checks the correct reading of a POI's position.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testPOIPosition() throws IOException {
		Repository<POI> poiRepo = conn.getPOIRepository();
		POI poi = poiRepo.getByID("0");
		Point2D pos = new Point2D.Double(100, 50);
		Point2D poiPos = poi.getPosition();
		assertEquals(pos.getX(), poiPos.getX(), DELTA);
		assertEquals(pos.getY(), poiPos.getY(), DELTA);
	}

	/**
	 * Checks the correct setting of a POI's type.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSetPOIType() throws IOException {
		Repository<POI> poiRepo = conn.getPOIRepository();
		POI poi = poiRepo.getByID("0");
		final String newType = "NEW_TYPE";
		poi.changeType(newType);
		assertEquals(newType, poi.getType());
	}

	/**
	 * Checks the correct setting of a POI's position.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSetPOIPosition() throws IOException {
		Repository<POI> poiRepo = conn.getPOIRepository();
		POI poi = poiRepo.getByID("0");
		final Point2D newPos = new Point2D.Double(0, 0);
		poi.changePosition(newPos);
		final Point2D pos = poi.getPosition();
		assertEquals(newPos.getX(), pos.getX(), DELTA);
		assertEquals(newPos.getY(), pos.getY(), DELTA);
	}

	/**
	 * Checks the correct setting of a POI's color.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSetPOIColor() throws IOException {
		Repository<POI> poiRepo = conn.getPOIRepository();
		POI poi = poiRepo.getByID("0");
		final Color newColor = Color.cyan;
		poi.changeColor(newColor);
		assertEquals(newColor, poi.getColor());
	}

	/**
	 * Checks for presence of a Multi-entry/Multi-exit detector (E3).
	 * 
	 * @throws IOException
	 */
	@Test
	public void testMeMeExistence() throws IOException {
		Repository<MeMeDetector> memeRepo = conn.getMeMeDetectorRepository();
		assertNotNull(memeRepo.getByID("e3_0"));
	}

	/**
	 * Checks for the correct behaviour of a Multi-entry/Multi-exit detector
	 * (E3).
	 * 
	 * @throws IOException
	 */
	@Test
	public void testMeMeDetectorIsDetecting() throws IOException {

		for (int t = 0; t < 100; t++) {
			conn.nextSimStep();
		}

		Repository<MeMeDetector> memeRepo = conn.getMeMeDetectorRepository();
		MeMeDetector detector = memeRepo.getByID("e3_0");

		assertEquals(39, (int) detector.getVehicleNumber());
	}

	/**
	 * Checks for presence of a Lane area detector (E2).
	 * 
	 * @throws IOException
	 */
	@Test
	public void testLaArExistence() throws IOException {
		Repository<LaArDetector> laArRepo = conn.getLaArDetectorRepository();
		assertNotNull(laArRepo.getByID("e2_0"));
	}

	/**
	 * Checks the get position method of a lane area detector (E2).
	 * 
	 * @throws IOException
	 */
	@Ignore
	@Test
	public void testLaArGetPosition() throws IOException {
		Repository<LaArDetector> laArRepo = conn.getLaArDetectorRepository();
		LaArDetector laAr = laArRepo.getByID("e2_0");
		assertEquals(10, laAr.getPosition(), 0);
	}

	/**
	 * Checks the get lane method of a lane area detector (E2).
	 * 
	 * @throws IOException
	 */
	@Ignore
	@Test
	public void testLaArGetLane() throws IOException {
		Repository<LaArDetector> laArRepo = conn.getLaArDetectorRepository();
		LaArDetector laAr = laArRepo.getByID("e2_0");
		assertEquals(laAr.getLane().getID(), "beg_0");
	}

	/**
	 * Checks for the correct behaviour of a Lane area detector (E2).
	 * 
	 * @throws IOException
	 */
	@Test
	public void testLaArDetectorIsDetecting() throws IOException {

		for (int t = 0; t < 100; t++) {
			conn.nextSimStep();
		}

		Repository<LaArDetector> laArRepo = conn.getLaArDetectorRepository();
		LaArDetector detector = laArRepo.getByID("e2_0");

		assertEquals(1, (int) detector.getVehicleNumber());
	}

	/**
	 * Checks for presence of a induction loop (E1).
	 * 
	 * @throws IOException
	 */
	@Test
	public void testInLoExistence() throws IOException {
		Repository<InductionLoop> inLoRepo = conn.getInductionLoopRepository();
		assertNotNull(inLoRepo.getByID("e1_0"));
	}

	/**
	 * Checks the get position method of an induction loop (E1).
	 * 
	 * @throws IOException
	 */
	@Test
	public void testInLoGetPosition() throws IOException {
		Repository<InductionLoop> inLoRepo = conn.getInductionLoopRepository();
		InductionLoop loop = inLoRepo.getByID("e1_0");
		assertEquals(30, loop.getPosition(), 0);
	}

	/**
	 * Checks the get lane method of an induction loop (E1).
	 * 
	 * @throws IOException
	 */
	@Test
	public void testInLoGetLane() throws IOException {
		Repository<InductionLoop> inLoRepo = conn.getInductionLoopRepository();
		InductionLoop loop = inLoRepo.getByID("e1_0");
		assertEquals(loop.getLane().getID(), "beg_0");
	}

	/**
	 * Checks for the correct behaviour of a induction loop (E1).
	 * 
	 * @throws IOException
	 */
	@Test
	public void testInductionLoopIsDetecting() throws IOException {

		for (int t = 0; t < 100; t++) {
			conn.nextSimStep();
		}

		Repository<InductionLoop> laArRepo = conn.getInductionLoopRepository();
		InductionLoop detector = laArRepo.getByID("e1_0");

		assertEquals(1, (int) detector.getVehicleNumber());
	}

	/**
	 * Checks for the correct adding of new vehicles.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAddVehicle() throws IOException {
		conn.nextSimStep();

		// assertTrue(conn.getVehicleRepository().getIDs().size() > 0);
		final String id1 = "A_NEW_VEHICLE";
		final String id2 = "ANOTHER_NEW_VEHICLE";
		Route route = conn.getRouteRepository().getByID("0");
		Lane lane = conn.getLaneRepository().getAll().values().iterator().next();
		VehicleType vType = conn.getVehicleTypeRepository().getByID("KRAUSS_DEFAULT");

		int now = conn.getCurrentSimTime(); // time is in ms

		/*
		 * Add one vehicle now and one at a later time.
		 */
		AddVehicleQuery avqNow = conn.queryAddVehicle();
		avqNow.setVehicleData(id1, vType, route, lane, now, 0, 0);
		avqNow.run();

		AddVehicleQuery avqLater = conn.queryAddVehicle();
		avqLater.setVehicleData(id2, vType, route, lane, now + 70001, 0, 0);
		avqLater.run();

		/*
		 * The new vehicle might not enter the simulation immediately because
		 * its lane must be freed of other waiting vehicles first.
		 */
		for (int t = 0; t < 70; t++)
			conn.nextSimStep();

		assertTrue(conn.getVehicleRepository().getAll().containsKey(id1));
		assertFalse(conn.getVehicleRepository().getAll().containsKey(id2));

		for (int t = 0; t < 80; t++)
			conn.nextSimStep();

		assertTrue(conn.getVehicleRepository().getAll().containsKey(id2));
	}

	/**
	 * Checks for the correct removal of a vehicle.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testRemoveVehicle() throws IOException {
		getFirstVehicle();
		RemoveVehicleQuery rvq = conn.queryRemoveVehicle();
		rvq.setVehicleData(firstVehicle, 1);
		rvq.run();
		conn.nextSimStep();
		assertNull(conn.getVehicleRepository().getByID(firstVehicle.getID()));
	}

	/**
	 * Checks the length of the vehicle type method.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testVehicleTypLength() throws IOException {
		Repository<VehicleType> repo = conn.getVehicleTypeRepository();
		assertEquals(5, repo.getByID("DEFAULT_VEHTYPE").getLength(), 0);
		assertEquals(3, repo.getByID("KRAUSS_DEFAULT").getLength(), 0);
		assertEquals(0.215, repo.getByID("DEFAULT_PEDTYPE").getLength(), 0);
	}

	/**
	 * Checks for the correct adding of a new route.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testAddRoute() throws IOException {
		conn.nextSimStep();

		final String id = "A_NEW_ROUTE";

		AddRouteQuery arq = conn.queryAddRoute();
		List<Edge> edges = new ArrayList<Edge>();
		edges.add(conn.getEdgeRepository().getByID("beg"));
		edges.add(conn.getEdgeRepository().getByID("beg2left"));
		edges.add(conn.getEdgeRepository().getByID("left"));
		edges.add(conn.getEdgeRepository().getByID("left2end"));
		edges.add(conn.getEdgeRepository().getByID("end"));
		arq.setVehicleData(id, edges);
		arq.run();

		assertTrue(conn.getRouteRepository().getAll().containsKey(id));
	}

	/**
	 * Checks for the edges of a route.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testRouteGetEdges() throws IOException {
		Route route = conn.getRouteRepository().getByID("0");
		List<Edge> edges = route.getRoute();
		assertEquals(4, edges.size());
		assertEquals("beg", edges.get(0).getID());
		assertEquals("middle", edges.get(1).getID());
		assertEquals("end", edges.get(2).getID());
		assertEquals("rend", edges.get(3).getID());
	}

	@Test
	public void testVehicleGetLaneIndex() throws IOException {
		/*
		 * NOTE: it's too easy to check for the lane index in a one-lane road.
		 * This should be tested in a simulation scenario with more lanes per
		 * road.
		 */
		getFirstVehicle();

		assertThat(firstVehicle.getLaneIndex(), equalTo(0));
	}

	@Test
	public void testVehicleGetLaneID() throws IOException {
		getFirstVehicle();

		assertThat(firstVehicle.getLaneId().getID(), equalTo("beg_0"));
	}

	@Test
	public void testLaneDimensions() throws IOException {
		Lane lane = conn.getLaneRepository().getByID("beg_0");
		assertEquals(498.55, lane.getLength(), 0);
		assertEquals(3.2, lane.getWidth(), 0);
	}

	@Test
	public void test500msStep() throws IOException, InterruptedException {
		conn.close();
		conn = new SumoTraciConnection(getSimConfigFileLocation(), 0);
		conn.setStepLength(500);
		conn.runServer();
		assertEquals(0, conn.getCurrentSimTime());
		conn.nextSimStep();
		assertEquals(500, conn.getCurrentSimTime());
	}
}
