package musicsoc;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Bartek
 */
public class ChatFrame extends javax.swing.JFrame {

    chatClient chat;
    ListObject friend;
    ListObject user;

    
    /**
     * Creates new form ChatFrame
     * @param user
     */
    public ChatFrame(ListObject _friend, ListObject _user) throws IOException 
    {
        initComponents();
        userNameLabel.setText(_friend.name);
        
        friend = _friend;
        user = _user;
        
        chat = new chatClient(user.id);
        
        //Infinite loop to constantly fetch the new messages
        new Thread(new Runnable() 
        {
        public void run() 
        {
            try 
            {
                while (true)
                {
                    //Receive the message
                    chat.receiveMessage();
                    //Add the message to the message area
                    messagesField.setText(messagesField.getText() + friend.name + ": " + chat.receivedMessage + "\n");
                }
            } catch (IOException ex) 
            {
                Logger.getLogger(ChatFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        }).start();
        
        
    }

 
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        messagesField = new javax.swing.JTextArea();
        messageField = new javax.swing.JTextField();
        UsernameLabel = new javax.swing.JLabel();
        userNameLabel = new javax.swing.JLabel();
        SendButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        messagesField.setEditable(false);
        messagesField.setColumns(20);
        messagesField.setRows(5);
        jScrollPane1.setViewportView(messagesField);

        messageField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                messageFieldKeyPressed(evt);
            }
        });

        UsernameLabel.setText("Username: ");

        SendButton.setText("Send");
        SendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SendButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(UsernameLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(userNameLabel))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(messageField, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(SendButton)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(UsernameLabel)
                    .addComponent(userNameLabel))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(messageField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SendButton))
                .addGap(20, 20, 20))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void SendButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (messageField.getText().isEmpty())
            return;
        
        try {
            chat.sendMessage(user.id, friend.id, messageField.getText());
        } catch (IOException ex) {
            Logger.getLogger(ChatFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        messagesField.setText(messagesField.getText() + user.name + ": " + messageField.getText() + "\n");
        messageField.setText("");
    }

    private void messageFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_messageFieldKeyPressed
        if (messageField.getText().isEmpty())
            return;
        
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
            try 
            {
                //Send the new message
                chat.sendMessage(user.id, friend.id, messageField.getText());
            } catch (IOException ex) {
                Logger.getLogger(ChatFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            messagesField.setText(messagesField.getText() + user.name + ": " + messageField.getText() + "\n");
            //Delete the content of the message
            messageField.setText("");
        }
    }//GEN-LAST:event_messageFieldKeyPressed

    // BDD Steps
    // Given a chat client connected to the server
    public void a_chat_client_connected_to_the_server() throws IOException {
        chat = new chatClient(user.id);
    }

    // When the user sends a message
    public void the_user_sends_a_message(String messageContent) throws IOException {
        chat.sendMessage(user.id, friend.id, messageContent);
    }

    // Then the user should see the sent message in the chat window
    public void the_user_should_see_the_sent_message(String messageContent) {
        String expectedMessage = user.name + ": " + messageContent + "\n";
        assertEquals(expectedMessage, messagesField.getText());
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton SendButton;
    private javax.swing.JLabel UsernameLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField messageField;
    private javax.swing.JTextArea messagesField;
    private javax.swing.JLabel userNameLabel;
    // End of variables declaration//GEN-END:variables
}
