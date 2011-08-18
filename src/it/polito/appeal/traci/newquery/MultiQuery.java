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

package it.polito.appeal.traci.newquery;

import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.RequestMessage;
import it.polito.appeal.traci.protocol.ResponseContainer;
import it.polito.appeal.traci.protocol.ResponseMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MultiQuery {
	private final DataOutputStream dos;
	private final DataInputStream dis;
	
	private final List<Query> queries = new ArrayList<Query>();
	
	public MultiQuery(DataOutputStream dos, DataInputStream dis) {
		this.dos = dos;
		this.dis = dis;
	}
	
	public void add(Query query) {
		queries.add(query);
	}
	
	public boolean remove(Query query) {
		return queries.remove(query);
	}
	
	public void sendRequestsAndDispatchResponses() throws IOException {
		RequestMessage reqMsg = new RequestMessage();
		for (Query q : queries) {
			for (Command req : q.getRequests()) {
				reqMsg.append(req);
			}
		}
		
		reqMsg.writeTo(dos);
		ResponseMessage respMsg = new ResponseMessage(dis);
		Iterator<ResponseContainer> responseIterator = respMsg.responses().iterator();
		for (Query q : queries) {
			q.pickResponses(responseIterator);			
		}
	}
}
