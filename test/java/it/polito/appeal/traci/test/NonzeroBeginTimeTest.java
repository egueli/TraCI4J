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

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class NonzeroBeginTimeTest extends SingleSimTraCITest {

	@Override
	protected String getSimConfigFileLocation() {
		return "test/resources/sumo_maps/box1l/test-nonzero-begin-time.sumo.cfg";
	}
	
	@Test
	public void testFirstStep() {
		assertEquals(50000000, conn.getCurrentSimTime());
	}
	
	@Test
	public void testNextStep() throws IllegalStateException, IOException {
		conn.nextSimStep();
		assertEquals(50001000, conn.getCurrentSimTime());
	}
}
