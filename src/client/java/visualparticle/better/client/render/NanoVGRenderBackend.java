package visualparticle.better.client.render;

import visualparticle.better.client.mixin.GuiGraphicsAccessor;
import visualparticle.better.client.render.nanovg.VisualParticleRenderState;
import visualparticle.better.client.render.nanovg.NanoVGCanvas;
import visualparticle.better.client.render.ui.UiDrawList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Matrix3x2f;

public final class NanoVGRenderBackend implements RenderBackend {
	private final NanoVGCanvas canvas = new NanoVGCanvas();

	@Override
	public void submit(GuiGraphics graphics, UiDrawList drawList) {
		if (drawList.isEmpty()) return;
		int width = graphics.guiWidth();
		int height = graphics.guiHeight();
		Matrix3x2f pose = new Matrix3x2f(graphics.pose());
		ScreenRectangle screen = new ScreenRectangle(0, 0, width, height).transformMaxBounds(pose);
		if (screen.width() <= 0 || screen.height() <= 0) return;
		var state = new VisualParticleRenderState(width, height, pose, null, screen, drawList.seal());
		((GuiGraphicsAccessor) graphics).visualParticleBetter$getGuiRenderState().submitPicturesInPictureState(state);
	}

	public NanoVGCanvas canvas() {
		return canvas;
	}

	@Override
	public void close() {
		canvas.close();
	}
}
