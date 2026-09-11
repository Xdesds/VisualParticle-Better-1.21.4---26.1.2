package visualparticle.better.client.ui;

import visualparticle.better.client.ClientCore;
import visualparticle.better.client.module.ModuleCategory;
import visualparticle.better.client.module.impl.render.ParticlesModule;
import visualparticle.better.client.render.ui.AnimatedFloat;
import visualparticle.better.client.render.ui.SliderModel;
import java.util.ArrayList;
import java.util.List;

public final class ClickGuiModel {
	public record Category(String name, int icon, List<Module> modules) {}

	public static final class Module {
		public final String name, description;
		public final int icon;
		public final List<Setting> settings;
		public final AnimatedFloat animation;
		public boolean enabled;
		private final visualparticle.better.client.module.Module backing;

		Module(visualparticle.better.client.module.Module backing, List<Setting> settings) {
			this(backing, settings, backing.name());
		}

		Module(visualparticle.better.client.module.Module backing, List<Setting> settings, String displayName) {
			this.backing = backing;
			name = displayName;
			description = backing.description();
			icon = backing.icon();
			enabled = backing.enabled();
			this.settings = List.copyOf(settings);
			animation = new AnimatedFloat(enabled ? 1 : 0);
		}

		public void toggle() {
			enabled = !enabled;
			backing.setEnabled(enabled);
			animation.setTarget(enabled ? 1 : 0);
		}
	}

	public sealed interface Setting permits Toggle, Slider, Choice, Group, ColorValue, Key {
		String name();
		String hint();
		default boolean visible() { return true; }
	}

	public static final class Toggle implements Setting {
		private final visualparticle.better.client.setting.BooleanSetting backing;
		public final AnimatedFloat animation;
		public boolean value;

		Toggle(visualparticle.better.client.setting.BooleanSetting backing) {
			this.backing = backing;
			value = backing.enabled();
			animation = new AnimatedFloat(value ? 1 : 0);
		}
		public String name() { return UiLanguage.text(backing.name()); }
		public String hint() { return UiLanguage.text(backing.hint()); }
		public boolean visible() { return backing.visible(); }
		public void toggle() {
			value = !value;
			backing.set(value);
			animation.setTarget(value ? 1 : 0);
		}
	}

	public static final class Slider implements Setting {
		private final visualparticle.better.client.setting.NumberSetting backing;
		private final String suffix;
		public final SliderModel model;

		Slider(visualparticle.better.client.setting.NumberSetting backing) {
			this.backing = backing;
			suffix = backing.suffix();
			model = new SliderModel(backing.min(), backing.max(), backing.value());
		}
		public String name() { return UiLanguage.text(backing.name()); }
		public String hint() { return UiLanguage.text(backing.hint()); }
		public boolean visible() { return backing.visible(); }
		public String value() {
			float value = model.value();
			String formatted = Math.abs(value - Math.round(value)) < .01f
					? Integer.toString(Math.round(value))
					: String.format(java.util.Locale.ROOT, "%.1f", value);
			return formatted + suffix;
		}
		public void sync() { backing.set(model.value()); }
	}

	public static final class Choice implements Setting {
		private final visualparticle.better.client.setting.ModeSetting backing;
		public final List<String> values;
		public int selected;

		Choice(visualparticle.better.client.setting.ModeSetting backing) {
			this.backing = backing;
			values = backing.modes();
			selected = Math.max(0, values.indexOf(backing.value()));
		}
		public String name() { return UiLanguage.text(backing.name()); }
		public String hint() { return UiLanguage.text(backing.hint()); }
		public boolean visible() { return backing.visible(); }
		public String value() { return UiLanguage.option(values.get(selected)); }
		public String display(int index) { return UiLanguage.option(values.get(index)); }
		public void next() { select((selected + 1) % values.size()); }
		public void select(int index) {
			selected = Math.max(0, Math.min(values.size() - 1, index));
			backing.set(values.get(selected));
		}
	}

	public static final class Group implements Setting {
		private final visualparticle.better.client.setting.MultiBooleanSetting backing;
		public final List<Toggle> values;

		Group(visualparticle.better.client.setting.MultiBooleanSetting backing) {
			this.backing = backing;
			values = backing.values().stream().map(Toggle::new).toList();
		}
		public String name() { return UiLanguage.text(backing.name()); }
		public String hint() { return UiLanguage.text(backing.hint()); }
		public boolean visible() { return backing.visible(); }
		public String summary() {
			long enabled = values.stream().filter(value -> value.value).count();
			return enabled + "/" + values.size() + " " + UiLanguage.text("enabled");
		}
	}

	public static final class ColorValue implements Setting {
		private final visualparticle.better.client.setting.ColorSetting backing;
		ColorValue(visualparticle.better.client.setting.ColorSetting backing){this.backing=backing;}
		public String name(){return UiLanguage.text(backing.name());}
		public String hint(){return UiLanguage.text(backing.hint());}
		public boolean visible(){return backing.visible();}
		public int argb(){return backing.argb();}
		public String value(){return String.format(java.util.Locale.ROOT,"#%08X",argb());}
		public float hue(){return hsb()[0];}
		public float saturation(){return hsb()[1];}
		public float brightness(){return hsb()[2];}
		public float alpha(){return ((argb()>>>24)&255)/255f;}
		public void setHsb(float hue,float saturation,float brightness){int alpha=argb()&0xFF000000;int rgb=java.awt.Color.HSBtoRGB(clamp(hue),clamp(saturation),clamp(brightness));backing.set(alpha|(rgb&0xFFFFFF));}
		public void setAlpha(float alpha){backing.set((Math.round(clamp(alpha)*255)<<24)|(argb()&0xFFFFFF));}
		private float[] hsb(){int value=argb();return java.awt.Color.RGBtoHSB(value>>16&255,value>>8&255,value&255,null);}
		private static float clamp(float value){return Math.max(0,Math.min(1,value));}
	}
	public record Key(String name, String hint, String key) implements Setting {}

	public static List<Category> create() {
		visualparticle.better.client.module.Module core = ClientCore.getInstance().modules().find("particles").orElseThrow();
		ParticlesModule particles = (ParticlesModule) core;
		return List.of(
				section(core, particles, "General", 0xE8B8),
				section(core, particles, "Events", 0xE7F4),
				section(core, particles, "World", 0xE80B),
				section(core, particles, "Colors", 0xE40A),
				section(core, particles, "Presets", 0xE8F4));
	}

	private static Category section(visualparticle.better.client.module.Module core, ParticlesModule particles, String name, int icon) {
		return new Category(name, icon, List.of(new Module(core, adapt(particles.settingsFor(name)), name)));
	}

	private static List<Module> modules(ModuleCategory category) {
		return ClientCore.getInstance().modules().all().stream()
				.filter(module -> module.category() == category)
				.filter(module -> !module.id().equals("client_diagnostics"))
				.map(ClickGuiModel::live)
				.toList();
	}
	private static Module live(visualparticle.better.client.module.Module core) {
		return new Module(core, adapt(core.settings()));
	}
	private static List<Setting> adapt(List<? extends visualparticle.better.client.setting.Setting<?>> source) {
		List<Setting> settings = new ArrayList<>();
		for (visualparticle.better.client.setting.Setting<?> setting : source) {
			if (setting instanceof visualparticle.better.client.setting.BooleanSetting value) {
				settings.add(new Toggle(value));
			} else if (setting instanceof visualparticle.better.client.setting.NumberSetting value) {
				settings.add(new Slider(value));
			} else if (setting instanceof visualparticle.better.client.setting.ModeSetting value) {
				settings.add(new Choice(value));
			} else if (setting instanceof visualparticle.better.client.setting.MultiBooleanSetting value) {
				settings.add(new Group(value));
			} else if (setting instanceof visualparticle.better.client.setting.ColorSetting value) {
				settings.add(new ColorValue(value));
			}
		}
		return settings;
	}


	private ClickGuiModel() {}
}
