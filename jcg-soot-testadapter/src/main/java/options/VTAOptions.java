package options;

import de.tud.cs.peaks.sootconfig.SparkOptions;

public class VTAOptions extends SparkOptions {

    private boolean enabled = false;

    public VTAOptions() {
        super();
    }

    public VTAOptions enableVTA() {
        this.enable(); // also enable SPARK
        this.enabled = true;
        return this;
    }

    public VTAOptions disableVTA() {
        this.enabled = false;
        return this;
    }

    @Override
    protected void pushToOptionSet() {
        super.pushToOptionSet();
        this.addOption("vta", this.enabled ? "true" : "false");
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        VTAOptions that = (VTAOptions) o;

        return enabled == that.enabled;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }
}
