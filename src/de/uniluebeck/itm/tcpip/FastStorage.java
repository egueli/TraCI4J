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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

public class FastStorage extends Storage {

	public static class NotReadableException extends IllegalStateException {
		private static final long serialVersionUID = 7821333062605489575L;
	}

	public static class NotWritableException extends IllegalStateException {
		private static final long serialVersionUID = 7821333062605489575L;
	}

	
	byte[] data;
	
	boolean readable;
	
	ByteArrayInputStream bais;
	ByteArrayOutputStream baos;
	DataInputStream dis;
	DataOutputStream dos;

	private int readBytes;
	
	public FastStorage() {
		setWritable();
	}

	
	public FastStorage(byte[] packet) {
//		super(packet);
		data = packet;
		setReadable();
	}

	private void setWritable() {
		readable = false;
		baos = new ByteArrayOutputStream();
		dos = new DataOutputStream(baos);
	}

	private void setReadable() {
		readable = true;
		bais = new ByteArrayInputStream(data);
		dis = new DataInputStream(bais);
	}

	public FastStorage(short[] packet) {
		this(packet, 0, packet.length);
	}

	public FastStorage(short[] packet, int offset, int length) {
		data = new byte[length];
		for(int i=0; i<length; i++)
			data[i] = checkByteRange(packet[i+offset]);
		
		readable = false;
		setWritable();
	}

	private static byte checkByteRange(short value) {
		if (value < -128 || value > 127)
			throw new IllegalArgumentException("Byte value may only range from -128 to 127.");
		return (byte) value;
	}
	
	
	@Override
	public int position() {
		if(readable)
			return readBytes;
		else
			throw new NotReadableException();
	}

	@Override
	public short readByte() throws IllegalStateException {
		if(readable)
		{
			try {
				readBytes++;
				return dis.readByte();
			}
			catch (EOFException ee) { throw new IllegalStateException(ee); }
			catch (IOException e) { throw new RuntimeException(e); }
		}
		throw new NotReadableException();
	}

	@Override
	public double readDouble() throws IllegalStateException {
		if(readable)
		{
			try {
				readBytes += 8;
				return dis.readDouble();
			}
			catch (EOFException ee) { throw new IllegalStateException(ee); }
			catch (IOException e) { throw new RuntimeException(e); }
		}
		throw new NotReadableException();
	}

	@Override
	public float readFloat() throws IllegalStateException {
		if(readable)
		{
			try {
				readBytes += 4;
				return dis.readFloat();
			}
			catch (EOFException ee) { throw new IllegalStateException(ee); }
			catch (IOException e) { throw new RuntimeException(e); }
		}
		throw new NotReadableException();
	}

	@Override
	public int readInt() throws IllegalStateException {
		if(readable)
		{
			try {
				readBytes += 4;
				return dis.readInt();
			}
			catch (EOFException ee) { throw new IllegalStateException(ee); }
			catch (IOException e) { throw new RuntimeException(e); }
		}
		throw new NotReadableException();
	}

	@Override
	public int readShort() throws IllegalStateException {
		if(readable)
		{
			try {
				readBytes += 2;
				return dis.readShort();
			}
			catch (EOFException ee) { throw new IllegalStateException(ee); }
			catch (IOException e) { throw new RuntimeException(e); }
		}
		throw new NotReadableException();
	}

	private String readString(String charset) throws IllegalStateException {
		if(readable)
		{
			try {
				int length = dis.readInt();
				byte[] buf = new byte[length];
				dis.read(buf);
				readBytes += 4 + length;
				return new String(buf, charset);
			}
			catch (EOFException ee) { throw new IllegalStateException(ee); }
			catch (IOException e) { throw new RuntimeException(e); }
		}
		throw new NotReadableException();
	}

	public String readStringUTF8() throws IllegalArgumentException
	{
		return readString("UTF-8");
	}
	
	public String readStringASCII() throws IllegalArgumentException
	{
		return readString("US-ASCII");
	}
	
	public String readStringISOLATIN1() throws IllegalArgumentException
	{
		return readString("ISO-8859-1");
	}
	
	public String readStringUTF16BE() throws IllegalArgumentException
	{
		return readString("UTF-16BE");
	}
	
	public String readStringUTF16LE() throws IllegalArgumentException
	{
		return readString("UTF-16LE");
	}
	

	@Override
	public short readUnsignedByte() throws IllegalStateException {
		if(readable)
		{
			try {
				readBytes++;
				return (short) dis.readUnsignedByte();
			}
			catch (EOFException ee) { throw new IllegalStateException(ee); }
			catch (IOException e) { throw new RuntimeException(e); }
		}
		throw new NotReadableException();
	}

	@Override
	public void reset() {
		throw new UnsupportedOperationException("TBD");
	}

	@Override
	public int size() {
		if(readable)
			return data.length;
		else
			throw new NotReadableException();		
	}

	@Override
	public boolean validPos() {
		return true;
	}

	@Override
	public void writeByte(int value) throws IllegalArgumentException {
		if(!readable)
			try {
				dos.writeByte(value);
			} catch (IOException e) { throw new RuntimeException(e); }
		else
			throw new NotWritableException();			
	}

	@Override
	public void writeByte(short value) throws IllegalArgumentException {
		if(!readable)
			try {
				dos.writeByte(value);
			} catch (IOException e) { throw new RuntimeException(e); }
		else
			throw new NotWritableException();			
	}

	@Override
	public void writeDouble(double value) throws IllegalArgumentException {
		if(!readable)
			try {
				dos.writeDouble(value);
			} catch (IOException e) { throw new RuntimeException(e); }
		else
			throw new NotWritableException();			
	}

	@Override
	public void writeFloat(float value) throws IllegalArgumentException {
		if(!readable)
			try {
				dos.writeFloat(value);
			} catch (IOException e) { throw new RuntimeException(e); }
		else
			throw new NotWritableException();			
	}

	@Override
	public void writeInt(int value) throws IllegalArgumentException {
		if(!readable)
			try {
				dos.writeInt(value);
			} catch (IOException e) { throw new RuntimeException(e); }
		else
			throw new NotWritableException();			
	}

	@Override
	public void writeShort(int value) throws IllegalArgumentException {
		if(!readable)
			try {
				dos.writeShort(value);
			} catch (IOException e) { throw new RuntimeException(e); }
		else
			throw new NotWritableException();			
	}

	@Override
	public void writeStringASCII(String value) throws IllegalArgumentException {
		throw new UnsupportedOperationException("TBD");
	}

	@Override
	public void writeStringISOLATIN1(String value)
			throws IllegalArgumentException {
		throw new UnsupportedOperationException("TBD");
	}

	/* (non-Javadoc)
	 * @see de.uniluebeck.itm.tcpip.Storage#writeStringUTF16BE(java.lang.String)
	 */
	@Override
	public void writeStringUTF16BE(String value)
			throws IllegalArgumentException {
		throw new UnsupportedOperationException("TBD");
	}

	/* (non-Javadoc)
	 * @see de.uniluebeck.itm.tcpip.Storage#writeStringUTF16LE(java.lang.String)
	 */
	@Override
	public void writeStringUTF16LE(String value)
			throws IllegalArgumentException {
		throw new UnsupportedOperationException("TBD");
	}

	@Override
	public void writeStringUTF8(String value) throws IllegalArgumentException {
		if(!readable)
			try {
				dos.writeUTF(value);
			} catch (IOException e) { throw new RuntimeException(e); }
		else
			throw new NotWritableException();			
	}

	@Override
	public void writeUnsignedByte(int value) throws IllegalArgumentException {
		writeByte(value);
	}

	@Override
	public void writeUnsignedByte(short value) throws IllegalArgumentException {
		writeByte(value);
	}

	public byte[] getBytes() {
		if(!readable)
			return baos.toByteArray();
		else
			throw new NotWritableException();			
	}
}
