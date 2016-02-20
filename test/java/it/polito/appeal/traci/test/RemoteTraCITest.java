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

import it.polito.appeal.traci.SumoTraciConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class RemoteTraCITest {

	private Logger log = LogManager.getLogger();

	private Process sumoProcess;

	private SumoTraciConnection conn;
	
	private static final int PORT = 5450;
	
	@Before
	public void setUp() throws Exception {
		String exe = System.getProperty(SumoTraciConnection.SUMO_EXE_PROPERTY);
		if (exe == null) {
			exe = "sumo";
		}

		// Issue #20 (since version 0.24.0 it was always sumo.exe)
		String exe64 = exe;
		if (System.getProperty(SumoTraciConnection.OS_ARCH_PROPERTY).contains("64") && System.getProperty(SumoTraciConnection.OS_NAME_PROPERTY).contains("Win")) {
			exe64 += "64";
		}
		
		String[] args = new String[] {
			exe64,
			"-c", "test/resources/sumo_maps/variable_speed_signs/test.sumo.cfg",
			"--remote-port", Integer.toString(PORT),
			// this avoids validation of the input xml files; if SUMO_HOME is not set correctly,
			// sumo will try to download the schema files from the web and may wait 30 seconds at startup
			// for the connection to time out.
			"--xml-validation", "never"
		};
		
		if (log.isDebugEnabled()) {
			StringBuilder cmdLine = new StringBuilder();
			for (String arg : args) {
				cmdLine.append(arg);
				cmdLine.append(" ");
			}
			log.debug("running " + cmdLine.toString());
		}
		
		try {
			sumoProcess = Runtime.getRuntime().exec(args);
		}
		catch (Exception e) {
			if (!exe64.equals(exe)) {
				log.debug("Try it again (x64).");
				args[0] = exe;
				sumoProcess = Runtime.getRuntime().exec(args);
			} else {
				throw e;
			}
		}
		
		try {
			int exitVal = sumoProcess.exitValue();
			throw new IOException("SUMO died with exit value " + exitVal);
		}
		catch (IllegalThreadStateException e) {
			log.debug("All OK, it's alive");
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
