package visualparticle.better.client.setting;

public final class NumberSetting extends Setting<Float> {
	private final float min;
	private final float max;
	private final float step;
	private final String suffix;

	public NumberSetting(String name, String hint, float value, float min, float max, float step, String suffix) {
		super(name, hint, value);
		this.min = min;
		this.max = max;
		this.step = step;
		this.suffix = suffix;
		set(value);
	}

	@Override public void set(Float value) {
		float snapped = Math.round(value / step) * step;
		super.set(Math.max(min, Math.min(max, snapped)));
	}

	public float min() { return min; }
	public float max() { return max; }
	public String suffix() { return suffix; }
}