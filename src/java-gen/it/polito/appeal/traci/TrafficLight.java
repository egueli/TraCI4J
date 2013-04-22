

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

public class TrafficLight 
extends TraciObject<TrafficLight.Variable>
implements StepAdvanceListener
{

	public static enum Variable {
		STATE,
		DEFAULT_CURRENT_PHASE_DURATION,
		CONTROLLED_LANES,
		CONTROLLED_LINKS,
		CURRENT_PHASE,
		CURRENT_PROGRAM,
		COMPLETE_DEFINITION,
		ASSUMED_NEXT_SWITCH_TIME,
		
	}
	
	
	private final ChangeLightsStateQuery csqvar_ChangeLightsState;
	
	private final ChangeObjectVarQuery.ChangeIntegerQ csqvar_ChangePhaseIndex;
	
	private final ChangeObjectVarQuery.ChangeStringQ csqvar_ChangeProgram;
	
	private final ChangeObjectVarQuery.ChangeIntegerQ csqvar_ChangePhaseDuration;
	
	private final ChangeCompleteProgramQuery csqvar_ChangeCompleteProgramDefinition;
	TrafficLight (
		DataInputStream dis,
		DataOutputStream dos, 
		String id
		
			, Repository<Lane> repoLane
	) {
		super(id, Variable.class);

		/*
		 * initialization of read queries
		 */
		
		addReadQuery(Variable.STATE, 
				new ReadTLStateQuery (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_TL_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.TL_RED_YELLOW_GREEN_STATE
				
				));
		
		addReadQuery(Variable.DEFAULT_CURRENT_PHASE_DURATION, 
				new ReadObjectVarQuery.IntegerQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_TL_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.TL_PHASE_DURATION
				
				));
		
		addReadQuery(Variable.CONTROLLED_LANES, 
				new LaneListQuery (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_TL_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.TL_CONTROLLED_LANES
				, repoLane
				
				));
		
		addReadQuery(Variable.CONTROLLED_LINKS, 
				new ReadControlledLinksQuery (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_TL_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.TL_CONTROLLED_LINKS
				, repoLane
				
				));
		
		addReadQuery(Variable.CURRENT_PHASE, 
				new ReadObjectVarQuery.IntegerQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_TL_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.TL_CURRENT_PHASE
				
				));
		
		addReadQuery(Variable.CURRENT_PROGRAM, 
				new ReadObjectVarQuery.StringQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_TL_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.TL_CURRENT_PROGRAM
				
				));
		
		addReadQuery(Variable.COMPLETE_DEFINITION, 
				new ReadCompleteDefinitionQuery (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_TL_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.TL_COMPLETE_DEFINITION_RYG
				
				));
		
		addReadQuery(Variable.ASSUMED_NEXT_SWITCH_TIME, 
				new ReadObjectVarQuery.IntegerQ (dis, dos, 
				it.polito.appeal.traci.protocol.Constants.CMD_GET_TL_VARIABLE, 
				id, 
				it.polito.appeal.traci.protocol.Constants.TL_RED_YELLOW_GREEN_STATE
				
				));
		

		/*
		 * initialization of change state queries
		 */
		
		csqvar_ChangeLightsState = new ChangeLightsStateQuery(dis, dos, id
		)
		;
		
		csqvar_ChangePhaseIndex = new ChangeObjectVarQuery.ChangeIntegerQ(dis, dos, id
		, it.polito.appeal.traci.protocol.Constants.CMD_SET_TL_VARIABLE, it.polito.appeal.traci.protocol.Constants.TL_CURRENT_PHASE)
		;
		
		csqvar_ChangeProgram = new ChangeObjectVarQuery.ChangeStringQ(dis, dos, id
		, it.polito.appeal.traci.protocol.Constants.CMD_SET_TL_VARIABLE, it.polito.appeal.traci.protocol.Constants.TL_PROGRAM)
		;
		
		csqvar_ChangePhaseDuration = new ChangeObjectVarQuery.ChangeIntegerQ(dis, dos, id
		, it.polito.appeal.traci.protocol.Constants.CMD_SET_TL_VARIABLE, it.polito.appeal.traci.protocol.Constants.TL_PHASE_DURATION)
		;
		
		csqvar_ChangeCompleteProgramDefinition = new ChangeCompleteProgramQuery(dis, dos, id
		, it.polito.appeal.traci.protocol.Constants.CMD_SET_TL_VARIABLE, it.polito.appeal.traci.protocol.Constants.TL_COMPLETE_PROGRAM_RYG)
		;
		
	
	}
	
	
	
	@Override
	public void nextStep(double step) {
		
		getReadQuery(Variable.STATE).setObsolete();
		
		getReadQuery(Variable.DEFAULT_CURRENT_PHASE_DURATION).setObsolete();
		
		getReadQuery(Variable.CURRENT_PHASE).setObsolete();
		
		getReadQuery(Variable.CURRENT_PROGRAM).setObsolete();
		
		getReadQuery(Variable.ASSUMED_NEXT_SWITCH_TIME).setObsolete();
		
	}
	
	
	
	
	
	/**
	 * @return the instance of {@link ReadTLStateQuery} relative to this query.
	 */
	public ReadTLStateQuery queryReadState() {
		return (ReadTLStateQuery) getReadQuery(Variable.STATE);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery} relative to this query.
	 */
	public ReadObjectVarQuery<java.lang.Integer> queryReadDefaultCurrentPhaseDuration() {
		return (ReadObjectVarQuery.IntegerQ) getReadQuery(Variable.DEFAULT_CURRENT_PHASE_DURATION);
	}
	
	
	/**
	 * @return the instance of {@link LaneListQuery} relative to this query.
	 */
	public LaneListQuery queryReadControlledLanes() {
		return (LaneListQuery) getReadQuery(Variable.CONTROLLED_LANES);
	}
	
	
	/**
	 * @return the instance of {@link ReadControlledLinksQuery} relative to this query.
	 */
	public ReadControlledLinksQuery queryReadControlledLinks() {
		return (ReadControlledLinksQuery) getReadQuery(Variable.CONTROLLED_LINKS);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery.IntegerQ} relative to this query.
	 */
	public ReadObjectVarQuery.IntegerQ queryReadCurrentPhase() {
		return (ReadObjectVarQuery.IntegerQ) getReadQuery(Variable.CURRENT_PHASE);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery.StringQ} relative to this query.
	 */
	public ReadObjectVarQuery.StringQ queryReadCurrentProgram() {
		return (ReadObjectVarQuery.StringQ) getReadQuery(Variable.CURRENT_PROGRAM);
	}
	
	
	/**
	 * @return the instance of {@link ReadCompleteDefinitionQuery} relative to this query.
	 */
	public ReadCompleteDefinitionQuery queryReadCompleteDefinition() {
		return (ReadCompleteDefinitionQuery) getReadQuery(Variable.COMPLETE_DEFINITION);
	}
	
	
	/**
	 * @return the instance of {@link ReadObjectVarQuery.IntegerQ} relative to this query.
	 */
	public ReadObjectVarQuery.IntegerQ queryReadAssumedNextSwitchTime() {
		return (ReadObjectVarQuery.IntegerQ) getReadQuery(Variable.ASSUMED_NEXT_SWITCH_TIME);
	}
	
	
	public ChangeLightsStateQuery queryChangeLightsState() {
		return csqvar_ChangeLightsState;
	}
	
	public ChangeObjectVarQuery.ChangeIntegerQ queryChangePhaseIndex() {
		return csqvar_ChangePhaseIndex;
	}
	
	public ChangeObjectVarQuery.ChangeStringQ queryChangeProgram() {
		return csqvar_ChangeProgram;
	}
	
	public ChangeObjectVarQuery.ChangeIntegerQ queryChangePhaseDuration() {
		return csqvar_ChangePhaseDuration;
	}
	
	public ChangeCompleteProgramQuery queryChangeCompleteProgramDefinition() {
		return csqvar_ChangeCompleteProgramDefinition;
	}
	
}

