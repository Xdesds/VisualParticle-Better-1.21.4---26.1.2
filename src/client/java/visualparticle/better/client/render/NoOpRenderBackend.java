package visualparticle.better.client.render;

import visualparticle.better.client.render.ui.UiDrawList;
import net.minecraft.client.gui.GuiGraphics;

public final class NoOpRenderBackend implements RenderBackend {
	@Override public void submit(GuiGraphics graphics, UiDrawList drawList) {}
	@Override public void close() {}
}
