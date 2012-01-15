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

import it.polito.appeal.traci.TraCIException.UnexpectedDatatype;
import it.polito.appeal.traci.protocol.Constants;
import de.uniluebeck.itm.tcpip.Storage;

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
	public Logic(String subID, int currentPhaseIndex, Phase[] phases) {
		this.subID = subID;
		this.currentPhaseIndex = currentPhaseIndex;
		this.phases = phases;
	}
	public String getSubID() {
		return subID;
	}
	public int getCurrentPhaseIndex() {
		return currentPhaseIndex;
	}
	public Phase[] getPhases() {
		return phases;
	}
}