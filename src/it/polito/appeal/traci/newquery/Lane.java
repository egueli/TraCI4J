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

import it.polito.appeal.traci.TraCIException;
import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.protocol.Polygon;

public class Lane extends TraciObject<Lane.Variable> {

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
	
	public enum Variable {
		SHAPE(Constants.VAR_SHAPE),
		PARENT_EDGE(Constants.LANE_EDGE_ID);
		;
		
		public final int id; 
		private Variable(int id) {
			this.id = id;
		}
	}
	
	Lane(DataInputStream dis, DataOutputStream dos, String id, Repository<Edge> edgeRepository) {
		super(id, Variable.class);
		
		addReadQuery(Variable.SHAPE, new ReadShapeQuery(dis, dos, id));
		addReadQuery(Variable.PARENT_EDGE, new ReadObjectVarQuery.EdgeQ(dis, dos, Constants.CMD_GET_LANE_VARIABLE, id, Variable.PARENT_EDGE.id, edgeRepository));
	}
	
	public ReadObjectVarQuery<Path2D> queryShape() {
		return (ReadShapeQuery) getReadQuery(Variable.SHAPE);
	}

	public ValueReadQuery<Edge> queryParentEdge() {
		return (ReadObjectVarQuery.EdgeQ) getReadQuery(Variable.PARENT_EDGE);
	}
}
