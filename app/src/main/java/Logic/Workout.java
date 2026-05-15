package Logic;

import java.time.LocalDate;
public class Workout {

    private String name;
    private String description;
    private String date;
    private String docID;

    private int duration;
    private int intensity;

    public Workout(String name, String description, int duration, int intensity) {
        this.date = LocalDate.now().toString(); // this will look like: 2026-03-18
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.intensity = intensity;
        this.docID = "";
    }

    public Workout(String name, String description, String date, int duration, int intensity, String docID) {
        this.date = date;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.intensity = intensity;
        this.docID = docID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDocID()
    {
        return docID;
    }

    public void setDocID(String docID)
    {
        this.docID = docID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

}
