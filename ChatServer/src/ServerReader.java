import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerReader implements Runnable
{	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("YYYY:MM:DD:HH:MM:SS");
	private static final String ALLOWED_CHARACTERS = "^[a-zA-Z0-9_]*$";
	private static final Pattern PATTERN_MATCHER = Pattern.compile(ALLOWED_CHARACTERS);
	private static final String SERVER_GREETING = "Welcome to the chat server. Enter '/w [username]' to send a private message.";

	private Socket socket;
	
	private InputStream serverInput;
	private InputStreamReader inputReader;
	private BufferedReader bufferedReader;
	private OutputStream serverOutput;
	private OutputStreamWriter outputWriter;
	private BufferedWriter bufferedWriter;

	private Vector<String> output;
	private Vector<String> usernames;
	private ConcurrentHashMap<String, BufferedWriter> userWriterMap;

	private String username;
	private boolean keepAlive;
	private boolean hasValidUsername;
	
	private String[] parsedData;
	private String packetData;
	private String broadcastData;
	

	public ServerReader(Socket socket, Vector<String> output, Vector<String> usernames, ConcurrentHashMap<String, BufferedWriter> userWriterMap)
	{
		this.socket = socket;
		
		this.output = output;
		this.usernames = usernames;
		this.userWriterMap = userWriterMap;
		
		try 
		{
			this.serverInput = socket.getInputStream();
			this.serverOutput = socket.getOutputStream();
			
			this.inputReader = new InputStreamReader(serverInput);
			this.bufferedReader = new BufferedReader(inputReader);
		
			this.outputWriter = new OutputStreamWriter(serverOutput);
			this.bufferedWriter = new BufferedWriter(outputWriter);

			this.username = null;
			this.keepAlive = true;
		} 
		catch (IOException e) 
		{
			System.out.println("An error occurred while attempting to open streams to the server.");
			e.printStackTrace();
		}
	}
	
	public void run() 
	{ 
		while(keepAlive)
		{
			String data = readSocket();	

			if(data != null)
			{
				handleData(data);
			}
		}

		close();
	}

	public BufferedWriter getSocketWriter()
	{
		return bufferedWriter;
	}
	
	public void close() 
	{
		try 
		{
			if(socket != null)
				socket.close();

			if(serverInput != null)
				serverInput.close();

			if(inputReader != null)
				inputReader.close();

			if(bufferedReader != null)
				bufferedReader.close();
			
			if(serverOutput != null)
				serverOutput.close();
			
			if(outputWriter != null)
				outputWriter.close();
			
			if(bufferedWriter != null)
				bufferedWriter.close();
			
			System.out.println("User, " + username + ", has disconnected.");
			
			if(hasValidUsername)
			{
				handleUserDisconnect();
			}
		}
		catch(IOException e)
		{
			System.out.println("An error occurred while attempting to close a socket attempt to the server.");
			e.printStackTrace();
		}
	}
	
	private String readSocket()
	{
		String data = null;
		System.out.println("Waiting for data");

		try 
		{
			data = bufferedReader.readLine();
		} 
		catch (IOException e) 
		{
			System.out.println("An error occurred while attempting to read a socket connected to the server.");
			keepAlive = false;
		}

		return data;
	}

	private void handleData(String parseable)
	{
		System.out.println("Data received:  " + parseable);
		packetData = null;
		broadcastData = null;
		parsedData = parseable.split(" ");

		int commandType = Integer.parseInt(parsedData[0]);
		
		switch(commandType)
		{
			case 0:
			{
				handleUsernameRequest();
				break;
			}
			
			case 3:
			{
				handleGlobalMessage();
				break;
			}
			
			case 4:
			{
				handlePrivateMessage();
				break;
			}
			
			case 7:
			{
				handleUserDisconnect();
				break;
			}
			
			default:
			{
				System.out.println("An unknown packet type was sent to the server.");
				break;
			}
		}
	}
	
	private void handleUsernameRequest()
	{	
		this.username = parsedData[1].toLowerCase().trim();
		
		System.out.println("Handled username: " + username);
		
		if(usernames.contains(username) || !isUsernameValid(username) || username.length() > 16)
		{
			keepAlive = false;
			hasValidUsername = false;
			
			packetData = String.format("%d\r\n", 2);
		}
		else
		{
			hasValidUsername = true;
			
			userWriterMap.put(username, bufferedWriter);
			
			packetData = String.format("%d %s %s\r\n", 1, getUsernameList(), SERVER_GREETING);
			broadcastData = String.format("%d %s\r\n", 10, username);
			
			usernames.add(username);
		}

		sendData();
	}
	
	private void handleGlobalMessage()
	{
		String message = "";
		int size = parsedData.length;
		
		for(int i = 1; i < size; i++)
		{
			if(i == parsedData.length - 1)
				message += String.format("%s", parsedData[i]);
			else
				message += String.format("%s ", parsedData[i]);
		}

		broadcastData = String.format("%d %s %s\r\n", 5, username, message);

		sendData();
	}
	
	private void handlePrivateMessage()
	{
		String message = "";
		String toUser = parsedData[2];
		int size = parsedData.length;
		
		for(int i = 3; i < size; i++)
		{
			if(i == parsedData.length - 1)
				message += String.format("%s", parsedData[i]);
			else
				message += String.format("%s ", parsedData[i]);
		}
		
		broadcastData = String.format("%d %s %s %s\r\n", 6, username, toUser, message);
		
		sendData();
	}
	
	private void handleUserDisconnect()
	{
		usernames.remove(username);
		userWriterMap.remove(username);

		keepAlive = false;
		
		packetData = String.format("%d\r\n", 8);
		broadcastData = String.format("%d %s\r\n", 9, username);
		
		sendData();
	}
	
	private void sendData()
	{
		if(packetData != null)
		{
			System.out.println("Handling read data: " + packetData);
			try 
			{
				bufferedWriter.write(packetData);
				bufferedWriter.flush();
			} 
			catch (IOException e) 
			{
				System.out.println("An error occurred while attempting to write to a socket connected to the server.");
				e.printStackTrace();
			}
		}
		
		if(broadcastData != null)
		{
			System.out.println("Handling read broadcast data: " + broadcastData);
			output.add(broadcastData);
		}
	}
	
	private boolean isUsernameValid(String user)
	{
		Matcher matcher = PATTERN_MATCHER.matcher(user);
		
		return matcher.matches();
	}
	
	private String getUsernameList()
	{
		int size = usernames.size() - 1;

		String userList = new String();
		
		for(int i = 0; i <= size; i++)
		{
			if(i == size)
				userList += String.format("%s", usernames.elementAt(i));
			else
				userList += String.format("%s,", usernames.elementAt(i));
		}

		return userList;
	}
	
	private String getTime()
	{
		Calendar date = Calendar.getInstance();
		String timeStamp = DATE_FORMAT.format(date.getTime());
		
		return timeStamp; 
	}
}