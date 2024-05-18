package musicsoc;

import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Login 
{
    String userName;
    char[] userPass;
    String cryptPass;
    String serverResponse;
    ObjectOutputStream outToServer;
    ObjectInputStream inFromServer;
    
    //Constructor
    Login(String _userName, char[] _userPass, 
            ObjectOutputStream _outToServer, ObjectInputStream _inFromServer) throws NoSuchAlgorithmException, IOException, ClassNotFoundException
    {
        userName = _userName;
        userPass = _userPass;
        outToServer = _outToServer;
        inFromServer = _inFromServer;
        this.login();
    }
    
    private void login() throws NoSuchAlgorithmException, IOException, ClassNotFoundException
    {
        //Encrypt the password
        cryptPass = encryptPassword(new String(userPass));
        
        //Send the login request
        outToServer.writeObject("Login" + ":" + userName + ":" + cryptPass);
        
        //Record the request response
        serverResponse = (String) inFromServer.readObject();        
    }
    
    //Return the server response.
    public String getResponse()
    {
        return serverResponse;
    }
    
    //Password encrypting function using SHA-1 algorithm
    public static String encryptPassword(String input) throws NoSuchAlgorithmException 
    {
        MessageDigest crypt = MessageDigest.getInstance("SHA1");
        byte[] result = crypt.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < result.length; i++) 
        {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
        input = null;
        return sb.toString();
    }
    
}
