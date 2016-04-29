/*
 * This class handles all incoming server traffic
 * and splits each connection into its own thread.
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatServer 
{
	//object that controls threads
	private static final Executor EXECUTOR = Executors.newCachedThreadPool();
	private static final int PORT = 1337;
	
	private ServerSocket serverSocket;
	//thread that handles client-bound traffic
	private ServerWriter chatWriter;

	//contains out-bound strings
	private Vector<String> output;
	//list of connected usernames
	private Vector<String> usernames;
	//map that contains a username and an object to write to that username's socket
	private ConcurrentHashMap<String, BufferedWriter> userWriterMap;

	public ChatServer()
	{
		this.output = new Vector<String>();	
		this.usernames = new Vector<String>();
		this.userWriterMap = new ConcurrentHashMap<String, BufferedWriter>();

		this.chatWriter = new ServerWriter(output, userWriterMap);

		EXECUTOR.execute(chatWriter);
		
		start();	
	}
	
	public static void main(String[] args) throws IOException
	{	
		ChatServer chatServer = new ChatServer();
	}

	private void start()
	{
		try 
		{
			//create socket at 1337
			serverSocket = new ServerSocket(PORT);

			//run forever
			while(true)
			{	
				//create a thread
				ServerReader connection = new ServerReader(serverSocket.accept(), output, usernames, userWriterMap);

				EXECUTOR.execute(connection);
			}
		} 
		catch (IOException e) 
		{
			System.out.println("An error occurred while trying to open the server socket.");
			e.printStackTrace();
		}

		close();
	}

	private void close()
	{
		try 
		{
			if(serverSocket != null)
				serverSocket.close();
		} 
		catch (IOException e) 
		{
			System.out.println("An error occurred while trying to dispose of all server connections.");
			e.printStackTrace();
		}
	}
}