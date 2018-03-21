package options;

import de.tud.cs.peaks.sootconfig.CallGraphPhaseSubOptions;

public class CHAOptions extends CallGraphPhaseSubOptions {

    private boolean enabled = false;
    private boolean verbose = false;

    public CHAOptions enable() {
        this.enabled = true;
        return this;
    }

    public CHAOptions disable() {
        this.enabled = false;
        return this;
    }

    public CHAOptions enableVerboseMode() {
        this.verbose = true;
        return this;
    }

    public CHAOptions disableVerboseMode() {
        this.verbose = true;
        return this;
    }

    public CHAOptions() {
        super("cha");
    }

    @Override
    protected void pushToOptionSet() {
        this.addOption("enabled", this.enabled ? "true" : "false");
        this.addOption("verbose", this.verbose ? "true" : "false");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CHAOptions that = (CHAOptions) o;

        if (enabled != that.enabled) return false;
        return verbose == that.verbose;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (verbose ? 1 : 0);
        return result;
    }
}