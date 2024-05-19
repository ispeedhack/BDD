package musicsoc;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
public class chatClient {
    Socket server;
    DataInputStream dis;
    DataOutputStream dos;
    String sentMessage;
    String receivedMessage;
    chatClient(int _userID) throws IOException {
        server = new Socket("localhost", 9091);
        dis = new DataInputStream(server.getInputStream());
        dos = new DataOutputStream(server.getOutputStream());
        dos.flush();
        dos.writeInt(_userID);
    }
    // Send the message to the server
    void sendMessage(int _userID, int _friendID, String content) throws IOException {
        if (dos == null)
            dos = new DataOutputStream(server.getOutputStream());
        dos.writeUTF(_userID + ":" + _friendID + ":" + content);
    }
    // Receive the message from the server
    void receiveMessage() throws IOException {
        receivedMessage = dis.readUTF();
    }
    // BDD Steps
    // Given a chat client connected to the server
    public void a_chat_client_connected_to_the_server() throws IOException {
        server = new Socket("localhost", 9091);
        dis = new DataInputStream(server.getInputStream());
        dos = new DataOutputStream(server.getOutputStream());
        dos.flush();
    }
    // When the client sends a message
    public void the_client_sends_a_message(int _userID, int _friendID, String content) throws IOException {
        sendMessage(_userID, _friendID, content);
    }
    // Then the client should receive a message from the server
    public void the_client_should_receive_a_message() throws IOException {
        receiveMessage();
    }
}
