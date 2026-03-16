package Logic;

public class Cardio extends Workout {
    private int duration, distance;

    Cardio(String _name, String _type, String _date, int _duration, int _distance) {
        super(_name, _date, _type);
        duration = _duration;
        distance = _distance;
    }

    Cardio(String _name, String _type, String _date, String _docID, int _duration, int _distance) {
        super(_name, _date, _type, _docID);
        duration = _duration;
        distance = _distance;
    }

    public int getDuration()
    {
        return duration;
    }

    public void setDuration(int _duration)
    {
        duration = _duration;
    }

    public int getDistance()
    {
        return distance;
    }

    public void setDistance(int _distance)
    {
        distance = _distance;
    }


}
