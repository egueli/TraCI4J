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

package it.polito.appeal.traci.examples;

import it.polito.appeal.traci.SumoTraciConnection;

import java.util.Set;

import org.apache.log4j.BasicConfigurator;

public class OpenStepsClose {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		
		SumoTraciConnection conn = new SumoTraciConnection(
				"test/sumo_maps/box1l/test.sumo.cfg",  // config file
				12345,                                 // random seed
				false                                  // look for geolocalization info in the map
				);
		try {
			conn.runServer();
			
			System.out.println("Map bounds are: " + conn.queryBounds());
			
			for (int i = 0; i < 10; i++) {
				int time = conn.getCurrentSimStep();
				Set<String> vehicles = conn.getActiveVehicles();
				
				System.out.println("At time step " + time + ", there are "
						+ vehicles.size() + " vehicles: " + vehicles);
				
				conn.nextSimStep();
			}
			
			conn.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
