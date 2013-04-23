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

package it.polito.appeal.traci.test.protocol;

import static org.junit.Assert.*;

import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.RequestMessage;
import it.polito.appeal.traci.protocol.ResponseMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class MessageTest {

	@Test
	public void testResponseMessageDataInputStream() throws IOException {
		ByteArrayInputStream bais = 
			new ByteArrayInputStream(new byte[] { 
					0, 0, 0, 13, // msg len
					7,           // status resp len
					0,           // status id
					0,           // status code
					0, 0, 0, 0,  // status descr len
					2,           // resp len
					0            // resp code
					});
		DataInputStream dis = new DataInputStream(bais);
		new ResponseMessage(dis);
	}

	@Test
	public void testRequestMessage() {
		new RequestMessage();
	}

	@Test
	public void testAppend() {
		RequestMessage m = new RequestMessage();
		Command cmd = new Command(0xAA);
		m.append(cmd);
		assertEquals(cmd, m.commands().iterator().next());
	}

	@Test
	public void testWriteTo() throws IOException {
		RequestMessage m = new RequestMessage();
		Command cmd = new Command(0xAA);
		m.append(cmd);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		m.writeTo(dos);
		
		byte[] buf = baos.toByteArray();
		assertEquals(10, buf.length);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		DataInputStream dis = new DataInputStream(bais);
		assertEquals(10, dis.readInt());
	}

}
