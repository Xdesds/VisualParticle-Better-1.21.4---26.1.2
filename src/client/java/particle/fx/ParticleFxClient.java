package particle.fx;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import particle.fx.particle.ParticleSettingsScreen;
import particle.fx.particle.ParticleSystem;

public class ParticleFxClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ParticleSystem particles = ParticleSystem.getInstance();
		KeyMapping settingsKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.visualparticle-better.settings",
				InputConstants.Type.KEYSYM,
				344,
				KeyMapping.Category.MISC
		));

		ClientTickEvents.END_CLIENT_TICK.register(particles::tick);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (settingsKey.consumeClick()) {
				client.setScreen(new ParticleSettingsScreen(client.screen));
			}
		});
		LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(particles::render);
	}
}
