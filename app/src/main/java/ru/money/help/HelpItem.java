package ru.money.help;

class HelpItem {

    private final String title, text;

    HelpItem(String title, String text) {
        this.text = text;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }
}
