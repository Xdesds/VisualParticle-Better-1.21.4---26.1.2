package visualparticle.better.client;

import com.mojang.blaze3d.platform.InputConstants;
import visualparticle.better.client.discord.DiscordPresenceManager;
import visualparticle.better.client.event.EventBus;
import visualparticle.better.client.event.WorldRenderEvent;
import visualparticle.better.client.module.ModuleManager;
import visualparticle.better.client.module.impl.render.ParticlesModule;
import visualparticle.better.client.render.NanoVGRenderBackend;
import visualparticle.better.client.render.RenderManager;
import visualparticle.better.client.render.nanovg.VisualParticlePictureInPictureRenderer;
import visualparticle.better.client.ui.VisualParticleClickGuiScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientCore {
	public static final String MOD_ID = "visualparticle-better";
	public static final String NAME = "VisualParticle Better";
	public static final String VERSION = "0.0.8";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
	private static final ClientCore INSTANCE = new ClientCore();

	private final EventBus events = new EventBus();
	private final ModuleManager modules = new ModuleManager();
	private final RenderManager renderer = new RenderManager();
	private final DiscordPresenceManager discordPresence = new DiscordPresenceManager();
	private final KeyMapping clickGuiKey = KeyBindingHelper.registerKeyBinding(
			new KeyMapping("key.visualparticle-better.clickgui", InputConstants.Type.KEYSYM,
					GLFW.GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.DEBUG));
	private boolean started;
	private long ticks;

	private ClientCore() {}
	public static ClientCore getInstance() { return INSTANCE; }

	public synchronized void start() {
		if (started) return;
		NanoVGRenderBackend renderBackend = new NanoVGRenderBackend();
		renderer.install(renderBackend);
		SpecialGuiElementRegistry.register(context ->
				new VisualParticlePictureInPictureRenderer(context.vertexConsumers(), renderBackend.canvas()));
		modules.register(new ParticlesModule());
		modules.enableDefaults();
		discordPresence.start();
		WorldRenderEvents.AFTER_ENTITIES.register(context -> events.post(
				new WorldRenderEvent(context, context.gameRenderer().getMainCamera().getPartialTickTime())));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			events.post(new ClientTickEvent(++ticks));
			discordPresence.tick(client);
			if (ticks == 40 && Boolean.getBoolean("visualparticle-better.previewClickGui")) client.setScreen(new VisualParticleClickGuiScreen());
			while (clickGuiKey.consumeClick()) client.setScreen(new VisualParticleClickGuiScreen());
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> stop());
		started = true;
		LOGGER.info("{} {} initialized with {} module(s) and NanoVG renderer", NAME, VERSION, modules.size());
	}

	public synchronized void stop() {
		if (!started) return;
		discordPresence.stop();
		modules.disableAll();
		events.clear();
		renderer.close();
		started = false;
		LOGGER.info("{} stopped", NAME);
	}

	public EventBus events() { return events; }
	public ModuleManager modules() { return modules; }
	public RenderManager renderer() { return renderer; }
}