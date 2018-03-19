package options;

import de.tud.cs.peaks.sootconfig.CallGraphPhaseOptions;


public class CGOptionsWithReflection extends CallGraphPhaseOptions {

    private boolean types_for_invoke = false;
    private boolean safe_forname = false;
    private boolean safe_newinstance = false;

    public CGOptionsWithReflection doNotUseTypesForInvoke() {
        this.types_for_invoke = false;
        return this;
    }

    public CGOptionsWithReflection useTypesForInvoke() {
        this.types_for_invoke = true;
        return this;
    }

    public CGOptionsWithReflection handleForNameSafe() {
        this.safe_forname = true;
        return this;
    }

    public CGOptionsWithReflection doNotHandleForNameSafe() {
        this.safe_forname = false;
        return this;
    }

    public CGOptionsWithReflection handleNewInstanceSafe() {
        this.safe_newinstance = true;
        return this;
    }

    public CGOptionsWithReflection doNotHandleNewInstanceSafe() {
        this.safe_newinstance = false;
        return this;
    }

    @Override
    protected void pushToOptionSet() {
        super.pushToOptionSet();
        this.addOption("types-for-invoke", this.types_for_invoke ? "true" : "false");
        this.addOption("safe-forname", this.safe_forname ? "true" : "false");
        this.addOption("safe-newinstance", this.safe_newinstance ? "true" : "false");

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CGOptionsWithReflection that = (CGOptionsWithReflection) o;

        if (safe_forname != that.safe_forname) return false;
        if (safe_newinstance != that.safe_newinstance) return false;
        if (types_for_invoke != that.types_for_invoke) return false;
        return true;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (safe_newinstance ? 1 : 0);
        result = 31 * result + (safe_forname ? 1 : 0);
        result = 31 * result + (types_for_invoke ? 1 : 0);
        return result;
    }

}
