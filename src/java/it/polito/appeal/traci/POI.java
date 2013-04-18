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

package it.polito.appeal.traci;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import de.uniluebeck.itm.tcpip.Storage;

import it.polito.appeal.traci.ChangeObjectVarQuery.ChangeStringQ;
import it.polito.appeal.traci.protocol.Constants;

/**
 * Represents a POI (Point-of-Interest) object.
 * <p>
 * This class can be used as a template for other, still-to-be-done SUMO object
 * class implementations.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
public class POI extends TraciObject<POI.Variable> {

	/*
	 * State change query classes
	 */
	
	/**
	 * This query allows to change the position of a POI.
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 */
	public class ChangePositionQuery extends ChangeObjectVarQuery<Point2D> {
		private ChangePositionQuery(DataInputStream dis, DataOutputStream dos,
				String objectID) {
			super(dis, dos, Constants.CMD_SET_POI_VARIABLE, objectID, Constants.VAR_POSITION);
		}

		/**
		 * After writing params, flushes the cache of {@link POI#changePositionQuery}.
		 */
		@Override
		protected void writeValueTo(Point2D position, Storage content) {
			content.writeByte(Constants.POSITION_2D);
			content.writeDouble(position.getX());
			content.writeDouble(position.getY());

			getReadPositionQuery().setObsolete();
		}
	}
	
	/**
	 * This query allows to change the color of a POI.
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 */
	public class ChangeColorQuery extends ChangeObjectVarQuery<Color> {
		private ChangeColorQuery(DataInputStream dis, DataOutputStream dos,
				String objectID) {
			super(dis, dos, Constants.CMD_SET_POI_VARIABLE, objectID, Constants.VAR_COLOR);
		}

		/**
		 * After writing params, flushes the cache of {@link POI#changeColorQuery}.
		 */
		@Override
		protected void writeValueTo(Color color, Storage content) {
			content.writeByte(Constants.TYPE_COLOR);
			content.writeUnsignedByte(color.getRed());
			content.writeUnsignedByte(color.getGreen());
			content.writeUnsignedByte(color.getBlue());
			content.writeUnsignedByte(color.getAlpha());
			getReadColorQuery().setObsolete();
		}
	}

	/**
	 * This enum lists all the variables that can be read.
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 */
	public enum Variable {
		TYPE,
		COLOR,
		POSITION
	}
	
	/** the "change position" query instance */
	private final ChangePositionQuery changePositionQuery;
	/** the "change color" query instance */
	private final ChangeColorQuery changeColorQuery;
	/** the "change type" query instance */
	private final ChangeStringQ changeTypeQuery;
	
	POI(String id, DataInputStream dis, DataOutputStream dos) {
		super(id, Variable.class);

		// read variable queries initialization
		
		addReadQuery(Variable.TYPE, new ReadObjectVarQuery.StringQ(dis, dos,
				Constants.CMD_GET_POI_VARIABLE, id, Constants.VAR_TYPE));
		addReadQuery(Variable.COLOR, new ReadObjectVarQuery.ColorQ(dis, dos,
				Constants.CMD_GET_POI_VARIABLE, id, Constants.VAR_COLOR));
		addReadQuery(Variable.POSITION, new ReadObjectVarQuery.PositionQ(dis, dos,
				Constants.CMD_GET_POI_VARIABLE, id, Constants.VAR_POSITION));
		
		// change state queries initialization
		
		changePositionQuery = new ChangePositionQuery(dis, dos, id);
		changeColorQuery = new ChangeColorQuery(dis, dos, id);
		changeTypeQuery = new ChangeStringQ(dis, dos, id, 
				Constants.CMD_SET_POI_VARIABLE, Constants.VAR_TYPE) {
			/**
			 * After writing params, flushes the cache of
			 * {@link POI#changeTypeQuery}.
			 */
			@Override
			protected void writeValueTo(String type, Storage content) {
				super.writeValueTo(type, content);
				getReadTypeQuery().setObsolete();
			}
		};
	}
	
	// read variable query getters
	
	public ReadObjectVarQuery<String> getReadTypeQuery() {
		return (ReadObjectVarQuery.StringQ) getReadQuery(Variable.TYPE);
	}
	
	public ReadObjectVarQuery<Color> getReadColorQuery() {
		return (ReadObjectVarQuery.ColorQ) getReadQuery(Variable.COLOR);
	}
	
	public ReadObjectVarQuery<Point2D> getReadPositionQuery() {
		return (ReadObjectVarQuery.PositionQ) getReadQuery(Variable.POSITION);
	}

	// change state query getters
	
	public ChangePositionQuery getChangePositionQuery() {
		return changePositionQuery;
	}

	public ChangeColorQuery getChangeColorQuery() {
		return changeColorQuery;
	}

	public ChangeStringQ getChangeTypeQuery() {
		return changeTypeQuery;
	}
}