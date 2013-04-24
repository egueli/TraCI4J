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

import java.io.IOException;
import java.net.InetAddress;

import it.polito.appeal.traci.SumoRunner;
import it.polito.appeal.traci.SumoTraciConnection;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class RemoteTraCITest {

	private Process sumoProcess;

	private SumoTraciConnection conn;
	
	private static final int PORT = 5450;
	
	static {
		BasicConfigurator.configure();
	}
	
	@Before
	public void setUp() throws IOException, InterruptedException {
		String exe = System.getProperty(SumoRunner.SUMO_EXE_PROPERTY);
		if (exe == null) {
			exe = "sumo";
		}
		
		String[] args = new String[] {
			exe,
			"-c", "test/sumo_maps/variable_speed_signs/test.sumo.cfg",
			"--remote-port", Integer.toString(PORT),
		};
		
		sumoProcess = Runtime.getRuntime().exec(args);
		
		try {
			int exitVal = sumoProcess.exitValue();
			throw new IOException("SUMO died with exit value " + exitVal);
		}
		catch (IllegalThreadStateException e) {
			// all OK, it's alive
		}
		
		conn = new SumoTraciConnection(InetAddress.getLocalHost(), PORT);
	}
	
	@After
	public void tearDown() throws IOException, InterruptedException {
		conn.close();
	}
	
	@Test
	public void testNotClosed() {
		assertFalse(conn.isClosed());
	}
	
	@Test
	public void testClosesIfAskedTo() throws IOException, InterruptedException {
		conn.close();
		assertTrue(conn.isClosed());
	}
	
	@Test
	public void testCommunicationWorks() throws IOException {
		int simTime = conn.getSimulationData().queryCurrentSimTime().get();
		assertEquals(0, simTime);
	}
}
