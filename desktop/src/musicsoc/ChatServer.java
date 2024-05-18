package musicsoc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ChatServer 
{
    private static ArrayList<chatHandler> clients = new ArrayList<>();
    
    public static void main(String args[]) throws IOException
    {
        ServerSocket server = new ServerSocket(9091);
        Socket client;
        
        while(true)
        {
            client = server.accept();
            System.out.println("Connected: " + server.getInetAddress());
            
            //Client handler
            chatHandler t;
            t = new chatHandler(client);
            clients.add(t);
            
            Thread clientThread = new Thread(t);
            clientThread.start();
        }
    }

    private static class chatHandler implements Runnable
    {
        public int userID;
        
        DataInputStream dis;
        DataOutputStream dos;
        
        Socket client;
        
        String messageToUser;
        String[] receivedFromUser;
        
        String messageToFriend;
        String messageFromFriend;
        
        public chatHandler(Socket _client) throws IOException 
        {
            client = _client;
            dis = new DataInputStream(client.getInputStream());
            dos = new DataOutputStream(client.getOutputStream());
            //Read the user ID
            userID = dis.readInt();
            System.out.println("User ID: " + userID);
        }

        @Override
        public void run() 
        {
            //Receiving new messages in a new thread
            new Thread(new Runnable() 
            {
            public void run() 
            {
                try 
                {
                    receiveMessage();
                } catch (IOException ex) 
                {
                    Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            }).start();
        }
        
        //Sending the message to the user you're talking to
        void sendToFriend() throws IOException
        {
            //Parsing the client list to find the desired user
            for (chatHandler client : clients)
            {
                System.out.println("Parsing clients");
                if (client.userID == Integer.parseInt(receivedFromUser[1]))
                {
                    try
                    {
                        //Send the message to the user
                        client.dos.writeUTF(receivedFromUser[2]);
                    }
                    catch (NullPointerException ex)
                    {
                        this.dos.writeUTF("Not connected");
                    }
                    break;
                }
            }
        }
        
        //Receive message from user
        void receiveMessage() throws IOException
        {
            while (true)
            {
                receivedFromUser = dis.readUTF().split(":");
                System.out.println("Received from user: " + receivedFromUser[0] + " message: " + receivedFromUser[2]);
                sendToFriend();
                System.out.println("Sent to user");
            }
        }
    }
}
