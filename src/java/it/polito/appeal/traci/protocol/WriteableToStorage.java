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

import de.uniluebeck.itm.tcpip.Storage;

/**
 * Classes that implement this interface can be serialized to a {@link Storage}.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 * 
 */
public interface WriteableToStorage {
	
	/**
	 * Serializes the contents at the end of the specified Storage.
	 * 
	 * @param storage the Storage where to put contents to
	 * @param withTypeID if <code>true</code>, prepend data with the SUMO type ID
	 */
	void writeTo(Storage storage, boolean withTypeID);
}
