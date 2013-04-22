

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

public class Edge 
extends TraciObject<Edge.Variable>
implements StepAdvanceListener
{

	public static enum Variable {
		TRAVEL_TIME,
		CO2_EMISSION,
		CO_EMISSION,
		HC_EMISSION,
		PMX_EMISSION,
		NOX_EMISSION,
		FUEL_CONSUMPTION,
		NOISE_EMISSION,
		
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
		
		addReadQuery(Variable.CO2_EMISSION, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_EDGE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_CO2EMISSION
				
				));
		
		addReadQuery(Variable.CO_EMISSION, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_EDGE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_COEMISSION
				
				));
		
		addReadQuery(Variable.HC_EMISSION, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_EDGE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_HCEMISSION
				
				));
		
		addReadQuery(Variable.PMX_EMISSION, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_EDGE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_PMXEMISSION
				
				));
		
		addReadQuery(Variable.NOX_EMISSION, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_EDGE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_NOXEMISSION
				
				));
		
		addReadQuery(Variable.FUEL_CONSUMPTION, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_EDGE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_FUELCONSUMPTION
				
				));
		
		addReadQuery(Variable.NOISE_EMISSION, 
				new ReadObjectVarQuery.DoubleQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_EDGE_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.VAR_NOISEEMISSION
				
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
		
		getReadQuery(Variable.CO2_EMISSION).setObsolete();
		
		getReadQuery(Variable.CO_EMISSION).setObsolete();
		
		getReadQuery(Variable.HC_EMISSION).setObsolete();
		
		getReadQuery(Variable.PMX_EMISSION).setObsolete();
		
		getReadQuery(Variable.NOX_EMISSION).setObsolete();
		
		getReadQuery(Variable.FUEL_CONSUMPTION).setObsolete();
		
		getReadQuery(Variable.NOISE_EMISSION).setObsolete();
		
	}
	
	
	
	
	
	/**
	 * @return the instance of {@link ReadGlobalTravelTimeQuery} relative to this query.
	 */
	public ReadGlobalTravelTimeQuery queryReadGlobalTravelTime() {
		return (ReadGlobalTravelTimeQuery) getReadQuery(Variable.TRAVEL_TIME);
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
	 * @return the instance of {@link ChangeGlobalTravelTimeQuery} relative to this query.
	 */
	public ChangeGlobalTravelTimeQuery queryChangeTravelTime() {
		return csqvar_ChangeTravelTime;
	}
	
}

