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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import it.polito.appeal.traci.ChangeLightsStateQuery;
import it.polito.appeal.traci.ControlledLink;
import it.polito.appeal.traci.ControlledLinks;
import it.polito.appeal.traci.Lane;
import it.polito.appeal.traci.LightState;
import it.polito.appeal.traci.Logic;
import it.polito.appeal.traci.Phase;
import it.polito.appeal.traci.Program;
import it.polito.appeal.traci.ReadObjectVarQuery;
import it.polito.appeal.traci.Repository;
import it.polito.appeal.traci.TLState;
import it.polito.appeal.traci.TrafficLight;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class TrafficLightsTest extends SingleSimTraCITest {

	private Repository<TrafficLight> repo;

	private static final Logger log = LogManager.getLogger();
	
	@Override
	protected String getSimConfigFileLocation() {
		return "test/resources/sumo_maps/cross3ltl/test.sumo.cfg";
	}
	
	@Before
	public void setUp() throws Exception {
		TraCITest.printSeparator();
		repo = conn.getTrafficLightRepository();
	}

	@Test
	public void testTrafficLightExistence() throws IOException {
		assertNotNull(repo.getByID("0"));
	}
	
	private static final LightState[][] PHASES = new LightState[][] {
		// phase 0
		new LightState[] {
				LightState.GREEN_NODECEL, 
				LightState.GREEN_NODECEL, 
				LightState.GREEN,         
				LightState.GREEN,         
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.GREEN_NODECEL, 
				LightState.GREEN_NODECEL, 
				LightState.GREEN,         
				LightState.GREEN,         
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
		},
		// phase 1
		new LightState[] {
				LightState.YELLOW, 
				LightState.YELLOW, 
				LightState.GREEN,         
				LightState.GREEN,         
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.YELLOW, 
				LightState.YELLOW, 
				LightState.GREEN,         
				LightState.GREEN,         
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
		},
		// phase 2	
		new LightState[] {
				LightState.RED, 
				LightState.RED, 
				LightState.GREEN_NODECEL,         
				LightState.GREEN_NODECEL,         
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED, 
				LightState.RED, 
				LightState.GREEN_NODECEL,         
				LightState.GREEN_NODECEL,         
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
		},
		// phase 3	
		new LightState[] {
				LightState.RED, 
				LightState.RED, 
				LightState.YELLOW,         
				LightState.YELLOW,         
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED, 
				LightState.RED, 
				LightState.YELLOW,         
				LightState.YELLOW,         
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
		},
		// phase 4
		new LightState[] {
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.GREEN_NODECEL, 
				LightState.GREEN_NODECEL, 
				LightState.GREEN,         
				LightState.GREEN,         
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.GREEN_NODECEL, 
				LightState.GREEN_NODECEL, 
				LightState.GREEN,         
				LightState.GREEN,         
		},
		// phase 5
		new LightState[] {
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.YELLOW, 
				LightState.YELLOW, 
				LightState.GREEN,         
				LightState.GREEN,         
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.YELLOW, 
				LightState.YELLOW, 
				LightState.GREEN,         
				LightState.GREEN,         
		},
		// phase 6	
		new LightState[] {
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED, 
				LightState.RED, 
				LightState.GREEN_NODECEL,         
				LightState.GREEN_NODECEL,         
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED, 
				LightState.RED, 
				LightState.GREEN_NODECEL,         
				LightState.GREEN_NODECEL,         
		},
		// phase 7	
		new LightState[] {
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED, 
				LightState.RED, 
				LightState.YELLOW,         
				LightState.YELLOW,         
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED,           
				LightState.RED, 
				LightState.RED, 
				LightState.YELLOW,         
				LightState.YELLOW,         
		},		
	};
	
	private static final int[] PHASES_DURATION = new int[] {
		31, 4, 6, 4, 31, 4, 6, 4
	};

	
	@Test
	public void testStateAtFirstStep() throws IOException {
		TrafficLight tl = repo.getByID("0");
		TLState tlState = tl.queryReadState().get();
		final LightState[] states = tlState.lightStates;
		assertEquals(16, states.length);
		assertArrayEquals(PHASES[0], states);
	}
	
	@Test
	public void testStateUpdate() throws IOException {
		TrafficLight tl = repo.getByID("0");
		final ReadObjectVarQuery<TLState> query = tl.queryReadState();
		
		// looks like SUMO shifts all TL timings ahead one second
		conn.nextSimStep();
		
		for (int p = 0; p < PHASES.length; p++) {
			TLState tlState = query.get();
			final LightState[] states = tlState.lightStates;
			log.info("state at t=" + conn.getCurrentSimTime() + "ms\n" 
					+ "  expected " + Arrays.toString(PHASES[p]) + "\n"
					+ "  actual   " + Arrays.toString(states));
			assertArrayEquals("state at t=" + conn.getCurrentSimTime() + "ms", PHASES[p], states);
			
			for (int t=0; t<PHASES_DURATION[p]; t++)
				conn.nextSimStep();
		}
	}
	
	@Test
	public void testCurrentDuration() throws IOException {
		TrafficLight tl = repo.getByID("0");
		final ReadObjectVarQuery<Integer> query = tl.queryReadDefaultCurrentPhaseDuration();

		// looks like SUMO shifts all TL timings ahead one second
		conn.nextSimStep();
		
		for (int p = 0; p < PHASES.length; p++) {
			int phaseDuration = query.get();
			assertEquals(PHASES_DURATION[p], phaseDuration / 1000);
			
			for (int t=0; t<PHASES_DURATION[p]; t++)
				conn.nextSimStep();
		}
	}
	
	private static final String[][] linksLaneIDs = new String[][] {
		new String[] { "4si_0", ":0_0_0", "1o_0" },
		new String[] { "4si_1", ":0_1_0", "3o_0" },
		new String[] { "4si_2", ":0_2_0", "2o_0" },
		new String[] { "4si_2", ":0_3_0", "4o_0" },
		new String[] { "2si_0", ":0_4_0", "4o_0" },
		new String[] { "2si_1", ":0_5_0", "1o_0" },
		new String[] { "2si_2", ":0_6_0", "3o_0" },
		new String[] { "2si_2", ":0_7_0", "2o_0" },
		new String[] { "3si_0", ":0_8_0", "2o_0" },
		new String[] { "3si_1", ":0_9_0", "4o_0" },
		new String[] { "3si_2", ":0_10_0", "1o_0" },
		new String[] { "3si_2", ":0_11_0", "3o_0" },
		new String[] { "1si_0", ":0_12_0", "3o_0" },
		new String[] { "1si_1", ":0_13_0", "2o_0" },
		new String[] { "1si_2", ":0_14_0", "4o_0" },
		new String[] { "1si_2", ":0_15_0", "1o_0" },
	};
	
	@Test
	public void testControlledLinks() throws IOException {
		TrafficLight tl = repo.getByID("0");
		ControlledLinks links = tl.queryReadControlledLinks().get();
		
		assertEquals(linksLaneIDs.length, links.getLinks().length);
		for (int i=0; i<linksLaneIDs.length; i++) {
			ControlledLink[] linksForSignal = links.getLinks()[i];
			assertEquals(1, linksForSignal.length);
			ControlledLink link = linksForSignal[0];
			assertEquals(linksLaneIDs[i][0], link.getIncomingLane().getID());
			assertEquals(linksLaneIDs[i][1], link.getAcrossLane().getID());
			assertEquals(linksLaneIDs[i][2], link.getOutgoingLane().getID());
		}
	}
	
	@Test
	public void testCompleteProgramDefinition() throws IOException {
		TrafficLight tl = repo.getByID("0");
		Program program = tl.queryReadCompleteDefinition().get();
		
		assertEquals(1, program.getLogics().length);
		Logic logic = program.getLogics()[0];
		
		assertEquals("0", logic.getSubID());
		assertEquals(0, logic.getCurrentPhaseIndex());
		
		Phase[] phases = logic.getPhases();
		assertEquals(8, phases.length);
		
		for (int i=0; i<phases.length; i++) {
			Phase ph = phases[i];
			assertEquals(PHASES_DURATION[i] * 1000, ph.getDuration());
			assertArrayEquals(PHASES[i], ph.getState().lightStates);
		}
	}
	
	@Test
	public void testChangingCompleteProgramDefinition() throws IOException {
		TrafficLight tl = repo.getByID("0");
		
		final Logic expectedLogic = new Logic("0", 0, new Phase[] {
			new Phase(10000, new TLState("rrGGyyyyggrryryr")),
			new Phase(15000, new TLState("GGyyrrrrrrGGrGrG")),
			new Phase(55000, new TLState("yyrrGGGGGGyyGyGy"))
		});
		tl.queryChangeCompleteProgramDefinition().setValue(expectedLogic);
		tl.queryChangeCompleteProgramDefinition().run();
		
		Program newProgram = tl.queryReadCompleteDefinition().get();
		
		assertEquals(1, newProgram.getLogics().length);
		Logic actualLogic = newProgram.getLogics()[0];

		assertEquals(
				expectedLogic.getSubID(),
				actualLogic.getSubID());
		assertEquals(
				expectedLogic.getCurrentPhaseIndex(),
				actualLogic.getCurrentPhaseIndex());

		Phase[] actualPhases = actualLogic.getPhases();
		Phase[] expectedPhases = expectedLogic.getPhases();
		assertEquals(expectedPhases.length, actualPhases.length);

		for (int i = 0; i < actualPhases.length; i++) {
			Phase actualPhase = actualPhases[i];
			Phase expectedPhase = expectedPhases[i];
			assertEquals(
					expectedPhase.getDuration(),
					actualPhase.getDuration());
			assertArrayEquals(
					expectedPhase.getState().lightStates,
					actualPhase.getState().lightStates);
		}
	}


	private static final TLState TEST_TL_STATE = new TLState("rrGGyyyyggrryryr");

	@Test
	public void testChangeState() throws IOException {
		TrafficLight tl = repo.getByID("0");
		ChangeLightsStateQuery q = tl.queryChangeLightsState();
		q.setValue(TEST_TL_STATE);
		q.run();
		
		assertEquals(TEST_TL_STATE, tl.queryReadState().get());
	}
	
	@Test
	public void testTrafficLightsPosition() throws IOException {
		TrafficLight tl = repo.getByID("0");
		List<Lane> lanes = tl.queryReadControlledLanes().get();
		
		assertEquals(16, lanes.size());
		
		for (Lane lane : lanes) {
		
			Point2D lastPoint = getLastPointOfALane(lane);

			assertTrue(lastPoint.getX() > 486.0);
			assertTrue(lastPoint.getY() > 486.0);
			assertTrue(lastPoint.getX() < 513.0);
			assertTrue(lastPoint.getY() < 513.0);
		}
	}

	private static Point2D getLastPointOfALane(Lane lane) throws IOException {
		Path2D shape = lane.queryReadShape().get();
		PathIterator it = shape.getPathIterator(null);
		double[] coords = new double[6];
		while (!it.isDone()) {
			it.currentSegment(coords);
			it.next();
		}
		Point2D lastPoint = new Point2D.Double(coords[0], coords[1]);
		return lastPoint;
	}
}
