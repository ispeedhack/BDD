package musicsoc;

import java.io.Serializable;

//ListObject class storing the id and name of users. Used in JLists
public class ListObject implements Serializable
{
    String name;
    int id;
    
    public ListObject(String _name, int _id)
    {
        name = _name;
        id = _id;
    }

    @Override
    public String toString() 
    {
        return name;
    }
}
