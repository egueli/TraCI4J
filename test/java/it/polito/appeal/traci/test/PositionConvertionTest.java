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

import static org.junit.Assert.*;
import it.polito.appeal.traci.ChangeLightsStateQuery;
import it.polito.appeal.traci.ControlledLink;
import it.polito.appeal.traci.ControlledLinks;
import it.polito.appeal.traci.Lane;
import it.polito.appeal.traci.LightState;
import it.polito.appeal.traci.Logic;
import it.polito.appeal.traci.Phase;
import it.polito.appeal.traci.PositionConversionQuery;
import it.polito.appeal.traci.Program;
import it.polito.appeal.traci.ReadObjectVarQuery;
import it.polito.appeal.traci.Repository;
import it.polito.appeal.traci.SumoTraciConnection;
import it.polito.appeal.traci.TLState;
import it.polito.appeal.traci.TrafficLight;
import it.polito.appeal.traci.protocol.RoadmapPosition;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class PositionConvertionTest {

	private static final String SIM_CONFIG_LOCATION = "test/sumo_maps/polito/test.sumo.cfg";
	private SumoTraciConnection conn;

	private static final String EDGE_NAME = "-105254616#1";
	private static final int LANE_NUM = 0;
	private static final Point2D LOCATION_LOCAL = new Point2D.Double(1071.38, 1561.38);
	private static final Point2D LOCATION_GEO = new Point2D.Double(7.659806, 45.064683);
	
	static {
		BasicConfigurator.configure();
	}
	
	@Before
	public void setUp() throws Exception {
		TraCITest.printSeparator();
		conn = TraCITest.startSumoConn(SIM_CONFIG_LOCATION);
	}

	@After
	public void tearDown() throws IOException, InterruptedException {
		TraCITest.stopSumoConn(conn);
	}

	@Test
	public void testConvertRoadmapBeginToLonLat() throws IOException {
		PositionConversionQuery conv = conn.queryPositionConversion();
		conv.setPositionToConvert(new RoadmapPosition(EDGE_NAME, 0, LANE_NUM));
		Point2D out = conv.get();
		assertEquals(LOCATION_GEO.getX(), out.getX(), 1e-5);
		assertEquals(LOCATION_GEO.getY(), out.getY(), 1e-5);
	}


	@Test
	public void testConvertXYToLonLat() throws IOException {
		PositionConversionQuery conv = conn.queryPositionConversion();
		conv.setPositionToConvert(LOCATION_LOCAL, true);
		Point2D out = conv.get();
		assertEquals(LOCATION_GEO.getX(), out.getX(), 1e-5);
		assertEquals(LOCATION_GEO.getY(), out.getY(), 1e-5);
	}

	@Test
	public void testConvertLonLatToXY() throws IOException {
		PositionConversionQuery conv = conn.queryPositionConversion();
		conv.setPositionToConvert(LOCATION_GEO, false);
		Point2D out = conv.get();
		assertEquals(LOCATION_LOCAL.getX(), out.getX(), 1e-5);
		assertEquals(LOCATION_LOCAL.getY(), out.getY(), 1e-5);
	}
}
