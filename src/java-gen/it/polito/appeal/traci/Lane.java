

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

public class Lane 
extends TraciObject<Lane.Variable>
implements StepAdvanceListener
{

	public static enum Variable {
		SHAPE,
		LENGTH,
		LAST_STEP_OCCUPANCY,
		MAX_SPEED,
		PARENT_EDGE,
		LINKS,
		LAST_STEP_VEHICLE_NUMBER,
		
	}
	
	Lane (
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
		
		addReadQuery(Variable.SHAPE, 
				new ReadShapeQuery (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_LANE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_SHAPE
				
				));
		
		addReadQuery(Variable.LENGTH, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_LANE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_LENGTH
				
				));
		
		addReadQuery(Variable.LAST_STEP_OCCUPANCY, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_LANE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.LAST_STEP_OCCUPANCY
				
				));
		
		addReadQuery(Variable.MAX_SPEED, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_LANE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_MAXSPEED
				
				));
		
		addReadQuery(Variable.PARENT_EDGE, 
				new ReadObjectVarQuery.EdgeQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_LANE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.LANE_EDGE_ID
				, repoEdge
				
				));
		
		addReadQuery(Variable.LINKS, 
				new ReadLinksQuery (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_LANE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.LANE_LINKS
				, repoLane
				
				));
		
		addReadQuery(Variable.LAST_STEP_VEHICLE_NUMBER, 
				new ReadObjectVarQuery.IntegerQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_LANE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.LAST_STEP_VEHICLE_NUMBER
				
				));
		

		/*
		 * initialization of change state queries
		 */
		
	
	}
	
	
	
	@Override
	public void nextStep(double step) {
		
		getReadQuery(Variable.LAST_STEP_OCCUPANCY).setObsolete();
		
		getReadQuery(Variable.LAST_STEP_VEHICLE_NUMBER).setObsolete();
		
	}
	
	
	
	
	
	public ReadObjectVarQuery<java.awt.geom.Path2D> queryReadShape() {
		  
		return (ReadShapeQuery) getReadQuery(Variable.SHAPE);
	}
	
	
	public ReadObjectVarQuery<Double> queryReadLength() {
		  
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.LENGTH);
	}
	
	
	public ReadObjectVarQuery<Double> queryReadLastStepOccupancy() {
		  
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.LAST_STEP_OCCUPANCY);
	}
	
	
	public ReadObjectVarQuery<Double> queryReadMaxSpeed() {
		  
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.MAX_SPEED);
	}
	
	
	public ReadObjectVarQuery<Edge> queryReadParentEdge() {
		  
		return (ReadObjectVarQuery.EdgeQ) getReadQuery(Variable.PARENT_EDGE);
	}
	
	
	public ReadObjectVarQuery<java.util.List<Link>> queryReadLinks() {
		  
		return (ReadLinksQuery) getReadQuery(Variable.LINKS);
	}
	
	
	public ReadObjectVarQuery<Integer> queryReadLastStepVehicleNumber() {
		  
		return (ReadObjectVarQuery.IntegerQ) getReadQuery(Variable.LAST_STEP_VEHICLE_NUMBER);
	}
	
	
}

