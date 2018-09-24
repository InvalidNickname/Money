package ru.money;

class Banknote {

    final String country, title, circulationTime, obversePath;
    final int id;

    Banknote(int id, String country, String title, String circulationTime, String obversePath) {
        this.id = id;
        this.country = country;
        this.title = title;
        this.circulationTime = circulationTime;
        this.obversePath = obversePath;
    }
}
