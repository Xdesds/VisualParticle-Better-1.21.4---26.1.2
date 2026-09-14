package visualparticle.better.client.setting;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class MultiBooleanSetting extends Setting<List<BooleanSetting>> {
	public MultiBooleanSetting(String name, String hint, BooleanSetting... values) {
		super(name, hint, List.copyOf(Arrays.asList(values)));
	}

	public List<BooleanSetting> values() { return value(); }
	public Optional<BooleanSetting> find(String name) {
		return values().stream().filter(setting -> setting.name().equalsIgnoreCase(name)).findFirst();
	}
	public boolean isEnabled(String name) { return find(name).map(BooleanSetting::enabled).orElse(false); }
}