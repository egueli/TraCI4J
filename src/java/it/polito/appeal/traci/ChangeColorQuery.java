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

import it.polito.appeal.traci.protocol.Constants;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import de.uniluebeck.itm.tcpip.Storage;

/**
 * This query allows to change the color attribute of a TraCI object.
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 */
public class ChangeColorQuery extends ChangeObjectVarQuery<Color> {
	ChangeColorQuery(DataInputStream dis, DataOutputStream dos,
			int commandID, String objectID, int variableID) {
		super(dis, dos, commandID, objectID, variableID);
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
	}
}