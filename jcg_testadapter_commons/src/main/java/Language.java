public enum Language {
    JAVA("java"),
    JS("js");

    private final String name;

    Language(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
