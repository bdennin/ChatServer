import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

public class ServerWriter implements Runnable
{
	private static final String USER_NOT_FOUND = "User not online.";
	private static final int SLEEP_TIME = 100;

	private Vector<String> output;
	private ConcurrentHashMap<String, BufferedWriter> userWriterMap;

	private String[] parsedData;

	public ServerWriter(Vector<String> output, ConcurrentHashMap<String, BufferedWriter> userWriterMap)
	{
		this.output = output;
		this.userWriterMap = userWriterMap;
	}

	public void run() 
	{
		while(true)
		{	
			handleOutput();
			
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

	private void handleOutput()
	{
		int size = output.size();
		
		for(int i = 0; i < size; i++)
		{
			String message = output.elementAt(i);
			System.out.println("Handling server output: " + message);
			parsedData = message.split(" ");

			int commandType = Integer.parseInt(parsedData[0]);

			switch (commandType)
			{
				case 5:
				{
					sendGlobalMessage(message);
					break;
				}
	
				case 6:
				{
					sendPrivateMessage(message);
					break;
				}
	
				case 9:
				{
					sendUserDisconnectMessage(message);
					break;
				}
	
				case 10:
				{
					sendUserConnectMessage(message);
					break;
				}
	
				default:
				{
					System.out.println("The server writer thread encountered an unknown command: " + commandType);
					break;
				}
			}
		}
		
		output.clear();
	}

	private void sendGlobalMessage(String message)
	{
		broadcast(message);
	}

	private void sendPrivateMessage(String message)
	{
		String fromUser = parsedData[1];
		String toUser = parsedData[2];
		
		BufferedWriter sender = userWriterMap.get(fromUser);
		BufferedWriter receiver = userWriterMap.get(toUser);
		
		try 
		{
			System.out.printf("from user:  %s to User:  %s", fromUser, toUser);
			
			if(receiver != null)
			{
				if(fromUser.equals(toUser))
				{
					receiver.write(message);
					receiver.flush();
				}
				else
				{
					receiver.write(message);
					receiver.flush();
					
					sender.write(message);
					sender.flush();
				}
			}
			else
			{
				String offline = String.format("%d %s %s %s\r\n", 6, fromUser, toUser, USER_NOT_FOUND);
				
				sender.write(offline);
				sender.flush();
			}
		} 
		catch (IOException e) 
		{
			System.out.println("An error occurred while trying to write a private message to a client.");
			e.printStackTrace();
		}
	}

	private void sendUserDisconnectMessage(String message)
	{
		broadcast(message);
	}

	private void sendUserConnectMessage(String message)
	{
		broadcast(message);
	}

	private void broadcast(String message)
	{
		int size = userWriterMap.size();

		System.out.println("broadcasted data " + message);
		System.out.println("NUmber of connections: " + size);
		
		for(BufferedWriter chatWriter : userWriterMap.values())
		{
			try 
			{
				chatWriter.write(message);
				chatWriter.flush();
			}
			catch (IOException e) 
			{
				System.out.println("An error occurred while trying to broadcast to a connection.");
			}
		}
	}
}
