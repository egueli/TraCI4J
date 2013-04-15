

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

public class MeMeDetector 
extends TraciObject<MeMeDetector.Variable>
implements StepAdvanceListener
{

	public static enum Variable {
		VEHICLE_NUMBER,
		MEAN_SPEED,
		VEHICLES,
		
	}
	
	MeMeDetector (
		DataInputStream dis,
		DataOutputStream dos, 
		String id
		
			, Repository<Vehicle> repoVehicle
	) {
		super(id, Variable.class);

		/*
		 * initialization of read queries
		 */
		
		addReadQuery(Variable.VEHICLE_NUMBER, 
				new ReadObjectVarQuery.IntegerQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_MULTI_ENTRY_EXIT_DETECTOR_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.LAST_STEP_VEHICLE_NUMBER
				
				));
		
		addReadQuery(Variable.MEAN_SPEED, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_MULTI_ENTRY_EXIT_DETECTOR_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.LAST_STEP_MEAN_SPEED
				
				));
		
		addReadQuery(Variable.VEHICLES, 
				new VehicleSetQuery (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_MULTI_ENTRY_EXIT_DETECTOR_VARIABLE, 
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
		
		getReadQuery(Variable.VEHICLES).setObsolete();
		
	}
	
	
	
	
	
	public ReadObjectVarQuery<Integer> queryReadLastStepVehicleNumber() {
		  
		return (ReadObjectVarQuery.IntegerQ) getReadQuery(Variable.VEHICLE_NUMBER);
	}
	
	
	public ReadObjectVarQuery<Double> queryReadLastStepMeanSpeed() {
		  
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.MEAN_SPEED);
	}
	
	
	public ReadObjectVarQuery<java.util.Set<Vehicle>> queryReadLastStepVehicles() {
		  
		return (VehicleSetQuery) getReadQuery(Variable.VEHICLES);
	}
	
	
}

