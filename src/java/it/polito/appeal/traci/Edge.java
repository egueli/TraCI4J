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

import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.protocol.ResponseContainer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Iterator;


public class Edge extends TraciObject<Edge.Variable> {

	public static enum Variable {
		TRAVEL_TIME(Constants.VAR_EDGE_TRAVELTIME)
		;
		
		public final int id; 
		private Variable(int id) {
			this.id = id;
		}
	}
	
	private final ChangeGlobalTravelTimeQuery changeTravelTimeQuery;
	
	Edge(DataInputStream dis, DataOutputStream dos, String id) {
		super(id, Variable.class);
		
		addReadQuery(Variable.TRAVEL_TIME, new ReadGlobalTravelTimeQuery(dis, dos,
				Constants.CMD_GET_EDGE_VARIABLE, id, Variable.TRAVEL_TIME.id));
		
		changeTravelTimeQuery = new ChangeGlobalTravelTimeQuery(dis, dos, id) {
			@Override
			void pickResponses(Iterator<ResponseContainer> responseIterator)
					throws TraCIException {
				super.pickResponses(responseIterator);
				
				queryReadGlobalTravelTime().setObsolete();
			}
		};
	}
	
	public ReadGlobalTravelTimeQuery queryReadGlobalTravelTime() {
		return (ReadGlobalTravelTimeQuery) getReadQuery(Variable.TRAVEL_TIME);
	}

	public ChangeGlobalTravelTimeQuery queryChangeTravelTime() {
		return changeTravelTimeQuery;
	}
}


