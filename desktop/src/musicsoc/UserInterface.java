package musicsoc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//UserInterface class which is sent every time the interface is updated
public class UserInterface implements Serializable
{
    public int userID = 0;
    public List<ListObject> friends = new ArrayList<>();
    public List<ListObject> friendRequests = new ArrayList<>();
    public List<ListObject> sharedMusic = new ArrayList<>();
    public List<ListObject> onlineUsers = new ArrayList<>();
    public List<Post> posts = new ArrayList<>();
    ListObject chatRequest = new ListObject("", -1);
}
