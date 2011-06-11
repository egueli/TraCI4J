package de.uniluebeck.itm.tcpip;

import java.io.*;
import java.net.*;
import java.util.*;

/*
 * Modifications by Enrico Gueli:
 * - added generic type to Vector
 * - changed visibility of attributes to protected
 */

public class Socket {
	protected		int		port;
	protected		String	host;
	protected		java.net.ServerSocket	serverSocket = null;
	protected		java.net.Socket	socketConnection = null;
	protected		InputStream 	inStream;
	protected		OutputStream 	outStream;	
	
	public Socket(String host, int port)
	{
		this.host = host;
		this.port = port;
		init();
	}
	
	private void init() {
		
	}

	public Socket(int port)
	{
		this.host = "localhost";
		this.port = port;
		init();
	}
	
	public void accept() throws IOException
	{
		serverSocket = new ServerSocket(port);
		socketConnection = serverSocket.accept();
		
		outStream = socketConnection.getOutputStream();
		inStream = socketConnection.getInputStream();
	}
	
	public void connect() throws UnknownHostException, IOException
	{
		socketConnection = new java.net.Socket(host, port);
		
		outStream = socketConnection.getOutputStream();
		inStream = socketConnection.getInputStream();
	}
	
	public void send(Vector<Integer> data) throws IOException
	{
		int numBytes = 0;
		byte[] buffer;
		
		if (socketConnection == null || outStream == null)
			return;
		
		numBytes = data.size();
		buffer = new byte[numBytes];
		for (int i=0; i < numBytes; i++)
			buffer[i] = ((Integer)(data.get(i))).byteValue();
		
//		System.err.println ("Send " + numBytes + " bytes via tcpip::socket: " + data.toString());
		outStream.write(buffer);
	}

	public void sendExact(Storage storageToSend) throws IOException
	{
		int length;
		byte[] buffer;
		
		buffer = new byte[4 + storageToSend.size()];
		
		Storage storageLength = new Storage();
		
		length = storageToSend.size() + 4;
		storageLength.writeInt(length);
		
		for (int i=0; i < 4; i++)
			buffer[i] = storageLength.getStorageList().get(i).byteValue();

		for (int i=0; i < storageToSend.size(); i++)
			buffer[i+4] = storageToSend.getStorageList().get(i).byteValue();

/*
		System.err.print("Send " + length + " bytes via tcpip::socket: ");
		for (int i = 0; i < length; ++i)
			System.err.print( " " + buffer[i] + " ");
		System.err.println("]");
*/	
		outStream.write(buffer);		
	}

	
	public Vector<Integer> receive(int bufSize) throws UnknownHostException, IOException
	{
		Vector<Integer> returnData = new Vector<Integer>(0);
		byte[] buffer;
		int bytesRead;
		int returnByte;
		
		if (socketConnection == null)
			connect();
		if (inStream == null)
			return returnData;
		
		buffer = new byte[bufSize];
		bytesRead = inStream.read(buffer, 0, bufSize);
		
		if (bytesRead == -1)
		{
			throw new IOException("Socket.receive(): Socket closed unexpectedly");
		}
		else
		{
			returnData.ensureCapacity(bytesRead);
			for (int i=0; i<bytesRead; i++)
			{
				returnByte = buffer[i] & 0xFF;
				returnData.add(i, new Integer(returnByte));
			}
		}
		
//		System.err.println("Rcvd " + bytesRead + " bytes via tcpip::socket: " + buffer);
		
		return returnData;
	}

	public byte[] receiveBytes(int bufSize) throws UnknownHostException, IOException
	{
		byte[] buffer;
		int bytesRead = 0;
		int readThisTime = 0;
		
		if (socketConnection == null)
			connect();
		if (inStream == null)
			return null;
		
		buffer = new byte[bufSize];

		while (bytesRead < bufSize)
		{
//			DataInputStream i = new DataInputStream(inStream);
			readThisTime = inStream.read(buffer, bytesRead, bufSize - bytesRead);
				
			if (readThisTime == -1)
			{
				throw new IOException("Socket.receive(): Socket closed unexpectedly");
			}
			
			bytesRead += readThisTime;
		}
/*		
		System.err.print("Rcvd " + bytesRead + " bytes via tcpip::socket: ");
		for (int i = 0; i < bytesRead; ++i)
			System.err.print( " " + buffer[i] + " ");
		System.err.println("]");
*/		
		return buffer;
	}
	
	public Vector<Integer> receive() throws UnknownHostException, IOException
	{
		return receive(2048);
	}
	
	public Storage receiveExact() throws UnknownHostException, IOException
	{
		int length;
		
		Storage storageLength = new Storage(receiveBytes(4));
		length = storageLength.readInt() - 4;
		
		return new Storage(receiveBytes(length));
	}
	
	public void close() throws IOException
	{
		if (socketConnection != null)
			socketConnection.close();
		if (serverSocket != null)
			serverSocket.close();
		if (inStream != null)
			inStream.close();
		if (outStream != null)
			outStream.close();
	}
	
	public int port()
	{
		return port;
	}
	
	public boolean has_client_connection()
	{
		return socketConnection != null;
	}
}
