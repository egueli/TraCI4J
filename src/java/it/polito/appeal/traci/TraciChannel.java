/*   
    Copyright (C) 2015 ApPeAL Group, Politecnico di Torino

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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class contains the minimum amount of objects needed to communicate with the
 * TraCI/SUMO server.
 * 
 * To avoid data interleaving and corruption in concurrent code, every access should be
 * protected using the {@link #accessLock} member.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
class TraciChannel {
	final DataInputStream in;
	final DataOutputStream out;
	final Lock accessLock;
	
	TraciChannel(DataInputStream in, DataOutputStream out, boolean locked) {
		this.in = in;
		this.out = out;
		
		if (locked) {
			accessLock = new ReentrantLock(true);
		}
		else {
			accessLock = new NullLock();
		}
	}
	
	
}
