package particle.fx.particle;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class ParticleSettingsScreen extends Screen {
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_WIDTH = 98;
	private static final int GAP = 8;

	private final Screen parent;
	private final ParticleSystem.Settings settings;
	private Tab tab = Tab.EVENTS;

	public ParticleSettingsScreen(Screen parent) {
		super(Text.literal("Visual Particle Better Settings"));
		this.parent = parent;
		this.settings = ParticleSystem.getInstance().getSettings();
	}

	@Override
	protected void init() {
		rebuildWidgets();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, width, height, 0x66000000);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 16, 0xFFFFFF);
		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		if (client != null) {
			client.setScreen(parent);
		}
	}

	private void rebuildWidgets() {
		clearChildren();

		int fullWidth = Math.min(320, width - 40);
		int left = (width - fullWidth) / 2;
		int right = left + fullWidth / 2 + GAP / 2;
		int halfWidth = (fullWidth - GAP) / 2;
		int y = 40;

		addDrawableChild(button(left, y, halfWidth, labelTab(Tab.EVENTS), button -> {
			tab = Tab.EVENTS;
			rebuildWidgets();
		}));
		addDrawableChild(button(right, y, halfWidth, labelTab(Tab.WORLD), button -> {
			tab = Tab.WORLD;
			rebuildWidgets();
		}));

		y += 26;
		addDrawableChild(toggle(left, y, halfWidth, "Enabled", () -> settings.enabled, value -> settings.enabled = value));
		addDrawableChild(button(right, y, halfWidth, () -> "Color: " + clean(settings.colorPreset.name()), button -> {
			settings.markCustom();
			settings.colorPreset = next(settings.colorPreset);
			saveSettings();
			rebuildWidgets();
		}));

		y += 26;
		addDrawableChild(button(left, y, fullWidth, () -> "Preset: " + activePresetLabel(), button -> {
			cyclePreset();
			saveSettings();
			rebuildWidgets();
		}));

		y += 26;
		addDrawableChild(button(left, y, halfWidth, Text.literal("Create Preset"), button -> {
			if (client != null) {
				client.setScreen(new PresetNameScreen(this, "Create Preset", "Create", this::createPreset));
			}
		}));
		addDrawableChild(button(right, y, halfWidth, Text.literal("Save Preset"), button -> {
			saveCurrentPreset();
			rebuildWidgets();
		})).active = hasSelectedCustomPreset();

		y += 30;
		if (tab == Tab.EVENTS) {
			addEventWidgets(left, right, y, fullWidth, halfWidth);
		} else {
			addWorldWidgets(left, right, y, fullWidth, halfWidth);
		}

		int bottomY = height - 28;
		addDrawableChild(button(left, bottomY, BUTTON_WIDTH, Text.literal("Save Config"), button -> saveSettings()));
		addDrawableChild(button(left + BUTTON_WIDTH + GAP, bottomY, BUTTON_WIDTH, Text.literal("Reset"), button -> {
			settings.reset();
			saveSettings();
			rebuildWidgets();
		}));
		addDrawableChild(button(left + (BUTTON_WIDTH + GAP) * 2, bottomY, BUTTON_WIDTH, Text.literal("Done"), button -> close()));
	}

	private void addEventWidgets(int left, int right, int y, int fullWidth, int halfWidth) {
		addDrawableChild(button(left, y, halfWidth, () -> "Mode: " + clean(settings.particleMode.name()), button -> {
			settings.markCustom();
			settings.particleMode = next(settings.particleMode);
			saveSettings();
			rebuildWidgets();
		}));
		addDrawableChild(button(right, y, halfWidth, () -> "Glow: " + clean(settings.glowMode.name()), button -> {
			settings.markCustom();
			settings.glowMode = next(settings.glowMode);
			saveSettings();
			rebuildWidgets();
		}));

		y += 26;
		addDrawableChild(toggle(left, y, halfWidth, "Attack", () -> settings.attackTrigger, value -> settings.attackTrigger = value));
		addDrawableChild(toggle(right, y, halfWidth, "Totem", () -> settings.totemTrigger, value -> settings.totemTrigger = value));

		y += 24;
		addDrawableChild(toggle(left, y, halfWidth, "Walk", () -> settings.walkTrigger, value -> settings.walkTrigger = value));
		addDrawableChild(toggle(right, y, halfWidth, "Projectiles", () -> settings.projectileTrigger, value -> settings.projectileTrigger = value));

		y += 24;
		addDrawableChild(toggle(left, y, halfWidth, "Elytra Trail", () -> settings.elytraTrigger, value -> settings.elytraTrigger = value));

		y += 32;
		addDrawableChild(slider(left, y, fullWidth, "Attack Amount", 10, 80, () -> settings.attackAmount, value -> settings.attackAmount = (int) Math.round(value), value -> Integer.toString((int) Math.round(value))));
		y += 24;
		addDrawableChild(slider(left, y, fullWidth, "Walk Amount", 0, 60, () -> settings.walkAmount, value -> settings.walkAmount = (int) Math.round(value), value -> Integer.toString((int) Math.round(value))));
		y += 24;
		addDrawableChild(slider(left, y, fullWidth, "Spread", 0.2, 3.0, () -> settings.spread, value -> settings.spread = value.floatValue(), ParticleSettingsScreen::format));
		y += 24;
		addDrawableChild(slider(left, y, fullWidth, "Speed", 0.1, 4.0, () -> settings.speed, value -> settings.speed = value.floatValue(), ParticleSettingsScreen::format));
		y += 24;
		addDrawableChild(slider(left, y, fullWidth, "Life", 0.3, 8.0, () -> settings.lifeTime, value -> settings.lifeTime = value.floatValue(), value -> format(value) + "s"));
		y += 24;
		addDrawableChild(slider(left, y, fullWidth, "Size", 0.2, 2.0, () -> settings.size, value -> settings.size = value.floatValue(), ParticleSettingsScreen::format));
	}

	private void addWorldWidgets(int left, int right, int y, int fullWidth, int halfWidth) {
		addDrawableChild(toggle(left, y, halfWidth, "World Particles", () -> settings.worldParticles, value -> settings.worldParticles = value));
		addDrawableChild(toggle(right, y, halfWidth, "World Physics", () -> settings.worldPhysics, value -> settings.worldPhysics = value));

		y += 28;
		addDrawableChild(button(left, y, halfWidth, () -> "World Mode: " + clean(settings.worldMode.name()), button -> {
			settings.markCustom();
			settings.worldMode = next(settings.worldMode);
			saveSettings();
			rebuildWidgets();
		}));
		addDrawableChild(button(right, y, halfWidth, () -> "Glow: " + clean(settings.glowMode.name()), button -> {
			settings.markCustom();
			settings.glowMode = next(settings.glowMode);
			saveSettings();
			rebuildWidgets();
		}));

		y += 34;
		addDrawableChild(slider(left, y, fullWidth, "World Amount", 10, 500, () -> settings.worldAmount, value -> settings.worldAmount = (int) Math.round(value), value -> Integer.toString((int) Math.round(value))));
		y += 26;
		addDrawableChild(slider(left, y, fullWidth, "World Life", 2.0, 60.0, () -> settings.worldLifeTime, value -> settings.worldLifeTime = value.floatValue(), value -> format(value) + "s"));
		y += 26;
		addDrawableChild(slider(left, y, fullWidth, "World Size", 0.1, 1.5, () -> settings.worldSize, value -> settings.worldSize = value.floatValue(), ParticleSettingsScreen::format));
		y += 26;
		addDrawableChild(slider(left, y, fullWidth, "World Glow", 0.1, 8.0, () -> settings.worldGlowSize, value -> settings.worldGlowSize = value.floatValue(), ParticleSettingsScreen::format));
		y += 26;
		addDrawableChild(slider(left, y, fullWidth, "Event Glow", 0.5, 12.0, () -> settings.glowSize, value -> settings.glowSize = value.floatValue(), ParticleSettingsScreen::format));
	}

	private void cyclePreset() {
		List<PresetOption> options = getPresetOptions();
		String currentKey = currentPresetKey();
		int currentIndex = 0;
		for (int i = 0; i < options.size(); i++) {
			if (options.get(i).key.equals(currentKey)) {
				currentIndex = i;
				break;
			}
		}

		PresetOption next = options.get((currentIndex + 1) % options.size());
		next.apply(settings);
	}

	private List<PresetOption> getPresetOptions() {
		List<PresetOption> options = new ArrayList<>();
		options.add(PresetOption.customState());
		options.add(PresetOption.builtIn(ParticleSystem.Preset.PVP, "PvP"));
		options.add(PresetOption.builtIn(ParticleSystem.Preset.MINIMAL, "Minimal"));
		options.add(PresetOption.builtIn(ParticleSystem.Preset.CINEMATIC, "Cinematic"));

		for (String presetName : ParticleConfig.getCustomPresetNames()) {
			options.add(PresetOption.customPreset(presetName));
		}
		return options;
	}

	private String currentPresetKey() {
		if (hasSelectedCustomPreset()) {
			return "custom:" + settings.customPresetName;
		}

		if (settings.preset == ParticleSystem.Preset.CUSTOM) {
			return "state:custom";
		}

		return "builtin:" + settings.preset.name();
	}

	private String activePresetLabel() {
		if (hasSelectedCustomPreset()) {
			return settings.customPresetName;
		}

		return switch (settings.preset) {
			case CUSTOM -> "Custom";
			case PVP -> "PvP";
			case MINIMAL -> "Minimal";
			case CINEMATIC -> "Cinematic";
		};
	}

	private boolean hasSelectedCustomPreset() {
		return !settings.customPresetName.isBlank() && ParticleConfig.hasCustomPreset(settings.customPresetName);
	}

	private void createPreset(String name) {
		ParticleConfig.saveCustomPreset(name, settings);
		settings.selectCustomPreset(name);
		saveSettings();
	}

	private void saveCurrentPreset() {
		if (!hasSelectedCustomPreset()) {
			return;
		}

		ParticleConfig.saveCustomPreset(settings.customPresetName, settings);
		saveSettings();
	}

	private ButtonWidget toggle(int x, int y, int width, String label, BooleanSupplier getter, Consumer<Boolean> setter) {
		return button(x, y, width, () -> label + ": " + (getter.getAsBoolean() ? "ON" : "OFF"), button -> {
			settings.markCustom();
			setter.accept(!getter.getAsBoolean());
			saveSettings();
			rebuildWidgets();
		});
	}

	private ButtonWidget button(int x, int y, int width, Text label, ButtonWidget.PressAction action) {
		return ButtonWidget.builder(label, action).dimensions(x, y, width, BUTTON_HEIGHT).build();
	}

	private ButtonWidget button(int x, int y, int width, Supplier<String> label, ButtonWidget.PressAction action) {
		return button(x, y, width, Text.literal(label.get()), action);
	}

	private SettingSlider slider(int x, int y, int width, String label, double min, double max, DoubleSupplier getter, Consumer<Double> setter, DoubleFunction<String> formatter) {
		return new SettingSlider(x, y, width, BUTTON_HEIGHT, label, min, max, getter, value -> {
			settings.markCustom();
			setter.accept(value);
			saveSettings();
		}, formatter);
	}

	private Text labelTab(Tab value) {
		String label = value == Tab.EVENTS ? "Event Particles" : "Particle World";
		if (tab == value) {
			return Text.literal("> " + label + " <");
		}
		return Text.literal(label);
	}

	private static Particle3D.ParticleMode next(Particle3D.ParticleMode mode) {
		Particle3D.ParticleMode[] values = Particle3D.ParticleMode.values();
		return values[(mode.ordinal() + 1) % values.length];
	}

	private static Particle3D.GlowMode next(Particle3D.GlowMode mode) {
		Particle3D.GlowMode[] values = Particle3D.GlowMode.values();
		return values[(mode.ordinal() + 1) % values.length];
	}

	private static ParticleSystem.ColorPreset next(ParticleSystem.ColorPreset preset) {
		ParticleSystem.ColorPreset[] values = ParticleSystem.ColorPreset.values();
		return values[(preset.ordinal() + 1) % values.length];
	}

	private static String clean(String value) {
		return value.toLowerCase(Locale.ROOT).replace('_', ' ');
	}

	private void saveSettings() {
		ParticleConfig.save(settings);
	}

	private static String format(double value) {
		return String.format(Locale.ROOT, "%.1f", value);
	}

	private enum Tab {
		EVENTS,
		WORLD
	}

	private record PresetOption(String key, String label, Consumer<ParticleSystem.Settings> applier) {
		private static PresetOption customState() {
			return new PresetOption("state:custom", "Custom", ParticleSystem.Settings::markCustom);
		}

		private static PresetOption builtIn(ParticleSystem.Preset preset, String label) {
			return new PresetOption("builtin:" + preset.name(), label, settings -> settings.applyPreset(preset));
		}

		private static PresetOption customPreset(String name) {
			return new PresetOption("custom:" + name, name, settings -> ParticleConfig.loadCustomPreset(name, settings));
		}

		private void apply(ParticleSystem.Settings settings) {
			applier.accept(settings);
		}
	}

	private static class SettingSlider extends SliderWidget {
		private final String label;
		private final double min;
		private final double max;
		private final Consumer<Double> setter;
		private final DoubleFunction<String> formatter;

		SettingSlider(int x, int y, int width, int height, String label, double min, double max, DoubleSupplier getter, Consumer<Double> setter, DoubleFunction<String> formatter) {
			super(x, y, width, height, Text.empty(), normalize(getter.getAsDouble(), min, max));
			this.label = label;
			this.min = min;
			this.max = max;
			this.setter = setter;
			this.formatter = formatter;
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			setMessage(Text.literal(label + ": " + formatter.apply(value())));
		}

		@Override
		protected void applyValue() {
			setter.accept(value());
		}

		private double value() {
			return min + (max - min) * value;
		}

		private static double normalize(double value, double min, double max) {
			return Math.max(0.0, Math.min(1.0, (value - min) / (max - min)));
		}
	}
}
