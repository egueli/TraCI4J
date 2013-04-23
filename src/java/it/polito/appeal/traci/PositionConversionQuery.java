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

/**
 * 
 */
package it.polito.appeal.traci;

import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.protocol.RoadmapPosition;

import java.awt.geom.Point2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;

/**
 * Query for position conversion.
 * <p>
 * This query can output only a 2D position, i.e. either local X/Y coordinates or
 * Longitude-Latitude coordinates. For both systems, the result is always a {@link Point2D}
 * object.
 * <p>
 * Although this class is a subclass of {@link ReadObjectVarQuery}, the related value is not
 * really a simulation-related value, but the result of the conversion. 
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * @see <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Simulation_Value_Retrieval#Command_0x82:_Position_Conversion">Position Conversion</a>
 * @see <a href="http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Protocol#Position_Representations">Position Representations</a>
 */
public class PositionConversionQuery extends ReadObjectVarQuery.PositionQ {

	private Point2D cartesianPos;
	private boolean srcLonLat;
	private RoadmapPosition roadmapPos;
	private boolean destLonLat;
	
	PositionConversionQuery(DataInputStream dis, DataOutputStream dos,
			int commandID, String objectID, int varID) {
		super(dis, dos, commandID, objectID, varID);
	}
	
	/**
	 * Set the data for a 2D position conversion.
	 * 
	 * @param pos
	 *            the position, either in local cartesian coordinates or
	 *            longitude/latitude
	 * @param outputLonLat
	 *            <code>true</code> if the coordinates should be converted to
	 *            latitude/longitude
	 */
	public void setPositionToConvert(Point2D pos, boolean outputLonLat) {
		if (destLonLat != outputLonLat || roadmapPos != null || pos.equals(cartesianPos))
			setObsolete();
		
		cartesianPos = pos;
		srcLonLat = !outputLonLat;
		roadmapPos = null;
		destLonLat = outputLonLat;
	}

	/**
	 * Set the data for a conversion from roadmap position to local. The result will
	 * be in longitude/latitude format.
	 * 
	 * @param pos
	 *            the roadmap position
	 */
	public void setPositionToConvert(RoadmapPosition pos) {
		if (cartesianPos != null || pos.equals(roadmapPos))
			setObsolete();
		
		cartesianPos = null;
		roadmapPos = pos;
	}

	@Override
	List<Command> getRequests() {
		if (roadmapPos == null && cartesianPos == null)
			throw new IllegalStateException("position must be set first");
		
		List<Command> reqs = super.getRequests();
		Command req = reqs.iterator().next();
		req.content().writeByte(Constants.TYPE_COMPOUND);
		req.content().writeInt(2);
		
		

		if (cartesianPos != null) { // conversion 2D-to-2D
			int srcType;
			double x;
			double y;
			if (srcLonLat) {
				srcType = Constants.POSITION_LAT_LON;
				// TraCI wants lat-long, which is vertical-horizontal
				x = cartesianPos.getY();
				y = cartesianPos.getX();
			}
			else {
				srcType = Constants.POSITION_2D;
				x = cartesianPos.getX();
				y = cartesianPos.getY();
			}
						
			int destType = destLonLat ? Constants.POSITION_LAT_LON : Constants.POSITION_2D;
			
			req.content().writeUnsignedByte(srcType);
			req.content().writeDouble(x);			
			req.content().writeDouble(y);
			req.content().writeUnsignedByte(Constants.TYPE_UBYTE);
			req.content().writeUnsignedByte(destType);		
			setPositionType(destType);
		}
		else {
			req.content().writeUnsignedByte(Constants.POSITION_ROADMAP);
			req.content().writeStringUTF8(roadmapPos.edgeID);
			req.content().writeDouble(roadmapPos.pos);
			req.content().writeUnsignedByte(roadmapPos.laneID);
			req.content().writeUnsignedByte(Constants.TYPE_UBYTE);
			req.content().writeUnsignedByte(Constants.POSITION_LAT_LON);
			setPositionType(Constants.POSITION_LAT_LON);
		}
		
		
		return reqs;
	}
	
}