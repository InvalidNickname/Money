package ru.money;

class Category {

    final String categoryName, imagePath;
    final int count, id;

    Category(String categoryName, String imagePath, int count, int id) {
        this.categoryName = categoryName;
        this.imagePath = imagePath;
        this.count = count;
        this.id = id;
    }
}
