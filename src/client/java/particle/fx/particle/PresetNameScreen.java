package particle.fx.particle;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class PresetNameScreen extends Screen {
	private final Screen parent;
	private final String buttonLabel;
	private final UiLanguage language;
	private final Consumer<String> onConfirm;
	private TextFieldWidget nameField;
	private ButtonWidget confirmButton;

	public PresetNameScreen(Screen parent, String title, String buttonLabel, UiLanguage language, Consumer<String> onConfirm) {
		super(Text.literal(title));
		this.parent = parent;
		this.buttonLabel = buttonLabel;
		this.language = language;
		this.onConfirm = onConfirm;
	}

	@Override
	protected void init() {
		int fieldWidth = Math.min(220, width - 40);
		int left = (width - fieldWidth) / 2;
		int centerY = height / 2 - 20;

		nameField = new TextFieldWidget(textRenderer, left, centerY, fieldWidth, 20, Text.literal(t("Preset Name")));
		nameField.setMaxLength(32);
		nameField.setChangedListener(value -> updateButtons());
		addDrawableChild(nameField);
		setInitialFocus(nameField);

		confirmButton = addDrawableChild(ButtonWidget.builder(Text.literal(buttonLabel), button -> confirm())
				.dimensions(left, centerY + 30, (fieldWidth - 8) / 2, 20)
				.build());
		addDrawableChild(ButtonWidget.builder(Text.literal(t("Cancel")), button -> close())
				.dimensions(left + (fieldWidth - 8) / 2 + 8, centerY + 30, (fieldWidth - 8) / 2, 20)
				.build());

		updateButtons();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, width, height, 0x88000000);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 48, 0xFFFFFF);
		context.drawTextWithShadow(textRenderer, Text.literal(t("Preset name")), width / 2 - 110, height / 2 - 34, 0xFFFFFF);
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

	private void confirm() {
		String value = normalizedName();
		if (value.isEmpty()) {
			return;
		}

		onConfirm.accept(value);
		close();
	}

	private void updateButtons() {
		if (confirmButton != null) {
			confirmButton.active = !normalizedName().isEmpty();
		}
	}

	private String normalizedName() {
		return nameField == null ? "" : nameField.getText().trim();
	}

	private String t(String key) {
		return language.text(key);
	}
}
