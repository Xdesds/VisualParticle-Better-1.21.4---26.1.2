package visualparticle.better.client.event;

import net.minecraft.client.gui.GuiGraphics;

public record HudRenderEvent(GuiGraphics graphics, float partialTick) implements Event {}