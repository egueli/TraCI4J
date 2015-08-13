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

package it.polito.appeal.traci.test;

import it.polito.appeal.traci.SumoTraciConnection;
import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.Constants;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MultiThreadAccessTest {

	private static final Logger log = Logger.getLogger(MultiThreadAccessTest.class);
	private MockTraciServer mockServer = new MockTraciServer();

	static {
		// Log4j configuration must be done only once, otherwise output will be duplicated for each test
		
		// Basic configuration that outputs everything		
		org.apache.log4j.BasicConfigurator.configure();
		
		// Configuration specified by a properties file
		//PropertyConfigurator.configure("test/log4j.properties");
	}
	
	@Before
	public void startMockServer() throws IOException {
		mockServer.start();
	}

	@After
	public void stopMockServer() throws IOException, InterruptedException {
		mockServer.stop();
	}
	
	
	@Test
	public void testMockServer() throws IOException, InterruptedException {
		connectToMockServer();
	}



	private SumoTraciConnection connectToMockServer() throws IOException,
			InterruptedException {
		return new SumoTraciConnection(InetAddress.getLoopbackAddress(), 5000);
	}
	
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 3];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 3] = hexArray[v >>> 4];
	        hexChars[j * 3 + 1] = hexArray[v & 0x0F];
	        hexChars[j * 3 + 2] = ' ';
	    }
	    return new String(hexChars);
	}
	
	static class MockTraciServer {
		Thread mockServerThread;
		ServerSocket mockServerSocket;
		Socket connectionSocket = null;
		Semaphore replySemaphore = new Semaphore(0);

		public void start() {
			Runnable serverRun = new Runnable() {
				@Override
				public void run() {
			        mockServerThread();
				}
				
			};
			mockServerThread = new Thread(serverRun, "Mock SUMO Server");
			mockServerThread.start();

		}
		
		public void stop() throws IOException, InterruptedException {
			mockServerThread.interrupt();
			mockServerSocket.close();
			connectionSocket.close();
			mockServerThread.join();
		}
		
		private void mockServerThread() {
			try {
				mockServerSocket = new ServerSocket(5000);
				while (!Thread.interrupted()) {
					connectionSocket = mockServerSocket.accept();
					handleConnection();				
				}
			}
			catch (Exception e) {
				if (!Thread.interrupted()) {
					throw new RuntimeException(e);
				}
			}
			log.debug("thread exit due to interruption");
		}

		private void handleConnection() throws IOException {
			try {
				DataInputStream dis = new DataInputStream(connectionSocket.getInputStream());
				DataOutputStream dos = new DataOutputStream(connectionSocket.getOutputStream());
				while (!serve(dis, dos))
					;
		
				}
			finally {
				connectionSocket.close();
				connectionSocket = null;
			}
		}

		private boolean serve(DataInputStream dis, DataOutputStream dos)
				throws IOException {
			int wholeLength = dis.readInt(); // length is ignored

			byte[] requestMsg = new byte[wholeLength - 4];
			dis.readFully(requestMsg);
			log.debug("got message with length " + wholeLength + ": " + Arrays.toString(requestMsg));
			
			int commandID = requestMsg[5];
			commandID += (commandID >= 0) ? 0 : 256; 
			log.debug("command id: " + toHex(commandID));
			
			switch(commandID) {
			case Constants.CMD_GET_SIM_VARIABLE:
				dos.writeInt(23);
				dos.write(new byte[] {7, -85, 0, 0, 0, 0, 0, 12, -69, 112, 0, 0, 0, 0, 9, 0, 0, 0, 0});
				break;
			case Constants.CMD_GET_VEHICLE_VARIABLE:
				dos.writeInt(23);
				dos.write(new byte[] {7, -92, 0, 0, 0, 0, 0, 12, -76, 0, 0, 0, 0, 0, 14, 0, 0, 0, 0});
				break;
			default:
				log.warn("Unhandled command ID " + toHex(commandID) + ", exiting");
				return true;	
			}
			
			return false;
		}

		private String toHex(int value) {
			return Integer.toHexString(value).toUpperCase();
		}

	}
}
