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

package it.polito.appeal.traci.newquery;

import java.awt.geom.Path2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.uniluebeck.itm.tcpip.Storage;

import it.polito.appeal.traci.TraCIException;
import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.protocol.Polygon;

import static it.polito.appeal.traci.newquery.Utils.checkType;

public class Lane extends TraciObject<Lane.Variable> {

	public static class Link {
		private final Lane nextInternalLane;
		private final Lane nextNonInternalLane;
		private final boolean hasPriority;
		private final boolean isOpened;
		private final boolean hasApproachingFoe;
		private final String currentState;
		private final String direction;
		private final double length;
		
		protected Link(Storage content, Repository<Lane> laneRepo) throws IOException {
			checkType(content, Constants.TYPE_STRING);
			// let's hope they don't point to this lane recursively!
			nextNonInternalLane = laneRepo.getByID(content.readStringASCII());
			checkType(content, Constants.TYPE_STRING);
			nextInternalLane = laneRepo.getByID(content.readStringASCII());
			checkType(content, Constants.TYPE_UBYTE);
			hasPriority = content.readUnsignedByte() > 0;
			checkType(content, Constants.TYPE_UBYTE);
			isOpened = content.readUnsignedByte() > 0;
			checkType(content, Constants.TYPE_UBYTE);
			hasApproachingFoe = content.readUnsignedByte() > 0;
			checkType(content, Constants.TYPE_STRING);
			currentState = content.readStringASCII();
			checkType(content, Constants.TYPE_STRING);
			direction = content.readStringASCII();
			checkType(content, Constants.TYPE_DOUBLE);
			length = content.readDouble();
		}
		
		public Lane getNextInternalLane() {
			return nextInternalLane;
		}
		public Lane getNextNonInternalLane() {
			return nextNonInternalLane;
		}
		public boolean hasPriority() {
			return hasPriority;
		}
		public boolean isOpened() {
			return isOpened;
		}
		public boolean hasApproachingFoe() {
			return hasApproachingFoe;
		}
		public String getCurrentState() {
			return currentState;
		}
		public String getDirection() {
			return direction;
		}
		public double getLength() {
			return length;
		}
	}

	public static class ReadShapeQuery extends ReadObjectVarQuery<Path2D> {

		ReadShapeQuery(DataInputStream dis, DataOutputStream dos,
				String objectID) {
			super(dis, dos, Constants.CMD_GET_LANE_VARIABLE, objectID, Constants.VAR_SHAPE);
		}

		@Override
		protected Path2D readValue(Command resp) throws TraCIException {
			return new Polygon(resp.content(), true).getShape();
		}
	}
	
	public static class ReadLinksQuery extends ReadObjectVarQuery<List<Lane.Link>> {

		private Repository<Lane> laneRepo;

		ReadLinksQuery(DataInputStream dis, DataOutputStream dos,
				int commandID, String objectID, int varID, Repository<Lane> laneRepo) {
			super(dis, dos, commandID, objectID, varID);
			this.laneRepo = laneRepo;
		}

		@Override
		protected List<Link> readValue(Command resp) throws TraCIException {
			List<Link> out = new ArrayList<Link>();
			Storage content = resp.content();
			Utils.checkType(content, Constants.TYPE_COMPOUND);
			content.readInt(); // ignore link data length
			Utils.checkType(content, Constants.TYPE_INTEGER);
			int count = content.readInt();
			for (int i=0; i<count; i++) {
				try {
					out.add(new Link(content, laneRepo));
				} catch (IOException e) {
					throw new TraCIException(e.toString());
				}
			}
			return out;
		}
		
	}
	
	public enum Variable {
		SHAPE(Constants.VAR_SHAPE),
		PARENT_EDGE(Constants.LANE_EDGE_ID),
		LINKS(Constants.LANE_LINKS);
		;
		
		public final int id; 
		private Variable(int id) {
			this.id = id;
		}
	}
	
	Lane(DataInputStream dis, DataOutputStream dos, String id, Repository<Edge> edgeRepository, Repository<Lane> laneRepository) {
		super(id, Variable.class);
		
		addReadQuery(Variable.SHAPE, new ReadShapeQuery(dis, dos, id));
		addReadQuery(Variable.PARENT_EDGE, new ReadObjectVarQuery.EdgeQ(dis, dos, Constants.CMD_GET_LANE_VARIABLE, id, Variable.PARENT_EDGE.id, edgeRepository));
		addReadQuery(Variable.LINKS, new ReadLinksQuery(dis, dos, Constants.CMD_GET_LANE_VARIABLE, id, Variable.LINKS.id, laneRepository));
	}
	
	public ReadObjectVarQuery<Path2D> queryReadShape() {
		return (ReadShapeQuery) getReadQuery(Variable.SHAPE);
	}

	public ValueReadQuery<Edge> queryReadParentEdge() {
		return (ReadObjectVarQuery.EdgeQ) getReadQuery(Variable.PARENT_EDGE);
	}
	
	public ValueReadQuery<List<Link>> queryReadLinks() {
		return (ReadLinksQuery) getReadQuery(Variable.LINKS);
	}
}
