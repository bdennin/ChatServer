import java.util.Collections;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class ClientWriter implements Runnable
{
	private static final int SLEEP_TIME = 100;

	private ChatClient chatClient;

	private Vector<String> input;
	private Vector<String> usernames;

	private int numberOfUsernames;

	public ClientWriter(ChatClient chatClient, Vector<String> input, Vector<String> usernames)
	{
		this.chatClient = chatClient;

		this.input = input;
		this.usernames = usernames;

		this.numberOfUsernames = 0;
	}

	public void run() 
	{	
		while(chatClient.isConnected())
		{
			handleData();

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

	private void handleData()
	{
		if(chatClient.getChatArea() != null)
		{
			JTextArea chatArea = chatClient.getChatArea();
			int size = input.size();

			for(int i = 0; i < size; i++)
			{
				String message = input.elementAt(i);
				chatArea.append(message);
			}

			input.clear();
		}

		if(chatClient.getUsernameModel() != null)
		{
			DefaultListModel<String> usernameModel = chatClient.getUsernameModel();
			int size = usernames.size();

			if(size != numberOfUsernames)
			{
				numberOfUsernames = size;

				usernameModel.clear();

				Collections.sort(usernames);
				
				for(int i = 0; i < size; i++)
				{
					String username = usernames.elementAt(i);
					usernameModel.addElement(username);
				}
			}
		}
	}
}
