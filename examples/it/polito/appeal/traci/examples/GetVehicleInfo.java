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

package it.polito.appeal.traci.examples;

import it.polito.appeal.traci.SumoTraciConnection;
import it.polito.appeal.traci.Vehicle;

import java.util.Collection;

/**
 * This code runs an instance of SUMO, queries the map bounds and does ten
 * simulation steps. For each step, it prints which and how many vehicles are
 * active.
 * <p>
 * The specified configuration file is relative to the TraCI4J package's base
 * directory.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * 
 */
public class GetVehicleInfo {

	/** main method */
	public static void main(String[] args) {
		SumoTraciConnection conn = new SumoTraciConnection(
				"test/sumo_maps/box1l/test.sumo.cfg",  // config file
				12345                                  // random seed
				);
		try {
			conn.runServer();
			
			// the first two steps of this simulation have no vehicles.
			conn.nextSimStep();
			conn.nextSimStep();
			
			Collection<Vehicle> vehicles = conn.getVehicleRepository().getAll().values();

			Vehicle aVehicle = vehicles.iterator().next();
			
			System.out.println("Vehicle " + aVehicle
					+ " will traverse these edges: "
					+ aVehicle.getCurrentRoute());
			
			conn.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
