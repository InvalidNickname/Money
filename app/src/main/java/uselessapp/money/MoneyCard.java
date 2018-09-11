package uselessapp.money;

public class MoneyCard {

    String country, title, circulationTime;
    int imageID;

    public MoneyCard(String country, String title, String circulationTime, int imageID) {
        this.country = country;
        this.title = title;
        this.circulationTime = circulationTime;
        this.imageID = imageID;
    }
}
