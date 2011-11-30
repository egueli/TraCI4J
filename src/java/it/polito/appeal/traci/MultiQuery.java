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

import org.apache.log4j.Logger;

/**
 * Represents a set of {@link Query}s to run as a whole, i.e. within a single
 * SUMO request and response message. This may significantly reduce the
 * execution time, because the TCP overhead is distributed along all the queries 
 * (JUnit tests show a performance increase of ~20x in an Intel T7300 dual
 * core CPU with Linux 2.6.38).
 * <p>
 * When the queries are added to this class with {@link #add(Query)}, they can
 * be executed with {@link #run()}.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public class MultiQuery {
	
	private static final Logger log = Logger.getLogger(MultiQuery.class);
	
	private final DataOutputStream dos;
	private final DataInputStream dis;
	
	private final List<Query> queries = new ArrayList<Query>();
	
	MultiQuery(DataOutputStream dos, DataInputStream dis) {
		this.dos = dos;
		this.dis = dis;
	}
	
	public void add(Query query) {
		queries.add(query);
	}
	
	public boolean remove(Query query) {
		return queries.remove(query);
	}
	
	public void run() throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("Running a batch of " + queries.size() + " queries");
		}
		
		if(queries.isEmpty())
			return;
		
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
