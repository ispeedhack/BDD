package musicsoc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class ChatServerTest {
    private Socket client;
    private DataInputStream dis;
    private DataOutputStream dos;
    private int userID;

    @Before
    public void setUp() throws IOException {
        client = new Socket("localhost", 3000); // Припустимо, що сервер працює на локальній машині з портом 3000
        dos = new DataOutputStream(client.getOutputStream());
        dis = new DataInputStream(client.getInputStream());
        userID = 1; // Припустимо, що ID користувача для тестування - 1
        dos.writeInt(userID); // Відправляємо ID користувача на сервер
    }

    @Test
    public void testUserSendMessageToServer() throws IOException {
        String messageContent = "Hello, server!";
        dos.writeUTF(userID + ":2:" + messageContent); // Припустимо, що користувач відправляє повідомлення користувачеві з ID 2
        assertEquals(messageContent, dis.readUTF()); // Перевіряємо, чи сервер відправляє правильне повідомлення назад користувачеві
    }

    @Test
    public void testServerSendMessageToUser() throws IOException {
        String messageContent = "Hello, user!";
        dos.writeUTF("2:" + userID + ":" + messageContent); // Припустимо, що сервер відправляє повідомлення користувачеві з ID 1
        assertEquals(messageContent, dis.readUTF()); // Перевіряємо, чи користувач отримує правильне повідомлення від сервера
    }
}
