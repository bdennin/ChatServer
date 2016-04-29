/*
 * This class handles input obtained from the chat GUI,
 * formats it, and sends it to the server
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

public class ClientReader implements Runnable
{
	private static final String USER_CONNECTED = "has joined the server.";
	private static final String USER_DISCONNECTED = "has left the server.";
	//sleep time between iterations
	private static final int SLEEP_TIME = 100;

	//this class is passed a chat client, so that it can manipulate the chat client
	private ChatClient chatClient;

	//stream that reads input from the server
	private BufferedReader bufferedReader;

	//see ChatServer
	private Vector<String> input;
	private Vector<String> usernames;
	private String[] parsedData;

	public ClientReader(ChatClient chatClient, BufferedReader bufferedReader, Vector<String> input, Vector<String> usernames)
	{
		this.chatClient = chatClient;

		this.bufferedReader = bufferedReader;
		
		this.input = input;
		this.usernames = usernames;
	}

	public void run()
	{
		while(chatClient.isConnected())
		{
			String data = readSocket();

			if(data != null)
				handleData(data);

			try 
			{
				Thread.sleep(SLEEP_TIME);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}

	private String readSocket()
	{
		String data = null;

		try 
		{
			data = bufferedReader.readLine();
		} 
		catch (IOException e) 
		{
			chatClient.disconnect();
			System.out.println("An error occurred while attempting to read a chat client socket.");
		}

		return data;
	}

	private void handleData(String parseable)
	{
		parsedData = parseable.split(" ");

		int commandType = Integer.parseInt(parsedData[0]);

		switch(commandType)
		{
		case 1:
		{
			handleUsernameAccept();
			break;
		}

		case 2:
		{
			handleUsernameReject();
			break;
		}

		case 5:
		{
			handleGlobalMessage();
			break;
		}

		case 6:
		{
			handlePrivateMessage();
			break;
		}

		case 8:
		{
			handleServerDisconnect();
			break;
		}

		case 9:
		{
			handleRemoveUsername();
			break;
		}

		case 10:
		{
			handleAddUsername();
			break;
		}

		default:
		{
			System.out.println("A client has read an unknown packet type");
			break;
		}
		}
	}

	private void handleUsernameAccept()
	{
		chatClient.setUsernameAccepted(true);

		String usernames = parsedData[1];
		String welcome = "";

		if(usernames != null)
		{
			usernames = usernames.replaceAll(",", " ");
			String[] splitUsers = usernames.split(" ");
			int size = splitUsers.length;
			
			for(int i = 0; i < size; i++)
				this.usernames.addElement(splitUsers[i]);
		}
		
		for(int i = 2; i < parsedData.length; i++)
		{
			if(i == parsedData.length - 1)
				welcome += String.format("%s", parsedData[i]);
			else
				welcome += String.format("%s ", parsedData[i]);
		}

		welcome = String.format("%s\r\n", welcome);
		
		input.addElement(welcome);
	}

	private void handleUsernameReject()
	{
		chatClient.disconnect();
		chatClient.setUsernameAccepted(false);
	}

	private void handleGlobalMessage()
	{
		String username = parsedData[1];
		String time = parsedData[2];
		String message = "";
		int size = parsedData.length;
		
		for(int i = 3; i < size; i++)
		{
			if(i == parsedData.length - 1)
				message += String.format("%s", parsedData[i]);
			else
				message += String.format("%s ", parsedData[i]);
		}
		
		message = String.format("[%s]%s: %s\r\n", time, username, message);

		input.addElement(message);
	}

	private void handlePrivateMessage()
	{
		String username = parsedData[1];
		String time = parsedData[3];
		String message = "";
		int size = parsedData.length;
		
		for(int i = 4; i < size; i++)
		{
			if(i == parsedData.length - 1)
				message += String.format("%s", parsedData[i]);
			else
				message += String.format("%s ", parsedData[i]);
		}
		
		message = String.format("[%s]%s: %s\r\n", time, username, message);
		
		input.addElement(message);
	}

	private void handleServerDisconnect()
	{
		chatClient.disconnect();
	}

	private void handleAddUsername()
	{
		String username = parsedData[1];

		usernames.addElement(username);
		
		String message = String.format("%s %s\r\n", username, USER_CONNECTED);

		input.addElement(message);
	}

	private void handleRemoveUsername()
	{
		String username = parsedData[1];
		
		usernames.remove(username);
		
		String message = String.format("%s %s\r\n", username, USER_DISCONNECTED);
		
		input.addElement(message);
	}
}