package visualparticle.better.client.setting;

import java.util.List;

public final class ModeSetting extends Setting<String> {
	private final List<String> modes;

	public ModeSetting(String name, String hint, String initial, String... modes) {
		super(name, hint, initial);
		this.modes = List.of(modes);
		if (!this.modes.contains(initial)) throw new IllegalArgumentException("Unknown initial mode: " + initial);
	}

	@Override public void set(String value) {
		if (!modes.contains(value)) throw new IllegalArgumentException("Unknown mode: " + value);
		super.set(value);
	}

	public List<String> modes() { return modes; }
	public boolean is(String mode) { return value().equals(mode); }
}