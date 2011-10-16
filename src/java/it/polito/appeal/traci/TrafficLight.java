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

import java.io.DataInputStream;
import java.io.DataOutputStream;

import de.uniluebeck.itm.tcpip.Storage;
import it.polito.appeal.traci.ChangeObjectVarQuery.ChangeIntegerQ;
import it.polito.appeal.traci.ChangeObjectVarQuery.ChangeStringQ;
import it.polito.appeal.traci.ReadObjectVarQuery.DoubleQ;
import it.polito.appeal.traci.ReadObjectVarQuery.IntegerQ;
import it.polito.appeal.traci.ReadObjectVarQuery.StringQ;
import it.polito.appeal.traci.TrafficLight.Variable;
import it.polito.appeal.traci.protocol.Constants;

/**
 * Represents a traffic light system (TLS) in SUMO.
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public class TrafficLight extends TraciObject<Variable> 
implements StepAdvanceListener {

	private static final int GET_CMD = Constants.CMD_GET_TL_VARIABLE;
	private static final int SET_CMD = Constants.CMD_SET_TL_VARIABLE;
	
	/**
	 * The list of variables that can be read.
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 */
	public enum Variable {
		STATE,
		DEFAULT_CURRENT_PHASE_DURATION,
		CONTROLLED_LANES,
		CONTROLLED_LINKS,
		CURRENT_PHASE,
		CURRENT_PROGRAM,
		COMPLETE_DEFINITION,
		ASSUMED_NEXT_SWITCH_TIME
	}

	private final ChangeLightsStateQuery changeLightsStateQuery;
	private final ChangeIntegerQ changePhaseIndexQuery;
	private final ChangeStringQ changeProgramQuery;
	private final ChangeIntegerQ changePhaseDurationQuery;
	
	TrafficLight(String id, DataInputStream dis, DataOutputStream dos, Repository<Lane> laneRepo) {
		super(id, Variable.class);
		
		addReadQuery(Variable.STATE, new ReadTLStateQuery(dis, dos, GET_CMD, id
				, Constants.TL_RED_YELLOW_GREEN_STATE));
		
		addReadQuery(Variable.DEFAULT_CURRENT_PHASE_DURATION, new IntegerQ(dis,
				dos, GET_CMD, id, Constants.TL_PHASE_DURATION));
		
		addReadQuery(Variable.CONTROLLED_LANES, new LaneListQuery(dis, dos,
				GET_CMD, laneRepo, id, Constants.TL_CONTROLLED_LANES));
		
		addReadQuery(Variable.CONTROLLED_LINKS, new ReadControlledLinksQuery(
				dis, dos, GET_CMD, id, Constants.TL_CONTROLLED_LINKS, laneRepo));
		
		addReadQuery(Variable.CURRENT_PHASE, new IntegerQ(dis, dos, GET_CMD,
				id, Constants.TL_CURRENT_PHASE));
		
		addReadQuery(Variable.CURRENT_PROGRAM, new StringQ(dis, dos, GET_CMD,
				id, Constants.TL_CURRENT_PROGRAM));
		
		addReadQuery(Variable.COMPLETE_DEFINITION, 
				new ReadCompleteDefinitionQuery(dis, dos, GET_CMD, id, Constants.TL_COMPLETE_DEFINITION_RYG));
		
		addReadQuery(Variable.ASSUMED_NEXT_SWITCH_TIME, new DoubleQ(dis, dos,
				GET_CMD, id, Constants.TL_NEXT_SWITCH));
		
		changeLightsStateQuery = new ChangeLightsStateQuery(dis, dos, SET_CMD, id, Constants.TL_RED_YELLOW_GREEN_STATE) {
			@Override
			protected void writeValueTo(TLState val, Storage content) {
				super.writeValueTo(val, content);
				getReadCurrentStateQuery().setObsolete();
			}
		};  
		
		changePhaseIndexQuery = new ChangeIntegerQ(dis, dos, SET_CMD, id,
				Constants.TL_CURRENT_PHASE) {
			@Override
			protected void writeValueTo(Integer val, Storage content) {
				super.writeValueTo(val, content);
				getReadCurrentPhaseQuery().setObsolete();
				getReadCurrentStateQuery().setObsolete();
			}
		};
		
		changeProgramQuery = new ChangeStringQ(dis, dos, SET_CMD, id,
				Constants.TL_PROGRAM) {
			@Override
			protected void writeValueTo(String val, Storage content) {
				super.writeValueTo(val, content);
				getReadCurrentPhaseQuery().setObsolete();
				getReadCurrentStateQuery().setObsolete();
				getReadCurrentProgramQuery().setObsolete();
			}
		};
		
		changePhaseDurationQuery = new ChangeIntegerQ(dis, dos, SET_CMD, id,
				Constants.TL_PHASE_DURATION) {
			@Override
			protected void writeValueTo(Integer val, Storage content) {
				super.writeValueTo(val, content);
				getReadCurrentPhaseDurationQuery().setObsolete();
			}
		};
		
		// TODO add "set complete program definition" initializer
	}

	@Override
	public void nextStep(double step) {
		getReadCurrentPhaseDurationQuery().setObsolete();
		getReadCurrentPhaseQuery().setObsolete();
		getReadCurrentProgramQuery().setObsolete();
		getReadCurrentStateQuery().setObsolete();
		getAssumedNextSwitchTimeQuery().setObsolete();
	}
	
	public ReadObjectVarQuery<TLState> getReadCurrentStateQuery() {
		return (ReadTLStateQuery) getReadQuery(Variable.STATE);
	}
	
	public ReadObjectVarQuery<Integer> getReadCurrentPhaseDurationQuery() {
		return (IntegerQ) getReadQuery(Variable.DEFAULT_CURRENT_PHASE_DURATION);
	}
	
	public ReadObjectVarQuery<ControlledLinks> getReadControlledLinksQuery() {
		return (ReadControlledLinksQuery) getReadQuery(Variable.CONTROLLED_LINKS);
	}
	
	public ReadObjectVarQuery<Integer> getReadCurrentPhaseQuery() {
		return (IntegerQ) getReadQuery(Variable.CURRENT_PHASE);
	}

	public ReadObjectVarQuery<String> getReadCurrentProgramQuery() {
		return (StringQ) getReadQuery(Variable.CURRENT_PROGRAM);
	}
	
	public ReadObjectVarQuery<Program> getCompleteDefinitionQuery() {
		return (ReadCompleteDefinitionQuery) getReadQuery(Variable.COMPLETE_DEFINITION);
	}
	
	public ReadObjectVarQuery<Double> getAssumedNextSwitchTimeQuery() {
		return (DoubleQ) getReadQuery(Variable.ASSUMED_NEXT_SWITCH_TIME);
	}

	public ChangeLightsStateQuery getChangeLightsStateQuery() {
		return changeLightsStateQuery;
	}

	public ChangeIntegerQ getChangePhaseIndexQuery() {
		return changePhaseIndexQuery;
	}

	public ChangeStringQ getChangeProgramQuery() {
		return changeProgramQuery;
	}

	public ChangeIntegerQ getChangePhaseDurationQuery() {
		return changePhaseDurationQuery;
	}

	
	
	// TODO add "set complete program definition" query getter
	
	
}
