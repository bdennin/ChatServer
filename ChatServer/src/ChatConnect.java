import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class ChatConnect extends JPanel implements ActionListener
{	
	private static final long serialVersionUID = -1558876937604962498L;
	private static final String INVALID_USERNAME = "Invalid Username.";
	private static final String BAD_CONNECTION = "Could not connect to server.";

	private ChatClientGUI outerFrame;
	private JTextField usernameTextbox;
	private JTextField serverAddressTextbox;
	private JButton connectButton;
	private JLabel errorLabel;

	private ChatClient chatClient;
	private JButton exitButton;

	public ChatConnect(ChatClientGUI outerFrame)
	{
		this.outerFrame = outerFrame;

		setup();
	}

	private void setup()
	{
		setLayout(null);

		Border raised = BorderFactory.createRaisedBevelBorder();
		Border lowered = BorderFactory.createLoweredBevelBorder();
		Border compound = BorderFactory.createCompoundBorder(raised, lowered);
		
		setBorder(compound);
		
		JLabel windowTitle = new JLabel("Public Chat");
		windowTitle.setBounds(11, 9, 389, 29);
		windowTitle.setHorizontalAlignment(SwingConstants.CENTER);
		windowTitle.setFont(new Font("Times New Roman", Font.PLAIN, 24));

		JPanel windowContents = new JPanel();
		windowContents.setBounds(11, 49, 389, 140);
		windowContents.setLayout(null);
		
		serverAddressTextbox = new JTextField(20);
		serverAddressTextbox.setFont(new Font("Arial Unicode MS", Font.PLAIN, 11));
		serverAddressTextbox.setBounds(98, 11, 194, 20);
		serverAddressTextbox.setText("localhost");

		JLabel serverAddressLabel = new JLabel("Server IP:");
		serverAddressLabel.setFont(new Font("Arial Unicode MS", Font.PLAIN, 11));
		serverAddressLabel.setBounds(8, 13, 80, 14);
		serverAddressLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		JLabel usernameLabel = new JLabel("Username:");
		usernameLabel.setFont(new Font("Arial Unicode MS", Font.PLAIN, 11));
		usernameLabel.setBounds(8, 45, 80, 14);
		usernameLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		usernameTextbox = new JTextField(20);
		usernameTextbox.setFont(new Font("Arial Unicode MS", Font.PLAIN, 11));
		usernameTextbox.setBounds(98, 42, 194, 20);
		usernameTextbox.setText("Tom");

		errorLabel = new JLabel(BAD_CONNECTION);
		errorLabel.setFont(new Font("Arial Unicode MS", Font.PLAIN, 11));
		errorLabel.setBounds(98, 107, 194, 22);
		errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
		errorLabel.setForeground(Color.RED);
		errorLabel.setVisible(false);
		
		exitButton = new JButton("Exit");
		exitButton.setFont(new Font("Arial Unicode MS", Font.PLAIN, 11));
		exitButton.setBounds(98, 73, 92, 23);
		exitButton.addActionListener(this);

		connectButton = new JButton("Connect");
		connectButton.setFont(new Font("Arial Unicode MS", Font.PLAIN, 11));
		connectButton.setBounds(200, 73, 92, 23);
		connectButton.addActionListener(this);

		windowContents.add(serverAddressLabel);
		windowContents.add(serverAddressTextbox);
		windowContents.add(usernameLabel);
		windowContents.add(usernameTextbox);
		windowContents.add(errorLabel);
		windowContents.add(connectButton);
		windowContents.add(exitButton);

		add(windowContents);
		add(windowTitle);
	}

	private void connect()
	{
		errorLabel.setVisible(false);

		String address = serverAddressTextbox.getText();
		String username = usernameTextbox.getText();

		chatClient = new ChatClient(address, username);

		System.out.println("connecting");

		if(chatClient.isConnected())
		{
			System.out.println("Connected.  SEnding username.");

			chatClient.sendUsername();

			while(null == chatClient.getUsernameAccepted())
			{
				System.out.println("Waiting");
			}

			if(chatClient.getUsernameAccepted())
			{
				outerFrame.setChatClient(chatClient);
				outerFrame.switchToChat();
				System.out.println("user name accepted");
			}
			else
			{
				errorLabel.setText(INVALID_USERNAME);
				errorLabel.setVisible(true);
				System.out.println("user name rejected");
			}
		}
		else
		{
			errorLabel.setVisible(true);
			errorLabel.setText(BAD_CONNECTION);
		}
	}

	public void actionPerformed(ActionEvent evt) 
	{
		Object source = evt.getSource();

		if (source == connectButton) 
			connect();
		else if (source == exitButton)
			System.exit(0);
	}
}
