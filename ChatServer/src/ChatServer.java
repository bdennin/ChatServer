/**
 * This class listens for HTML requests at port 2880
 * 
 * @author Greg Gagne, Brandon Denning
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class ChatServer 
{
	private static final Executor EXECUTOR = Executors.newCachedThreadPool();
	private static final int PORT = 1337;
	
	private ServerSocket serverSocket;
	private ServerWriter chatWriter;

	private Vector<String> output;
	private Vector<String> usernames;
	private ConcurrentHashMap<String, BufferedWriter> userWriterMap;

	public ChatServer(int port)
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
		ChatServer chatServer = new ChatServer(1337);
	}

	private void start()
	{
		try 
		{
			serverSocket = new ServerSocket(PORT);

			while(true)
			{	
				ServerReader connection = new ServerReader(serverSocket.accept(), output, usernames, userWriterMap);

				System.out.println("Client connected");

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