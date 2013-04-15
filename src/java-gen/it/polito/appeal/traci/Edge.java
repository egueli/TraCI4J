

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

public class Edge 
extends TraciObject<Edge.Variable>
implements StepAdvanceListener
{

	public static enum Variable {
		TRAVEL_TIME,
		
	}
	
	
	private final ChangeGlobalTravelTimeQuery csqvar_ChangeTravelTime;
	Edge (
		DataInputStream dis,
		DataOutputStream dos, 
		String id
		
	) {
		super(id, Variable.class);

		/*
		 * initialization of read queries
		 */
		
		addReadQuery(Variable.TRAVEL_TIME, 
				new ReadGlobalTravelTimeQuery (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_EDGE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_EDGE_TRAVELTIME
				
				));
		

		/*
		 * initialization of change state queries
		 */
		
		csqvar_ChangeTravelTime = new ChangeGlobalTravelTimeQuery(dis, dos, id
		)
		{
			@Override
			void pickResponses(java.util.Iterator<it.polito.appeal.traci.protocol.ResponseContainer> responseIterator)
					throws TraCIException {
				super.pickResponses(responseIterator);
				
				queryReadGlobalTravelTime().setObsolete();
				
			}
		};
		
	
	}
	
	
	
	@Override
	public void nextStep(double step) {
		
	}
	
	
	
	
	
	public ReadGlobalTravelTimeQuery queryReadGlobalTravelTime() {
		  
		return (ReadGlobalTravelTimeQuery) getReadQuery(Variable.TRAVEL_TIME);
	}
	
	
	public ChangeGlobalTravelTimeQuery queryChangeTravelTime() {
		return csqvar_ChangeTravelTime;
	}
	
}

