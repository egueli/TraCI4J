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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A query for reading an ordered list of {@link Edge}s that make a route.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public class RouteQuery extends ObjectCollectionQuery<Edge, List<Edge>> {

	RouteQuery(final DataInputStream dis, final DataOutputStream dos, int commandID, String vehicleID, int varID, Repository<Edge> repo) {
		super(dis, dos, commandID, repo, vehicleID, varID);
	}

	@Override
	protected List<Edge> makeCollection() {
		return new ArrayList<Edge>();
	}
	
}