package Logic;

import java.util.Vector;

abstract class Workout {
    public String name, type, date, docID;


    public Workout(String _name, String _type, String _date) {
        date = _date;
        name = _name;
        docID = "";
        type = _type;
    }

    public Workout(String _name, String _type, String _date, String _docID) {
        date = _date;
        name = _name;
        docID = _docID;
        type = _type;
    }

    String getName() {
        return name;
    }

    public void setName(String _name) {
        name = _name;
    }

    public String getType() {
        return type;
    }

    public void setType(String _type) {
        type = _type;
    }

    public String getDate() {
        return date;
    }

    public void setCompletionDate(String _date) {
        date = _date;
    }

    public String getDocID()
    {
        return docID;
    }

    public void setDocID(String _docID)
    {
        docID = _docID;
    }
}
