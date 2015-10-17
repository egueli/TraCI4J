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

package it.polito.appeal.traci.examples;

import it.polito.appeal.traci.SumoTraciConnection;

import java.util.ArrayList;

/**
 * Based on the OpenStepsClose example. Shows how to control the TCP_NODELAY
 * setting, which can be a problem for Linux platforms.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt; <br/>
 *         with modifications by Mardi &lt;mardi.rmit@gmail.com&gt;
 * 
 */
public class TcpNoDelayControl {

	/** main method */
	public static void main(String[] args) {
		SumoTraciConnection conn = new SumoTraciConnection(
				"test/sumo_maps/box1l/test.sumo.cfg", // config file
				12345 // random seed
		);

		/*
		 * Sets the TCP_NODELAY property on the socket connection to SUMO based
		 * on the OS we are executing in.
		 */
		String os = System.getProperty("os.name");
		if (os.matches("Linux")) {
			conn.enableTcpNoDelay();
		} else {
			conn.disableTcpNoDelay();
		}

		/*
		 * Just some simple checking code.
		 */
		boolean check = conn.isTcpNoDelayActive();

		System.out.println();
		System.out.printf("Setting TcpNoDelay to [%b] as we are in %s\n",
				check, os);

		/*
		 * For calculating mean execution times.
		 */
		ArrayList<Long> val = new ArrayList<Long>();

		try {
			/*
			 * If we did not want to depend on the OS, we can simply override
			 * the TCP_NODELAY setting by doing conn.setTcpNoDelay(false) here.
			 */
			// conn.setTcpNoDelay(false);
			conn.runServer();

			System.out.println();
			System.out.println("Map bounds are: " + conn.queryBounds());
			System.out.println();

			int i;

			for (i = 0; i < 20; i++) {
				int time = conn.getCurrentSimTime() / 1000;

				long bgn;
				long end;
				long dif;

				bgn = System.currentTimeMillis();
				conn.nextSimStep();
				end = System.currentTimeMillis();
				dif = end - bgn;

				System.out.println();
				System.out.printf("Begin Time: %s ms\n", bgn);
				System.out.printf("End Time  : %s ms\n", end);
				System.out.printf("Tick %03d : %d ms\n", time, dif);
				val.add(dif);
			}

			double sum = 0;

			for (Long l : val) {
				sum += l;
			}

			double avg = sum / (double) val.size();

			System.out.println();
			System.out.printf(
					"Average: %.2f ms for %d ticks, with tcpnodelay=%b\n", avg,
					i, conn.isTcpNoDelayActive());

			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
