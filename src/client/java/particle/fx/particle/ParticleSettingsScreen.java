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
	private static final int WINDOW_W = 420;
	private static final int WINDOW_H = 274;
	private static final int SIDEBAR_W = 96;
	private static final int TOP_H = 30;
	private static final int FOOTER_H = 28;
	private static final int PAD = 8;
	private static final int SETTING_H = 18;
	private static final int SETTING_GAP = 4;

	private static final int TEXT = 0xFFE9ECF5;
	private static final int MUTED = 0xFF9096A8;
	private static final int DIM = 0xFF626A7E;
	private static final int GOOD = 0xFF73E2A7;
	private static final int DANGER = 0xFFFF6C77;

	private final Screen parent;
	private final ParticleSystem.Settings settings;
	private final List<Control> controls = new ArrayList<>();

	private Tab tab = Tab.GENERAL;
	private SliderControl activeSlider;
	private String openDropdown;
	private String openColor;
	private ColorDrag colorDrag = ColorDrag.NONE;
	private float scroll;
	private float maxScroll;
	private int x;
	private int y;
	private int windowW;
	private int windowH;
	private int contentX;
	private int contentY;
	private int contentW;
	private int contentH;
	private long lastSaveTime;

	public ParticleSettingsScreen(Screen parent) {
		super(Text.literal("VisualParticle"));
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

		context.fill(0, 0, width, height, 0xA8000000);
		renderWindow(context, mouseX, mouseY);
		renderSidebar(context, mouseX, mouseY);
		renderTopBar(context);
		renderControls(context, mouseX, mouseY);
		renderFooter(context, mouseX, mouseY);
		renderDropdownOverlay(context, mouseX, mouseY);
	}

	private void updateBounds() {
		windowW = Math.min(WINDOW_W, width - 12);
		windowH = Math.min(WINDOW_H, height - 12);
		x = (width - windowW) / 2;
		y = (height - windowH) / 2;
		contentX = x + SIDEBAR_W;
		contentY = y + TOP_H;
		contentW = windowW - SIDEBAR_W;
		contentH = windowH - TOP_H - FOOTER_H;
	}

	private void renderWindow(DrawContext context, int mouseX, int mouseY) {
		UiRender2D.blurredBackdrop(context, x, y, windowW, windowH, 7, 0xF008090D);
		UiRender2D.roundedRect(context, x, y, SIDEBAR_W, windowH, 7, 0xF00B0C11);
		UiRender2D.roundedRect(context, contentX, y + TOP_H, contentW, contentH + FOOTER_H, 0, 0xD506070A);
		UiRender2D.roundedRect(context, contentX, y, contentW, TOP_H + 1, 0, 0xEA090A0F);
		UiRender2D.border(context, x, y, windowW, windowH, 7, UiRender2D.withAlpha(primaryColor(), 70));
		context.fill(contentX, y + 8, contentX + 1, y + windowH - 8, 0x1EFFFFFF);
		context.fill(contentX + 8, y + TOP_H, x + windowW - 8, y + TOP_H + 1, 0x16FFFFFF);
		context.fill(contentX + 8, y + windowH - FOOTER_H, x + windowW - 8, y + windowH - FOOTER_H + 1, 0x16FFFFFF);
	}

	private void renderSidebar(DrawContext context, int mouseX, int mouseY) {
		drawText(context, "Visual", x + 12, y + 12, TEXT);
		drawText(context, "Particle", x + 48, y + 12, primaryColor());
		drawText(context, activePresetLabel(), x + 12, y + 23, MUTED);

		int tabY = y + 48;
		for (Tab value : Tab.values()) {
			boolean selected = value == tab;
			boolean hover = hovered(mouseX, mouseY, x + 8, tabY, SIDEBAR_W - 16, 19);
			int fill = selected ? UiRender2D.withAlpha(primaryColor(), 210) : hover ? 0xFF161922 : 0x00000000;
			if (selected) {
				UiRender2D.gradientRoundedRect(context, x + 8, tabY, SIDEBAR_W - 16, 19, 5, primaryColor(), secondaryColor());
			} else if (hover) {
				UiRender2D.roundedRect(context, x + 8, tabY, SIDEBAR_W - 16, 19, 5, fill);
			}
			drawText(context, value.label, x + 17, tabY + 6, selected ? 0xFFFFFFFF : MUTED);
			tabY += 22;
		}

		int statusY = y + windowH - 50;
		UiRender2D.pill(context, x + 10, statusY, SIDEBAR_W - 20, 17, settings.enabled ? 0x88215F42 : 0x884A252A);
		drawText(context, settings.enabled ? "Enabled" : "Disabled", x + 20, statusY + 5, settings.enabled ? GOOD : DANGER);
		UiRender2D.pill(context, x + 10, statusY + 22, SIDEBAR_W - 20, 17, 0xAA11131A);
		drawText(context, clean(settings.colorPreset.name()), x + 18, statusY + 27, DIM);
	}

	private void renderTopBar(DrawContext context) {
		drawText(context, tab.title, contentX + 10, y + 8, TEXT);
		drawText(context, tab.subtitle, contentX + 10 + client.textRenderer.getWidth(tab.title) + 8, y + 9, MUTED);
		int chipW = Math.max(58, client.textRenderer.getWidth(settings.glowMode.name()) + 16);
		UiRender2D.pill(context, x + windowW - chipW - 10, y + 8, chipW, 15, 0x99151720);
		context.drawCenteredTextWithShadow(client.textRenderer, clean(settings.glowMode.name()), x + windowW - chipW / 2 - 10, y + 12, DIM);
	}

	private void renderControls(DrawContext context, int mouseX, int mouseY) {
		context.enableScissor(contentX, contentY, contentX + contentW, contentY + contentH);
		for (Control control : controls) {
			if (control.y + control.height < contentY - 6 || control.y > contentY + contentH + 6) {
				continue;
			}
			control.render(context, mouseX, mouseY);
		}
		context.disableScissor();

		if (maxScroll > 0.0F) {
			int trackX = contentX + contentW - 7;
			int trackY = contentY + 8;
			int trackH = contentH - 16;
			float progress = MathHelper.clamp(-scroll / maxScroll, 0.0F, 1.0F);
			int thumbH = Math.max(22, (int) (trackH * contentH / (contentH + maxScroll)));
			int thumbY = trackY + (int) ((trackH - thumbH) * progress);
			UiRender2D.pill(context, trackX, trackY, 2, trackH, 0x22FFFFFF);
			UiRender2D.pill(context, trackX - 1, thumbY, 4, thumbH, primaryColor());
		}
	}

	private void renderFooter(DrawContext context, int mouseX, int mouseY) {
		int fy = y + windowH - FOOTER_H + 6;
		int bx = contentX + 10;
		drawFooterButton(context, mouseX, mouseY, bx, fy, 58, 17, "Save", primaryColor());
		drawFooterButton(context, mouseX, mouseY, bx + 64, fy, 58, 17, "Reset", DANGER);
		drawFooterButton(context, mouseX, mouseY, bx + 128, fy, 58, 17, "Done", GOOD);
		drawText(context, System.currentTimeMillis() - lastSaveTime < 1300L ? "saved" : "autosave", x + windowW - 58, fy + 5, DIM);
	}

	private void rebuildControls() {
		controls.clear();
		int cx = contentX + PAD;
		int cy = Math.round(contentY + PAD + scroll);
		int cw = contentW - PAD * 2 - 6;

		if (tab == Tab.GENERAL) {
			cy = addCard(cy, cx, cw, "Main", List.of(
					toggle("Master switch", () -> settings.enabled, value -> settings.enabled = value),
					mode("Color preset", () -> clean(settings.colorPreset.name()), colorPresetOptions()),
					mode("Particle shape", () -> clean(settings.particleMode.name()), particleModeOptions(false)),
					mode("Glow mode", () -> clean(settings.glowMode.name()), glowModeOptions())
			));
			cy = addCard(cy, cx, cw, "Tuning", List.of(
					slider("Spread", 0.2, 3.0, () -> settings.spread, value -> settings.spread = value.floatValue(), ParticleSettingsScreen::format),
					slider("Speed", 0.1, 4.0, () -> settings.speed, value -> settings.speed = value.floatValue(), ParticleSettingsScreen::format),
					slider("Life", 0.3, 8.0, () -> settings.lifeTime, value -> settings.lifeTime = value.floatValue(), value -> format(value) + "s"),
					slider("Size", 0.2, 2.0, () -> settings.size, value -> settings.size = value.floatValue(), ParticleSettingsScreen::format),
					slider("Glow", 0.5, 12.0, () -> settings.glowSize, value -> settings.glowSize = value.floatValue(), ParticleSettingsScreen::format)
			));
		} else if (tab == Tab.EVENTS) {
			cy = addCard(cy, cx, cw, "Triggers", List.of(
					toggle("Attack particles", () -> settings.attackTrigger, value -> settings.attackTrigger = value),
					toggle("Totem particles", () -> settings.totemTrigger, value -> settings.totemTrigger = value),
					toggle("Walk particles", () -> settings.walkTrigger, value -> settings.walkTrigger = value),
					toggle("Elytra particles", () -> settings.elytraTrigger, value -> settings.elytraTrigger = value),
					toggle("Projectile particles", () -> settings.projectileTrigger, value -> settings.projectileTrigger = value)
			));
			cy = addCard(cy, cx, cw, "Amounts", List.of(
					slider("Attack amount", 10, 80, () -> settings.attackAmount, value -> settings.attackAmount = (int) Math.round(value), value -> Integer.toString((int) Math.round(value))),
					slider("Walk amount", 0, 60, () -> settings.walkAmount, value -> settings.walkAmount = (int) Math.round(value), value -> Integer.toString((int) Math.round(value)))
			));
		} else if (tab == Tab.WORLD) {
			cy = addCard(cy, cx, cw, "World", List.of(
					toggle("World particles", () -> settings.worldParticles, value -> settings.worldParticles = value),
					toggle("World physics", () -> settings.worldPhysics, value -> settings.worldPhysics = value),
					mode("World shape", () -> clean(settings.worldMode.name()), particleModeOptions(true)),
					slider("World amount", 10, 500, () -> settings.worldAmount, value -> settings.worldAmount = (int) Math.round(value), value -> Integer.toString((int) Math.round(value))),
					slider("World life", 2.0, 60.0, () -> settings.worldLifeTime, value -> settings.worldLifeTime = value.floatValue(), value -> format(value) + "s"),
					slider("World size", 0.1, 1.5, () -> settings.worldSize, value -> settings.worldSize = value.floatValue(), ParticleSettingsScreen::format),
					slider("World glow", 0.1, 8.0, () -> settings.worldGlowSize, value -> settings.worldGlowSize = value.floatValue(), ParticleSettingsScreen::format)
			));
		} else if (tab == Tab.COLORS) {
			cy = addCard(cy, cx, cw, "Theme", List.of(
					color("GUI primary", () -> settings.uiPrimaryColor, value -> settings.uiPrimaryColor = value),
					color("GUI secondary", () -> settings.uiSecondaryColor, value -> settings.uiSecondaryColor = value),
					toggle("Custom particle colors", () -> settings.customColors, value -> settings.customColors = value),
					toggle("Animated gradients", () -> settings.animatedGradient, value -> settings.animatedGradient = value)
			));
			cy = addCard(cy, cx, cw, "Particle colors", List.of(
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
			cy = addCard(cy, cx, cw, "Presets", presetControls());
		}

		maxScroll = Math.max(0.0F, cy - Math.round(contentY + PAD + scroll) + PAD - contentH);
	}

	private int addCard(int y, int x, int width, String title, List<Control> cardControls) {
		int controlsHeight = 0;
		for (Control control : cardControls) {
			controlsHeight += control.desiredHeight();
		}
		int cardH = 20 + controlsHeight + Math.max(0, cardControls.size() - 1) * SETTING_GAP + 10;
		CardControl card = new CardControl(title);
		card.setBounds(x, y, width, cardH);
		controls.add(card);
		int cy = y + 23;
		for (Control control : cardControls) {
			int controlH = control.desiredHeight();
			control.setBounds(x + 8, cy, width - 16, controlH);
			controls.add(control);
			cy += controlH + SETTING_GAP;
		}
		return y + cardH + 7;
	}

	private List<Control> presetControls() {
		List<Control> result = new ArrayList<>();
		result.add(mode("Active preset", this::activePresetLabel, presetOptions()));
		result.add(button("Create preset", this::openPresetNameScreen, primaryColor(), "Create"));
		result.add(button("Save selected custom", () -> {
			saveCurrentPreset();
			lastSaveTime = System.currentTimeMillis();
		}, GOOD, "Save", this::hasSelectedCustomPreset));
		result.add(button("PvP preset", () -> applyPreset(ParticleSystem.Preset.PVP), DANGER, "Apply"));
		result.add(button("Minimal preset", () -> applyPreset(ParticleSystem.Preset.MINIMAL), GOOD, "Apply"));
		result.add(button("Cinematic preset", () -> applyPreset(ParticleSystem.Preset.CINEMATIC), primaryColor(), "Apply"));
		for (String presetName : ParticleConfig.getCustomPresetNames()) {
			result.add(button(presetName, () -> {
				ParticleConfig.loadCustomPreset(presetName, settings);
				saveSettings();
			}, primaryColor(), "Load"));
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

		int tabY = y + 48;
		for (Tab value : Tab.values()) {
			if (hovered(mouseX, mouseY, x + 8, tabY, SIDEBAR_W - 16, 19)) {
				tab = value;
				scroll = 0.0F;
				activeSlider = null;
				openDropdown = null;
				openColor = null;
				colorDrag = ColorDrag.NONE;
				return true;
			}
			tabY += 22;
		}

		int fy = y + windowH - FOOTER_H + 6;
		int bx = contentX + 10;
		if (hovered(mouseX, mouseY, bx, fy, 58, 17)) {
			saveSettings();
			lastSaveTime = System.currentTimeMillis();
			return true;
		}
		if (hovered(mouseX, mouseY, bx + 64, fy, 58, 17)) {
			settings.reset();
			saveSettings();
			scroll = 0.0F;
			return true;
		}
		if (hovered(mouseX, mouseY, bx + 128, fy, 58, 17)) {
			close();
			return true;
		}

		if (openDropdown != null) {
			for (Control control : controls) {
				if (control instanceof DropdownControl dropdown && dropdown.overlayClick(mouseX, mouseY)) {
					return true;
				}
			}
			boolean clickedOpenHeader = false;
			for (Control control : controls) {
				if (control instanceof DropdownControl dropdown && dropdown.isOpen() && hovered(mouseX, mouseY, dropdown.x, dropdown.y, dropdown.width, dropdown.height)) {
					clickedOpenHeader = true;
					break;
				}
			}
			if (!clickedOpenHeader) {
				openDropdown = null;
				return true;
			}
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
		colorDrag = ColorDrag.NONE;
		return super.mouseReleased(click);
	}

	@Override
	public boolean mouseDragged(Click click, double offsetX, double offsetY) {
		if (activeSlider != null) {
			activeSlider.setFromMouse((float) click.x());
			return true;
		}
		if (colorDrag != ColorDrag.NONE && openColor != null) {
			for (Control control : controls) {
				if (control instanceof ColorControl color && color.isOpen()) {
					color.drag((float) click.x(), (float) click.y(), colorDrag);
					return true;
				}
			}
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

	private DropdownControl mode(String label, Supplier<String> getter, List<Option> options) {
		return new DropdownControl(label, getter, options);
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

	private ButtonControl button(String label, Runnable action, int color, String actionLabel) {
		return button(label, action, color, actionLabel, () -> true);
	}

	private ButtonControl button(String label, Runnable action, int color, String actionLabel, BooleanSupplier enabled) {
		return new ButtonControl(label, action, color, actionLabel, enabled);
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

	private List<Option> colorPresetOptions() {
		List<Option> options = new ArrayList<>();
		for (ParticleSystem.ColorPreset preset : ParticleSystem.ColorPreset.values()) {
			options.add(new Option(clean(preset.name()), () -> {
				settings.markCustom();
				settings.colorPreset = preset;
				saveSettings();
			}));
		}
		return options;
	}

	private List<Option> particleModeOptions(boolean world) {
		List<Option> options = new ArrayList<>();
		for (Particle3D.ParticleMode mode : Particle3D.ParticleMode.values()) {
			options.add(new Option(clean(mode.name()), () -> {
				settings.markCustom();
				if (world) {
					settings.worldMode = mode;
				} else {
					settings.particleMode = mode;
				}
				saveSettings();
			}));
		}
		return options;
	}

	private List<Option> glowModeOptions() {
		List<Option> options = new ArrayList<>();
		for (Particle3D.GlowMode mode : Particle3D.GlowMode.values()) {
			options.add(new Option(clean(mode.name()), () -> {
				settings.markCustom();
				settings.glowMode = mode;
				saveSettings();
			}));
		}
		return options;
	}

	private List<Option> presetOptions() {
		List<Option> options = new ArrayList<>();
		options.add(new Option("Custom", () -> {
			settings.markCustom();
			saveSettings();
		}));
		options.add(new Option("PvP", () -> applyPreset(ParticleSystem.Preset.PVP)));
		options.add(new Option("Minimal", () -> applyPreset(ParticleSystem.Preset.MINIMAL)));
		options.add(new Option("Cinematic", () -> applyPreset(ParticleSystem.Preset.CINEMATIC)));
		for (String presetName : ParticleConfig.getCustomPresetNames()) {
			options.add(new Option(presetName, () -> {
				ParticleConfig.loadCustomPreset(presetName, settings);
				saveSettings();
			}));
		}
		return options;
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

	private int primaryColor() {
		return settings.uiPrimaryColor;
	}

	private int secondaryColor() {
		return settings.uiSecondaryColor;
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

	private void drawFooterButton(DrawContext context, int mouseX, int mouseY, int x, int y, int w, int h, String text, int color) {
		boolean hover = hovered(mouseX, mouseY, x, y, w, h);
		UiRender2D.pill(context, x, y, w, h, hover ? color : 0xAA151720);
		context.drawCenteredTextWithShadow(client.textRenderer, text, x + w / 2, y + 5, 0xFFFFFFFF);
	}

	private void renderDropdownOverlay(DrawContext context, int mouseX, int mouseY) {
		if (openDropdown == null) {
			return;
		}
		for (Control control : controls) {
			if (control instanceof DropdownControl dropdown && dropdown.isOpen()) {
				dropdown.renderOverlay(context, mouseX, mouseY);
				return;
			}
		}
	}

	private enum Tab {
		GENERAL("General", "main controls"),
		EVENTS("Events", "triggers"),
		WORLD("World", "ambient"),
		COLORS("Colors", "palette"),
		PRESETS("Presets", "profiles");

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

	private record Option(String label, Runnable action) {
	}

	private enum ColorDrag {
		NONE,
		PICKER,
		HUE,
		ALPHA
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

		protected int desiredHeight() {
			return SETTING_H;
		}
	}

	private final class CardControl extends Control {
		private CardControl(String label) {
			super(label);
		}

		@Override
		protected void render(DrawContext context, int mouseX, int mouseY) {
			UiRender2D.roundedRect(context, x - 1, y - 1, width + 2, height + 2, 7, 0x24FFFFFF);
			UiRender2D.roundedRect(context, x, y, width, height, 7, 0xAD0D0E13);
			drawText(context, label, x + 8, y + 8, TEXT);
			context.fill(x + 8, y + 20, x + width - 8, y + 21, 0x14FFFFFF);
		}
	}

	private abstract class SettingControl extends Control {
		protected SettingControl(String label) {
			super(label);
		}

		protected float hover(int mouseX, int mouseY) {
			return hovered(mouseX, mouseY, x, y, width, height) ? 1.0F : 0.0F;
		}

		protected void renderHighlight(DrawContext context, int mouseX, int mouseY) {
			if (hover(mouseX, mouseY) > 0.0F) {
				UiRender2D.roundedRect(context, x - 3, y - 1, width + 6, height + 2, 5, 0x221C2030);
			}
		}
	}

	private final class ToggleControl extends SettingControl {
		private final BooleanSupplier getter;
		private final Consumer<Boolean> setter;

		private ToggleControl(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
			super(label);
			this.getter = getter;
			this.setter = setter;
		}

		@Override
		protected void render(DrawContext context, int mouseX, int mouseY) {
			renderHighlight(context, mouseX, mouseY);
			drawText(context, label, x, y + 5, TEXT);
			boolean enabled = getter.getAsBoolean();
			int tw = 24;
			int th = 12;
			int tx = x + width - tw;
			int ty = y + 3;
			UiRender2D.roundedRect(context, tx - 1, ty - 1, tw + 2, th + 2, 5, enabled ? UiRender2D.withAlpha(primaryColor(), 76) : 0x334A5062);
			UiRender2D.gradientRoundedRect(context, tx, ty, tw, th, 5,
					enabled ? primaryColor() : 0xFF20232D,
					enabled ? secondaryColor() : 0xFF20232D);
			int knob = 8;
			int kx = tx + 2 + (enabled ? tw - knob - 4 : 0);
			UiRender2D.roundedRect(context, kx, ty + 2, knob, knob, 4, enabled ? 0xFFFFFFFF : 0xFF8B92A4);
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

	private final class CycleControl extends SettingControl {
		private final Supplier<String> getter;
		private final Runnable action;

		private CycleControl(String label, Supplier<String> getter, Runnable action) {
			super(label);
			this.getter = getter;
			this.action = action;
		}

		@Override
		protected void render(DrawContext context, int mouseX, int mouseY) {
			renderHighlight(context, mouseX, mouseY);
			drawText(context, label, x, y + 5, TEXT);
			String value = getter.get();
			int valueW = Math.min(96, Math.max(54, client.textRenderer.getWidth(value) + 14));
			UiRender2D.pill(context, x + width - valueW, y + 3, valueW, 12, 0xBB151720);
			context.drawCenteredTextWithShadow(client.textRenderer, value, x + width - valueW / 2, y + 5, primaryColor());
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

	private final class DropdownControl extends SettingControl {
		private final Supplier<String> getter;
		private final List<Option> options;

		private DropdownControl(String label, Supplier<String> getter, List<Option> options) {
			super(label);
			this.getter = getter;
			this.options = options;
		}

		@Override
		protected void render(DrawContext context, int mouseX, int mouseY) {
			renderHighlight(context, mouseX, mouseY);
			drawText(context, label, x, y + 5, TEXT);
			String value = getter.get();
			int boxW = dropdownWidth();
			int boxX = x + width - boxW;
			UiRender2D.roundedRect(context, boxX - 1, y + 2, boxW + 2, 14, 5, hovered(mouseX, mouseY, boxX, y + 2, boxW, 14) || isOpen() ? 0x30FFFFFF : 0x18FFFFFF);
			UiRender2D.roundedRect(context, boxX, y + 3, boxW, 12, 5, 0xE30A0B10);
			context.enableScissor(boxX + 5, y + 3, boxX + boxW - 15, y + 15);
			drawText(context, value, boxX + 5, y + 5, primaryColor());
			context.disableScissor();
			drawText(context, isOpen() ? "^" : "v", boxX + boxW - 10, y + 5, MUTED);
		}

		private void renderOverlay(DrawContext context, int mouseX, int mouseY) {
			int boxW = dropdownWidth();
			int boxX = x + width - boxW;
			int optionH = 13;
			int pad = 4;
			int listH = pad * 2 + options.size() * optionH;
			boolean up = y + 16 + listH > ParticleSettingsScreen.this.y + windowH - 4 && y - listH > ParticleSettingsScreen.this.y + 4;
			int listY = up ? y + 15 - listH : y + 17;
			UiRender2D.softShadow(context, boxX, listY, boxW, listH, 6, 10, 0xAA000000);
			UiRender2D.roundedRect(context, boxX - 1, listY - 1, boxW + 2, listH + 2, 6, 0x35FFFFFF);
			UiRender2D.roundedRect(context, boxX, listY, boxW, listH, 6, 0xF508090D);
			int rowY = listY + pad;
			String current = getter.get();
			for (Option option : options) {
				boolean hover = hovered(mouseX, mouseY, boxX + 2, rowY, boxW - 4, optionH);
				boolean selected = option.label.equals(current);
				if (hover || selected) {
					UiRender2D.roundedRect(context, boxX + 3, rowY + 1, boxW - 6, optionH - 2, 4,
							selected ? UiRender2D.withAlpha(primaryColor(), 90) : 0x22FFFFFF);
				}
				if (selected) {
					UiRender2D.pill(context, boxX + 5, rowY + 4, 2, optionH - 8, primaryColor());
				}
				context.enableScissor(boxX + 10, rowY, boxX + boxW - 5, rowY + optionH);
				drawText(context, option.label, boxX + 10, rowY + 3, selected ? TEXT : MUTED);
				context.disableScissor();
				rowY += optionH;
			}
		}

		private boolean overlayClick(double mouseX, double mouseY) {
			if (!isOpen()) {
				return false;
			}
			int boxW = dropdownWidth();
			int boxX = x + width - boxW;
			int optionH = 13;
			int pad = 4;
			int listH = pad * 2 + options.size() * optionH;
			boolean up = y + 16 + listH > ParticleSettingsScreen.this.y + windowH - 4 && y - listH > ParticleSettingsScreen.this.y + 4;
			int listY = up ? y + 15 - listH : y + 17;
			int rowY = listY + pad;
			for (Option option : options) {
				if (hovered(mouseX, mouseY, boxX + 2, rowY, boxW - 4, optionH)) {
					option.action.run();
					openDropdown = null;
					return true;
				}
				rowY += optionH;
			}
			return false;
		}

		@Override
		protected boolean click(double mouseX, double mouseY) {
			if (!hovered(mouseX, mouseY, x, y, width, height)) {
				return false;
			}
			openDropdown = isOpen() ? null : label;
			return true;
		}

		private boolean isOpen() {
			return label.equals(openDropdown);
		}

		private int dropdownWidth() {
			return Math.min(116, Math.max(76, width / 2));
		}
	}

	private final class SliderControl extends SettingControl {
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
			renderHighlight(context, mouseX, mouseY);
			String value = formatter.apply(getter.getAsDouble());
			drawText(context, label, x, y + 1, TEXT);
			drawText(context, value, x + width - client.textRenderer.getWidth(value), y + 1, MUTED);
			float progress = (float) ((getter.getAsDouble() - min) / (max - min));
			UiRender2D.slider(context, x, y + 13, width, progress, 0xFF222631, primaryColor(), 0xFFFFFFFF);
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
			float progress = MathHelper.clamp((mouseX - x) / width, 0.0F, 1.0F);
			setter.accept(min + (max - min) * progress);
		}
	}

	private final class ColorControl extends SettingControl {
		private final IntSupplier getter;
		private final Consumer<Integer> setter;
		private float hueCache;
		private boolean hueReady;

		private ColorControl(String label, IntSupplier getter, Consumer<Integer> setter) {
			super(label);
			this.getter = getter;
			this.setter = setter;
		}

		@Override
		protected void render(DrawContext context, int mouseX, int mouseY) {
			initHue();
			if (isOpen() && colorDrag != ColorDrag.NONE) {
				drag(mouseX, mouseY, colorDrag);
			}
			renderHighlight(context, mouseX, mouseY);
			drawText(context, label, x, y + 5, TEXT);
			int color = getter.getAsInt();
			int sw = 36;
			int sx = x + width - sw;
			UiRender2D.roundedRect(context, sx - 1, y + 3, sw + 2, 12, 4, 0x44FFFFFF);
			UiRender2D.gradientRoundedRect(context, sx, y + 4, sw, 10, 4, color, UiRender2D.multiply(color, 0.62F));
			drawText(context, isOpen() ? "^" : "v", sx - 12, y + 5, MUTED);

			if (isOpen()) {
				renderPicker(context);
			}
		}

		@Override
		protected boolean click(double mouseX, double mouseY) {
			if (!hovered(mouseX, mouseY, x, y, width, height)) {
				return false;
			}
			if (isOpen()) {
				if (hovered(mouseX, mouseY, pickerX(), pickerY(), pickerW(), pickerH())) {
					colorDrag = ColorDrag.PICKER;
					drag((float) mouseX, (float) mouseY, colorDrag);
					return true;
				}
				if (hovered(mouseX, mouseY, pickerX(), hueY(), pickerW(), barH())) {
					colorDrag = ColorDrag.HUE;
					drag((float) mouseX, (float) mouseY, colorDrag);
					return true;
				}
				if (hovered(mouseX, mouseY, pickerX(), alphaY(), pickerW(), barH())) {
					colorDrag = ColorDrag.ALPHA;
					drag((float) mouseX, (float) mouseY, colorDrag);
					return true;
				}
			}
			openDropdown = null;
			openColor = isOpen() ? null : label;
			colorDrag = ColorDrag.NONE;
			return true;
		}

		@Override
		protected int desiredHeight() {
			return isOpen() ? SETTING_H + 82 : SETTING_H;
		}

		private void renderPicker(DrawContext context) {
			int color = getter.getAsInt();
			float[] hsb = hsb(color);
			int px = pickerX();
			int py = pickerY();
			int pw = pickerW();
			int ph = pickerH();
			int bh = barH();
			int radius = 5;

			UiRender2D.colorPicker(context, px, py, pw, ph, radius, hueCache);
			UiRender2D.border(context, px - 1, py - 1, pw + 2, ph + 2, radius + 1, 0x34FFFFFF);
			UiRender2D.hueBar(context, px, hueY(), pw, bh, 3);
			UiRender2D.alphaBar(context, px, alphaY(), pw, bh, 3, color);

			int satX = px + Math.round(hsb[1] * pw);
			int briY = py + Math.round((1.0F - hsb[2]) * ph);
			selectorDot(context, satX, briY, 5);
			selectorLine(context, px + Math.round(hueCache * pw), hueY(), bh);
			selectorLine(context, px + Math.round(alpha(color) * pw), alphaY(), bh);
		}

		private void drag(float mouseX, float mouseY, ColorDrag drag) {
			int color = getter.getAsInt();
			float[] hsb = hsb(color);
			int alpha = (color >>> 24) & 0xFF;
			if (drag == ColorDrag.PICKER) {
				float sat = MathHelper.clamp((mouseX - pickerX()) / pickerW(), 0.0F, 1.0F);
				float bri = 1.0F - MathHelper.clamp((mouseY - pickerY()) / pickerH(), 0.0F, 1.0F);
				setColor(hueCache, sat, bri, alpha);
			} else if (drag == ColorDrag.HUE) {
				hueCache = MathHelper.clamp((mouseX - pickerX()) / pickerW(), 0.0F, 1.0F);
				setColor(hueCache, hsb[1], hsb[2], alpha);
			} else if (drag == ColorDrag.ALPHA) {
				int nextAlpha = Math.round(MathHelper.clamp((mouseX - pickerX()) / pickerW(), 0.0F, 1.0F) * 255.0F);
				setColor(hsb[0], hsb[1], hsb[2], nextAlpha);
			}
		}

		private void setColor(float hue, float sat, float bri, int alpha) {
			int rgb = Color.HSBtoRGB(hue, sat, bri) & 0x00FFFFFF;
			setter.accept((MathHelper.clamp(alpha, 0, 255) << 24) | rgb);
		}

		private void initHue() {
			if (hueReady) {
				return;
			}
			hueCache = hsb(getter.getAsInt())[0];
			hueReady = true;
		}

		private boolean isOpen() {
			return label.equals(openColor);
		}

		private int pickerX() {
			return x;
		}

		private int pickerY() {
			return y + SETTING_H + 2;
		}

		private int pickerW() {
			return width;
		}

		private int pickerH() {
			return 50;
		}

		private int barH() {
			return 6;
		}

		private int hueY() {
			return pickerY() + pickerH() + 7;
		}

		private int alphaY() {
			return hueY() + barH() + 7;
		}

		private void selectorDot(DrawContext context, int cx, int cy, int size) {
			UiRender2D.roundedRect(context, cx - size / 2 - 1, cy - size / 2 - 1, size + 2, size + 2, size / 2 + 1, 0xDD000000);
			UiRender2D.roundedRect(context, cx - size / 2, cy - size / 2, size, size, size / 2, 0xFFFFFFFF);
		}

		private void selectorLine(DrawContext context, int cx, int y, int h) {
			UiRender2D.roundedRect(context, cx - 2, y - 2, 4, h + 4, 2, 0xDD000000);
			UiRender2D.roundedRect(context, cx - 1, y - 1, 2, h + 2, 1, 0xFFFFFFFF);
		}

		private float[] hsb(int color) {
			Color awt = new Color(color, true);
			return Color.RGBtoHSB(awt.getRed(), awt.getGreen(), awt.getBlue(), null);
		}

		private float alpha(int color) {
			return ((color >>> 24) & 0xFF) / 255.0F;
		}
	}

	private final class ButtonControl extends SettingControl {
		private final Runnable action;
		private final int color;
		private final String actionLabel;
		private final BooleanSupplier enabled;

		private ButtonControl(String label, Runnable action, int color, String actionLabel, BooleanSupplier enabled) {
			super(label);
			this.action = action;
			this.color = color;
			this.actionLabel = actionLabel;
			this.enabled = enabled;
		}

		@Override
		protected void render(DrawContext context, int mouseX, int mouseY) {
			renderHighlight(context, mouseX, mouseY);
			drawText(context, label, x, y + 5, enabled.getAsBoolean() ? TEXT : DIM);
			int bw = Math.max(42, client.textRenderer.getWidth(actionLabel) + 16);
			UiRender2D.pill(context, x + width - bw, y + 3, bw, 12, enabled.getAsBoolean() ? color : 0xFF252936);
			context.drawCenteredTextWithShadow(client.textRenderer, actionLabel, x + width - bw / 2, y + 5, 0xFFFFFFFF);
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
