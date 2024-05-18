package musicsoc;

//Post class which stores the userName and content of the post
public class Post implements java.io.Serializable
{
    String userName;
    String content;
    
    public Post(String _userName, String _content)
    {
        userName = _userName;
        content = _content;
    }
}
