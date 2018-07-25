module expressions {
    requires java.base;
    exports lib;
    exports lib.internal to parserimpl;
}