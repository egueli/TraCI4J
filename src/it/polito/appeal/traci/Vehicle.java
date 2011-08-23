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
import it.polito.appeal.traci.protocol.StringList;

import java.awt.geom.Point2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Iterator;
import java.util.List;

import de.uniluebeck.itm.tcpip.Storage;

public class Vehicle extends TraciObject<Vehicle.Variable> implements StepAdvanceListener {
	
	
	public static class ChangeEdgeTravelTimeQuery extends ChangeObjectStateQuery {

		private Edge edge;
		private double travelTime;

		public ChangeEdgeTravelTimeQuery(DataInputStream dis, DataOutputStream dos,
				String objectID) {
			super(dis, dos, Constants.CMD_SET_VEHICLE_VARIABLE, objectID, Constants.VAR_EDGE_TRAVELTIME);
		}

		@Override
		protected void writeParamsTo(Storage content) {
			content.writeByte(Constants.TYPE_COMPOUND);
			content.writeInt(2);
			content.writeByte(Constants.TYPE_STRING);
			content.writeStringASCII(edge.getID());
			content.writeByte(Constants.TYPE_DOUBLE);
			content.writeDouble(travelTime);
		}

		public void setEdge(Edge edge) {
			this.edge = edge;
		}

		public void setTravelTime(double travelTime) {
			this.travelTime = travelTime;
		}

	}
	
	public static class ChangeTargetQuery extends ChangeObjectStateQuery {

		private Edge newTarget;
		
		public ChangeTargetQuery(DataInputStream dis, DataOutputStream dos,
				String objectID) {
			super(dis, dos, Constants.CMD_SET_VEHICLE_VARIABLE, objectID, Constants.CMD_CHANGETARGET);
		}
		
		public void setNewTarget(Edge newTarget) {
			this.newTarget = newTarget;
		}

		@Override
		protected void writeParamsTo(Storage content) {
			content.writeByte(Constants.TYPE_STRING);
			content.writeStringASCII(newTarget.getID());
		}
	}

	public static class ChangeRouteQuery extends ChangeObjectStateQuery {

		private List<Edge> newRoute;
		
		public ChangeRouteQuery(DataInputStream dis, DataOutputStream dos,
				String objectID) {
			super(dis, dos, Constants.CMD_SET_VEHICLE_VARIABLE, objectID, Constants.VAR_ROUTE);
		}
		
		public void setNewRoute(List<Edge> newRoute) {
			this.newRoute = newRoute;
		}

		@Override
		protected void writeParamsTo(Storage content) {
			StringList edgeIDs = new StringList();
			for (Edge e : newRoute)
				edgeIDs.add(e.getID());
			
			edgeIDs.writeTo(content, true);
		}
	}

	
	public static class RerouteQuery extends ChangeObjectStateQuery {

		public RerouteQuery(DataInputStream dis, DataOutputStream dos,
				String objectID) {
			super(dis, dos, Constants.CMD_SET_VEHICLE_VARIABLE, objectID, Constants.CMD_REROUTE_TRAVELTIME);
		}

		@Override
		protected void writeParamsTo(Storage content) {
			content.writeByte(Constants.TYPE_COMPOUND);
			content.writeInt(0);
		}
	}

	
	static enum Variable {
		SPEED(Constants.VAR_SPEED),
		POSITION(Constants.VAR_POSITION),
		ANGLE(Constants.VAR_ANGLE),
		CURRENT_EDGE(Constants.VAR_ROAD_ID),
		CURRENT_LANE(Constants.VAR_LANE_ID),
		ROUTE(Constants.VAR_ROUTE)
		;
		
		public final int id; 
		private Variable(int id) {
			this.id = id;
		}
	}
	
	private final ChangeEdgeTravelTimeQuery edgeTravelTimeQuery;
	private final RerouteQuery rerouteQuery;
	private final ChangeTargetQuery changeTargetQuery;
	private final ChangeRouteQuery changeRouteQuery;
	
	Vehicle(DataInputStream dis, DataOutputStream dos, String id, Repository<Edge> edges, Repository<Lane> lanes) {
		super(id, Variable.class);
		
		addReadQuery(Variable.SPEED, 
				new ReadObjectVarQuery.DoubleQ  (dis, dos, Constants.CMD_GET_VEHICLE_VARIABLE, id, Variable.SPEED.id));
		addReadQuery(Variable.POSITION, 
				new ReadObjectVarQuery.PositionQ(dis, dos, Constants.CMD_GET_VEHICLE_VARIABLE, id, Variable.POSITION.id));
		addReadQuery(Variable.ANGLE, 
				new ReadObjectVarQuery.DoubleQ  (dis, dos, Constants.CMD_GET_VEHICLE_VARIABLE, id, Variable.ANGLE.id));
		addReadQuery(Variable.CURRENT_EDGE, 
				new ReadObjectVarQuery.EdgeQ (dis, dos, Constants.CMD_GET_VEHICLE_VARIABLE, id, Variable.CURRENT_EDGE.id, edges));
		addReadQuery(Variable.CURRENT_LANE, 
				new ReadObjectVarQuery.LaneQ (dis, dos, Constants.CMD_GET_VEHICLE_VARIABLE, id, Variable.CURRENT_LANE.id, lanes));
		addReadQuery(Variable.ROUTE, 
				new RouteQuery(dis, dos, id, edges));
		
		edgeTravelTimeQuery = new ChangeEdgeTravelTimeQuery(dis, dos, id);
		changeTargetQuery = new ChangeTargetQuery(dis, dos, id);
		changeRouteQuery = new ChangeRouteQuery(dis, dos, id);
		
		/*
		 * when a reroute is issued, the read route query must forget its
		 * current value and re-read it from SUMO.
		 */
		rerouteQuery = new RerouteQuery(dis, dos, id) {
			@Override
			void pickResponses(Iterator<ResponseContainer> responseIterator)
					throws TraCIException {
				super.pickResponses(responseIterator);
				queryReadRoute().setObsolete();
			}
			
		};
	}
	
	@Override
	public void nextStep(double step) {
		for (ReadObjectVarQuery<?> q : getAllReadQueries().values()) {
			q.setObsolete();
		}
	}

	public ValueReadQuery<Double> queryReadSpeed() {
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.SPEED);
	}
	
	public ValueReadQuery<Point2D> queryReadPosition() {
		return (ReadObjectVarQuery.PositionQ) getReadQuery(Variable.POSITION);
	}
	
	public ValueReadQuery<Edge> queryReadCurrentEdge() {
		return (ReadObjectVarQuery.EdgeQ) getReadQuery(Variable.CURRENT_EDGE);
	}
	
	public ValueReadQuery<Lane> queryReadCurrentLane() {
		return (ReadObjectVarQuery.LaneQ) getReadQuery(Variable.CURRENT_LANE);
	}
	
	public ValueReadQuery<List<Edge>> queryReadRoute() {
		return (RouteQuery) getReadQuery(Variable.ROUTE);
	}

	public ChangeEdgeTravelTimeQuery querySetEdgeTravelTime() {
		return edgeTravelTimeQuery;
	}
	
	public RerouteQuery queryReroute() {
		return rerouteQuery;
	}
	
	public ChangeTargetQuery queryChangeTarget() {
		return changeTargetQuery;
	}
	
	public ChangeRouteQuery queryChangeRoute() {
		return changeRouteQuery;
	}
}


