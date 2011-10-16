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

import java.awt.geom.Path2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;


import it.polito.appeal.traci.protocol.Constants;


public class Lane extends TraciObject<Lane.Variable> {

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
		
		addReadQuery(Variable.SHAPE, new ReadShapeQuery(dis, dos, Constants.CMD_GET_LANE_VARIABLE, id, Constants.VAR_SHAPE));
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
