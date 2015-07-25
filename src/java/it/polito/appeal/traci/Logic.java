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

import it.polito.appeal.traci.TraCIException.UnexpectedDatatype;
import it.polito.appeal.traci.protocol.Constants;
import de.uniluebeck.itm.tcpip.Storage;

/**
 * Represents a TLS program.
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * @see <a href="http://sumo.sourceforge.net/doc/current/docs/userdoc/Simulation/Traffic_Lights.html#Loading_new_TLS-Programs">SUMO documentation</a>
 */
public class Logic {
	private final String subID;
	private final int currentPhaseIndex;
	private final Phase[] phases;
	Logic(Storage content) throws UnexpectedDatatype {
		Utils.checkType(content, Constants.TYPE_STRING);
		subID = content.readStringASCII();
		Utils.checkType(content, Constants.TYPE_INTEGER);
		content.readInt(); // type ignored
		Utils.checkType(content, Constants.TYPE_COMPOUND);
		int compSize = content.readInt();
		for (int i=0; i<compSize; i++)
			content.readByte(); // ignore compound type SubParameter
		Utils.checkType(content, Constants.TYPE_INTEGER);
		currentPhaseIndex = content.readInt();
		Utils.checkType(content, Constants.TYPE_INTEGER);
		int nPhases = content.readInt();
		phases = new Phase[nPhases];
		for (int i=0; i<nPhases; i++) {
			phases[i] = new Phase(content);
		}
	}
	
	/**
	 * Creates a new TLS program
	 * @param subID the program's string ID
	 * @param currentPhaseIndex the index in the phases array to begin from
	 * @param phases the ordered list of traffic light phases over time (see {@link Phase})
	 */
	public Logic(String subID, int currentPhaseIndex, Phase[] phases) {
		this.subID = subID;
		this.currentPhaseIndex = currentPhaseIndex;
		this.phases = phases;
	}
	
	/**
	 * 
	 * @return the program's string ID
	 */
	public String getSubID() {
		return subID;
	}
	
	/**
	 * 
	 * @return the current phase index
	 * @see #getPhases()
	 */
	public int getCurrentPhaseIndex() {
		return currentPhaseIndex;
	}
	
	/**
	 * 
	 * @return the ordered list of traffic light phases over time (see {@link Phase}) 
	 */
	public Phase[] getPhases() {
		return phases;
	}
}