package options;

import de.tud.cs.peaks.sootconfig.SparkOptions;

public class RTAOptions extends SparkOptions {

    private boolean enabled = false;

    public RTAOptions() {
        super();
    }

    public RTAOptions enableRTA() {
        this.enabled = true;
        enable();
        return this;
    }

    public RTAOptions disableRTA() {
        this.enabled = false;
        return this;
    }

    @Override
    protected void pushToOptionSet() {
        super.pushToOptionSet();
        this.addOption("rta", this.enabled ? "true" : "false");
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RTAOptions that = (RTAOptions) o;

        return enabled == that.enabled;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }
}
