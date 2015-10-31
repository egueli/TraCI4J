/*   
    Copyright (C) 2015 ApPeAL Group, Politecnico di Torino

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

import org.junit.Test;

public class SumoGuiTest extends SingleSimTraCITest {

	@Override
	protected String getSimConfigFileLocation() {
		return "test/resources/sumo_maps/variable_speed_signs/test.sumo.cfg";
	}



	@Override
	public void startSumoConnection() throws IOException, InterruptedException {
		conn = makeConnection();
		conn.addOption("start", "1"); // auto-run on GUI show
		conn.addOption("quit-on-end", "1"); // auto-close on end
		conn.runServer(true);
	}


	@Test
	public void testRunGui() throws IllegalStateException, IOException {
		for (int i = 0; i < 10; i++) {
			conn.nextSimStep();
		}
	}

}
