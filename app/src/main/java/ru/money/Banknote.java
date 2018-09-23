package ru.money;

public class Banknote {

    String country, title, circulationTime, description;
    String obversePath;
    int id;

    Banknote(int id, String country, String title, String circulationTime, String obversePath, String description) {
        this.id = id;
        this.country = country;
        this.title = title;
        this.circulationTime = circulationTime;
        this.obversePath = obversePath;
        this.description = description;
    }
}
