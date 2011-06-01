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

package de.uniluebeck.itm.tcpip;

import java.io.*;
import java.net.*;
import java.util.*;

/*
 * Modifications by Enrico Gueli:
 * - added generic type to Vector
 */

public class FastSocket extends Socket {
	public FastSocket(String host, int port)
	{
		super(host, port);
	}
	
	public FastSocket(int port)
	{
		super(port);
	}
	
	public void send(Vector<Integer> data) throws IOException
	{
		throw new UnsupportedOperationException("TBD");
	}

	public void sendExact(FastStorage storageToSend) throws IOException
	{
		byte[] buffer;
		
		buffer = new byte[4 + storageToSend.size()];
		
		FastStorage storageLength = new FastStorage();
		storageLength.writeInt(storageToSend.size() + 4);
		
		System.arraycopy(storageLength.getBytes(), 0, buffer, 0, 4);
		System.arraycopy(storageToSend.getBytes(), 0, buffer, 4, storageLength.size());

		outStream.write(buffer);		
	}

	
	public Vector<Integer> receive(int bufSize) throws UnknownHostException, IOException
	{
		throw new UnsupportedOperationException("TBD");
	}

	
	public Vector<Integer> receive() throws UnknownHostException, IOException
	{
		throw new UnsupportedOperationException("TBD");
	}
	
	public FastStorage receiveExact() throws UnknownHostException, IOException
	{
		int length;
		
		FastStorage storageLength = new FastStorage(receiveBytes(4));
		length = storageLength.readInt() - 4;
		
		return new FastStorage(receiveBytes(length));
	}


}
