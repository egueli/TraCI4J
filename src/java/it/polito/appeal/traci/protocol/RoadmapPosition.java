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

package it.polito.appeal.traci.protocol;

/**
 * Representation of a position inside a road, identified by the edge ID, the
 * lane index and position in meters from the beginning of the road.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public class RoadmapPosition {
	/** the ID of the edge. */
	public final String edgeID;
	
	/** the driving distance in meters from the edge's begin point */
	public final double pos;
	
	/** the lane index, starting from zero */
	public final int laneID;
	
	/**
	 * Constructor from a given edge ID, distance from begin and lane index.
	 * 
	 * @param edgeID
	 * @param pos
	 * @param laneID
	 */
	public RoadmapPosition(String edgeID, double pos, int laneID) {
		this.edgeID = edgeID;
		this.pos = pos;
		this.laneID = laneID;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) 
			return false;
		if (obj == this) 
			return true;
		if (!(obj instanceof RoadmapPosition)) 
			return false;
		
		RoadmapPosition that = (RoadmapPosition)obj;
		if (laneID != that.laneID)
			return false;
		
		if (Math.abs(pos - that.pos) > 1e-6)
			return false;
		
		if (!edgeID.equals(that.edgeID))
			return false;
		
		return true;
	}

	@Override
	public int hashCode() {
		return edgeID.hashCode() ^ laneID ^ (new Double(pos)).hashCode();
	}

	@Override
	public String toString() {
		return edgeID + " lane " + laneID + " pos " + Math.round(pos*100)/100; 
	}
	
	
}

