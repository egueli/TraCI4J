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

import java.io.DataInputStream;
import java.io.DataOutputStream;

import de.uniluebeck.itm.tcpip.Storage;

/**
 * Query for reading the complete program of a TLS.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * @see <a href="http://sumo.sourceforge.net/doc/current/docs/userdoc/TraCI/Traffic_Lights_Value_Retrieval.html">TraCI docs</a>
 */
public class ReadCompleteDefinitionQuery extends ReadObjectVarQuery<Program> {
	ReadCompleteDefinitionQuery(DataInputStream dis,
			DataOutputStream dos, int commandID, String objectID, int varID) {
		super(dis, dos, commandID, objectID, varID);
	}

	@Override
	protected Program readValue(Command resp) throws TraCIException {
		Storage content = resp.content();
		return new Program(content);
	}
	
}