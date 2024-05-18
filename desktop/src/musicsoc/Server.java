package musicsoc;

import java.util.List;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;


public class Server 
{    
    public static void main(String args[]) throws IOException, SQLException
    {
        //server socket
        ServerSocket server = new ServerSocket(9090);
        
        //Synchronized list with all connected clients
        List<ListObject> connectedClients = Collections.synchronizedList(new ArrayList<>());

        while(true)
        {
            Socket client = server.accept();
            System.out.println("Connected: " + server.getInetAddress());
            
            //Record the new connection in the database
            recordConnection(client);
            
            //Handle each user
            Handler t;
            t = new Handler(client, connectedClients);
            Thread clientThread = new Thread(t);
            clientThread.start();
        }
    }
    
    //Record the new connection in the database
    public static void recordConnection(Socket _client) throws SQLException
    {
        long currentTime = System.currentTimeMillis();
        Timestamp timeStamp = new Timestamp(currentTime);
    
        //Connect to database
        Connection con = DriverManager.getConnection("jdbc:h2:~/NTU/n0633796/SystemsSoftware/SystemsDatabase", "sa", "");
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM CONNECTION");
        int connectionID = 0;
        if(rs.next())
            connectionID = Integer.parseInt(rs.getString("COUNT(*)"));
        
        //Insert the new connection into the database
        stmt = con.createStatement();
        stmt.executeUpdate("INSERT INTO CONNECTION VALUES("
                            + "'" + connectionID + "',"
                            + "'" + _client.getInetAddress().toString() + "',"
                            + "'Connected','"
                            + timeStamp.toString() + "')");
    }
}

//User hanlder class
class Handler implements Runnable
{
    Socket client;
    List<ListObject> connectedClients;
    boolean loggedIn = false;
    
    Handler(Socket _client, List<ListObject> _connectedClients)
    {
        client = _client;
        connectedClients = _connectedClients;
    }
        
    @Override
    public void run()
    {
        try
        {
            //Define datastreams
            ObjectOutputStream outToClient = new ObjectOutputStream(client.getOutputStream());
            outToClient.flush();
            ObjectInputStream inFromClient = new ObjectInputStream(client.getInputStream());
            
            //Request class which handles all requests
            Request request = new Request(client, outToClient, inFromClient, connectedClients);
            
            do
            {
                //Update the online list for 
                request.updateOnline(connectedClients);
                
                //Handle the request
                request.handleRequest();
                loggedIn = request.isLoggedIn();
            } while(loggedIn); //Continue the loop only if the user is logged in
        } catch (IOException | ClassNotFoundException | SQLException ex) {} 
    }

}