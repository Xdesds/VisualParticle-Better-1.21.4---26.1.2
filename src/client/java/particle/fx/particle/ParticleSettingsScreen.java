package particle.fx.particle;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class ParticleSettingsScreen extends Screen {
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_WIDTH = 150;
	private static final int GAP = 8;

	private final Screen parent;
	private final ParticleSystem.Settings settings;
	private Tab tab = Tab.EVENTS;

	public ParticleSettingsScreen(Screen parent) {
		super(Component.literal("Visual Particle Better Settings"));
		this.parent = parent;
		this.settings = ParticleSystem.getInstance().getSettings();
	}

	@Override
	protected void init() {
		rebuildWidgets();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, width, height, 0x66000000);
		context.centeredText(font, title, width / 2, 16, 0xFFFFFF);
		super.extractRenderState(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		if (minecraft != null) {
			minecraft.setScreen(parent);
		}
	}

	protected void rebuildWidgets() {
		clearWidgets();

		int fullWidth = Math.min(320, width - 40);
		int left = (width - fullWidth) / 2;
		int right = left + fullWidth / 2 + GAP / 2;
		int halfWidth = (fullWidth - GAP) / 2;
		int y = 40;

		addRenderableWidget(button(left, y, halfWidth, labelTab(Tab.EVENTS), button -> {
			tab = Tab.EVENTS;
			rebuildWidgets();
		}));
		addRenderableWidget(button(right, y, halfWidth, labelTab(Tab.WORLD), button -> {
			tab = Tab.WORLD;
			rebuildWidgets();
		}));

		y += 26;
		addRenderableWidget(toggle(left, y, halfWidth, "Enabled", () -> settings.enabled, value -> settings.enabled = value));
		addRenderableWidget(button(right, y, halfWidth, () -> "Color: " + clean(settings.colorPreset.name()), button -> {
			settings.colorPreset = next(settings.colorPreset);
			button.setMessage(Component.literal("Color: " + clean(settings.colorPreset.name())));
		}));

		y += 28;
		if (tab == Tab.EVENTS) {
			addEventWidgets(left, right, y, fullWidth, halfWidth);
		} else {
			addWorldWidgets(left, right, y, fullWidth, halfWidth);
		}

		int bottomY = height - 28;
		addRenderableWidget(button(width / 2 - BUTTON_WIDTH - 4, bottomY, BUTTON_WIDTH, Component.literal("Reset"), button -> {
			settings.reset();
			rebuildWidgets();
		}));
		addRenderableWidget(button(width / 2 + 4, bottomY, BUTTON_WIDTH, Component.literal("Done"), button -> onClose()));
	}

	private void addEventWidgets(int left, int right, int y, int fullWidth, int halfWidth) {
		addRenderableWidget(button(left, y, halfWidth, () -> "Mode: " + clean(settings.particleMode.name()), button -> {
			settings.particleMode = next(settings.particleMode);
			button.setMessage(Component.literal("Mode: " + clean(settings.particleMode.name())));
		}));
		addRenderableWidget(button(right, y, halfWidth, () -> "Glow: " + clean(settings.glowMode.name()), button -> {
			settings.glowMode = next(settings.glowMode);
			button.setMessage(Component.literal("Glow: " + clean(settings.glowMode.name())));
		}));

		y += 26;
		addRenderableWidget(toggle(left, y, halfWidth, "Attack", () -> settings.attackTrigger, value -> settings.attackTrigger = value));
		addRenderableWidget(toggle(right, y, halfWidth, "Totem", () -> settings.totemTrigger, value -> settings.totemTrigger = value));

		y += 24;
		addRenderableWidget(toggle(left, y, halfWidth, "Walk", () -> settings.walkTrigger, value -> settings.walkTrigger = value));
		addRenderableWidget(toggle(right, y, halfWidth, "Projectiles", () -> settings.projectileTrigger, value -> settings.projectileTrigger = value));

		y += 32;
		addRenderableWidget(slider(left, y, fullWidth, "Attack Amount", 10, 80, () -> settings.attackAmount, value -> settings.attackAmount = (int) Math.round(value), value -> Integer.toString((int) Math.round(value))));
		y += 24;
		addRenderableWidget(slider(left, y, fullWidth, "Walk Amount", 0, 60, () -> settings.walkAmount, value -> settings.walkAmount = (int) Math.round(value), value -> Integer.toString((int) Math.round(value))));
		y += 24;
		addRenderableWidget(slider(left, y, fullWidth, "Spread", 0.2, 3.0, () -> settings.spread, value -> settings.spread = value.floatValue(), ParticleSettingsScreen::format));
		y += 24;
		addRenderableWidget(slider(left, y, fullWidth, "Speed", 0.1, 4.0, () -> settings.speed, value -> settings.speed = value.floatValue(), ParticleSettingsScreen::format));
		y += 24;
		addRenderableWidget(slider(left, y, fullWidth, "Life", 0.3, 8.0, () -> settings.lifeTime, value -> settings.lifeTime = value.floatValue(), value -> format(value) + "s"));
		y += 24;
		addRenderableWidget(slider(left, y, fullWidth, "Size", 0.2, 2.0, () -> settings.size, value -> settings.size = value.floatValue(), ParticleSettingsScreen::format));
	}

	private void addWorldWidgets(int left, int right, int y, int fullWidth, int halfWidth) {
		addRenderableWidget(toggle(left, y, halfWidth, "World Particles", () -> settings.worldParticles, value -> settings.worldParticles = value));
		addRenderableWidget(toggle(right, y, halfWidth, "World Physics", () -> settings.worldPhysics, value -> settings.worldPhysics = value));

		y += 28;
		addRenderableWidget(button(left, y, halfWidth, () -> "World Mode: " + clean(settings.worldMode.name()), button -> {
			settings.worldMode = next(settings.worldMode);
			button.setMessage(Component.literal("World Mode: " + clean(settings.worldMode.name())));
		}));
		addRenderableWidget(button(right, y, halfWidth, () -> "Glow: " + clean(settings.glowMode.name()), button -> {
			settings.glowMode = next(settings.glowMode);
			button.setMessage(Component.literal("Glow: " + clean(settings.glowMode.name())));
		}));

		y += 34;
		addRenderableWidget(slider(left, y, fullWidth, "World Amount", 10, 500, () -> settings.worldAmount, value -> settings.worldAmount = (int) Math.round(value), value -> Integer.toString((int) Math.round(value))));
		y += 26;
		addRenderableWidget(slider(left, y, fullWidth, "World Life", 2.0, 60.0, () -> settings.worldLifeTime, value -> settings.worldLifeTime = value.floatValue(), value -> format(value) + "s"));
		y += 26;
		addRenderableWidget(slider(left, y, fullWidth, "World Size", 0.1, 1.5, () -> settings.worldSize, value -> settings.worldSize = value.floatValue(), ParticleSettingsScreen::format));
		y += 26;
		addRenderableWidget(slider(left, y, fullWidth, "World Glow", 0.1, 8.0, () -> settings.worldGlowSize, value -> settings.worldGlowSize = value.floatValue(), ParticleSettingsScreen::format));
		y += 26;
		addRenderableWidget(slider(left, y, fullWidth, "Event Glow", 0.5, 12.0, () -> settings.glowSize, value -> settings.glowSize = value.floatValue(), ParticleSettingsScreen::format));
	}

	private Button toggle(int x, int y, int width, String label, BooleanSupplier getter, Consumer<Boolean> setter) {
		return button(x, y, width, () -> label + ": " + (getter.getAsBoolean() ? "ON" : "OFF"), button -> {
			setter.accept(!getter.getAsBoolean());
			button.setMessage(Component.literal(label + ": " + (getter.getAsBoolean() ? "ON" : "OFF")));
		});
	}

	private Button button(int x, int y, int width, Component label, Button.OnPress action) {
		return Button.builder(label, action).bounds(x, y, width, BUTTON_HEIGHT).build();
	}

	private Button button(int x, int y, int width, Supplier<String> label, Button.OnPress action) {
		return button(x, y, width, Component.literal(label.get()), action);
	}

	private SettingSlider slider(int x, int y, int width, String label, double min, double max, DoubleSupplier getter, Consumer<Double> setter, DoubleFunction<String> formatter) {
		return new SettingSlider(x, y, width, BUTTON_HEIGHT, label, min, max, getter, setter, formatter);
	}

	private Component labelTab(Tab value) {
		String label = value == Tab.EVENTS ? "Event Particles" : "Particle World";
		if (tab == value) {
			return Component.literal("> " + label + " <");
		}
		return Component.literal(label);
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

	private static String format(double value) {
		return String.format(Locale.ROOT, "%.1f", value);
	}

	private enum Tab {
		EVENTS,
		WORLD
	}

	private static class SettingSlider extends AbstractSliderButton {
		private final String label;
		private final double min;
		private final double max;
		private final Consumer<Double> setter;
		private final DoubleFunction<String> formatter;

		SettingSlider(int x, int y, int width, int height, String label, double min, double max, DoubleSupplier getter, Consumer<Double> setter, DoubleFunction<String> formatter) {
			super(x, y, width, height, Component.empty(), normalize(getter.getAsDouble(), min, max));
			this.label = label;
			this.min = min;
			this.max = max;
			this.setter = setter;
			this.formatter = formatter;
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.literal(label + ": " + formatter.apply(value())));
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
