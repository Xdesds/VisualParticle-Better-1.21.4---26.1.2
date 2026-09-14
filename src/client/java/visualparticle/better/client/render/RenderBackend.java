package visualparticle.better.client.render;

import visualparticle.better.client.render.ui.UiDrawList;
import net.minecraft.client.gui.GuiGraphics;

public interface RenderBackend extends AutoCloseable {
	void submit(GuiGraphics graphics, UiDrawList drawList);
	@Override
	void close();
}
