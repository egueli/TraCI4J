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
import java.util.Iterator;
import java.util.List;

import de.uniluebeck.itm.tcpip.Storage;
import it.polito.appeal.traci.ReadObjectVarQuery.IntegerQ;
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

	TrafficLight(String id, DataInputStream dis, DataOutputStream dos, Repository<Lane> laneRepo) {
		super(id, Variable.class);
		
		addReadQuery(Variable.STATE, new ReadTLStateQuery(dis, dos, id));
		
		addReadQuery(Variable.DEFAULT_CURRENT_PHASE_DURATION, new IntegerQ(dis,
				dos, GET_CMD, id, Constants.TL_PHASE_DURATION));
		
		addReadQuery(Variable.CONTROLLED_LANES, new LaneListQuery(dis, dos,
				GET_CMD, laneRepo, id, Constants.TL_CONTROLLED_LANES));
		
		addReadQuery(Variable.CONTROLLED_LINKS, new ReadControlledLinksQuery(
				dis, dos, id, laneRepo));
		
		// TODO add remaining read queries
		
		// TODO add change state queries
	}
	
	public ReadObjectVarQuery<TLState> getReadStateQuery() {
		return (ReadTLStateQuery) getReadQuery(Variable.STATE);
	}
	
	public ReadObjectVarQuery<Integer> getReadCurrentPhaseDurationQuery() {
		return (IntegerQ) getReadQuery(Variable.DEFAULT_CURRENT_PHASE_DURATION);
	}
	
	public ReadObjectVarQuery<ControlledLinks> getReadControlledLinksQuery() {
		return (ReadControlledLinksQuery) getReadQuery(Variable.CONTROLLED_LINKS);
	}
	
	// TODO add remaining query getters
	
	
}
