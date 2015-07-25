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
import java.io.IOException;

import de.uniluebeck.itm.tcpip.Storage;

/**
 * Query for obtaining the controlled links in a TLS.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * @see <a href="http://sumo.sourceforge.net/doc/current/docs/userdoc/TraCI/Traffic_Lights_Value_Retrieval.html">TraCI docs</a>
 */
public class ReadControlledLinksQuery extends ReadObjectVarQuery<ControlledLinks> {

	private final Repository<Lane> laneRepo;

	ReadControlledLinksQuery(DataInputStream dis, DataOutputStream dos,
			int commandID, String objectID, int varID, Repository<Lane> laneRepo) {
		super(dis, dos, commandID, objectID, varID);
		this.laneRepo = laneRepo;
	}

	@Override
	protected ControlledLinks readValue(Command resp)
			throws TraCIException {
		final Storage content = resp.content();
		Utils.checkType(content, Constants.TYPE_COMPOUND);
		content.readInt(); // ignore data length
		try {
			return new ControlledLinks(content, laneRepo);
		} catch (IOException e) {
			throw new TraCIException(e.getMessage());
		}
	}
	
}