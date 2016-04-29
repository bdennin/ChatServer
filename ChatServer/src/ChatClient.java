/*
 * This class embodies the client connection to the server.
 * This class is used to handle all traffic the client uses.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextArea;

public class ChatClient 
{	
	//object for executing threads
	private static final Executor EXECUTOR = Executors.newCachedThreadPool();
	private static final int PORT = 1337;
	
	//threads for reading data from the server, and writing it to the GUI
	private ClientReader clientReader;
	private ClientWriter clientWriter;

	//stored textarea where server content will be displayed
	private JTextArea chatArea;
	//area where usernames will be written
	private JList<String> usernameArea;
	//model for username area
	private DefaultListModel<String> usernameModel;

	//socket and all of its reader/writing objects
	private Socket socket;
	private InputStream clientInput;
	private InputStreamReader inputReader;
	private BufferedReader bufferedReader;
	private OutputStream clientOutput;
	private OutputStreamWriter outputWriter;
	private BufferedWriter bufferedWriter;

	//See ChatServer
	private Vector<String> input;
	private Vector<String> usernames;

	//address of server
	private String serverIP;
	//client username
	private String username;
	//determines if the client is connected to the server
	private Boolean isConnected;
	//determines if the username is valid
	private Boolean isUsernameAccepted;

	public ChatClient(String serverIP, String username)
	{
		this.serverIP = serverIP;
		this.username = username;
		
		this.input = new Vector<String>();
		this.usernames = new Vector<String>(); 
		
		try 
		{	
			this.socket = new Socket(this.serverIP, PORT);
			this.clientInput = socket.getInputStream();
			this.clientOutput = socket.getOutputStream();

			this.inputReader = new InputStreamReader(clientInput);
			this.bufferedReader = new BufferedReader(inputReader);
			
			this.outputWriter = new OutputStreamWriter(clientOutput);
			this.bufferedWriter = new BufferedWriter(outputWriter);

			this.clientReader = new ClientReader(this, bufferedReader, input, usernames);
			this.clientWriter = new ClientWriter(this, input, usernames);
			
			this.isUsernameAccepted = null;
			this.isConnected = true;
			
			EXECUTOR.execute(clientReader);
			EXECUTOR.execute(clientWriter);
		} 
		catch (IOException e) 
		{
			System.out.println("An error occurred while attempting to establish a connection with the server.");
			
			this.isConnected = false;
			close();
		}
	}
	
	public void sendUsername()
	{
		if(isConnected)
		{
			String packetData = String.format("%d %s\r\n", 0, username.trim());

			sendPacketData(packetData);
		}
	}

	public void sendGlobalMessage(String message)
	{		
		if(isConnected)
		{
			String packetData = String.format("%d %s\r\n", 3, message.trim());

			sendPacketData(packetData);
		}
	}

	public void sendPrivateMessage(String toUsername, String message)
	{		
		if(isConnected)
		{
			String packetData = String.format("%d %s %s %s\r\n", 4, username.trim(), toUsername.trim(), message.trim());
			
			sendPacketData(packetData);
		}
	}

	public void sendDisconnectRequest()
	{
		if(isConnected)
		{
			String packetData = String.format("%d\r\n", 7);

			sendPacketData(packetData);
		}
	}

	public Boolean isConnected()
	{
		return isConnected;
	}
	
	public void disconnect()
	{
		isConnected = false;
		close();
	}
	
	public Boolean getUsernameAccepted()
	{
		return isUsernameAccepted;
	}
	
	public void setUsernameAccepted(Boolean isUsernameAccepted)
	{
		this.isUsernameAccepted = isUsernameAccepted;
	}
	
	public JTextArea getChatArea()
	{
		return chatArea;
	}
	
	public void setChatArea(JTextArea chatArea)
	{
		this.chatArea = chatArea;
	}
	
	public JList<String> getUsernameArea()
	{
		return usernameArea;
	}
	
	public void setUsernameArea(JList<String> usernameArea)
	{
		this.usernameArea = usernameArea;
	}
	
	public DefaultListModel<String> getUsernameModel()
	{
		return usernameModel;
	}
	
	public void setUsernameModel(DefaultListModel<String> usernameModel)
	{
		this.usernameModel = usernameModel;
	}
	
	public void close()
	{
		try 
		{
			if(socket != null)
				socket.close();

			if(clientInput != null)
				clientInput.close();

			if(inputReader != null)
				inputReader.close();

			if(bufferedReader != null)
				bufferedReader.close();

			if(clientOutput != null)
				clientOutput.close();

			if(outputWriter != null)
				outputWriter.close();

			if(bufferedWriter != null)
				bufferedWriter.close();
		}
		catch(IOException e)
		{
			System.out.println("An error occurred while attempting to close a socket attempt to the server.");
			e.printStackTrace();
		}
	}

	private void sendPacketData(String packetData)
	{
		try 
		{
			bufferedWriter.write(packetData);
			bufferedWriter.flush();
		}
		catch (IOException e) 
		{
			System.out.println("An error occurred while trying to send data to the server.");
			e.printStackTrace();
		}
	}
}
