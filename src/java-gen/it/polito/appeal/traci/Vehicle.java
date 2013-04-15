

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

/*
THIS FILE IS GENERATED AUTOMATICALLY. DO NOT EDIT: CHANGES WILL BE OVERWRITTEN.
*/

package it.polito.appeal.traci;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Vehicle 
extends TraciObject<Vehicle.Variable>
implements StepAdvanceListener
{

	public static enum Variable {
		SPEED,
		POSITION,
		LANE_POSITION,
		CURRENT_ROUTE,
		CURRENT_EDGE,
		
	}
	
	
	private final ChangeEdgeTravelTimeQuery csqvar_SetEdgeTravelTime;
	
	private final RerouteQuery csqvar_Reroute;
	
	private final ChangeTargetQuery csqvar_ChangeTarget;
	
	private final ChangeMaxSpeedQuery csqvar_ChangeMaxSpeed;
	
	private final ChangeRouteQuery csqvar_ChangeRoute;
	Vehicle (
		DataInputStream dis,
		DataOutputStream dos, 
		String id
		
			, Repository<Edge> repoEdge
			, Repository<Lane> repoLane
	) {
		super(id, Variable.class);

		/*
		 * initialization of read queries
		 */
		
		addReadQuery(Variable.SPEED, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_VEHICLE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_SPEED
				
				));
		
		addReadQuery(Variable.POSITION, 
				new ReadObjectVarQuery.PositionQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_VEHICLE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_POSITION
				
				));
		
		addReadQuery(Variable.LANE_POSITION, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_VEHICLE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_LANEPOSITION
				
				));
		
		addReadQuery(Variable.CURRENT_ROUTE, 
				new RouteQuery (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_VEHICLE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_EDGES
				, repoEdge
				
				));
		
		addReadQuery(Variable.CURRENT_EDGE, 
				new ReadObjectVarQuery.EdgeQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_VEHICLE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_ROAD_ID
				, repoEdge
				
				));
		

		/*
		 * initialization of change state queries
		 */
		
		csqvar_SetEdgeTravelTime = new ChangeEdgeTravelTimeQuery(dis, dos, id
		)
		;
		
		csqvar_Reroute = new RerouteQuery(dis, dos, id
		)
		{
			@Override
			void pickResponses(java.util.Iterator<it.polito.appeal.traci.protocol.ResponseContainer> responseIterator)
					throws TraCIException {
				super.pickResponses(responseIterator);
				
				queryReadCurrentRoute().setObsolete();
				
			}
		};
		
		csqvar_ChangeTarget = new ChangeTargetQuery(dis, dos, id
		)
		{
			@Override
			void pickResponses(java.util.Iterator<it.polito.appeal.traci.protocol.ResponseContainer> responseIterator)
					throws TraCIException {
				super.pickResponses(responseIterator);
				
				queryReadCurrentRoute().setObsolete();
				
			}
		};
		
		csqvar_ChangeMaxSpeed = new ChangeMaxSpeedQuery(dis, dos, id
		)
		{
			@Override
			void pickResponses(java.util.Iterator<it.polito.appeal.traci.protocol.ResponseContainer> responseIterator)
					throws TraCIException {
				super.pickResponses(responseIterator);
				
			}
		};
		
		csqvar_ChangeRoute = new ChangeRouteQuery(dis, dos, id
		)
		{
			@Override
			void pickResponses(java.util.Iterator<it.polito.appeal.traci.protocol.ResponseContainer> responseIterator)
					throws TraCIException {
				super.pickResponses(responseIterator);
				
				queryReadCurrentRoute().setObsolete();
				
			}
		};
		
	
	}
	
	
	
	@Override
	public void nextStep(double step) {
		
		getReadQuery(Variable.SPEED).setObsolete();
		
		getReadQuery(Variable.POSITION).setObsolete();
		
		getReadQuery(Variable.LANE_POSITION).setObsolete();
		
		getReadQuery(Variable.CURRENT_EDGE).setObsolete();
		
	}
	
	
	
	
	
	public ReadObjectVarQuery<java.lang.Double> queryReadSpeed() {
		  
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.SPEED);
	}
	
	
	public ReadObjectVarQuery<java.awt.geom.Point2D> queryReadPosition() {
		  
		return (ReadObjectVarQuery.PositionQ) getReadQuery(Variable.POSITION);
	}
	
	
	public ReadObjectVarQuery<java.lang.Double> queryReadLanePosition() {
		  
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.LANE_POSITION);
	}
	
	
	public ReadObjectVarQuery<java.util.List<Edge>> queryReadCurrentRoute() {
		  
		return (RouteQuery) getReadQuery(Variable.CURRENT_ROUTE);
	}
	
	
	public ReadObjectVarQuery<Edge> queryReadCurrentEdge() {
		  
		return (ReadObjectVarQuery.EdgeQ) getReadQuery(Variable.CURRENT_EDGE);
	}
	
	
	public ChangeEdgeTravelTimeQuery querySetEdgeTravelTime() {
		return csqvar_SetEdgeTravelTime;
	}
	
	public RerouteQuery queryReroute() {
		return csqvar_Reroute;
	}
	
	public ChangeTargetQuery queryChangeTarget() {
		return csqvar_ChangeTarget;
	}
	
	public ChangeMaxSpeedQuery queryChangeMaxSpeed() {
		return csqvar_ChangeMaxSpeed;
	}
	
	public ChangeRouteQuery queryChangeRoute() {
		return csqvar_ChangeRoute;
	}
	
}

