package visualparticle.better.client.module;

import visualparticle.better.client.event.Subscription;
import visualparticle.better.client.setting.Setting;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
	private final String id;
	private final String name;
	private final String description;
	private final int icon;
	private final ModuleCategory category;
	private final boolean enabledByDefault;
	private final List<Subscription> subscriptions = new ArrayList<>();
	private final List<Setting<?>> settings = new ArrayList<>();
	private boolean enabled;

	protected Module(String id, String name, ModuleCategory category, boolean enabledByDefault) {
		this(id, name, "", 0xE86F, category, enabledByDefault);
	}

	protected Module(String id, String name, String description, int icon, ModuleCategory category, boolean enabledByDefault) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.icon = icon;
		this.category = category;
		this.enabledByDefault = enabledByDefault;
	}

	public final void setEnabled(boolean value) {
		if (enabled == value) return;
		enabled = value;
		if (value) {
			onEnable();
		} else {
			subscriptions.forEach(Subscription::close);
			subscriptions.clear();
			onDisable();
		}

	}

	public final void toggle() { setEnabled(!enabled); }
	protected final void track(Subscription subscription) { subscriptions.add(subscription); }
	protected final void addSettings(Setting<?>... values) { settings.addAll(List.of(values)); }
	protected void onEnable() {}
	protected void onDisable() {}
	public final String id() { return id; }
	public final String name() { return name; }
	public final String description() { return description; }
	public final int icon() { return icon; }
	public final ModuleCategory category() { return category; }
	public final boolean enabledByDefault() { return enabledByDefault; }
	public final boolean enabled() { return enabled; }
	public final List<Setting<?>> settings() { return List.copyOf(settings); }
}