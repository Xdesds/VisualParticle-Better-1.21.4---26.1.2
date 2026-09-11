package visualparticle.better.client.setting;

public final class BooleanSetting extends Setting<Boolean> {
	public BooleanSetting(String name, String hint, boolean value) {
		super(name, hint, value);
	}

	public boolean enabled() { return value(); }
	public void toggle() { set(!enabled()); }
}