/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package musicsoc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author bartek
 */
public class Music_Soc extends javax.swing.JFrame {

    //Variables
    int userID;
    String userName = "";
    String servResponse = "";
    Socket server;
    Login login;
    File selectedImageFile;
    File selectedMusicFile;
    
    //Semaphore variable
    boolean canUpdate = true;
    
    //Image variables
    BufferedImage profileImage;
    BufferedImage InspectedUserImage;
    JLabel imageLabel;
    JLabel inspectedUserImageLabel;
    
    //Data streams
    ObjectOutputStream outToServer;
    ObjectInputStream inFromServer;
    UserInterface userInterface;
    
    //JList models for the interface
    DefaultListModel<ListObject> friendListModel = new DefaultListModel<>();
    DefaultListModel<ListObject> friendRequestListModel = new DefaultListModel<>();
    DefaultListModel<ListObject> sharedMusicListModel = new DefaultListModel<>();
    DefaultListModel<ListObject> onlineListModel = new DefaultListModel<>();
    DefaultListModel<String> MusicTypeModel = new DefaultListModel<>();
    DefaultListModel<String> MusicProfileModel = new DefaultListModel<>();
    DefaultListModel<String> SharedMusicModel = new DefaultListModel<>();
    
    //Inspected user
    User newUser = null;
    
    //Music player
    Clip musicClip = null;
    
    /**
     * Creates new form Music_Soc
     */
    public Music_Soc() 
    {
        initComponents();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, 
                dim.height / 2 - this.getSize().height / 2);
        LoginDialog.setVisible(true);
        
    }
    
    //Function used to update the user interface
    private void updateInterface() throws IOException, ClassNotFoundException
    {
        canUpdate = false;
        //Sending the request
        outToServer.writeObject("interfaceUpdate:" + userID);
        
        //Receiving the interface data
        userInterface = (UserInterface) inFromServer.readObject();
        
        //Clearing the lists
        friendListModel.clear();
        onlineListModel.clear();
        friendRequestListModel.clear();
        sharedMusicListModel.clear();
        FriendPostsArea.setText("");
        
        //Add friends to the list
        for (ListObject e : userInterface.friends)
        {
            friendListModel.addElement(e);
        }
        
        //Add online users to the list
        for (ListObject e : userInterface.onlineUsers)
        {
            onlineListModel.addElement(e);
        }
        
        //Add friend requests to the list
        for (ListObject e : userInterface.friendRequests)
        {
            friendRequestListModel.addElement(e);
        }
        
        //Add shared music to the list
        for (ListObject e : userInterface.sharedMusic)
        {
            e.name = e.name.substring(0, e.name.lastIndexOf("."));
            sharedMusicListModel.addElement(e);
        }
        
        //Update posts
        for (Post post : userInterface.posts)
        {
            FriendPostsArea.setText(FriendPostsArea.getText() + post.userName + ": " + post.content + "\n");
        }
        
        //Check if there are any chat requests, if yes display a dialog
        if (userInterface.chatRequest.id != -1)
        {
            ChatRequest.setVisible(true);
            ChatRequest.setLocationRelativeTo(this);
            ChatMessage.setText("You have a chat request from user: " + userInterface.chatRequest.name);
        }
        
        canUpdate = true;
    }
    
    //Logout function
    private void logOut() throws IOException
    {
        canUpdate = false;
        outToServer.writeObject("Logout:" + userID);
        canUpdate = true;
        System.exit(1);
    }
    
    //Sending a post thats visible to all friends
    private void sendPost(String content) throws IOException
    {
        canUpdate = false;
        outToServer.writeObject("Post:" + userID + ":" + content);
        canUpdate = true;
    }
    
    //Request friendship from a user
    private void requestFriendship(ListObject selectedValue) throws IOException 
    {
        canUpdate = false;
        outToServer.writeObject("requestFriend:" + userID + ":" + selectedValue.id);
        canUpdate = true;
    }
    
    //Decline friendship from a user
    private void declineFriendship(ListObject selectedValue) throws IOException 
    {
        canUpdate = false;
        outToServer.writeObject("declineFriend:" + userID + ":" + selectedValue.id);
        canUpdate = true;
    }
    
    //Accept friendship from a user
    private void acceptFriendship(ListObject selectedValue) throws IOException 
    {
        canUpdate = false;
        outToServer.writeObject("acceptFriend:" + userID + ":" + selectedValue.id);
        canUpdate = true;
    }
    
    //Function to update the user's music preference in 'Edit Profile'
    private void updateMusicPreference(DefaultListModel<String> MusicTypeModel) throws IOException 
    {
        if (MusicTypeModel.isEmpty())
            return;
        
        String request = "updateMusicType:" + userID;
        for (int i = 0; i < MusicTypeModel.size(); i++)
        {
            request += (":" + MusicTypeModel.get(i));
        }
        canUpdate = false;
        outToServer.writeObject(request);
        canUpdate = true;
    }
    
    //Function which requests data about a user and displays his information
    private void ViewProfile(ListObject selectedValue) throws IOException, ClassNotFoundException 
    {
        MusicProfileModel.clear();
        SharedMusicModel.clear();
        
        inspectedUserImageLabel.setIcon(null);
        canUpdate = false;
        outToServer.writeObject("viewProfile:" + userID + ":" + selectedValue.id);
        
        //Receive user data
        newUser = (User) inFromServer.readObject();
        
        //Set the variables
        newUsername.setText(newUser.userName);
        newFirstname.setText(newUser.firstName);
        newLastname.setText(newUser.lastName);
        newDOB.setText(newUser.dateOfBirth);
        
        //Add user's preferred music to the list
        for (String temp : newUser.musicTypes)
        {
            MusicProfileModel.addElement(temp);
        }
        
        //Add user's shared music to the list
        for (String temp : newUser.sharedMusic)
        {
            temp = temp.substring(0, temp.lastIndexOf("."));
            SharedMusicModel.addElement(temp);
        }
        
        //Check if the user has a picture set, if not then a null exception is handled
        try
        {
            InspectedUserImage = requestImage(String.valueOf(selectedValue.id));
            inspectedUserImageLabel.setIcon(new ImageIcon(InspectedUserImage));
        }
        catch (NullPointerException ex) {}
        canUpdate = true;
    }
    
    //Function which requests the selected shared music to be downloaded
    private AudioInputStream requestMusic(ListObject selectedValue) throws IOException, ClassNotFoundException, UnsupportedAudioFileException
    {
        canUpdate = false;
        //Send request
        outToServer.writeObject("requestMusic:" + userID + ":" + selectedValue.id);
        String musicName = (String) inFromServer.readObject();
        //Download the music as a byte array
        byte[] musicArray = (byte[]) inFromServer.readObject();
        
        //write the byte array into a file
        FileOutputStream fos = new FileOutputStream("client//music//" + musicName);
        fos.write(musicArray);
        fos.close();
        
        canUpdate = true;
        
        //Return the AudioInputStream from a file
        return AudioSystem.getAudioInputStream(new File("client//music//" + musicName));
    }
    
    //Function which requests the user's image
    private BufferedImage requestImage(String _userID) throws IOException, ClassNotFoundException
    {
        canUpdate = false;
        outToServer.writeObject("requestImage:" + _userID);
        
        //Fetch the image as byte array
        byte[] imageArray = (byte[]) inFromServer.readObject();
        
        //Write the byte array into an image
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageArray));
        
        canUpdate = true;
        
        return image;
    }

 
   /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LoginDialog = new javax.swing.JDialog();
        LoginTextLabel = new javax.swing.JLabel();
        LoginField = new javax.swing.JTextField();
        LoginLabel = new javax.swing.JLabel();
        PasswordLabel = new javax.swing.JLabel();
        PasswordField = new javax.swing.JPasswordField();
        LoginButton = new javax.swing.JButton();
        RegisterButton = new javax.swing.JLabel();
        LoginMessage = new javax.swing.JDialog();
        LoginMessageButton = new javax.swing.JButton();
        LoginOutput = new javax.swing.JLabel();
        EditProfile = new javax.swing.JDialog();
        firstNameLabel = new javax.swing.JLabel();
        lastNameLabel = new javax.swing.JLabel();
        DateofBirthLabel = new javax.swing.JLabel();
        Day = new javax.swing.JComboBox<>();
        Month = new javax.swing.JComboBox<>();
        Year = new javax.swing.JComboBox<>();
        UploadButton = new javax.swing.JButton();
        imagePanel = new javax.swing.JPanel();
        InfoLabel = new javax.swing.JLabel();
        firstNameField = new javax.swing.JTextField();
        lastNameField = new javax.swing.JTextField();
        confirmPasswordLabel = new javax.swing.JLabel();
        newPassportLabel = new javax.swing.JLabel();
        newUsernameField = new javax.swing.JTextField();
        UpdateUsernameButton = new javax.swing.JButton();
        newPasswordField = new javax.swing.JPasswordField();
        confirmPasswordField = new javax.swing.JPasswordField();
        UpdatePasswordButton = new javax.swing.JButton();
        UpdateDetailsButton = new javax.swing.JButton();
        ChooseMusic = new javax.swing.JComboBox<>();
        MusicTypePane = new javax.swing.JScrollPane();
        MusicTypeList = new javax.swing.JList<>();
        ClearButton = new javax.swing.JButton();
        AddButton = new javax.swing.JButton();
        updateMusicButton = new javax.swing.JButton();
        MusicTypeLabel = new javax.swing.JLabel();
        newUsernameLabel = new javax.swing.JLabel();
        MusicTypesLabel = new javax.swing.JLabel();
        viewProfile = new javax.swing.JDialog();
        UserImagePanel = new javax.swing.JPanel();
        UsernameViewLabel = new javax.swing.JLabel();
        FirstNameViewLabel = new javax.swing.JLabel();
        LastNameViewLabel = new javax.swing.JLabel();
        DoBViewLabel = new javax.swing.JLabel();
        MusicProfileLabel = new javax.swing.JLabel();
        userSharedMusicLabel = new javax.swing.JLabel();
        newUsername = new javax.swing.JLabel();
        newFirstname = new javax.swing.JLabel();
        newLastname = new javax.swing.JLabel();
        newDOB = new javax.swing.JLabel();
        viewSharedMusicPane = new javax.swing.JScrollPane();
        SharedMusicJList = new javax.swing.JList<>();
        viewMusicProfilePane = new javax.swing.JScrollPane();
        MusicProfileList = new javax.swing.JList<>();
        UploadMusic = new javax.swing.JDialog();
        ChooseFileButton = new javax.swing.JButton();
        NameTextLabel = new javax.swing.JLabel();
        SizeTextLabel = new javax.swing.JLabel();
        MusicTypeBox = new javax.swing.JComboBox<>();
        MusicTypeTextLabel = new javax.swing.JLabel();
        NameLabel = new javax.swing.JLabel();
        SizeLabel = new javax.swing.JLabel();
        SendButton = new javax.swing.JButton();
        ChatRequest = new javax.swing.JDialog();
        AcceptButton = new javax.swing.JButton();
        DeclineButton = new javax.swing.JButton();
        ChatMessage = new javax.swing.JLabel();
        FriendRequest = new javax.swing.JScrollPane();
        FriendRequestsList = new javax.swing.JList<>(friendRequestListModel);
        UploadMusicButton = new javax.swing.JButton();
        FriendsLabel = new javax.swing.JLabel();
        AcceptFriendButton = new javax.swing.JButton();
        FriendScrollPanel = new javax.swing.JScrollPane();
        FriendList = new javax.swing.JList<>(friendListModel);
        FriendPosts = new javax.swing.JScrollPane();
        FriendPostsArea = new javax.swing.JTextArea();
        DeclineFriendButton = new javax.swing.JButton();
        LogoutButton = new javax.swing.JButton();
        ChatFriendButton = new javax.swing.JButton();
        FriendPostsLabel = new javax.swing.JLabel();
        PostContent = new javax.swing.JTextField();
        ViewFriendProfileButton = new javax.swing.JButton();
        OnlineUsers = new javax.swing.JScrollPane();
        OnlineUsersList = new javax.swing.JList<>(onlineListModel);
        PostButton = new javax.swing.JButton();
        SharedMusicPane = new javax.swing.JScrollPane();
        SharedMusicList = new javax.swing.JList<>(sharedMusicListModel);
        OnlineUsersLabel = new javax.swing.JLabel();
        PlayMusicButton = new javax.swing.JButton();
        FriendRequestButton = new javax.swing.JButton();
        SharedMusicLabel = new javax.swing.JLabel();
        ChatOnlineButton = new javax.swing.JButton();
        EditProfileButton = new javax.swing.JButton();
        FriendRequestsLabel = new javax.swing.JLabel();
        ViewUserProfileButton = new javax.swing.JButton();
        StopMusicButton = new javax.swing.JButton();

        LoginDialog.setTitle("Login");
        LoginDialog.setResizable(false);
        LoginDialog.setSize(new java.awt.Dimension(400, 300));

        LoginTextLabel.setFont(new java.awt.Font("Ubuntu", 0, 24)); // NOI18N
        LoginTextLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LoginTextLabel.setText("Login");

        LoginField.setHorizontalAlignment(javax.swing.JTextField.LEFT);

        LoginLabel.setText("Login:");

        PasswordLabel.setText("Password:");

        PasswordField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PasswordFieldActionPerformed(evt);
            }
        });

        LoginButton.setText("Log in");
        LoginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoginButtonActionPerformed(evt);
            }
        });

        RegisterButton.setFont(new java.awt.Font("Ubuntu", 0, 18)); // NOI18N
        RegisterButton.setText("<HTML>Register</HTML>");
        RegisterButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        RegisterButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RegisterButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                RegisterButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                RegisterButtonMouseExited(evt);
            }
        });

        LoginDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });

        javax.swing.GroupLayout LoginDialogLayout = new javax.swing.GroupLayout(LoginDialog.getContentPane());
        LoginDialog.getContentPane().setLayout(LoginDialogLayout);
        LoginDialogLayout.setHorizontalGroup(
            LoginDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LoginDialogLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(LoginDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LoginDialogLayout.createSequentialGroup()
                        .addComponent(LoginTextLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(164, 164, 164))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LoginDialogLayout.createSequentialGroup()
                        .addComponent(RegisterButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(161, 161, 161))))
            .addGroup(LoginDialogLayout.createSequentialGroup()
                .addGap(70, 70, 70)
                .addGroup(LoginDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LoginDialogLayout.createSequentialGroup()
                        .addGroup(LoginDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(PasswordLabel)
                            .addComponent(LoginLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(LoginDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(PasswordField)
                            .addComponent(LoginField, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LoginDialogLayout.createSequentialGroup()
                        .addComponent(LoginButton)
                        .addGap(50, 50, 50)))
                .addGap(0, 122, Short.MAX_VALUE))
        );
        LoginDialogLayout.setVerticalGroup(
            LoginDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LoginDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(LoginTextLabel)
                .addGap(29, 29, 29)
                .addGroup(LoginDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LoginField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LoginLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(LoginDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(PasswordLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(LoginButton)
                .addGap(41, 41, 41)
                .addComponent(RegisterButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(77, Short.MAX_VALUE))
        );

        LoginDialog.setLocationRelativeTo(this);

        LoginMessage.setTitle("Login Message");
        LoginMessage.setResizable(false);
        LoginMessage.setSize(new java.awt.Dimension(185, 150));
        LoginMessage.setUndecorated(true);

        LoginMessageButton.setText("Ok");
        LoginMessageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoginMessageButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout LoginMessageLayout = new javax.swing.GroupLayout(LoginMessage.getContentPane());
        LoginMessage.getContentPane().setLayout(LoginMessageLayout);
        LoginMessageLayout.setHorizontalGroup(
            LoginMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LoginMessageLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(LoginMessageButton)
                .addGap(77, 77, 77))
            .addGroup(LoginMessageLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(LoginOutput, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );
        LoginMessageLayout.setVerticalGroup(
            LoginMessageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LoginMessageLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(LoginOutput, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(LoginMessageButton)
                .addContainerGap())
        );

        EditProfile.setTitle("Edit Profile");
        EditProfile.setResizable(false);
        EditProfile.setSize(new java.awt.Dimension(460, 600));

        firstNameLabel.setText("First name:");

        lastNameLabel.setText("Last name:");

        DateofBirthLabel.setText("Date of Birth:");

        String[] dobDays = new String[31];
        for (int i = 0; i < 31; i++)
        dobDays[i] = Integer.toString(i+1);
        Day.setModel(new javax.swing.DefaultComboBoxModel<>(dobDays));

        Month.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "January",
            "February", "March", "April", "May", "June", "July", "August", "September", "October",
            "November", "December" }));

String[] years = new String[104];
for (int i = 0; i < 104; i++)
years[i] = Integer.toString(1900+i);
Year.setModel(new javax.swing.DefaultComboBoxModel<>(years));

UploadButton.setText("Upload");
UploadButton.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        UploadButtonActionPerformed(evt);
    }
    });

    javax.swing.GroupLayout imagePanelLayout = new javax.swing.GroupLayout(imagePanel);
    imagePanel.setLayout(imagePanelLayout);
    imagePanelLayout.setHorizontalGroup(
        imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 170, Short.MAX_VALUE)
    );
    imagePanelLayout.setVerticalGroup(
        imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 170, Short.MAX_VALUE)
    );

    InfoLabel.setText("<html>Picture can't be bigger than 170px or<br> smaller than 130px</html>");

    confirmPasswordLabel.setText("Confirm Password:");

    newPassportLabel.setText("Password:");

    UpdateUsernameButton.setText("Update username");
    UpdateUsernameButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            UpdateUsernameButtonActionPerformed(evt);
        }
    });

    UpdatePasswordButton.setText("Update password");
    UpdatePasswordButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            UpdatePasswordButtonActionPerformed(evt);
        }
    });

    UpdateDetailsButton.setText("Update details");
    UpdateDetailsButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            UpdateDetailsButtonActionPerformed(evt);
        }
    });

    ChooseMusic.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hip Hop", "Rap", "Rock", "Metal", "Techno", "Classic", "Opera", "Dupstep"}));

    MusicTypeList.setModel(MusicTypeModel);
    MusicTypePane.setViewportView(MusicTypeList);

    ClearButton.setText("Clear");
    ClearButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            ClearButtonActionPerformed(evt);
        }
    });

    AddButton.setText("Add");
    AddButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            AddButtonActionPerformed(evt);
        }
    });

    updateMusicButton.setText("Update music");
    updateMusicButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            updateMusicButtonActionPerformed(evt);
        }
    });

    MusicTypeLabel.setText("Music type:");

    newUsernameLabel.setText("Username:");

    MusicTypesLabel.setText("Music types:");

    javax.swing.GroupLayout EditProfileLayout = new javax.swing.GroupLayout(EditProfile.getContentPane());
    EditProfile.getContentPane().setLayout(EditProfileLayout);
    EditProfileLayout.setHorizontalGroup(
        EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(EditProfileLayout.createSequentialGroup()
            .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(EditProfileLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(imagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(InfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(UploadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(EditProfileLayout.createSequentialGroup()
                    .addGap(28, 28, 28)
                    .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(MusicTypeLabel)
                        .addComponent(MusicTypesLabel))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(EditProfileLayout.createSequentialGroup()
                            .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(newPasswordField)
                                .addComponent(confirmPasswordField)
                                .addComponent(firstNameField, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(lastNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                                .addComponent(newUsernameField))
                            .addGap(12, 12, 12)
                            .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(UpdateUsernameButton)
                                .addComponent(UpdatePasswordButton)
                                .addComponent(updateMusicButton)))
                        .addGroup(EditProfileLayout.createSequentialGroup()
                            .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(ChooseMusic, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(MusicTypePane, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(AddButton, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(ClearButton))))))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addGroup(EditProfileLayout.createSequentialGroup()
            .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(confirmPasswordLabel)
                .addComponent(firstNameLabel)
                .addComponent(lastNameLabel)
                .addComponent(DateofBirthLabel)
                .addGroup(EditProfileLayout.createSequentialGroup()
                    .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(newUsernameLabel)
                        .addComponent(newPassportLabel))
                    .addGap(2, 2, 2)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(Day, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(Month, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(Year, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(UpdateDetailsButton)
            .addGap(0, 39, Short.MAX_VALUE))
    );
    EditProfileLayout.setVerticalGroup(
        EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, EditProfileLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(EditProfileLayout.createSequentialGroup()
                    .addComponent(InfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(36, 36, 36)
                    .addComponent(UploadButton))
                .addComponent(imagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addGroup(EditProfileLayout.createSequentialGroup()
                    .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(ChooseMusic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(MusicTypeLabel)
                        .addComponent(AddButton))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(MusicTypePane, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(MusicTypesLabel)))
                .addGroup(EditProfileLayout.createSequentialGroup()
                    .addGap(29, 29, 29)
                    .addComponent(ClearButton)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(updateMusicButton)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 96, Short.MAX_VALUE)
            .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(newUsernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(UpdateUsernameButton)
                .addComponent(newUsernameLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(newPassportLabel)
                .addComponent(newPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(confirmPasswordLabel)
                .addComponent(confirmPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(UpdatePasswordButton))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(firstNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(firstNameLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lastNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lastNameLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(EditProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(DateofBirthLabel)
                .addComponent(Day, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(Month, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(Year, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(UpdateDetailsButton))
            .addContainerGap())
    );

    EditProfile.setLocationRelativeTo(this);

    imageLabel = new JLabel();
    imageLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
    imageLabel.setBounds(0, 0, 170, 170);
    imagePanel.add(imageLabel);

    viewProfile.setTitle("View Profile");
    viewProfile.setResizable(false);
    viewProfile.setSize(new java.awt.Dimension(460, 375));

    javax.swing.GroupLayout UserImagePanelLayout = new javax.swing.GroupLayout(UserImagePanel);
    UserImagePanel.setLayout(UserImagePanelLayout);
    UserImagePanelLayout.setHorizontalGroup(
        UserImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 170, Short.MAX_VALUE)
    );
    UserImagePanelLayout.setVerticalGroup(
        UserImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 170, Short.MAX_VALUE)
    );

    UsernameViewLabel.setText("Username: ");

    FirstNameViewLabel.setText("First Name: ");

    LastNameViewLabel.setText("Last Name: ");

    DoBViewLabel.setText("Date Of Birth: ");

    MusicProfileLabel.setText("Music profile: ");

    userSharedMusicLabel.setText("Shared music: ");

    SharedMusicJList.setModel(SharedMusicModel);
    viewSharedMusicPane.setViewportView(SharedMusicJList);

    MusicProfileList.setModel(MusicProfileModel);
    viewMusicProfilePane.setViewportView(MusicProfileList);

    javax.swing.GroupLayout viewProfileLayout = new javax.swing.GroupLayout(viewProfile.getContentPane());
    viewProfile.getContentPane().setLayout(viewProfileLayout);
    viewProfileLayout.setHorizontalGroup(
        viewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(viewProfileLayout.createSequentialGroup()
            .addGroup(viewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(viewProfileLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(UserImagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addGroup(viewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(UsernameViewLabel)
                        .addComponent(FirstNameViewLabel)
                        .addComponent(LastNameViewLabel)
                        .addComponent(DoBViewLabel))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(viewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(newUsername)
                        .addComponent(newFirstname)
                        .addComponent(newLastname)
                        .addComponent(newDOB)))
                .addGroup(viewProfileLayout.createSequentialGroup()
                    .addGap(21, 21, 21)
                    .addGroup(viewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(userSharedMusicLabel)
                        .addComponent(viewSharedMusicPane, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(55, 55, 55)
                    .addGroup(viewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(MusicProfileLabel)
                        .addComponent(viewMusicProfilePane, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addContainerGap(84, Short.MAX_VALUE))
    );
    viewProfileLayout.setVerticalGroup(
        viewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(viewProfileLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(viewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(viewProfileLayout.createSequentialGroup()
                    .addComponent(UserImagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(26, 26, 26))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, viewProfileLayout.createSequentialGroup()
                    .addGroup(viewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(UsernameViewLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(newUsername))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(viewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(FirstNameViewLabel)
                        .addComponent(newFirstname))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(viewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(LastNameViewLabel)
                        .addComponent(newLastname))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(viewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(DoBViewLabel)
                        .addComponent(newDOB))
                    .addGap(62, 62, 62)))
            .addGroup(viewProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addGroup(viewProfileLayout.createSequentialGroup()
                    .addComponent(userSharedMusicLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(viewSharedMusicPane, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(viewProfileLayout.createSequentialGroup()
                    .addComponent(MusicProfileLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(viewMusicProfilePane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
            .addGap(0, 28, Short.MAX_VALUE))
    );

    inspectedUserImageLabel = new JLabel();
    inspectedUserImageLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
    inspectedUserImageLabel.setBounds(0, 0, 170, 170);
    UserImagePanel.add(inspectedUserImageLabel);

    UploadMusic.setResizable(false);
    UploadMusic.setSize(new java.awt.Dimension(380, 220));

    ChooseFileButton.setText("Choose File");
    ChooseFileButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            ChooseFileButtonActionPerformed(evt);
        }
    });

    NameTextLabel.setText("Name: ");

    SizeTextLabel.setText("Size: ");

    MusicTypeBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hip Hop", "Rap", "Rock", "Metal", "Techno", "Classic", "Opera", "Dupstep"}));

    MusicTypeTextLabel.setText("Music type:");

    SendButton.setText("Send");
    SendButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            SendButtonActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout UploadMusicLayout = new javax.swing.GroupLayout(UploadMusic.getContentPane());
    UploadMusic.getContentPane().setLayout(UploadMusicLayout);
    UploadMusicLayout.setHorizontalGroup(
        UploadMusicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(UploadMusicLayout.createSequentialGroup()
            .addGroup(UploadMusicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(UploadMusicLayout.createSequentialGroup()
                    .addGap(18, 18, 18)
                    .addGroup(UploadMusicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(SizeTextLabel)
                        .addComponent(NameTextLabel))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(UploadMusicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(UploadMusicLayout.createSequentialGroup()
                            .addComponent(NameLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 137, Short.MAX_VALUE)
                            .addComponent(MusicTypeTextLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(MusicTypeBox, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(UploadMusicLayout.createSequentialGroup()
                            .addComponent(SizeLabel)
                            .addGap(0, 0, Short.MAX_VALUE))))
                .addGroup(UploadMusicLayout.createSequentialGroup()
                    .addGap(35, 35, 35)
                    .addComponent(ChooseFileButton)
                    .addGap(0, 0, Short.MAX_VALUE)))
            .addContainerGap())
        .addGroup(UploadMusicLayout.createSequentialGroup()
            .addGap(160, 160, 160)
            .addComponent(SendButton)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    UploadMusicLayout.setVerticalGroup(
        UploadMusicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(UploadMusicLayout.createSequentialGroup()
            .addGap(31, 31, 31)
            .addGroup(UploadMusicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(NameTextLabel)
                .addComponent(MusicTypeBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(MusicTypeTextLabel)
                .addComponent(NameLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(UploadMusicLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(SizeTextLabel)
                .addComponent(SizeLabel))
            .addGap(18, 18, 18)
            .addComponent(ChooseFileButton)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
            .addComponent(SendButton)
            .addContainerGap())
    );

    ChatRequest.setResizable(false);
    ChatRequest.setSize(new java.awt.Dimension(294, 158));

    AcceptButton.setText("Accept");
    AcceptButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            AcceptButtonActionPerformed(evt);
        }
    });

    DeclineButton.setText("Decline");
    DeclineButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            DeclineButtonActionPerformed(evt);
        }
    });

    ChatMessage.setText("You have a chat request from user: ");

    javax.swing.GroupLayout ChatRequestLayout = new javax.swing.GroupLayout(ChatRequest.getContentPane());
    ChatRequest.getContentPane().setLayout(ChatRequestLayout);
    ChatRequestLayout.setHorizontalGroup(
        ChatRequestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(ChatRequestLayout.createSequentialGroup()
            .addContainerGap(72, Short.MAX_VALUE)
            .addComponent(AcceptButton)
            .addGap(18, 18, 18)
            .addComponent(DeclineButton)
            .addGap(72, 72, 72))
        .addGroup(ChatRequestLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(ChatMessage)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    ChatRequestLayout.setVerticalGroup(
        ChatRequestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ChatRequestLayout.createSequentialGroup()
            .addGap(40, 40, 40)
            .addComponent(ChatMessage)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
            .addGroup(ChatRequestLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(AcceptButton)
                .addComponent(DeclineButton))
            .addGap(34, 34, 34))
    );

    ChatRequest.setUndecorated(true);

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("Social Network");
    setResizable(false);

    FriendRequest.setViewportView(FriendRequestsList);

    UploadMusicButton.setText("<html>Upload<br>music</html>");
    UploadMusicButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            UploadMusicButtonActionPerformed(evt);
        }
    });

    FriendsLabel.setText("Friends:");

    AcceptFriendButton.setText("Accept");
    AcceptFriendButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            AcceptFriendButtonActionPerformed(evt);
        }
    });

    FriendScrollPanel.setViewportView(FriendList);

    FriendPostsArea.setEditable(false);
    FriendPostsArea.setColumns(20);
    FriendPostsArea.setRows(5);
    FriendPosts.setViewportView(FriendPostsArea);

    DeclineFriendButton.setText("Decline");
    DeclineFriendButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            DeclineFriendButtonActionPerformed(evt);
        }
    });

    LogoutButton.setText("Logout");
    LogoutButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            LogoutButtonActionPerformed(evt);
        }
    });

    ChatFriendButton.setText("Chat");
    ChatFriendButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            ChatFriendButtonActionPerformed(evt);
        }
    });

    FriendPostsLabel.setText("Friend Posts:");

    PostContent.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            PostContentActionPerformed(evt);
        }
    });

    ViewFriendProfileButton.setText("View Profile");
    ViewFriendProfileButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            ViewFriendProfileButtonActionPerformed(evt);
        }
    });

    OnlineUsers.setViewportView(OnlineUsersList);

    PostButton.setText("Post");
    PostButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            PostButtonActionPerformed(evt);
        }
    });

    SharedMusicPane.setViewportView(SharedMusicList);

    OnlineUsersLabel.setText("Online Users:");

    PlayMusicButton.setText("PLAY");
    PlayMusicButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            PlayMusicButtonActionPerformed(evt);
        }
    });

    FriendRequestButton.setText("<html>Request<br>Friendship</html>");
    FriendRequestButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            FriendRequestButtonActionPerformed(evt);
        }
    });

    SharedMusicLabel.setText("Shared music:");

    ChatOnlineButton.setText("Chat");
    ChatOnlineButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            ChatOnlineButtonActionPerformed(evt);
        }
    });

    EditProfileButton.setText("Edit profile");
    EditProfileButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            EditProfileButtonActionPerformed(evt);
        }
    });

    FriendRequestsLabel.setText("Friend requests:");

    ViewUserProfileButton.setText("View Profile");
    ViewUserProfileButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            ViewUserProfileButtonActionPerformed(evt);
        }
    });

    StopMusicButton.setText("STOP");
    StopMusicButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            StopMusicButtonActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(FriendPosts)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(FriendsLabel)
                        .addComponent(FriendPostsLabel)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(FriendScrollPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(ChatFriendButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(ViewFriendProfileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGap(47, 47, 47)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(SharedMusicLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(PlayMusicButton, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(28, 28, 28)
                                    .addComponent(StopMusicButton, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 0, Short.MAX_VALUE))
                                .addComponent(SharedMusicPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                            .addGap(29, 29, 29)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(LogoutButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(EditProfileButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(UploadMusicButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(PostContent)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(PostButton, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(OnlineUsers, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(FriendRequestButton)
                                .addComponent(ChatOnlineButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(ViewUserProfileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(OnlineUsersLabel)
                            .addGap(0, 0, Short.MAX_VALUE)))
                    .addGap(18, 18, 18)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(FriendRequestsLabel)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(FriendRequest, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(AcceptFriendButton, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(DeclineFriendButton, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGap(11, 11, 11)))
            .addContainerGap())
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(FriendsLabel)
                            .addGap(5, 5, 5)
                            .addComponent(FriendScrollPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(FriendPostsLabel))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(LogoutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(32, 32, 32)
                                    .addComponent(EditProfileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(UploadMusicButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGap(0, 0, Short.MAX_VALUE))))
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(SharedMusicLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(SharedMusicPane, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(48, 48, 48)
                            .addComponent(ChatFriendButton, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(ViewFriendProfileButton)))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(StopMusicButton)
                        .addComponent(PlayMusicButton))
                    .addGap(5, 5, 5)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(FriendPosts, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(PostContent, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(PostButton))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(OnlineUsersLabel)
                .addComponent(FriendRequestsLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(FriendRequestButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(ChatOnlineButton)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(ViewUserProfileButton))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(AcceptFriendButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(DeclineFriendButton))
                .addComponent(OnlineUsers, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(FriendRequest, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap())
    );

    pack();
    }// </editor-fold>//GEN-END:initComponents

    private void PasswordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PasswordFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_PasswordFieldActionPerformed

    private void LoginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoginButtonActionPerformed
        try
        {
            //Connect to the server on button press
            server = new Socket("localhost", 9090);
            
            //Define data streams
            outToServer = new ObjectOutputStream(server.getOutputStream());
            outToServer.flush();
            inFromServer = new ObjectInputStream(server.getInputStream());
            
            //Checking if information is valid
            String message = "<html>";
            if (LoginField.getText().length() < 1)
            message += "Type in the username<br>";
            if (PasswordField.getPassword().length < 1)
            message += "Type in the password<br>";
            if (message.equals("<html>"))
            {
                try
                {
                    //If the information is valid, create a Login class instance to initiate the login
                    login = new Login(LoginField.getText(), PasswordField.getPassword(), outToServer, inFromServer);
                    this.servResponse = login.getResponse(); // Get the server's response to the login
                    if (!this.servResponse.isEmpty())
                    message += this.servResponse.split(":")[0];
                }
                catch (IOException ex)
                {
                    message = "<html>Can't connect to server";
                }
                catch (NullPointerException ex)
                {
                    message = "<html>Server sent no response";
                } 
                catch (ClassNotFoundException ex) { }
            }

            message += "</html>";

            //Initialise the login message dialog
            LoginOutput.setText(message);
            LoginMessage.setLocationRelativeTo(this);
            LoginMessage.setVisible(true);
        }
        catch (NoSuchAlgorithmException | IOException ex) {}
    }//GEN-LAST:event_LoginButtonActionPerformed

    private void RegisterButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RegisterButtonMouseEntered
        RegisterButton.setText("<HTML><FONT color=\"#0645AD\"><U>Register</U></FONT></HTML>");
    }//GEN-LAST:event_RegisterButtonMouseEntered

    private void RegisterButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RegisterButtonMouseExited
        RegisterButton.setText("<HTML><FONT color=\"#000000\">Register</FONT></HTML>");
    }//GEN-LAST:event_RegisterButtonMouseExited

    private void ChatFriendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChatFriendButtonActionPerformed
        try 
        {
            canUpdate = false;
            //Send the chat request
            outToServer.writeObject("requestChat:" + userID + ":" + FriendList.getSelectedValue().id);
            
            //Open the chat frame after sending chat request
            ChatFrame chatFrame = new ChatFrame(FriendList.getSelectedValue(), new ListObject(userName, userID));
            chatFrame.setVisible(true);
            chatFrame.setLocationRelativeTo(this);
            canUpdate = true;
        } catch (IOException ex) 
        {
            Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_ChatFriendButtonActionPerformed

    private void PlayMusicButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PlayMusicButtonActionPerformed
        try 
        {   
            //Play the fetched music
            musicClip = AudioSystem.getClip();
            musicClip.open(requestMusic(SharedMusicList.getSelectedValue()));
            musicClip.start();
        } catch (IOException | ClassNotFoundException | LineUnavailableException | UnsupportedAudioFileException ex) 
        {
            Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_PlayMusicButtonActionPerformed

    private void LogoutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LogoutButtonActionPerformed
        try 
        {
            //Use the logout function
            logOut();
        } catch (IOException ex) 
        {
            Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_LogoutButtonActionPerformed

    private void EditProfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EditProfileButtonActionPerformed
        //Open the EditProfile JDialog
        EditProfile.setVisible(true);
        EditProfile.setLocationRelativeTo(this);
    }//GEN-LAST:event_EditProfileButtonActionPerformed

    private void UploadMusicButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UploadMusicButtonActionPerformed
        //Open the UploadMusic JDialog
        UploadMusic.setVisible(true);
        UploadMusic.setLocationRelativeTo(this);
    }//GEN-LAST:event_UploadMusicButtonActionPerformed

    private void FriendRequestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FriendRequestButtonActionPerformed
        try 
        {
            //Call the requestFriendship function with the selected user from the list
            requestFriendship(OnlineUsersList.getSelectedValue());
        } catch (IOException ex) 
        {
            Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {}
    }//GEN-LAST:event_FriendRequestButtonActionPerformed

    private void PostContentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PostContentActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_PostContentActionPerformed

    private void LoginMessageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoginMessageButtonActionPerformed
        //Timer class running on a sepperate thread to call the update function
        //after the login phase was successful.
        class InterfaceUpdate extends TimerTask 
        {
            @Override
            public void run() 
            {
                try 
                { 
                    if (canUpdate) updateInterface();
                } catch (IOException | ClassNotFoundException ex) 
                {
                    Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        LoginMessage.dispose();
        String[] loginResponse = this.servResponse.split(":");
        String result = loginResponse[0];
        //Parsing the userID from request response
        userID = Integer.parseInt(loginResponse[1]);
        
        //If successful login, initialise all variables and display the main JFrame
        if (result.equals("Login successful"))
        {
            userName = LoginField.getText();
            this.setVisible(true);
            LoginDialog.setVisible(false);
            try 
            {
                updateInterface();
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
            }
            Timer timer = new Timer();
            try 
            {
                imageLabel.setIcon((new ImageIcon(requestImage(String.valueOf(userInterface.userID)))));
            } catch (IOException | ClassNotFoundException ex) 
            {
                Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NullPointerException ex) {}
            
            timer.schedule(new InterfaceUpdate(), 0, 5000);
            
            this.setTitle(this.getTitle() + " : " + userName);
        }
    }//GEN-LAST:event_LoginMessageButtonActionPerformed

    private void UploadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UploadButtonActionPerformed
        //Uploading the image
        JFileChooser chooser;
        chooser = new JFileChooser();
        //Setting the image filters to jpg and jpeg
        FileFilter filter = new FileNameExtensionFilter("JPEG file", new String[] {"jpg", "jpeg"});
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);

        //Open the file chooser
        int returnVal = chooser.showOpenDialog(EditProfile);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            selectedImageFile = new File(chooser.getSelectedFile().getAbsolutePath());
            try
            {
                //If the image is within the set bounds, upload it
                profileImage = ImageIO.read(selectedImageFile);
                if (!(profileImage.getHeight() < 140 || profileImage.getHeight() > 170
                    || profileImage.getWidth() < 140 || profileImage.getWidth() > 170))
                {
                    imageLabel.setIcon(new ImageIcon(profileImage));
                    imageLabel.setBounds(0, 0, profileImage.getWidth(), profileImage.getHeight());
                    
                    canUpdate = false;
                    
                    //Tell the server an image is going to be sent
                    outToServer.writeObject("sendImage:" + userInterface.userID + ":");
                    
                    //Convert the image into a byte array and send it
                    ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream(); 
                    ImageIO.write(profileImage, "jpg", byteArrayOutput);

                    outToServer.writeObject(byteArrayOutput.toByteArray());
                    outToServer.flush();
                    
                    canUpdate = true;
                    
                }
            }
            catch (IOException | NullPointerException ex) { }
        }
    }//GEN-LAST:event_UploadButtonActionPerformed

    private void RegisterButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RegisterButtonMouseClicked
        //Open the registration form
        RegisterForm registration = new RegisterForm();
        registration.setLocationRelativeTo(this);
        registration.setVisible(true);
    }//GEN-LAST:event_RegisterButtonMouseClicked

    private void PostButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PostButtonActionPerformed
        //Send the post
        FriendPostsArea.setText(FriendPostsArea.getText() + userName + ": " + PostContent.getText() + "\n");
        try 
        {
            //Call the sendPost function
            sendPost(PostContent.getText());
        } catch (IOException ex) 
        {
            Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
        }
        PostContent.setText("");
    }//GEN-LAST:event_PostButtonActionPerformed

    private void DeclineFriendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeclineFriendButtonActionPerformed
        try
        {
            //Call the declineFriendship function
            declineFriendship(FriendRequestsList.getSelectedValue());
        } catch (IOException ex) 
        {
            Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {}
        friendRequestListModel.removeElement(FriendRequestsList.getSelectedValue());
    }//GEN-LAST:event_DeclineFriendButtonActionPerformed

    private void AcceptFriendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AcceptFriendButtonActionPerformed
        try 
        {
            //call the acceptFriendship function
            acceptFriendship(FriendRequestsList.getSelectedValue());
        } catch (IOException ex) 
        {
            Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {}
    }//GEN-LAST:event_AcceptFriendButtonActionPerformed

    private void UpdateUsernameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateUsernameButtonActionPerformed
        //Update the username
        if (newUsernameField.getText().length() < 6)
        {
            JOptionPane.showMessageDialog(EditProfile, "Username too short");
            return;
        }
        
        try 
        {
            canUpdate = false;
            
            outToServer.writeObject("editProfile:updateUsername:" + userInterface.userID + ":" + newUsernameField.getText());
            
            canUpdate = true;
        } catch (IOException ex) 
        {
            Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_UpdateUsernameButtonActionPerformed

    private void UpdatePasswordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdatePasswordButtonActionPerformed
        //Update the password
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        if (!newPassword.equals(confirmPassword) || newPassword.length() < 6)
        {
            JOptionPane.showMessageDialog(EditProfile, "Passwords are not the same or password is too short");
            return;
        }
        
        try 
        {
            //Encrypt the password
            newPassword = Login.encryptPassword(newPassword);
            
            canUpdate = false;
            //Send the new password
            outToServer.writeObject("editProfile:updatePassword:" + userInterface.userID + ":" + newPassword);
            canUpdate = true;
        } catch (IOException | NoSuchAlgorithmException ex) 
        {
            Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_UpdatePasswordButtonActionPerformed

    private void UpdateDetailsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpdateDetailsButtonActionPerformed
        //Update user details
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        
        if (firstName.isEmpty() || lastName.isEmpty())
        {
            JOptionPane.showMessageDialog(EditProfile, "First name or last name fields are empty");
            return;
        }
        
        //Parse the date
        String newYearString = (String) Year.getSelectedItem();
        int newYear = Integer.parseInt(newYearString);
        int newMonth = Month.getSelectedIndex() + 1;
        String newDayString = (String) Day.getSelectedItem();
        int newDay = Integer.parseInt(newDayString);
        
        String newDateString = newYear + "-" + newMonth + "-" + newDay;
        
        DateFormat df; 
        df = new SimpleDateFormat("yyyy-MM-dd");
        Date newDate = null;
        try 
        {
            newDate = new java.sql.Date(df.parse(newDateString).getTime());
        } catch (ParseException e) {}
        
        try 
        {
            canUpdate = false;
            //Send the new details
            outToServer.writeObject("editProfile:updateDetails:" + userInterface.userID + ":" + firstName + ":" + lastName
                    + ":" + newDate);
            canUpdate = true;
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_UpdateDetailsButtonActionPerformed

    private void ViewFriendProfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ViewFriendProfileButtonActionPerformed
        try 
        {
            //Call the ViewProfile function and display the information
            ViewProfile(FriendList.getSelectedValue());
            viewProfile.setVisible(true);
            viewProfile.setLocationRelativeTo(this);
        } catch (IOException | ClassNotFoundException ex) 
        {
            Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {}
    }//GEN-LAST:event_ViewFriendProfileButtonActionPerformed

    private void ViewUserProfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ViewUserProfileButtonActionPerformed
        try 
        {
            //Call the ViewProfile function and display the information
            ViewProfile(OnlineUsersList.getSelectedValue());
            viewProfile.setVisible(true);
            viewProfile.setLocationRelativeTo(this);
        } catch (IOException | ClassNotFoundException ex) 
        {
            Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {}
    }//GEN-LAST:event_ViewUserProfileButtonActionPerformed

    private void ClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearButtonActionPerformed
        MusicTypeModel.clear();
    }//GEN-LAST:event_ClearButtonActionPerformed

    private void AddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddButtonActionPerformed
        //Add new music preferences to the list
        String selectedType = (String) ChooseMusic.getSelectedItem();
        if (!MusicTypeModel.contains(selectedType))
            MusicTypeModel.addElement(selectedType);
    }//GEN-LAST:event_AddButtonActionPerformed

    private void updateMusicButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateMusicButtonActionPerformed
        try 
        {
            //Call the updateMusicPreference function
            updateMusicPreference(MusicTypeModel);
        } catch (IOException ex) 
        {
            Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_updateMusicButtonActionPerformed

    private void ChooseFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChooseFileButtonActionPerformed
        //Choose the music file to be uploaded
        selectedMusicFile = null;
        
        JFileChooser chooser;
        chooser = new JFileChooser();
        //Filter the file to only .wav files
        FileFilter filter = new FileNameExtensionFilter("MP3 file", new String[] {"wav"});
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
        
        int returnVal = chooser.showOpenDialog(UploadMusic);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            selectedMusicFile = new File(chooser.getSelectedFile().getAbsolutePath());
            NameLabel.setText(selectedMusicFile.getName());
            SizeLabel.setText(String.valueOf(selectedMusicFile.length()/1024) + " kB");
        }
    }//GEN-LAST:event_ChooseFileButtonActionPerformed

    private void SendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SendButtonActionPerformed
        //Send the uploaded music file
        if (!(selectedMusicFile.isFile() && selectedMusicFile.exists()))
            return;
        
        try 
        {
            canUpdate = false;
            outToServer.writeObject("sendMusic:" + userID + ":" + NameLabel.getText() + ":" + ((String) MusicTypeBox.getSelectedItem()));
            //Convert the music file into a byte array
            byte[] data = Files.readAllBytes(Paths.get(selectedMusicFile.getAbsolutePath()));
            //Send the byte array 
            outToServer.writeObject(data);
            
            canUpdate = true;
            
            UploadMusic.setVisible(false);
        } catch (IOException ex) 
        {
            Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_SendButtonActionPerformed

    private void StopMusicButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StopMusicButtonActionPerformed
        musicClip.stop();
    }//GEN-LAST:event_StopMusicButtonActionPerformed

    private void AcceptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AcceptButtonActionPerformed
        //Accept chat request button
        try 
        {
            canUpdate = false;
            outToServer.writeObject("acceptChat:" + userID + ":" + userInterface.chatRequest.id);
            ChatFrame chatFrame = new ChatFrame(userInterface.chatRequest, new ListObject(userName, userID));
            chatFrame.setVisible(true);
            chatFrame.setLocationRelativeTo(this);
            ChatRequest.dispose();
            canUpdate = true;
        } catch (IOException ex) 
        {
            Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_AcceptButtonActionPerformed

    private void DeclineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeclineButtonActionPerformed
        //Decline chat request button
        try 
        {
            canUpdate = false;
            outToServer.writeObject("declineChat:" + userID + ":" + userInterface.chatRequest.id);
            ChatRequest.dispose();
            canUpdate = true;
        } catch (IOException ex) 
        {
            Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_DeclineButtonActionPerformed

    private void ChatOnlineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChatOnlineButtonActionPerformed
        //Send a chat request to online users
        try 
        {
            canUpdate = false;
            outToServer.writeObject("requestChat:" + userID + ":" + OnlineUsersList.getSelectedValue().id);
            ChatFrame chatFrame = new ChatFrame(OnlineUsersList.getSelectedValue(), new ListObject(userName, userID));
            chatFrame.setVisible(true);
            chatFrame.setLocationRelativeTo(this);
            canUpdate = true;
        } catch (IOException ex) 
        {
            Logger.getLogger(Music_Soc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_ChatOnlineButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) 
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Music_Soc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Music_Soc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Music_Soc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Music_Soc.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() 
        {
            public void run() 
            {
                new Music_Soc().setVisible(false);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AcceptButton;
    private javax.swing.JButton AcceptFriendButton;
    private javax.swing.JButton AddButton;
    private javax.swing.JButton ChatFriendButton;
    private javax.swing.JLabel ChatMessage;
    private javax.swing.JButton ChatOnlineButton;
    private javax.swing.JDialog ChatRequest;
    private javax.swing.JButton ChooseFileButton;
    private javax.swing.JComboBox<String> ChooseMusic;
    private javax.swing.JButton ClearButton;
    private javax.swing.JLabel DateofBirthLabel;
    private javax.swing.JComboBox<String> Day;
    private javax.swing.JButton DeclineButton;
    private javax.swing.JButton DeclineFriendButton;
    private javax.swing.JLabel DoBViewLabel;
    private javax.swing.JDialog EditProfile;
    private javax.swing.JButton EditProfileButton;
    private javax.swing.JLabel FirstNameViewLabel;
    private javax.swing.JList<ListObject> FriendList;
    private javax.swing.JScrollPane FriendPosts;
    private javax.swing.JTextArea FriendPostsArea;
    private javax.swing.JLabel FriendPostsLabel;
    private javax.swing.JScrollPane FriendRequest;
    private javax.swing.JButton FriendRequestButton;
    private javax.swing.JLabel FriendRequestsLabel;
    private javax.swing.JList<ListObject> FriendRequestsList;
    private javax.swing.JScrollPane FriendScrollPanel;
    private javax.swing.JLabel FriendsLabel;
    private javax.swing.JLabel InfoLabel;
    private javax.swing.JLabel LastNameViewLabel;
    private javax.swing.JButton LoginButton;
    private javax.swing.JDialog LoginDialog;
    private javax.swing.JTextField LoginField;
    private javax.swing.JLabel LoginLabel;
    private javax.swing.JDialog LoginMessage;
    private javax.swing.JButton LoginMessageButton;
    private javax.swing.JLabel LoginOutput;
    private javax.swing.JLabel LoginTextLabel;
    private javax.swing.JButton LogoutButton;
    private javax.swing.JComboBox<String> Month;
    private javax.swing.JLabel MusicProfileLabel;
    private javax.swing.JList<String> MusicProfileList;
    private javax.swing.JComboBox<String> MusicTypeBox;
    private javax.swing.JLabel MusicTypeLabel;
    private javax.swing.JList<String> MusicTypeList;
    private javax.swing.JScrollPane MusicTypePane;
    private javax.swing.JLabel MusicTypeTextLabel;
    private javax.swing.JLabel MusicTypesLabel;
    private javax.swing.JLabel NameLabel;
    private javax.swing.JLabel NameTextLabel;
    private javax.swing.JScrollPane OnlineUsers;
    private javax.swing.JLabel OnlineUsersLabel;
    private javax.swing.JList<ListObject> OnlineUsersList;
    private javax.swing.JPasswordField PasswordField;
    private javax.swing.JLabel PasswordLabel;
    private javax.swing.JButton PlayMusicButton;
    private javax.swing.JButton PostButton;
    private javax.swing.JTextField PostContent;
    private javax.swing.JLabel RegisterButton;
    private javax.swing.JButton SendButton;
    private javax.swing.JList<String> SharedMusicJList;
    private javax.swing.JLabel SharedMusicLabel;
    private javax.swing.JList<ListObject> SharedMusicList;
    private javax.swing.JScrollPane SharedMusicPane;
    private javax.swing.JLabel SizeLabel;
    private javax.swing.JLabel SizeTextLabel;
    private javax.swing.JButton StopMusicButton;
    private javax.swing.JButton UpdateDetailsButton;
    private javax.swing.JButton UpdatePasswordButton;
    private javax.swing.JButton UpdateUsernameButton;
    private javax.swing.JButton UploadButton;
    private javax.swing.JDialog UploadMusic;
    private javax.swing.JButton UploadMusicButton;
    private javax.swing.JPanel UserImagePanel;
    private javax.swing.JLabel UsernameViewLabel;
    private javax.swing.JButton ViewFriendProfileButton;
    private javax.swing.JButton ViewUserProfileButton;
    private javax.swing.JComboBox<String> Year;
    private javax.swing.JPasswordField confirmPasswordField;
    private javax.swing.JLabel confirmPasswordLabel;
    private javax.swing.JTextField firstNameField;
    private javax.swing.JLabel firstNameLabel;
    private javax.swing.JPanel imagePanel;
    private javax.swing.JTextField lastNameField;
    private javax.swing.JLabel lastNameLabel;
    private javax.swing.JLabel newDOB;
    private javax.swing.JLabel newFirstname;
    private javax.swing.JLabel newLastname;
    private javax.swing.JLabel newPassportLabel;
    private javax.swing.JPasswordField newPasswordField;
    private javax.swing.JLabel newUsername;
    private javax.swing.JTextField newUsernameField;
    private javax.swing.JLabel newUsernameLabel;
    private javax.swing.JButton updateMusicButton;
    private javax.swing.JLabel userSharedMusicLabel;
    private javax.swing.JScrollPane viewMusicProfilePane;
    private javax.swing.JDialog viewProfile;
    private javax.swing.JScrollPane viewSharedMusicPane;
    // End of variables declaration//GEN-END:variables
}
