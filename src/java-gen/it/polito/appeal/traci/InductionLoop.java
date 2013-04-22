

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

public class InductionLoop 
extends TraciObject<InductionLoop.Variable>
implements StepAdvanceListener
{

	public static enum Variable {
		LANE,
		POSITION,
		VEHICLE_NUMBER,
		MEAN_SPEED,
		LAST_STEP_VEHICLES,
		
	}
	
	InductionLoop (
		DataInputStream dis,
		DataOutputStream dos, 
		String id
		
			, Repository<Lane> repoLane
			, Repository<Vehicle> repoVehicle
	) {
		super(id, Variable.class);

		/*
		 * initialization of read queries
		 */
		
		addReadQuery(Variable.LANE, 
				new ReadObjectVarQuery.LaneQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_INDUCTIONLOOP_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_LANE_ID
				, repoLane
				
				));
		
		addReadQuery(Variable.POSITION, 
				new ReadObjectVarQuery.PositionQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_INDUCTIONLOOP_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_POSITION
				
				));
		
		addReadQuery(Variable.VEHICLE_NUMBER, 
				new ReadObjectVarQuery.IntegerQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_INDUCTIONLOOP_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.LAST_STEP_VEHICLE_NUMBER
				
				));
		
		addReadQuery(Variable.MEAN_SPEED, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_INDUCTIONLOOP_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.LAST_STEP_MEAN_SPEED
				
				));
		
		addReadQuery(Variable.LAST_STEP_VEHICLES, 
				new VehicleSetQuery (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_INDUCTIONLOOP_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.LAST_STEP_VEHICLE_ID_LIST
				, repoVehicle
				
				));
		

		/*
		 * initialization of change state queries
		 */
		
	
	}
	
	
	
	@Override
	public void nextStep(double step) {
		
		getReadQuery(Variable.VEHICLE_NUMBER).setObsolete();
		
		getReadQuery(Variable.MEAN_SPEED).setObsolete();
		
		getReadQuery(Variable.LAST_STEP_VEHICLES).setObsolete();
		
	}
	
	
	
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<Lane> queryReadLane() {
		return (ReadObjectVarQuery.LaneQ) getReadQuery(Variable.LANE);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<java.awt.geom.Point2D> queryReadPositionInLane() {
		return (ReadObjectVarQuery.PositionQ) getReadQuery(Variable.POSITION);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<Integer> queryReadLastStepVehicleNumber() {
		return (ReadObjectVarQuery.IntegerQ) getReadQuery(Variable.VEHICLE_NUMBER);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<Double> queryReadLastStepMeanSpeed() {
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.MEAN_SPEED);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<java.util.Set<Vehicle>> queryReadLastStepVehicles() {
		return (VehicleSetQuery) getReadQuery(Variable.LAST_STEP_VEHICLES);
	}
	
	
}

