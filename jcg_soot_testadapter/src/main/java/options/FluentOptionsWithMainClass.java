package options;

import de.tud.cs.peaks.sootconfig.FluentOptions;
import soot.options.Options;

public class FluentOptionsWithMainClass extends FluentOptions {

    private String mainClass = null;

    @Override
    public Options applyTo(Options o) {
        if (mainClass != null)
            o.set_main_class(mainClass);
        return super.applyTo(o);
    }

    public FluentOptionsWithMainClass setMainClass(String mainClass) {
        this.mainClass = mainClass;
        return this;
    }
}
