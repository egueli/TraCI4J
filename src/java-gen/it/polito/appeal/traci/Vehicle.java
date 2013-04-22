

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
		CO2_EMISSION,
		CO_EMISSION,
		HC_EMISSION,
		PMX_EMISSION,
		NOX_EMISSION,
		FUEL_CONSUMPTION,
		NOISE_EMISSION,
		
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
		
		addReadQuery(Variable.CO2_EMISSION, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_VEHICLE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_CO2EMISSION
				
				));
		
		addReadQuery(Variable.CO_EMISSION, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_VEHICLE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_COEMISSION
				
				));
		
		addReadQuery(Variable.HC_EMISSION, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_VEHICLE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_HCEMISSION
				
				));
		
		addReadQuery(Variable.PMX_EMISSION, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_VEHICLE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_PMXEMISSION
				
				));
		
		addReadQuery(Variable.NOX_EMISSION, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_VEHICLE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_NOXEMISSION
				
				));
		
		addReadQuery(Variable.FUEL_CONSUMPTION, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_VEHICLE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_FUELCONSUMPTION
				
				));
		
		addReadQuery(Variable.NOISE_EMISSION, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_VEHICLE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_NOISEEMISSION
				
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
		
		getReadQuery(Variable.CO2_EMISSION).setObsolete();
		
		getReadQuery(Variable.CO_EMISSION).setObsolete();
		
		getReadQuery(Variable.HC_EMISSION).setObsolete();
		
		getReadQuery(Variable.PMX_EMISSION).setObsolete();
		
		getReadQuery(Variable.NOX_EMISSION).setObsolete();
		
		getReadQuery(Variable.FUEL_CONSUMPTION).setObsolete();
		
		getReadQuery(Variable.NOISE_EMISSION).setObsolete();
		
	}
	
	
	
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<java.lang.Double> queryReadSpeed() {
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.SPEED);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<java.awt.geom.Point2D> queryReadPosition() {
		return (ReadObjectVarQuery.PositionQ) getReadQuery(Variable.POSITION);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<java.lang.Double> queryReadLanePosition() {
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.LANE_POSITION);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<java.util.List<Edge>> queryReadCurrentRoute() {
		return (RouteQuery) getReadQuery(Variable.CURRENT_ROUTE);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<Edge> queryReadCurrentEdge() {
		return (ReadObjectVarQuery.EdgeQ) getReadQuery(Variable.CURRENT_EDGE);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<java.lang.Double> queryReadCO2Emission() {
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.CO2_EMISSION);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<java.lang.Double> queryReadCOEmission() {
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.CO_EMISSION);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<java.lang.Double> queryReadHCEmission() {
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.HC_EMISSION);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<java.lang.Double> queryReadPMXEmission() {
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.PMX_EMISSION);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<java.lang.Double> queryReadNOXEmission() {
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.NOX_EMISSION);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<java.lang.Double> queryReadFuelConsumption() {
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.FUEL_CONSUMPTION);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<java.lang.Double> queryReadNoiseEmission() {
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.NOISE_EMISSION);
	}
	
	
	/**
	 * @return the instance of {@link ChangeEdgeTravelTimeQuery} relative to this query.
	 */
	public ChangeEdgeTravelTimeQuery querySetEdgeTravelTime() {
		return csqvar_SetEdgeTravelTime;
	}
	
	/**
	 * @return the instance of {@link RerouteQuery} relative to this query.
	 */
	public RerouteQuery queryReroute() {
		return csqvar_Reroute;
	}
	
	/**
	 * @return the instance of {@link ChangeTargetQuery} relative to this query.
	 */
	public ChangeTargetQuery queryChangeTarget() {
		return csqvar_ChangeTarget;
	}
	
	/**
	 * @return the instance of {@link ChangeMaxSpeedQuery} relative to this query.
	 */
	public ChangeMaxSpeedQuery queryChangeMaxSpeed() {
		return csqvar_ChangeMaxSpeed;
	}
	
	/**
	 * @return the instance of {@link ChangeRouteQuery} relative to this query.
	 */
	public ChangeRouteQuery queryChangeRoute() {
		return csqvar_ChangeRoute;
	}
	
}

