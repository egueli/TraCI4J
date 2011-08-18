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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QueryRoadsTest {

	static {
		// this need to be done only once
		BasicConfigurator.configure();
	}
	
	private SumoTraciConnection conn;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		if (conn != null)
			conn.close();
	}

	
	@Test
	public void testQueryRoads() throws IOException, InterruptedException {
		conn = new SumoTraciConnection("test/sumo_maps/variable_speed_signs/test.sumo.cfg", 0, false);
		conn.runServer();
		
		Set<String> expectedLaneIDs = new HashSet<String>();
		expectedLaneIDs.add("beg_0");
		expectedLaneIDs.add("beg2left_0");
		expectedLaneIDs.add("end_0");
		expectedLaneIDs.add("left_0");
		expectedLaneIDs.add("left2end_0");
		expectedLaneIDs.add("middle_0");
		expectedLaneIDs.add("rend_0");
		
		
		Collection<Lane> lanes = conn.queryLanes();
		for (Lane lane : lanes) {
			String id = lane.externalID;
			if (expectedLaneIDs.contains(id))
				expectedLaneIDs.remove(id);
			else
				fail("unexpected lane " + id);
		}
		
		assertTrue(expectedLaneIDs.isEmpty());
		
		conn.close();
	}
	
	@Test
	public void testQueryLargeRoadmap() throws IOException, InterruptedException {
		boolean wentBad = false;
		String path = System.getProperty("largeSimConfig");
		InetSocketAddress servSockAddr = null;
		if (path == null) {
			String largeSimAddr = System.getProperty("largeSimSock.addr");
			if (largeSimAddr == null)
				fail("please set -DlargeSimConfig or -DlargeSimSock.addr -DlargeSimSock.port to run this test");
				
			servSockAddr = new InetSocketAddress(
					largeSimAddr, 
					Integer.parseInt(System.getProperty("largeSimSock.port")));
		}
		
		for (int i=0; i<5; i++) {
			try {
				if (path == null)
					conn = new SumoTraciConnection(servSockAddr);
				else {
					conn = new SumoTraciConnection(path, 0, false); 
					conn.runServer();
				}
				
				conn.queryLanes();
				conn.close();
			}
			catch(Exception e) {
				wentBad = true;
				e.printStackTrace();
			}
		}
		
		assertFalse(wentBad);
	}
}
