package visualparticle.better.client.event;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;

public record WorldRenderEvent(WorldRenderContext context, float partialTick) implements Event {}