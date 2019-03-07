package ru.mycollection.list;

@SuppressWarnings("WeakerAccess")
public class Category {

    private final String categoryName, imagePath;
    private final int count, id;

    public Category(String categoryName, String imagePath, int count, int id) {
        this.categoryName = categoryName;
        this.imagePath = imagePath;
        this.count = count;
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getCount() {
        return count;
    }

    public int getId() {
        return id;
    }
}
