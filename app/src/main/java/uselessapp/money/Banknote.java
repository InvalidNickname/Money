package uselessapp.money;

public class Banknote {

    String country, title, circulationTime, description;
    String obversePath, reversePath;

    Banknote(String country, String title, String circulationTime, String obversePath, String reversePath, String description) {
        this.country = country;
        this.title = title;
        this.circulationTime = circulationTime;
        this.obversePath = obversePath;
        this.reversePath = reversePath;
        this.description = description;
    }
}
