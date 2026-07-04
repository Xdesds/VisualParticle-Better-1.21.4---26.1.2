package particle.fx.particle;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class ParticleSettingsScreen extends Screen {
	private static final int BG = 0xE0101018;
	private static final int PANEL = 0xFF1B1D2A;
	private static final int PANEL_DARK = 0xFF141620;
	private static final int ROW = 0xFF222535;
	private static final int ROW_HOVER = 0xFF2B3044;
	private static final int BORDER = 0xFF3A4057;
	private static final int TEXT = 0xFFEDEFF7;
	private static final int MUTED = 0xFFA8AEC1;
	private static final int ACCENT = 0xFF6DA8FF;
	private static final int GOOD = 0xFF63D995;
	private static final int DANGER = 0xFFE86666;
	private static final int W = 560;
	private static final int H = 365;
	private static final int LEFT_W = 116;
	private static final int TOP_H = 34;
	private static final int FOOTER_H = 34;
	private static final int ROW_H = 27;
	private static final int GAP = 6;

	private final Screen parent;
	private final ParticleSystem.Settings settings;
	private final List<Control> controls = new ArrayList<>();

	private Tab tab = Tab.GENERAL;
	private SliderControl activeSlider;
	private float scroll;
	private float maxScroll;
	private int x;
	private int y;
	private int contentX;
	private int contentY;
	private int contentW;
	private int contentH;
	private long lastSaveTime;

	public ParticleSettingsScreen(Screen parent) {
		super(Text.literal("VisualParticle Settings"));
		this.parent = parent;
		this.settings = ParticleSystem.getInstance().getSettings();
	}

	@Override
	protected void init() {
		scroll = 0.0F;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		updateBounds();
		rebuildControls();
		clampScroll();

		context.fill(0, 0, width, height, BG);
		renderFrame(context, mouseX, mouseY);
		renderTabs(context, mouseX, mouseY);
		renderHeader(context);
		renderControls(context, mouseX, mouseY);
		renderFooter(context, mouseX, mouseY);
	}

	private void updateBounds() {
		x = (width - Math.min(W, width - 24)) / 2;
		y = (height - Math.min(H, height - 24)) / 2;
		int frameW = Math.min(W, width - 24);
		int frameH = Math.min(H, height - 24);
		contentX = x + LEFT_W;
		contentY = y + TOP_H;
		contentW = frameW - LEFT_W;
		contentH = frameH - TOP_H - FOOTER_H;
	}

	private int frameW() {
		return Math.min(W, width - 24);
	}

	private int frameH() {
		return Math.min(H, height - 24);
	}

	private void renderFrame(DrawContext context, int mouseX, int mouseY) {
		context.fill(x, y, x + frameW(), y + frameH(), PANEL);
		context.fill(x, y, x + LEFT_W, y + frameH(), PANEL_DARK);
		context.fill(contentX, y, contentX + 1, y + frameH(), BORDER);
		context.fill(contentX, y + TOP_H - 1, x + frameW(), y + TOP_H, BORDER);
		context.fill(contentX, y + frameH() - FOOTER_H, x + frameW(), y + frameH() - FOOTER_H + 1, BORDER);
	}

	private void renderTabs(DrawContext context, int mouseX, int mouseY) {
		drawText(context, "VisualParticle", x + 12, y + 12, TEXT);
		drawText(context, activePresetLabel(), x + 12, y + 24, MUTED);

		int tabY = y + 54;
		for (Tab value : Tab.values()) {
			boolean selected = value == tab;
			boolean hover = hovered(mouseX, mouseY, x + 8, tabY, LEFT_W - 16, 24);
			context.fill(x + 8, tabY, x + LEFT_W - 8, tabY + 24, selected ? ACCENT : hover ? ROW_HOVER : PANEL_DARK);
			drawText(context, value.label, x + 18, tabY + 8, selected ? 0xFFFFFFFF : TEXT);
			tabY += 28;
		}

		context.fill(x + 10, y + frameH() - 58, x + LEFT_W - 10, y + frameH() - 40, settings.enabled ? 0xFF21452F : 0xFF4A2525);
		drawText(context, settings.enabled ? "Enabled" : "Disabled", x + 20, y + frameH() - 53, settings.enabled ? GOOD : DANGER);
		context.fill(x + 10, y + frameH() - 34, x + LEFT_W - 10, y + frameH() - 16, ROW);
		drawText(context, clean(settings.colorPreset.name()), x + 20, y + frameH() - 29, MUTED);
	}

	private void renderHeader(DrawContext context) {
		drawText(context, tab.title, contentX + 12, y + 10, TEXT);
		drawText(context, tab.subtitle, contentX + 12, y + 22, MUTED);
	}

	private void renderControls(DrawContext context, int mouseX, int mouseY) {
		context.enableScissor(contentX, contentY, contentX + contentW, contentY + contentH);
		for (Control control : controls) {
			if (control.y + control.height < contentY - 4 || control.y > contentY + contentH + 4) {
				continue;
			}
			control.render(context, mouseX, mouseY);
		}
		context.disableScissor();

		if (maxScroll > 0.0F) {
			int trackX = contentX + contentW - 8;
			int trackY = contentY + 8;
			int trackH = contentH - 16;
			float progress = MathHelper.clamp(-scroll / maxScroll, 0.0F, 1.0F);
			int thumbH = Math.max(24, (int) (trackH * contentH / (contentH + maxScroll)));
			int thumbY = trackY + (int) ((trackH - thumbH) * progress);
			context.fill(trackX, trackY, trackX + 2, trackY + trackH, BORDER);
			context.fill(trackX - 1, thumbY, trackX + 3, thumbY + thumbH, ACCENT);
		}
	}

	private void renderFooter(DrawContext context, int mouseX, int mouseY) {
		int fy = y + frameH() - FOOTER_H + 7;
		int bx = contentX + 12;
		drawButton(context, mouseX, mouseY, bx, fy, 74, 20, "Save", ACCENT);
		drawButton(context, mouseX, mouseY, bx + 82, fy, 74, 20, "Reset", DANGER);
		drawButton(context, mouseX, mouseY, bx + 164, fy, 74, 20, "Done", GOOD);
		drawText(context, System.currentTimeMillis() - lastSaveTime < 1300L ? "Saved" : "Autosave", contentX + contentW - 65, fy + 6, MUTED);
	}

	private void rebuildControls() {
		controls.clear();
		int cx = contentX + 12;
		int cy = Math.round(contentY + 10 + scroll);
		int cw = contentW - 30;

		if (tab == Tab.GENERAL) {
			cy = addSection(cy, cx, cw, "Main", List.of(
					toggle("Master switch", () -> settings.enabled, value -> settings.enabled = value),
					cycle("Color preset", () -> clean(settings.colorPreset.name()), () -> settings.colorPreset = next(settings.colorPreset)),
					cycle("Particle shape", () -> clean(settings.particleMode.name()), () -> settings.particleMode = next(settings.particleMode)),
					cycle("Glow mode", () -> clean(settings.glowMode.name()), () -> settings.glowMode = next(settings.glowMode))
			));
			cy = addSection(cy, cx, cw, "Tuning", List.of(
					slider("Spread", 0.2, 3.0, () -> settings.spread, value -> settings.spread = value.floatValue(), ParticleSettingsScreen::format),
					slider("Speed", 0.1, 4.0, () -> settings.speed, value -> settings.speed = value.floatValue(), ParticleSettingsScreen::format),
					slider("Life", 0.3, 8.0, () -> settings.lifeTime, value -> settings.lifeTime = value.floatValue(), value -> format(value) + "s"),
					slider("Size", 0.2, 2.0, () -> settings.size, value -> settings.size = value.floatValue(), ParticleSettingsScreen::format),
					slider("Glow", 0.5, 12.0, () -> settings.glowSize, value -> settings.glowSize = value.floatValue(), ParticleSettingsScreen::format)
			));
		} else if (tab == Tab.EVENTS) {
			cy = addSection(cy, cx, cw, "Triggers", List.of(
					toggle("Attack particles", () -> settings.attackTrigger, value -> settings.attackTrigger = value),
					toggle("Totem particles", () -> settings.totemTrigger, value -> settings.totemTrigger = value),
					toggle("Walk particles", () -> settings.walkTrigger, value -> settings.walkTrigger = value),
					toggle("Elytra particles", () -> settings.elytraTrigger, value -> settings.elytraTrigger = value),
					toggle("Projectile particles", () -> settings.projectileTrigger, value -> settings.projectileTrigger = value)
			));
			cy = addSection(cy, cx, cw, "Amounts", List.of(
					slider("Attack amount", 10, 80, () -> settings.attackAmount, value -> settings.attackAmount = (int) Math.round(value), value -> Integer.toString((int) Math.round(value))),
					slider("Walk amount", 0, 60, () -> settings.walkAmount, value -> settings.walkAmount = (int) Math.round(value), value -> Integer.toString((int) Math.round(value)))
			));
		} else if (tab == Tab.WORLD) {
			cy = addSection(cy, cx, cw, "World", List.of(
					toggle("World particles", () -> settings.worldParticles, value -> settings.worldParticles = value),
					toggle("World physics", () -> settings.worldPhysics, value -> settings.worldPhysics = value),
					cycle("World shape", () -> clean(settings.worldMode.name()), () -> settings.worldMode = next(settings.worldMode)),
					slider("World amount", 10, 500, () -> settings.worldAmount, value -> settings.worldAmount = (int) Math.round(value), value -> Integer.toString((int) Math.round(value))),
					slider("World life", 2.0, 60.0, () -> settings.worldLifeTime, value -> settings.worldLifeTime = value.floatValue(), value -> format(value) + "s"),
					slider("World size", 0.1, 1.5, () -> settings.worldSize, value -> settings.worldSize = value.floatValue(), ParticleSettingsScreen::format),
					slider("World glow", 0.1, 8.0, () -> settings.worldGlowSize, value -> settings.worldGlowSize = value.floatValue(), ParticleSettingsScreen::format)
			));
		} else if (tab == Tab.COLORS) {
			cy = addSection(cy, cx, cw, "Theme", List.of(
					color("GUI primary", () -> settings.uiPrimaryColor, value -> settings.uiPrimaryColor = value),
					color("GUI secondary", () -> settings.uiSecondaryColor, value -> settings.uiSecondaryColor = value),
					toggle("Custom particle colors", () -> settings.customColors, value -> settings.customColors = value),
					toggle("Animated gradients", () -> settings.animatedGradient, value -> settings.animatedGradient = value)
			));
			cy = addSection(cy, cx, cw, "Particle colors", List.of(
					color("Attack primary", () -> settings.attackColor, value -> settings.attackColor = value),
					color("Attack gradient", () -> settings.attackSecondColor, value -> settings.attackSecondColor = value),
					color("Move primary", () -> settings.moveColor, value -> settings.moveColor = value),
					color("Move gradient", () -> settings.moveSecondColor, value -> settings.moveSecondColor = value),
					color("Projectile primary", () -> settings.projectileColor, value -> settings.projectileColor = value),
					color("Projectile gradient", () -> settings.projectileSecondColor, value -> settings.projectileSecondColor = value),
					color("Elytra primary", () -> settings.elytraColor, value -> settings.elytraColor = value),
					color("Elytra gradient", () -> settings.elytraSecondColor, value -> settings.elytraSecondColor = value),
					color("World primary", () -> settings.worldColor, value -> settings.worldColor = value),
					color("World gradient", () -> settings.worldSecondColor, value -> settings.worldSecondColor = value),
					color("Totem primary", () -> settings.totemColor, value -> settings.totemColor = value),
					color("Totem gradient", () -> settings.totemSecondColor, value -> settings.totemSecondColor = value)
			));
		} else {
			cy = addSection(cy, cx, cw, "Presets", presetControls());
		}

		maxScroll = Math.max(0.0F, cy - Math.round(contentY + 10 + scroll) + 12 - contentH);
	}

	private int addSection(int y, int x, int width, String title, List<Control> sectionControls) {
		SectionHeader header = new SectionHeader(title);
		header.setBounds(x, y, width, 20);
		controls.add(header);
		y += 24;
		for (Control control : sectionControls) {
			control.setBounds(x, y, width, ROW_H);
			controls.add(control);
			y += ROW_H + GAP;
		}
		return y + 8;
	}

	private List<Control> presetControls() {
		List<Control> result = new ArrayList<>();
		result.add(cycle("Active preset", this::activePresetLabel, this::cyclePreset));
		result.add(button("Create preset", this::openPresetNameScreen, ACCENT));
		result.add(button("Save selected custom", () -> {
			saveCurrentPreset();
			lastSaveTime = System.currentTimeMillis();
		}, GOOD, this::hasSelectedCustomPreset));
		result.add(button("PvP preset", () -> applyPreset(ParticleSystem.Preset.PVP), DANGER));
		result.add(button("Minimal preset", () -> applyPreset(ParticleSystem.Preset.MINIMAL), GOOD));
		result.add(button("Cinematic preset", () -> applyPreset(ParticleSystem.Preset.CINEMATIC), ACCENT));
		for (String presetName : ParticleConfig.getCustomPresetNames()) {
			result.add(button(presetName, () -> {
				ParticleConfig.loadCustomPreset(presetName, settings);
				saveSettings();
			}, ACCENT));
		}
		return result;
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		double mouseX = click.x();
		double mouseY = click.y();
		if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			return super.mouseClicked(click, doubled);
		}

		int tabY = y + 54;
		for (Tab value : Tab.values()) {
			if (hovered(mouseX, mouseY, x + 8, tabY, LEFT_W - 16, 24)) {
				tab = value;
				scroll = 0.0F;
				return true;
			}
			tabY += 28;
		}

		int fy = y + frameH() - FOOTER_H + 7;
		int bx = contentX + 12;
		if (hovered(mouseX, mouseY, bx, fy, 74, 20)) {
			saveSettings();
			lastSaveTime = System.currentTimeMillis();
			return true;
		}
		if (hovered(mouseX, mouseY, bx + 82, fy, 74, 20)) {
			settings.reset();
			saveSettings();
			scroll = 0.0F;
			return true;
		}
		if (hovered(mouseX, mouseY, bx + 164, fy, 74, 20)) {
			close();
			return true;
		}

		if (hovered(mouseX, mouseY, contentX, contentY, contentW, contentH)) {
			for (Control control : controls) {
				if (control.click(mouseX, mouseY)) {
					return true;
				}
			}
		}

		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseReleased(Click click) {
		activeSlider = null;
		return super.mouseReleased(click);
	}

	@Override
	public boolean mouseDragged(Click click, double offsetX, double offsetY) {
		if (activeSlider != null) {
			activeSlider.setFromMouse((float) click.x());
			return true;
		}
		return super.mouseDragged(click, offsetX, offsetY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (hovered(mouseX, mouseY, contentX, contentY, contentW, contentH)) {
			scroll += (float) verticalAmount * 22.0F;
			clampScroll();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		boolean controlDown = (input.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0;
		if (controlDown && input.key() == GLFW.GLFW_KEY_S) {
			saveSettings();
			lastSaveTime = System.currentTimeMillis();
			return true;
		}
		if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
			close();
			return true;
		}
		return super.keyPressed(input);
	}

	@Override
	public void close() {
		if (client != null) {
			client.setScreen(parent);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	private ToggleControl toggle(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
		return new ToggleControl(label, getter, value -> {
			settings.markCustom();
			setter.accept(value);
			saveSettings();
		});
	}

	private CycleControl cycle(String label, Supplier<String> getter, Runnable action) {
		return new CycleControl(label, getter, () -> {
			settings.markCustom();
			action.run();
			saveSettings();
		});
	}

	private SliderControl slider(String label, double min, double max, DoubleSupplier getter, Consumer<Double> setter, DoubleFunction<String> formatter) {
		return new SliderControl(label, min, max, getter, value -> {
			settings.markCustom();
			setter.accept(value);
			saveSettings();
		}, formatter);
	}

	private ColorControl color(String label, IntSupplier getter, Consumer<Integer> setter) {
		return new ColorControl(label, getter, value -> {
			settings.markCustom();
			setter.accept(value);
			saveSettings();
		});
	}

	private ButtonControl button(String label, Runnable action, int color) {
		return button(label, action, color, () -> true);
	}

	private ButtonControl button(String label, Runnable action, int color, BooleanSupplier enabled) {
		return new ButtonControl(label, action, color, enabled);
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
		options.get((currentIndex + 1) % options.size()).apply(settings);
		saveSettings();
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

	private void openPresetNameScreen() {
		if (client != null) {
			client.setScreen(new PresetNameScreen(this, "Create Preset", "Create", this::createPreset));
		}
	}

	private void createPreset(String name) {
		ParticleConfig.saveCustomPreset(name, settings);
		settings.selectCustomPreset(name);
		saveSettings();
	}

	private void saveCurrentPreset() {
		if (hasSelectedCustomPreset()) {
			ParticleConfig.saveCustomPreset(settings.customPresetName, settings);
			saveSettings();
		}
	}

	private void applyPreset(ParticleSystem.Preset preset) {
		settings.applyPreset(preset);
		saveSettings();
	}

	private String currentPresetKey() {
		if (hasSelectedCustomPreset()) {
			return "custom:" + settings.customPresetName;
		}
		return settings.preset == ParticleSystem.Preset.CUSTOM ? "state:custom" : "builtin:" + settings.preset.name();
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

	private void saveSettings() {
		ParticleConfig.save(settings);
	}

	private void clampScroll() {
		scroll = MathHelper.clamp(scroll, -maxScroll, 0.0F);
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

	private static boolean hovered(double mouseX, double mouseY, int x, int y, int w, int h) {
		return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
	}

	private void drawText(DrawContext context, String text, int x, int y, int color) {
		context.drawText(client.textRenderer, text, x, y, color, false);
	}

	private void drawButton(DrawContext context, int mouseX, int mouseY, int x, int y, int w, int h, String text, int color) {
		boolean hover = hovered(mouseX, mouseY, x, y, w, h);
		context.fill(x, y, x + w, y + h, hover ? color : ROW);
		context.drawCenteredTextWithShadow(client.textRenderer, text, x + w / 2, y + 6, TEXT);
	}

	private enum Tab {
		GENERAL("General", "Main particle options"),
		EVENTS("Events", "Combat and movement triggers"),
		WORLD("World", "Ambient world particles"),
		COLORS("Colors", "Theme and particle colors"),
		PRESETS("Presets", "Save and load settings");

		private final String label;
		private final String title;
		private final String subtitle;

		Tab(String label, String subtitle) {
			this.label = label;
			this.title = label;
			this.subtitle = subtitle;
		}
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

	private abstract static class Control {
		protected int x;
		protected int y;
		protected int width;
		protected int height;
		protected final String label;

		protected Control(String label) {
			this.label = label;
		}

		protected void setBounds(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		protected abstract void render(DrawContext context, int mouseX, int mouseY);

		protected boolean click(double mouseX, double mouseY) {
			return false;
		}
	}

	private final class SectionHeader extends Control {
		private SectionHeader(String label) {
			super(label);
		}

		@Override
		protected void render(DrawContext context, int mouseX, int mouseY) {
			drawText(context, label, x, y + 6, MUTED);
			context.fill(x, y + height - 1, x + width, y + height, BORDER);
		}
	}

	private abstract class RowControl extends Control {
		protected RowControl(String label) {
			super(label);
		}

		protected void renderRow(DrawContext context, int mouseX, int mouseY) {
			context.fill(x, y, x + width, y + height, hovered(mouseX, mouseY, x, y, width, height) ? ROW_HOVER : ROW);
			drawText(context, label, x + 8, y + 9, TEXT);
		}
	}

	private final class ToggleControl extends RowControl {
		private final BooleanSupplier getter;
		private final Consumer<Boolean> setter;

		private ToggleControl(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
			super(label);
			this.getter = getter;
			this.setter = setter;
		}

		@Override
		protected void render(DrawContext context, int mouseX, int mouseY) {
			renderRow(context, mouseX, mouseY);
			int bx = x + width - 48;
			int by = y + 6;
			context.fill(bx, by, bx + 36, by + 15, getter.getAsBoolean() ? GOOD : BORDER);
			context.fill(bx + (getter.getAsBoolean() ? 22 : 2), by + 2, bx + (getter.getAsBoolean() ? 34 : 14), by + 13, 0xFFFFFFFF);
		}

		@Override
		protected boolean click(double mouseX, double mouseY) {
			if (!hovered(mouseX, mouseY, x, y, width, height)) {
				return false;
			}
			setter.accept(!getter.getAsBoolean());
			return true;
		}
	}

	private final class CycleControl extends RowControl {
		private final Supplier<String> getter;
		private final Runnable action;

		private CycleControl(String label, Supplier<String> getter, Runnable action) {
			super(label);
			this.getter = getter;
			this.action = action;
		}

		@Override
		protected void render(DrawContext context, int mouseX, int mouseY) {
			renderRow(context, mouseX, mouseY);
			String value = getter.get();
			int valueW = Math.max(72, client.textRenderer.getWidth(value) + 18);
			context.fill(x + width - valueW - 8, y + 5, x + width - 8, y + height - 5, ACCENT);
			context.drawCenteredTextWithShadow(client.textRenderer, value, x + width - valueW / 2 - 8, y + 9, 0xFFFFFFFF);
		}

		@Override
		protected boolean click(double mouseX, double mouseY) {
			if (!hovered(mouseX, mouseY, x, y, width, height)) {
				return false;
			}
			action.run();
			return true;
		}
	}

	private final class SliderControl extends RowControl {
		private final double min;
		private final double max;
		private final DoubleSupplier getter;
		private final Consumer<Double> setter;
		private final DoubleFunction<String> formatter;

		private SliderControl(String label, double min, double max, DoubleSupplier getter, Consumer<Double> setter, DoubleFunction<String> formatter) {
			super(label);
			this.min = min;
			this.max = max;
			this.getter = getter;
			this.setter = setter;
			this.formatter = formatter;
		}

		@Override
		protected void render(DrawContext context, int mouseX, int mouseY) {
			renderRow(context, mouseX, mouseY);
			String value = formatter.apply(getter.getAsDouble());
			drawText(context, value, x + width - client.textRenderer.getWidth(value) - 8, y + 9, MUTED);
			int barX = x + 140;
			int barY = y + 19;
			int barW = Math.max(60, width - 205);
			float progress = (float) ((getter.getAsDouble() - min) / (max - min));
			progress = MathHelper.clamp(progress, 0.0F, 1.0F);
			context.fill(barX, barY, barX + barW, barY + 3, BORDER);
			context.fill(barX, barY, barX + Math.round(barW * progress), barY + 3, ACCENT);
			context.fill(barX + Math.round(barW * progress) - 3, barY - 3, barX + Math.round(barW * progress) + 4, barY + 6, 0xFFFFFFFF);
		}

		@Override
		protected boolean click(double mouseX, double mouseY) {
			if (!hovered(mouseX, mouseY, x, y, width, height)) {
				return false;
			}
			activeSlider = this;
			setFromMouse((float) mouseX);
			return true;
		}

		private void setFromMouse(float mouseX) {
			int barX = x + 140;
			int barW = Math.max(60, width - 205);
			float progress = MathHelper.clamp((mouseX - barX) / barW, 0.0F, 1.0F);
			setter.accept(min + (max - min) * progress);
		}
	}

	private final class ColorControl extends RowControl {
		private final IntSupplier getter;
		private final Consumer<Integer> setter;

		private ColorControl(String label, IntSupplier getter, Consumer<Integer> setter) {
			super(label);
			this.getter = getter;
			this.setter = setter;
		}

		@Override
		protected void render(DrawContext context, int mouseX, int mouseY) {
			renderRow(context, mouseX, mouseY);
			int color = getter.getAsInt();
			int bx = x + width - 66;
			context.fill(bx, y + 6, bx + 46, y + height - 6, 0xFF000000);
			context.fill(bx + 1, y + 7, bx + 45, y + height - 7, color);
		}

		@Override
		protected boolean click(double mouseX, double mouseY) {
			if (!hovered(mouseX, mouseY, x, y, width, height)) {
				return false;
			}
			setter.accept(shiftHue(getter.getAsInt(), 0.08F).getRGB());
			return true;
		}
	}

	private final class ButtonControl extends RowControl {
		private final Runnable action;
		private final int color;
		private final BooleanSupplier enabled;

		private ButtonControl(String label, Runnable action, int color, BooleanSupplier enabled) {
			super(label);
			this.action = action;
			this.color = color;
			this.enabled = enabled;
		}

		@Override
		protected void render(DrawContext context, int mouseX, int mouseY) {
			renderRow(context, mouseX, mouseY);
			int bx = x + width - 78;
			context.fill(bx, y + 5, bx + 68, y + height - 5, enabled.getAsBoolean() ? color : BORDER);
			context.drawCenteredTextWithShadow(client.textRenderer, "Apply", bx + 34, y + 9, 0xFFFFFFFF);
		}

		@Override
		protected boolean click(double mouseX, double mouseY) {
			if (!enabled.getAsBoolean() || !hovered(mouseX, mouseY, x, y, width, height)) {
				return false;
			}
			action.run();
			return true;
		}
	}

	private static Color shiftHue(int argb, float amount) {
		Color color = new Color(argb, true);
		float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
		hsb[0] = (hsb[0] + amount) % 1.0F;
		Color shifted = new Color(Color.HSBtoRGB(hsb[0], Math.max(0.25F, hsb[1]), Math.max(0.35F, hsb[2])));
		return new Color(shifted.getRed(), shifted.getGreen(), shifted.getBlue(), color.getAlpha());
	}
}
