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

import it.polito.appeal.traci.protocol.RoadmapPosition;

import org.junit.Test;


public class RoadmapPositionTest {
	@Test
	public void testEqualsHashcode() {
		RoadmapPosition same1 = new RoadmapPosition("edgeID", 1.5, 3);
		RoadmapPosition same2 = new RoadmapPosition("edgeID", 1.5, 3);
		RoadmapPosition different = new RoadmapPosition("anotherEdgeID", 1.5, 3);
		EqualsHashcodeTester.testAll(same1, same2, different);
	}
}
