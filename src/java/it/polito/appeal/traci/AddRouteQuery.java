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
import it.polito.appeal.traci.protocol.StringList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import de.uniluebeck.itm.tcpip.Storage;

/**
 * Query for adding a new route in the simulation.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public class AddRouteQuery extends ChangeStateQuery {

	private String id;
	private StringList edges;
	private final Repository<Route> routes;

	AddRouteQuery(DataInputStream dis, DataOutputStream dos,
			Repository<Route> routes) {
		super(dis, dos, Constants.CMD_SET_ROUTE_VARIABLE);
		this.routes = routes;
	}

	/**
	 * Sets the parameters for the new route.
	 * @see <a href="http://sumo.sourceforge.net/doc/current/docs/userdoc/TraCI/Change_Route_State.html">TraCI docs</a>
	 * @param id
	 * @param edges
	 * @throws IOException
	 */
	public void setVehicleData(
			String id,
			List<Edge> edges) 
	throws IOException {
		
		if (routes.getByID(id) != null)
			throw new IllegalArgumentException("route already exists");
		
		this.id = id;
		
		this.edges = new StringList();
		for (Edge edge : edges) {
			this.edges.add(edge.getID());
		}
	}
	
	@Override
	protected void writeRequestTo(Storage content) {
		content.writeUnsignedByte(Constants.ADD);
		
		content.writeStringASCII(id);
		edges.writeTo(content, true);
		
		routes.getQuery().setObsolete();
	}
	
}