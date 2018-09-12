package uselessapp.money;

public class Banknote {

    String country, title, circulationTime;
    int imageID;

    Banknote(String country, String title, String circulationTime, int imageID) {
        this.country = country;
        this.title = title;
        this.circulationTime = circulationTime;
        this.imageID = imageID;
    }
}
