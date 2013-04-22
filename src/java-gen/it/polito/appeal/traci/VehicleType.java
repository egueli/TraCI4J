

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

public class VehicleType 
extends TraciObject<VehicleType.Variable>
implements StepAdvanceListener
{

	public static enum Variable {
		LENGTH,
		
	}
	
	VehicleType (
		DataInputStream dis,
		DataOutputStream dos, 
		String id
		
	) {
		super(id, Variable.class);

		/*
		 * initialization of read queries
		 */
		
		addReadQuery(Variable.LENGTH, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_VEHICLETYPE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_LENGTH
				
				));
		

		/*
		 * initialization of change state queries
		 */
		
	
	}
	
	
	
	@Override
	public void nextStep(double step) {
		
	}
	
	
	
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<Double> queryReadGlobalTravelTime() {
		return (ReadObjectVarQuery.DoubleQ) getReadQuery(Variable.LENGTH);
	}
	
	
}

