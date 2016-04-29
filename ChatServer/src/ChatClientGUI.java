/*
 * This class displays the contents of the chat client.
 * It is only the visible manifestation of the client.
 * All logic is handled by the ChatClient.
 */

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ChatClientGUI extends JFrame
{
	private static final long serialVersionUID = 1114766517554919813L;

	private static final Dimension START_DIMENSIONS = new Dimension(416, 226);
	private static final Dimension CHAT_DIMENSIONS = new Dimension(646, 486);
	
	private JFrame outerFrame;
	private JPanel contentPane;
	
	private ChatClient chatClient;

	public ChatClientGUI() 
	{
		outerFrame = new JFrame("Public Chat");
		chatClient = null;

		switchToConnect();
		
		outerFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		outerFrame.setResizable(false);
		outerFrame.setVisible(true);
	}
	
	public void switchToChat()
	{
		outerFrame.setSize(CHAT_DIMENSIONS);
		outerFrame.setLocationRelativeTo(null);
		
		contentPane = new ChatScreen(this);
		outerFrame.setContentPane(contentPane);
		outerFrame.revalidate();
	}
	
	public void switchToConnect()
	{
		outerFrame.setSize(START_DIMENSIONS);
		outerFrame.setLocationRelativeTo(null);
		
		contentPane = new ChatConnect(this);
		outerFrame.setContentPane(contentPane);
		outerFrame.revalidate();
	}
	
	public ChatClient getChatClient()
	{
		return chatClient;
	}
		
	public void setChatClient(ChatClient chatClient)
	{
		this.chatClient = chatClient;
	}
	
	public static void main(String[] args)
	{
		ChatClientGUI chatGUI = new ChatClientGUI();
	}
}
