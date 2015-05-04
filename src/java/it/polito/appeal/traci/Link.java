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

import static it.polito.appeal.traci.Utils.checkType;
import it.polito.appeal.traci.protocol.Constants;

import java.io.IOException;

import de.uniluebeck.itm.tcpip.Storage;

/**
 * Describes a link between a given lane and another lane in a junction.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * @see <a href="http://sumo.sourceforge.net/doc/current/docs/userdoc/TraCI/Lane_Value_Retrieval.html">TraCI docs</a>
 */
public class Link {
	private final Lane nextInternalLane;
	private final Lane nextNonInternalLane;
	private final boolean hasPriority;
	private final boolean isOpened;
	private final boolean hasApproachingFoe;
	private final String currentState;
	private final String direction;
	private final double length;
	
	protected Link(Storage content, Repository<Lane> laneRepo) throws IOException {
		checkType(content, Constants.TYPE_STRING);
		// let's hope they don't point to this lane recursively!
		nextNonInternalLane = laneRepo.getByID(content.readStringASCII());
		checkType(content, Constants.TYPE_STRING);
		nextInternalLane = laneRepo.getByID(content.readStringASCII());
		checkType(content, Constants.TYPE_UBYTE);
		hasPriority = content.readUnsignedByte() > 0;
		checkType(content, Constants.TYPE_UBYTE);
		isOpened = content.readUnsignedByte() > 0;
		checkType(content, Constants.TYPE_UBYTE);
		hasApproachingFoe = content.readUnsignedByte() > 0;
		checkType(content, Constants.TYPE_STRING);
		currentState = content.readStringASCII();
		checkType(content, Constants.TYPE_STRING);
		direction = content.readStringASCII();
		checkType(content, Constants.TYPE_DOUBLE);
		length = content.readDouble();
	}
	
	/**
	 * @return the internal lane in the junction
	 */
	public Lane getNextInternalLane() {
		return nextInternalLane;
	}
	
	/**
	 * @return the outgoing lane that this link connects to
	 */
	public Lane getNextNonInternalLane() {
		return nextNonInternalLane;
	}
	
	/**
	 * @return <code>true</code> if this links has priority over other links in the junction
	 */
	public boolean hasPriority() {
		return hasPriority;
	}
	
	/**
	 * @return an unknown value
	 */
	public boolean isOpened() {
		return isOpened;
	}
	
	/**
	 * @return an unknown value
	 */
	public boolean hasApproachingFoe() {
		return hasApproachingFoe;
	}
	
	/**
	 * @return always an empty string (not implemented by SUMO)
	 */
	public String getCurrentState() {
		return currentState;
	}
	
	/**
	 * @return always an empty string (not implemented by SUMO)
	 */
	public String getDirection() {
		return direction;
	}

	/**
	 * @return the length of the link, or 0 if using internal lanes
	 */
	public double getLength() {
		return length;
	}
}