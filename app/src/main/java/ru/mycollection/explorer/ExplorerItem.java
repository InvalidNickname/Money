package ru.mycollection.explorer;

class ExplorerItem {

    private final String name;
    private final String path;

    ExplorerItem(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}
