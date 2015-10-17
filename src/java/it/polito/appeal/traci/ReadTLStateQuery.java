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

import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Query for reading a TLS state.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * @see <a href="http://sumo.sourceforge.net/doc/current/docs/userdoc/TraCI/Traffic_Lights_Value_Retrieval.html">TraCI docs</a>
 *
 */
public class ReadTLStateQuery extends ReadObjectVarQuery<TLState> {

	ReadTLStateQuery(DataInputStream dis, DataOutputStream dos,
			int commandID, String objectID, int varID) {
		super(dis, dos, commandID, objectID, varID);
	}

	@Override
	protected TLState readValue(Command resp) throws TraCIException {
		Utils.checkType(resp.content(), Constants.TYPE_STRING);
		String desc = resp.content().readStringASCII();
		return new TLState(desc);
	}
}