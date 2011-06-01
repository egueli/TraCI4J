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

package it.polito.appeal.traci.query;

import it.polito.appeal.traci.TraCIException;
import it.polito.appeal.traci.TraCIException.UnexpectedData;
import de.uniluebeck.itm.tcpip.Socket;
import de.uniluebeck.itm.tcpip.Storage;

public abstract class RoadmapQuery extends DomainQuery {

	protected final int nodeID;

	public RoadmapQuery(Socket sock, int edgeID) {
		super(sock);
		this.nodeID = edgeID;
	}

	protected Storage makeCommand(int variable, int varType)
			throws TraCIException {
		return makeCommand(DOMAIN_ROADMAP, nodeID, variable, varType);
	}

	protected void readAndCheckResponse(Storage response, int variable,
			int varType) throws UnexpectedData {
		readAndCheckResponse(response, DOMAIN_ROADMAP, nodeID, variable,
				varType);
	}
}
