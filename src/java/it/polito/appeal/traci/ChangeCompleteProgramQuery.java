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

import it.polito.appeal.traci.protocol.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import de.uniluebeck.itm.tcpip.Storage;

public class ChangeCompleteProgramQuery extends ChangeObjectVarQuery<Logic> {

	public ChangeCompleteProgramQuery(DataInputStream dis,
			DataOutputStream dos, String objectID, int commandID, int variableID) {
		super(dis, dos, commandID, objectID, variableID);
	}

	@Override
	protected void writeValueTo(Logic program, Storage content) {
		content.writeByte(Constants.TYPE_COMPOUND);
		content.writeInt(0); // item number is ignored anyway.
		content.writeByte(Constants.TYPE_STRING);
		content.writeStringASCII(program.getSubID());
		content.writeByte(Constants.TYPE_INTEGER);
		content.writeInt(0); // Type (always 0);
		content.writeByte(Constants.TYPE_COMPOUND);
		content.writeInt(0); // Compound length (always 0!)
		content.writeByte(Constants.TYPE_INTEGER);
		content.writeInt(program.getCurrentPhaseIndex()); // Phase index
		content.writeByte(Constants.TYPE_INTEGER);
		content.writeInt(program.getPhases().length);
		for (Phase phase : program.getPhases()) {
			content.writeByte(Constants.TYPE_INTEGER);
			content.writeInt(phase.getDuration());
			content.writeByte(Constants.TYPE_INTEGER);
			content.writeInt(0); // unused
			content.writeByte(Constants.TYPE_INTEGER);
			content.writeInt(0); // unused
			content.writeByte(Constants.TYPE_STRING);
			content.writeStringASCII(phase.getState().toString());
		}
	}
}
