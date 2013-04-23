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

package it.polito.appeal.traci;

/**
 * Interface for a class that wants to be notified about the life-cycle of a
 * vehicle.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * 
 */
public interface VehicleLifecycleObserver {

	/**
	 * Method called when a vehicle has entered the simulation. This method will
	 * be called during an invocation of
	 * {@link SumoTraciConnection#nextSimStep()} and after any other
	 * {@link #vehicleArrived(Vehicle)} in the same simulation step.
	 * 
	 * @param vehicle
	 *            the vehicle that just entered
	 */
	void vehicleDeparted(Vehicle vehicle);

	/**
	 * Method called when a vehicle has exited the simulation. This method will
	 * be called during an invocation of
	 * {@link SumoTraciConnection#nextSimStep()} and before any other
	 * {@link #vehicleDeparted(Vehicle)} in the same simulation step.
	 * 
	 * @param vehicle
	 *            the vehicle that just left the simulation. Note: no queries
	 *            can be executed with this vehicle anymore
	 */
	void vehicleArrived(Vehicle vehicle);

	/**
	 * Method called when a vehicle has begun a teleport. While a vehicle is
	 * teleporting, some queries may not be available and may cause an error.
	 * This method will be called during an invocation of
	 * {@link SumoTraciConnection#nextSimStep()}.
	 * 
	 * @param vehicle
	 *            the vehicle that just begun teleporting. Note: queries
	 *            executed before teleport end may lead to inconsistent results
	 */
	void vehicleTeleportStarting(Vehicle vehicle);

	/**
	 * Method called when a vehicle has ended a teleport. While a vehicle is
	 * teleporting, some queries may not be available and may cause an error.
	 * This method will be called during an invocation of
	 * {@link SumoTraciConnection#nextSimStep()}.
	 * 
	 * @param vehicle
	 *            the vehicle that just ended teleporting
	 */
	void vehicleTeleportEnding(Vehicle vehicle);
}
