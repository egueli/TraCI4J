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

import java.io.IOException;

/**
 * An exception in the TraCI protocol.
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
@SuppressWarnings("serial")
public class TraCIException extends IOException {
	
	/**
	 * Constructor with no description.
	 */
	public TraCIException() {
		super();
	}

	/**
	 * Constructor with a text description.
	 * @param msg
	 */
	public TraCIException(String msg) {
		super(msg);
	}

	/**
	 * A check of the response data failed.
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 *
	 */
	public static class UnexpectedData extends TraCIException {
		/**
		 * 
		 * @param what a description of the checked data
		 * @param expected the expected value
		 * @param got the read value
		 */
		public UnexpectedData(String what, Object expected, Object got) {
			super("Unexpected " + what + ": expected " + expected + ", got " + got);
		}
	}
	
	/**
	 * The data type of a value in the response was not the expected one.
	 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
	 *
	 */
	public static class UnexpectedDatatype extends UnexpectedData {
		/**
		 * 
		 * @param expected the expected data type
		 * @param got the read data type 
		 */
		public UnexpectedDatatype(int expected, int got) {
			super("datatype", expected, got);
		}
	}

}
