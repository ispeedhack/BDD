package musicsoc;

import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Register 
{
    Socket client;
    String userName;
    char[] userPass;
    Socket server;
    String passCode;
    String serverResponse;
    
    Register(String _userName, char[] _userPass, String _passCode) throws NoSuchAlgorithmException, IOException, ClassNotFoundException
    {
        userName = _userName;
        userPass = _userPass;
        passCode = _passCode;
        client = new Socket();
        
        this.register();
    }
    
    //Register the user
    private void register() throws NoSuchAlgorithmException, IOException, ClassNotFoundException
    {
        //Encrypting the password
        String cryptPass = encryptPassword(new String(userPass));
        String cryptPassCode = encryptPassword(passCode);
        
        //Open new connection
        server = new Socket("localhost", 9090);
        //Open data streams
        ObjectOutputStream outToServer = new ObjectOutputStream(server.getOutputStream());
        outToServer.flush();
        ObjectInputStream inFromServer = new ObjectInputStream(server.getInputStream());
        
        //Send the request
        outToServer.writeObject("Register" + ":" + userName + ":" + cryptPass
        + ":" + cryptPassCode);
        
        //Record the server reponse
        serverResponse = (String) inFromServer.readObject();
    }
    
    //Get server response
    final public String getResponse()
    {
        return serverResponse;
    }
    
    //Encrypt password method using SHA-1 algorithm
    private String encryptPassword(String input) throws NoSuchAlgorithmException 
    {
        MessageDigest crypt = MessageDigest.getInstance("SHA1");
        byte[] result = crypt.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < result.length; i++) 
        {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}