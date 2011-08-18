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

import it.polito.appeal.traci.TraCIException;
import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.protocol.ResponseContainer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Iterator;
import java.util.List;

import de.uniluebeck.itm.tcpip.Storage;

public class Edge extends TraciObject<Edge.Variable> {

	public static enum Variable {
		TRAVEL_TIME(Constants.VAR_EDGE_TRAVELTIME)
		;
		
		public final int id; 
		private Variable(int id) {
			this.id = id;
		}
	}
	
	public class ChangeGlobalTravelTimeQuery extends ChangeObjectStateQuery {

		private int beginTime;
		private int endTime;
		private double travelTime;

		public ChangeGlobalTravelTimeQuery(DataInputStream dis, DataOutputStream dos,
				String edgeID) {
			super(dis, dos, Constants.CMD_SET_EDGE_VARIABLE, edgeID, Constants.VAR_EDGE_TRAVELTIME);
		}
		
		/**
		 * @param beginTime the beginTime to set
		 */
		public void setBeginTime(int beginTime) {
			this.beginTime = beginTime;
		}

		/**
		 * @param endTime the endTime to set
		 */
		public void setEndTime(int endTime) {
			this.endTime = endTime;
		}

		/**
		 * @param travelTime the travelTime to set
		 */
		public void setTravelTime(double travelTime) {
			this.travelTime = travelTime;
		}

		@Override
		protected void writeParamsTo(Storage content) {
			content.writeByte(Constants.TYPE_COMPOUND);
			content.writeInt(3);

			content.writeByte(Constants.TYPE_INTEGER);
			content.writeInt(beginTime);
			content.writeByte(Constants.TYPE_INTEGER);
			content.writeInt(endTime);
			content.writeByte(Constants.TYPE_DOUBLE);
			content.writeDouble(travelTime);
		}
		
		@Override
		void pickResponses(Iterator<ResponseContainer> responseIterator)
				throws TraCIException {
			super.pickResponses(responseIterator);
			
			queryReadGlobalTravelTime().setObsolete();
		}
	}

	public static class ReadGlobalTravelTimeQuery extends ReadObjectVarQuery.DoubleQ {

		private int time = -1;
		
		ReadGlobalTravelTimeQuery(DataInputStream dis, DataOutputStream dos,
				int commandID, String objectID, int varID) {
			super(dis, dos, commandID, objectID, varID);
		}
		
		public void setTime(int time) {
			/*
			 * if the time is modified, forget the old value
			 */
			if (this.time != time)
				setObsolete();
			
			this.time = time;
		}

		@Override
		List<Command> getRequests() {
			if (time == -1)
				throw new IllegalStateException("time must be set first");
			
			List<Command> reqs = super.getRequests();
			Command req = reqs.iterator().next();
			req.content().writeByte(Constants.TYPE_INTEGER);
			req.content().writeInt(time);
			return reqs;
		}
		
	}
	
	private final ChangeGlobalTravelTimeQuery changeTravelTimeQuery;
	
	Edge(DataInputStream dis, DataOutputStream dos, String id) {
		super(id, Variable.class);
		
		addReadQuery(Variable.TRAVEL_TIME, new ReadGlobalTravelTimeQuery(dis, dos,
				Constants.CMD_GET_EDGE_VARIABLE, id, Variable.TRAVEL_TIME.id));
		
		changeTravelTimeQuery = new ChangeGlobalTravelTimeQuery(dis, dos, id);
	}
	
	ReadGlobalTravelTimeQuery queryReadGlobalTravelTime() {
		return (ReadGlobalTravelTimeQuery) getReadQuery(Variable.TRAVEL_TIME);
	}

	public ChangeGlobalTravelTimeQuery queryChangeTravelTime() {
		return changeTravelTimeQuery;
	}
}


