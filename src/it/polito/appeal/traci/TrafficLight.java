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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import de.uniluebeck.itm.tcpip.Storage;
import it.polito.appeal.traci.ChangeObjectVarQuery.ChangeIntegerQ;
import it.polito.appeal.traci.ChangeObjectVarQuery.ChangeStringQ;
import it.polito.appeal.traci.ReadObjectVarQuery.DoubleQ;
import it.polito.appeal.traci.ReadObjectVarQuery.IntegerQ;
import it.polito.appeal.traci.ReadObjectVarQuery.StringQ;
import it.polito.appeal.traci.TraCIException.UnexpectedDatatype;
import it.polito.appeal.traci.TrafficLight.Variable;
import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.protocol.StringList;

/**
 * Represents a traffic light system (TLS) in SUMO.
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public class TrafficLight extends TraciObject<Variable> {

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

	/**
	 * Represents the states of each traffic light 
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 *
	 */
	public enum LightState {
		RED           ('r'),
		RED_NODECEL   ('R'),
		YELLOW        ('y'),
		YELLOW_NODECEL('Y'),
		GREEN         ('g'),
		GREEN_NODECEL ('G'),
		OFF           ('O');
		
		final char symbol;
		LightState(char symbol) {
			this.symbol = symbol;
		}
		public boolean isRed()    { return symbol=='R' || symbol=='r'; }
		public boolean isYellow() { return symbol=='Y' || symbol=='y'; }
		public boolean isGreen()  { return symbol=='G' || symbol=='g'; }
		public boolean isOff()    { return symbol=='O'; }
		public boolean willDecelerate() {return Character.isLowerCase(symbol); }
		static LightState fromSymbol(char symbol) {
			for (LightState ls : LightState.values())
				if (symbol == ls.symbol)
					return ls;
			return null;
		}
	}
	
	/**
	 * Represents the state of a traffic light, i.e. the status of each light.
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 *
	 */
	public static class TLState {
		public final LightState[] lightStates;
		/**
		 * Constructs a {@link TLState} from a string description of a phase
		 * (one letter for each signal).
		 * @param phase the description of a phase. It must contain only the
		 * following characters: rRyYgGO
		 * @throws IllegalArgumentException if the phase contains invalid
		 * characters
		 */
		public TLState(String phase) {
			final int len = phase.length();
			lightStates = new LightState[len];
			for (int i=0; i<len; i++) {
				final char ch = phase.charAt(i);
				final LightState ls = LightState.fromSymbol(ch);
				if (ls == null)
					throw new IllegalArgumentException("unknown TL symbol: " + ch);
				lightStates[i] = ls;
			}
		}
		public TLState(LightState[] lightStates) {
			this.lightStates = lightStates;
		}
		public String toString() {
			char[] desc = new char[lightStates.length];
			for (int i=0; i<desc.length; i++)
				desc[i] = lightStates[i].symbol;
			return new String(desc);
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (obj == this) return true;
			if (!(obj instanceof TLState)) return false;
			return Arrays.deepEquals(((TLState)obj).lightStates, lightStates);
		}
		@Override
		public int hashCode() {
			return Arrays.hashCode(lightStates);
		}
	}
	
	public static class ControlledLink {
		private final Lane incomingLane;
		private final Lane acrossLane;
		private final Lane outgoingLane;

		ControlledLink(Storage content, Repository<Lane> laneRepo)
				throws IOException {
			StringList list = new StringList(content, true);
			Iterator<String> listIt = list.iterator();
			incomingLane = laneRepo.getByID(listIt.next());
			outgoingLane = laneRepo.getByID(listIt.next());
			acrossLane = laneRepo.getByID(listIt.next());
		}

		public Lane getIncomingLane() {
			return incomingLane;
		}

		public Lane getAcrossLane() {
			return acrossLane;
		}

		public Lane getOutgoingLane() {
			return outgoingLane;
		}
	}

	/**
	 * Represents the set of links for every signal in a TLS.
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 *
	 */
	public static class ControlledLinks {
		private final ControlledLink[][] links;
		ControlledLinks(Storage content, Repository<Lane> laneRepo) throws TraCIException, IOException {
			Utils.checkType(content, Constants.TYPE_INTEGER);
			int signals = content.readInt();
			links = new ControlledLink[signals][];
			for (int s=0; s < signals; s++) {
				Utils.checkType(content, Constants.TYPE_INTEGER);
				int linksOfSignal = content.readInt();
				links[s] = new ControlledLink[linksOfSignal];
				for (int l=0; l<linksOfSignal; l++) {
					links[s][l] = new ControlledLink(content, laneRepo);
				}
			}

		}
		/**
		 * Returns an array describing the controlled links of a TLS.
		 * The first index points to a signal; the second index points to a link
		 * controlled by that signal.
		 * @return
		 */
		public ControlledLink[][] getLinks() {
			return links;
		}
	}
	
	static class ReadControlledLinksQuery extends ReadObjectVarQuery<ControlledLinks> {

		private final Repository<Lane> laneRepo;

		ReadControlledLinksQuery(DataInputStream dis, DataOutputStream dos,
				String objectID, Repository<Lane> laneRepo) {
			super(dis, dos, GET_CMD, objectID, Constants.TL_CONTROLLED_LINKS);
			this.laneRepo = laneRepo;
		}

		@Override
		protected ControlledLinks readValue(Command resp)
				throws TraCIException {
			final Storage content = resp.content();
			Utils.checkType(content, Constants.TYPE_COMPOUND);
			content.readInt(); // ignore data length
			try {
				return new ControlledLinks(content, laneRepo);
			} catch (IOException e) {
				throw new TraCIException(e.getMessage());
			}
		}
		
	}
	
	public static class ReadTLStateQuery extends ReadObjectVarQuery<TLState> {

		ReadTLStateQuery(DataInputStream dis, DataOutputStream dos
				, String objectID) {
			super(dis, dos, GET_CMD, objectID,
					Constants.TL_RED_YELLOW_GREEN_STATE);
		}

		@Override
		protected TLState readValue(Command resp) throws TraCIException {
			Utils.checkType(resp.content(), Constants.TYPE_STRING);
			String desc = resp.content().readStringASCII();
			return new TLState(desc);
		}
	}
	
	public static class LaneListQuery extends ObjectCollectionQuery<Lane, List<Lane>> {
		LaneListQuery(DataInputStream dis, DataOutputStream dos, int commandID,
				Repository<Lane> repository, String objectID, int varID) {
			super(dis, dos, commandID, repository, objectID, varID);
		}
		@Override
		protected List<Lane> makeCollection() {
			return new ArrayList<Lane>();
		}
	}

	public static class Phase {
		private final int duration;
		private final TLState state;
		Phase(Storage content) throws UnexpectedDatatype {
			Utils.checkType(content, Constants.TYPE_INTEGER);
			duration = content.readInt();
			Utils.checkType(content, Constants.TYPE_INTEGER);
			content.readInt(); // duration 2 ignored
			Utils.checkType(content, Constants.TYPE_INTEGER);
			content.readInt(); // duration 3 ignored
			Utils.checkType(content, Constants.TYPE_STRING);
			state = new TLState(content.readStringASCII());
		}
		public int getDuration() {
			return duration;
		}
		public TLState getState() {
			return state;
		}
	}
	
	public static class Logic {
		private final String subID;
		private final int currentPhaseIndex;
		private final Phase[] phases;
		Logic(Storage content) throws UnexpectedDatatype {
			Utils.checkType(content, Constants.TYPE_STRING);
			subID = content.readStringASCII();
			Utils.checkType(content, Constants.TYPE_INTEGER);
			content.readInt(); // type ignored
			Utils.checkType(content, Constants.TYPE_COMPOUND);
			int compSize = content.readInt();
			for (int i=0; i<compSize; i++)
				content.readByte(); // ignore compound type SubParameter
			Utils.checkType(content, Constants.TYPE_INTEGER);
			currentPhaseIndex = content.readInt();
			Utils.checkType(content, Constants.TYPE_INTEGER);
			int nPhases = content.readInt();
			phases = new Phase[nPhases];
			for (int i=0; i<nPhases; i++) {
				phases[i] = new Phase(content);
			}
		}
		public String getSubID() {
			return subID;
		}
		public int getCurrentPhaseIndex() {
			return currentPhaseIndex;
		}
		public Phase[] getPhases() {
			return phases;
		}
	}
	
	public static class Program {
		private final Logic[] logics;
		Program(Storage content) throws UnexpectedDatatype {
			Utils.checkType(content, Constants.TYPE_COMPOUND);
			content.readInt(); // compund length ignored
			Utils.checkType(content, Constants.TYPE_INTEGER);
			int nLogics = content.readInt();
			logics = new Logic[nLogics];
			for (int i=0; i<nLogics; i++) {
				logics[i] = new Logic(content);
			}
		}
		public Logic[] getLogics() {
			return logics;
		}
	}
	
	static class ReadCompleteDefinitionQuery extends ReadObjectVarQuery<Program> {
		ReadCompleteDefinitionQuery(DataInputStream dis,
				DataOutputStream dos, String objectID) {
			super(dis, dos, GET_CMD, objectID, Constants.TL_COMPLETE_DEFINITION_RYG);
		}

		@Override
		protected Program readValue(Command resp) throws TraCIException {
			Storage content = resp.content();
			return new Program(content);
		}
		
	}
	
	public static class ChangeLightsStateQuery extends ChangeObjectVarQuery<TLState> {
		ChangeLightsStateQuery(DataInputStream dis, DataOutputStream dos,
				String objectID) {
			super(dis, dos, SET_CMD, objectID, Constants.TL_RED_YELLOW_GREEN_STATE);
		}

		@Override
		protected void writeValueTo(TLState val, Storage content) {
			content.writeByte(Constants.TYPE_STRING);
			content.writeStringASCII(val.toString());
		}
	}
	
	/*
	 * TODO add "set complete program definition" as I didn't fully understood
	 * how the TLS model is composed; in particular, I can't say if the
	 * Program/Logic/Phase model I wrote for reading program can be re-used,
	 * since the related page http://sourceforge.net/apps/mediawiki/sumo/index.php?title=TraCI/Change_Traffic_Lights_State
	 * doesn't mention any "logic"...
	 */
	
	private final ChangeLightsStateQuery changeLightsStateQuery;
	private final ChangeIntegerQ changePhaseIndexQuery;
	private final ChangeStringQ changeProgramQuery;
	private final ChangeIntegerQ changePhaseDurationQuery;
	
	TrafficLight(String id, DataInputStream dis, DataOutputStream dos, Repository<Lane> laneRepo) {
		super(id, Variable.class);
		
		addReadQuery(Variable.STATE, new ReadTLStateQuery(dis, dos, id));
		
		addReadQuery(Variable.DEFAULT_CURRENT_PHASE_DURATION, new IntegerQ(dis,
				dos, GET_CMD, id, Constants.TL_PHASE_DURATION));
		
		addReadQuery(Variable.CONTROLLED_LANES, new LaneListQuery(dis, dos,
				GET_CMD, laneRepo, id, Constants.TL_CONTROLLED_LANES));
		
		addReadQuery(Variable.CONTROLLED_LINKS, new ReadControlledLinksQuery(
				dis, dos, id, laneRepo));
		
		addReadQuery(Variable.CURRENT_PHASE, new IntegerQ(dis, dos, GET_CMD,
				id, Constants.TL_CURRENT_PHASE));
		
		addReadQuery(Variable.CURRENT_PROGRAM, new StringQ(dis, dos, GET_CMD,
				id, Constants.TL_CURRENT_PROGRAM));
		
		addReadQuery(Variable.COMPLETE_DEFINITION, 
				new ReadCompleteDefinitionQuery(dis, dos, id));
		
		addReadQuery(Variable.ASSUMED_NEXT_SWITCH_TIME, new DoubleQ(dis, dos,
				GET_CMD, id, Constants.TL_NEXT_SWITCH));
		
		changeLightsStateQuery = new ChangeLightsStateQuery(dis, dos, id) {
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
