# ChatServer
The chat server!  This project was so much fun to work on!  I had a blast learning about computer networks, which is why I wrote this program.  I was amazed at how easily some languages implement sockets, and the IP stack.  It makes my mind boggle to think of the shoulders that we now stand upon.

This project is written in Java.  It makes uses of threads to handle how many clients can connect to the server, and is throttled for the system the server is running on.  It also takes advantage of several thread-safe data structures such as the Vector and ConcurrentHashMap.  The main classes for the server and the chat client, which also has a GUI, are implemented in the ChatServer and ChatClientGUI files, respectively.  Thanks for looking!
