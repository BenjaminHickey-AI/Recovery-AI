package Logic;

public class Strength extends Workout{
    private int sets, reps, weight;

    Strength(String _name, String _type, String _date, int _sets, int _reps, int _weight) {
        super(_name, _date, _type);
        sets = _sets;
        reps = _reps;
        weight = _weight;
    }

    Strength(String _name, String _type, String _date, String _docID, int _sets, int _reps, int _weight) {
        super(_name, _date, _type, _docID);
        sets = _sets;
        reps = _reps;
        weight = _weight;
    }

    public int getSets()
    {
        return sets;
    }

    public void setSets(int _sets)
    {
        sets = _sets;
    }

    public int getReps()
    {
        return reps;
    }

    public void setReps(int _reps)
    {
        reps = _reps;
    }

    public int getWeight()
    {
        return weight;
    }

    public void setWeight(int _weight)
    {
        weight = _weight;
    }

}
