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
import java.util.ArrayList;
import java.util.List;

import de.uniluebeck.itm.tcpip.Storage;

/**
 * Query for obtaining the {@link Link}s connected to the end of a given 
 * {@link Lane}.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public class ReadLinksQuery extends ReadObjectVarQuery<List<Link>> {

	private Repository<Lane> laneRepo;

	ReadLinksQuery(DataInputStream dis, DataOutputStream dos,
			int commandID, String objectID, int varID, Repository<Lane> laneRepo) {
		super(dis, dos, commandID, objectID, varID);
		this.laneRepo = laneRepo;
	}

	@Override
	protected List<Link> readValue(Command resp) throws TraCIException {
		List<Link> out = new ArrayList<Link>();
		Storage content = resp.content();
		Utils.checkType(content, Constants.TYPE_COMPOUND);
		content.readInt(); // ignore link data length
		Utils.checkType(content, Constants.TYPE_INTEGER);
		int count = content.readInt();
		for (int i=0; i<count; i++) {
			try {
				out.add(new Link(content, laneRepo));
			} catch (IOException e) {
				throw new TraCIException(e.toString());
			}
		}
		return out;
	}
	
}