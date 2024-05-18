package musicsoc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//User class used to send user data
public class User implements java.io.Serializable
{
    int userID;
    String userName;
    String firstName;
    String lastName;
    String dateOfBirth;
    List<String> sharedMusic = new ArrayList<>();
    List<String> musicTypes = new ArrayList<>();
}
