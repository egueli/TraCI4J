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


import it.polito.appeal.traci.SumoTraciConnection;

import java.io.IOException;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class HttpDownloadTest {


	@Test
	public void testHttpDownload() throws IOException, InterruptedException {
		new NanoHTTPD(5432);

		SumoTraciConnection conn = new SumoTraciConnection("http://127.0.0.1:5432/test/resources/sumo_maps/variable_speed_signs/test.sumo.cfg", 1);
		conn.runServer();
		conn.nextSimStep();
		conn.nextSimStep();
	}

}
