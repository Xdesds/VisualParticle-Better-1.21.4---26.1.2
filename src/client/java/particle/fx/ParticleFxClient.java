package particle.fx;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import particle.fx.particle.ParticleSettingsScreen;
import particle.fx.particle.ParticleSystem;

public class ParticleFxClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ParticleSystem particles = ParticleSystem.getInstance();
		KeyBinding settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.visualparticle-better.settings",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				KeyBinding.Category.MISC
		));

		ClientTickEvents.END_CLIENT_TICK.register(particles::tick);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (settingsKey.wasPressed()) {
				client.setScreen(new ParticleSettingsScreen(client.currentScreen));
			}
		});
		WorldRenderEvents.AFTER_ENTITIES.register(particles::render);
	}
}
