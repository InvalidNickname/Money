package ru.mycollection.list;

@SuppressWarnings("WeakerAccess")
public class Banknote {

    private final String country, title, circulationTime, obversePath;
    private final int id;

    Banknote(int id, String country, String title, String circulationTime, String obversePath) {
        this.id = id;
        this.country = country;
        this.title = title;
        this.circulationTime = circulationTime;
        this.obversePath = obversePath;
    }

    public String getCountry() {
        return country;
    }

    public String getTitle() {
        return title;
    }

    public String getCirculationTime() {
        return circulationTime;
    }

    String getObversePath() {
        return obversePath;
    }

    public int getId() {
        return id;
    }
}
