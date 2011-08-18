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
import it.polito.appeal.traci.query.ChangeVehicleStateQuery;
import it.polito.appeal.traci.query.ReadVehicleVarQuery;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.net.Socket;

/**
 * Represents a single vehicle in the simulation. Instances of this class can be
 * used both to obtain info and to modify vehicle's behaviour, like changing the
 * route.
 * <p>
 * Before using any of the methods that interact with SUMO, the user must ensure
 * that the vehicle referred by this instance is still running in the
 * simulation, otherwise a {@link NotActiveException} is thrown.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * 
 */
public class Vehicle {

	public static final String NAME_UNABLE_TO_QUERY = "[unable to query]";

	/**
	 * This exception is thrown when SUMO was requested to do some action about
	 * a vehicle that doesn't currently exist in the simulation.
	 * 
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 * 
	 */
	public static class NotActiveException extends Exception {
		private static final long serialVersionUID = -553158628106975541L;

		public NotActiveException() {
		}

		public NotActiveException(String arg0) {
			super(arg0);
		}

		public NotActiveException(Throwable arg0) {
			super(arg0);
		}

		public NotActiveException(String arg0, Throwable arg1) {
			super(arg0, arg1);
		}

	}

	private final Socket socket;
	private SumoTraciConnection conn;
	private final String id;
	private List<String> routeCache;
	private final int creationTime;
	
	private RoadmapPosition currentEdgeCache;
	private int currentEdgeCacheTimestep;
	
	/** set to false by SumoTraciConnection when a vehicle is no longer active */
	boolean alive;
	
	boolean teleport;
	
	private Point2D position;

	Vehicle(String id, Socket socket, SumoTraciConnection conn) {
		this.id = id;
		this.socket = socket;
		this.conn = conn;
		this.creationTime = conn.getCurrentSimStep();
		alive = true;
	}

	/**
	 * Returns the ID of this vehicle.
	 */
	public String getID() {
		return id;
	}

	/**
	 * Returns the SUMO's string identifier.
	 * 
	 * @deprecated version 2 of the TraCI API now use the string ID only, i.e.
	 * no numeri ID. getID() can be used instead.
	 * @return the vehicles' string ID, or &quot;{@value #NAME_UNABLE_TO_QUERY}
	 *         &quot; if the vehicle doesn't currently exist.
	 * @throws IOException
	 */
	@Deprecated
	public String getName() {
		return getID();
	}

	/**
	 * Returns the vehicle's route, represented as an ordered list of its roads.
	 * 
	 * @throws IOException
	 * @throws NotActiveException
	 */
	public List<String> getCurrentRoute() throws IOException,
			NotActiveException {

		if(routeCache == null)
			routeCache = queryRoute();

		return routeCache;
	}

	private List<String> queryRoute() throws IOException, NotActiveException {
		if (!alive)
			throw new NotActiveException();

		List<String> route = (new ReadVehicleVarQuery(socket, id)).queryRoute();
		return route;
	}

	/**
	 * Returns the current X/Y coordinates of the vehicle in the simulation
	 * field.
	 * 
	 * @throws IOException
	 * @throws NotActiveException
	 */
	public Point2D queryCurrentPosition2D() throws IOException,
			NotActiveException {
		if (!alive)
			throw new NotActiveException();

//		Point2D readPosition = (new VehiclePositionQuery(socket, id)).getPosition2D();
		if(position == null)
			throw new IllegalStateException("should never happen: position wasn't set");
		
		return position;
	}

	/**
	 * Returns the road the vehicle is currently running into.
	 * 
	 * @throws IOException
	 * @throws NotActiveException
	 */
	public String queryCurrentEdge() throws IOException, NotActiveException {
		if (!alive)
			throw new NotActiveException();

		int currentSimStep = conn.getCurrentSimStep();
		if (currentEdgeCache == null || (currentEdgeCacheTimestep < currentSimStep)) {
			currentEdgeCache = (new ReadVehicleVarQuery(socket, id))
				.queryPositionRoadmap();
			currentEdgeCacheTimestep = currentSimStep;
		}
		return currentEdgeCache.edgeID; 
	}

	/**
	 * Try to change the vehicle's route by changing the estimated time to
	 * travel the given road.
	 * <p>
	 * According to the SUMO documentation, the default travel time of a road is
	 * the product between the road's max speed and its length. If the travel
	 * time set via this method is greater than the default travel time, SUMO
	 * will try to find a faster route; if it finds none, the route will be
	 * unchanged.
	 * See also <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Mobility-related_commands#Command_0x30:_Change_Route">the documentation in the Wiki</a>.
	 * @param edgeID
	 * @param travelTime
	 * @throws IOException
	 * @throws NotActiveException
	 */
	public void setEdgeTravelTime(String edgeID, Number travelTime)
			throws IOException, NotActiveException {
		
		setEdgeTravelTimeMany(Collections.singletonMap(edgeID, travelTime));
	}

	/**
	 * Try to change the vehicle's route by changing the estimated time to
	 * travel the roads specified in the map.
	 * See also <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Mobility-related_commands#Command_0x30:_Change_Route">the documentation in the Wiki</a>.
	 * @see #setEdgeTravelTime(String, Number)
	 * @param travelTimes
	 * @throws NotActiveException
	 * @throws IOException
	 */
	public void setEdgeTravelTimeMany(Map<String, ? extends Number> travelTimes)
			throws NotActiveException, IOException {
		if (!alive)
			throw new NotActiveException();

		ChangeVehicleStateQuery cvsq = new ChangeVehicleStateQuery(socket, id);
		
		for (Entry<String, ? extends Number> entry : travelTimes.entrySet()) {
			cvsq.changeEdgeTravelTime(0, Integer.MAX_VALUE, entry.getKey(),
					entry.getValue().doubleValue());			
		}
		
		cvsq.reroute();
		clearRouteCache();
	}
	
	public void setMaxSpeed(double maxSpeed) throws NotActiveException, IOException {
		if (!alive)
			throw new NotActiveException();

		ChangeVehicleStateQuery cvsq = new ChangeVehicleStateQuery(socket, id);
		cvsq.setMaxSpeed(maxSpeed);
	}
	
	public String toString() {
		String currentEdge;
		try {
			currentEdge = queryCurrentEdge();
		} catch (Exception e) {
			currentEdge = NAME_UNABLE_TO_QUERY;
		}

		return "id=" + id + " edge=" + currentEdge;
	}

	/**
	 * Returns true if the vehicle is currently active in the simulation.
	 * This method should be called before every invocation of other methods,
	 * in order to avoid {@link NotActiveException}s.
	 */
	public boolean isAlive() {
		return alive;
	}
	
	public boolean isTeleporting() {
		return teleport;
	}

	/**
	 * Returns the {@link Lane} object matching the specified road ID.
	 * @see SumoTraciConnection#getLane(String)
	 * @param roadID
	 * @throws IOException
	 */
	public Lane getLane(String roadID) throws IOException {
		return conn.getLane(roadID);
	}
	
	public Map<String, Lane> getLanesMap() throws IOException {
		return conn.getLanesMap();
	}

	void setPosition(Point2D pos) {
		position = pos;
	}

	/**
	 * Returns the creation time, i.e. the SUMO departure time.
	 */
	public int getCreationTime() {
		return creationTime;
	}

	/**
	 * (Re-)makes a computation of the route of this vehicle, using local,
	 * global and default travel times. This method should be called after
	 * {@link SumoTraciConnection#changeEdgeTravelTime(int, int, String, float)}
	 * to be effective.
	 * @see SumoTraciConnection#changeEdgeTravelTime(int, int, String, float)
	 * @throws NotActiveException
	 * @throws IOException
	 */
	public void reroute() throws NotActiveException, IOException {
		if (!alive)
			throw new NotActiveException();
		
		ChangeVehicleStateQuery rq = new ChangeVehicleStateQuery(socket,
				id);
		rq.reroute();
		
		clearRouteCache();
	}

	void clearRouteCache() {
		routeCache = null;
	}

	void setCurrentRoadmapPos(RoadmapPosition pos) {
		currentEdgeCache = pos;
		currentEdgeCacheTimestep = conn.getCurrentSimStep();
	}
	
	/**
	 * @see <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Change_Vehicle_State">Change Vehicle State</a>
	 * @param edgeID
	 * @throws NotActiveException
	 * @throws IOException
	 */
	public void changeTarget(String edgeID) throws NotActiveException, IOException {
		if (!alive)
			throw new NotActiveException();
		
		ChangeVehicleStateQuery rq = new ChangeVehicleStateQuery(socket, id);
		rq.changeTarget(edgeID);
	}
	
	public void changeRoute(List<String> newRoute) throws NotActiveException, IOException {
		if (!alive)
			throw new NotActiveException();
		
		ChangeVehicleStateQuery rq = new ChangeVehicleStateQuery(socket, id);
		rq.changeRoute(newRoute);
	}
}
