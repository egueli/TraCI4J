/*   
    Copyright (C) 2014 ApPeAL Group, Politecnico di Torino

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

import java.io.IOException;

import it.polito.appeal.traci.SumoTraciConnection;

import org.junit.After;
import org.junit.Before;

/**
 * An abstract class that contains common methods for all TraCI4J test classes.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * 
 */
public abstract class SingleSimTraCITest {

	/**
	 * @return the location of the SUMO config file to be used by all tests in
	 *         this class.
	 */
	protected abstract String getSimConfigFileLocation();
	
	protected SumoTraciConnection conn;
	
	/**
	 * Start SUMO and connect to it.
	 * 
	 * @param simConfigLocation
	 * @return
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws Exception
	 */
	@Before
	public void startSumoConnection() throws IOException, InterruptedException  {
		conn = makeConnection();
		conn.runServer();
	}

	protected SumoTraciConnection makeConnection() {
		return new SumoTraciConnection(getSimConfigFileLocation(), 0);
	}
	
	@After
	public void stopSumoConnection() throws IOException, InterruptedException {
		if (conn != null)
			conn.close();
	}
}
