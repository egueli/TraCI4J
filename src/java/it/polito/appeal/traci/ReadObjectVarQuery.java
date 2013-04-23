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

import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.protocol.ResponseContainer;
import it.polito.appeal.traci.protocol.StatusResponse;
import it.polito.appeal.traci.protocol.StringList;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.uniluebeck.itm.tcpip.Storage;

/**
 * Represents a {@link ValueReadQuery} that reads a variable of a specific
 * SUMO object.
 * <p>
 * The subclass can have methods that specify further parameters for the query before it
 * is run. Since a query instance can be re-used, the subclass implementation should take care of
 * calling {@link #setObsolete()} whenever any parameter is changed.  
 * 
 * @see ValueReadQuery
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 * @param <V>
 */
public abstract class ReadObjectVarQuery<V> extends ValueReadQuery<V> {

	private final int commandID;
	private final String objectID;
	private final int varID;
	
	ReadObjectVarQuery(DataInputStream dis, DataOutputStream dos, int commandID, String objectID, int varID) {
		super(dis, dos);
		this.commandID = commandID;
		this.objectID = objectID;
		this.varID = varID;
	}
	
	@Override
	List<Command> getRequests() {
		Command cmd = new Command(commandID);
		Storage content = cmd.content();
		content.writeUnsignedByte(varID);
		content.writeStringASCII(objectID);
		return Collections.singletonList(cmd);
	}

	@Override
	void pickResponses(Iterator<ResponseContainer> responseIterator) throws TraCIException {
		ResponseContainer respc = responseIterator.next();
		StatusResponse statusResp = respc.getStatus();
		Utils.checkStatusResponse(statusResp, commandID);
		Command resp = respc.getResponse();
		Utils.checkByte(resp.content(), varID);
		Utils.checkObjectID(resp.content(), objectID);
		
		V value = readValue(resp);
		setDone(value);
	}
	
	protected abstract V readValue(Command resp) throws TraCIException; 

	/*
	 * The following type-specific queries will tell how to read each of the
	 * SUMO basic types. Compound types, which are used only by a SUMO object
	 * class, should be declared in the corresponding TraciObject class.
	 */
	
	/**
	 * Specialization of {@link ReadObjectVarQuery} whose value type is an
	 * {@link Integer}.
	 * 
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 * 
	 */
	public static class IntegerQ extends ReadObjectVarQuery<Integer> {

		IntegerQ(DataInputStream dis, DataOutputStream dos, int commandID,
				String objectID, int varID) {
			super(dis, dos, commandID, objectID, varID);
		}

		@Override
		protected Integer readValue(Command resp)
				throws TraCIException {
			Storage content = resp.content();
			Utils.checkType(content, Constants.TYPE_INTEGER);
			return content.readInt();
		}
		
	}
	
	/**
	 * Specialization of {@link ReadObjectVarQuery} whose value type is an
	 * {@link Double}.
	 * 
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 * 
	 */
	public static class DoubleQ extends ReadObjectVarQuery<Double> {

		DoubleQ(DataInputStream dis, DataOutputStream dos, int commandID, String objectID, int varID) {
			super(dis, dos, commandID, objectID, varID);
		}

		@Override
		protected Double readValue(Command resp) throws TraCIException {
			Storage content = resp.content();
			Utils.checkType(content, Constants.TYPE_DOUBLE);
			return content.readDouble();
		}
	}

	/**
	 * Specialization of {@link ReadObjectVarQuery} whose value type is an
	 * {@link Color}.
	 * 
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 * 
	 */
	public static class ColorQ extends ReadObjectVarQuery<Color> {

		ColorQ(DataInputStream dis, DataOutputStream dos, int commandID,
				String objectID, int varID) {
			super(dis, dos, commandID, objectID, varID);
		}

		@Override
		protected Color readValue(Command resp) throws TraCIException {
			Storage content = resp.content();
			Utils.checkType(content, Constants.TYPE_COLOR);
			int r = content.readUnsignedByte();
			int g = content.readUnsignedByte();
			int b = content.readUnsignedByte();
			int a = content.readUnsignedByte();
			return new Color(r, g, b, a);
		}
	}

	/**
	 * Specialization of {@link ReadObjectVarQuery} whose value type is a
	 * {@link Point2D}.
	 * 
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 * 
	 */
	public static class PositionQ extends ReadObjectVarQuery<Point2D> {

		private int positionType = Constants.POSITION_2D;
		
		PositionQ(DataInputStream dis, DataOutputStream dos, int commandID, String vehicleID, int varID) {
			super(dis, dos, commandID, vehicleID, varID);
		}
		
		/**
		 * Sets the position type.
		 * 
		 * @param type
		 *            the position type. It can be {@link Constants#POSITION_2D}
		 *            or {@link Constants#POSITION_LAT_LON}
		 */
		public void setPositionType(int type) {
			if (type == Constants.POSITION_2D || type == Constants.POSITION_LAT_LON) {
				if (type != positionType)
					setObsolete();
				positionType = type;
			}
			else
				throw new IllegalArgumentException("invalid position type");
		}

		@Override
		protected Point2D readValue(Command resp) throws TraCIException {
			Storage content = resp.content();
			Utils.checkType(content, positionType);
			double x = content.readDouble();
			double y = content.readDouble();
			return new Point2D.Double(x, y);
		}
	}

	/**
	 * Specialization of {@link ReadObjectVarQuery} whose value type is a
	 * {@link String}.
	 * 
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 * 
	 */
	public static class StringQ extends ReadObjectVarQuery<String> {
		
		StringQ(DataInputStream dis, DataOutputStream dos, int commandID, String objectID, int varID) {
			super(dis, dos, commandID, objectID, varID);
		}
		
		protected String readValue(Command resp) throws TraCIException {
			Storage content = resp.content();
			Utils.checkType(content, Constants.TYPE_STRING);
			return content.readStringASCII();
		}
	}

	/**
	 * Specialization of {@link ReadObjectVarQuery} whose value type is a
	 * {@link List} of {@link String}s.
	 * 
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 * 
	 */
	public static class StringListQ extends ReadObjectVarQuery<List<String>> {

		StringListQ(DataInputStream dis, DataOutputStream dos, int commandID,
				String objectID, int varID) {
			super(dis, dos, commandID, objectID, varID);
		}

		@Override
		protected List<String> readValue(
				Command resp) throws TraCIException {
			return new StringList(resp.content(), true);
		}
	}
	
	/**
	 * Specialization of {@link ReadObjectVarQuery} whose value type is a
	 * {@link Rectangle2D}.
	 * 
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 * 
	 */
	public static class BoundingBoxQ extends ReadObjectVarQuery<Rectangle2D> {

		BoundingBoxQ(DataInputStream dis, DataOutputStream dos, int commandID, String vehicleID, int varID) {
			super(dis, dos, commandID, vehicleID, varID);
		}

		@Override
		protected Rectangle2D readValue(Command resp) throws TraCIException {
			Storage content = resp.content();
			return new it.polito.appeal.traci.protocol.BoundingBox(content, true);
		}
	}
	
	/**
	 * Specialization of {@link ReadObjectVarQuery} whose value type is a
	 * {@link TraciObject}.
	 * 
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 * 
	 */
	public static class TraciObjectQ<V extends TraciObject<?>> extends ReadObjectVarQuery<V> {

		private final Repository<V> repo;
		
		TraciObjectQ(DataInputStream dis, DataOutputStream dos,
				int commandID, String objectID, int varID, Repository<V> repo) {
			super(dis, dos, commandID, objectID, varID);
			this.repo = repo;
		}

		@Override
		protected V readValue(Command resp) throws TraCIException {
			Storage content = resp.content();
			Utils.checkType(content, Constants.TYPE_STRING);
			String id = content.readStringASCII();
			try {
				return repo.getByID(id);
			} catch (IOException e) {
				throw new TraCIException(e.toString());
			}
		}
	}
	
	/**
	 * Specialization of {@link ReadObjectVarQuery} whose value type is an
	 * {@link Edge}.
	 * 
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 * 
	 */
	public static class EdgeQ extends TraciObjectQ<Edge> {
		EdgeQ(DataInputStream dis, DataOutputStream dos, int commandID,
				String objectID, int varID, Repository<Edge> repo) {
			super(dis, dos, commandID, objectID, varID, repo);
		}
	}
	
	/**
	 * Specialization of {@link ReadObjectVarQuery} whose value type is a
	 * {@link Lane}.
	 * 
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 * 
	 */
	public static class LaneQ extends TraciObjectQ<Lane> {
		LaneQ(DataInputStream dis, DataOutputStream dos, int commandID,
				String objectID, int varID, Repository<Lane> repo) {
			super(dis, dos, commandID, objectID, varID, repo);
		}
	}

}
