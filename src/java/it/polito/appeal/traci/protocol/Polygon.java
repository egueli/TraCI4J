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

package it.polito.appeal.traci.protocol;

import it.polito.appeal.traci.TraCIException;

import java.awt.geom.Path2D;

import de.uniluebeck.itm.tcpip.Storage;

/**
 * Representation of a polygon in the SUMO environment.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * @see <a
 *      href="http://sumo.sourceforge.net/doc/current/docs/userdoc/Simulation/Shapes.html">SUMO
 *      documentation</a>
 */
public class Polygon implements WriteableToStorage {

	private final Path2D shape;

	/**
	 * De-serializes a polygon from a {@link Storage}.
	 * 
	 * @param packet
	 * @param verifyType
	 * @throws TraCIException
	 */
	public Polygon(Storage packet, boolean verifyType) throws TraCIException {
		super();
		if (verifyType) {
			if (packet.readUnsignedByte() != Constants.TYPE_POLYGON)
				throw new TraCIException("polygon expected");
		}

		shape = new Path2D.Double();

		int count = packet.readUnsignedByte();
		for (int i = 0; i < count; i++) {
			double x = packet.readDouble();
			double y = packet.readDouble();
			if (i == 0)
				shape.moveTo(x, y);
			else
				shape.lineTo(x, y);

		}
	}

	/**
	 * 
	 * @return the shape that describes this polygon
	 */
	public Path2D getShape() {
		return shape;
	}

	public void writeTo(Storage storage, boolean withTypeID) {
		throw new UnsupportedOperationException("to be done");
	}
}
