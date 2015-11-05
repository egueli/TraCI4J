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

package it.polito.appeal.traci.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import it.polito.appeal.traci.SumoTraciConnection;
import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.Constants;
import it.polito.appeal.traci.protocol.RequestMessage;
import it.polito.appeal.traci.protocol.ResponseContainer;
import it.polito.appeal.traci.protocol.ResponseMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Here we verify some assumptions about the TraCI protocol and the behaviour of
 * SUMO.
 * 
 * @author Enrico Gueli &lt;enrico.gueli@polito.it&gt;
 *
 */
@SuppressWarnings("javadoc")
public class TraCIServerTest {
	
	private static final int API_VERSION = 10;

	/**
	 * The system property name to get the executable path and name to run.
	 */
	public static final String SUMO_EXE_PROPERTY = "it.polito.appeal.traci.sumo_exe";

	private static final int TRACI_PORT = 15000;
	
	private DataInputStream inStream;
	private DataOutputStream outStream;
	private Socket socket;

	private Process sumoProcess;
	
	private static final Logger log = LogManager.getLogger();
	
	@Before
	public void setUp() throws UnknownHostException, IOException, InterruptedException {
		
		runSUMO();
		
		Thread.sleep(1000);
		
		socket = new Socket();
		socket.connect(new InetSocketAddress(InetAddress.getLocalHost(),
				TRACI_PORT));
		
		inStream = new DataInputStream(socket.getInputStream());
		outStream = new DataOutputStream(socket.getOutputStream());
	}

	private void runSUMO() throws IOException {
		String sumoEXE = System.getProperty(SUMO_EXE_PROPERTY);
		if (sumoEXE == null)
			sumoEXE = "sumo";

		if (System.getProperty(SumoTraciConnection.OS_ARCH_PROPERTY).contains("64") && System.getProperty(SumoTraciConnection.OS_NAME_PROPERTY).contains("Win")) {
			sumoEXE += "64";
		}

		String[] args;
		args = new String[] { 
				sumoEXE, 
				"-c", "test/resources/sumo_maps/box1l/test.sumo.cfg", 
				"--remote-port", Integer.toString(TRACI_PORT)
				};

		sumoProcess = Runtime.getRuntime().exec(args);

	}
	
	@After
	public void tearDown() throws IOException, InterruptedException {
		inStream.close();
		outStream.close();
		sumoProcess.waitFor();
	}
	
	@Test
	public void testJustConnect() {
		
	}
	
	@Test
	public void testGetVersionLowLevel() throws IOException {
		outStream.writeInt(6); // msg length
		outStream.writeByte(2); // cmd length
		outStream.writeByte(Constants.CMD_GETVERSION);

		
		int msgLength = inStream.readInt();
		log.info("message length = " + msgLength);
		assertTrue("minimum message length", msgLength >= 21);

		/*
		byte[] all = new byte[msgLength - 4];
		inStream.readFully(all);
		log.info(Arrays.toString(all));
		fail();
		*/
		
		testVersionResponseLowLevel();
	}

	private void testVersionResponseLowLevel() throws IOException {
		
		assertEquals("status resp len", 7, inStream.readByte());
		assertEquals("status resp ID", Constants.CMD_GETVERSION, inStream.readByte());
		assertEquals("status code OK", 0, inStream.readByte());
		assertEquals("status descr empty", 0, inStream.readInt());
		
		int respLen = inStream.readByte();
		log.info("response length = " + respLen);
		assertTrue("minimum response length", respLen > 5);
		assertEquals("Response ID", Constants.CMD_GETVERSION, inStream.readByte());
		int version = inStream.readInt();
		assertTrue("API version is " + version, version >= API_VERSION);
		int nameLen = inStream.readInt();
		assertEquals(1 + 1 + 4 + 4 + nameLen, respLen);
		byte[] name = new byte[nameLen];
		inStream.readFully(name);
		log.info("Version name: \"" + new String(name) + "\"");
	}

	@Test
	public void testTwoGetVersion() throws IOException {
		outStream.writeInt(8); // msg length
		outStream.writeByte(2); // cmd length
		outStream.writeByte(Constants.CMD_GETVERSION);
		outStream.writeByte(2); // cmd length
		outStream.writeByte(Constants.CMD_GETVERSION);

		
		int msgLength = inStream.readInt();
		log.info("message length = " + msgLength);
		assertTrue("minimum message length", msgLength > 21);

		/*
		byte[] all = new byte[msgLength - 4];
		inStream.readFully(all);
		log.info(Arrays.toString(all));
		fail();
		*/

		testVersionResponseLowLevel();
		testVersionResponseLowLevel();
	}

	@Test
	public void testLongMessageLowLevel() throws IOException {
		final int REPETITIONS = 30;
		
		outStream.writeInt(4 + 2 * REPETITIONS);
		for (int i=0; i<REPETITIONS; i++) {
			outStream.writeByte(2); // cmd length
			outStream.writeByte(Constants.CMD_GETVERSION);
		}
		
		int msgLength = inStream.readInt();
		log.info("message length = " + msgLength);
		assertTrue("minimum message length", msgLength > 255);

		for (int i=0; i<REPETITIONS; i++) {
			testVersionResponseLowLevel();
		}
	}
	
	@Test
	public void testGetVersionHighLevel() throws IOException {
		RequestMessage reqm = new RequestMessage();
		reqm.append(new Command(Constants.CMD_GETVERSION));
		reqm.writeTo(outStream);
		
		ResponseMessage respm = new ResponseMessage(inStream);
		assertEquals(1, respm.responses().size());
		
		ResponseContainer pair = respm.responses().get(0);
		assertEquals(Constants.CMD_GETVERSION, pair.getStatus().id());
		
		Command resp = pair.getResponse();
		assertEquals(Constants.CMD_GETVERSION, resp.id());
		assertEquals(API_VERSION, resp.content().readInt());
		log.info(resp.content().readStringASCII());
	}
	
	@Test
	public void testCloseHighLevel() throws IOException {
		RequestMessage reqm = new RequestMessage();
		reqm.append(new Command(Constants.CMD_CLOSE));
		reqm.writeTo(outStream);
		
		ResponseMessage respm = new ResponseMessage(inStream);
		assertEquals(1, respm.responses().size());
		
		ResponseContainer pair = respm.responses().get(0);
		assertEquals(Constants.CMD_CLOSE, pair.getStatus().id());
		
		assertNull(pair.getResponse());
	}
	
}
