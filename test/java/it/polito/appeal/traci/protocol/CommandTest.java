/*   
    Copyright (C) 2011 ApPeAL Group, Politecnico di Torino

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

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import de.uniluebeck.itm.tcpip.Storage;

public class CommandTest {

	private static final int AN_INTEGER = 0x12345678;
	private static final int A_COMMAND_ID = 0xAA;

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testCommandStorage() {
		Storage s = sampleStorage();
		new Command(s);
	}

	private Storage sampleStorage() throws IllegalArgumentException {
		byte[] buf = new byte[] {
			(byte) 6,
			(byte) A_COMMAND_ID,
			(byte) (AN_INTEGER >> 24),
			(byte) (AN_INTEGER >> 16),
			(byte) (AN_INTEGER >>  8),
			(byte) (AN_INTEGER      )
		};
		
		return new Storage(buf);
	}

	@Test
	public void testCommandInt() {
		new Command(A_COMMAND_ID);
	}

	@Test
	public void testId() {
		Command cmd = new Command(sampleStorage());
		assertEquals(A_COMMAND_ID, cmd.id());
	}

	@Test
	public void testContent() {
		Command cmd = new Command(sampleStorage());
		assertEquals(AN_INTEGER, cmd.content().readInt());
	}

	@Test
	public void testWriteRawTo() {
		Command cmd = new Command(sampleStorage());
		Storage s = new Storage();
		cmd.writeRawTo(s);
		Iterator<Byte> bytes = s.getStorageList().iterator();
		assertEquals(0, (int)bytes.next());
		assertEquals(0, (int)bytes.next());
		assertEquals(0, (int)bytes.next());
		assertEquals(0, (int)bytes.next());
		assertEquals(10, (int)bytes.next());
		assertEquals((byte)A_COMMAND_ID, (byte)bytes.next());
		assertEquals((byte)(AN_INTEGER >> 24), (byte)bytes.next());
		assertEquals((byte)(AN_INTEGER >> 16), (byte)bytes.next());
		assertEquals((byte)(AN_INTEGER >>  8), (byte)bytes.next());
		assertEquals((byte)(AN_INTEGER),       (byte)bytes.next());
	}

}
