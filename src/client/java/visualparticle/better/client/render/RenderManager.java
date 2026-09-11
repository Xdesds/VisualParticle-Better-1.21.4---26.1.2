package visualparticle.better.client.render;

import visualparticle.better.client.render.ui.UiDrawList;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;

public final class RenderManager implements AutoCloseable {
	private RenderBackend backend = new NoOpRenderBackend();

	public synchronized void install(RenderBackend newBackend) {
		Objects.requireNonNull(newBackend, "newBackend");
		backend.close();
		backend = newBackend;
	}

	public void submit(GuiGraphics graphics, UiDrawList drawList) {
		backend.submit(graphics, drawList);
	}

	public float textWidth(String value, float size, boolean strong) {
		if (backend instanceof NanoVGRenderBackend nano) return nano.canvas().textWidth(value, size, strong);
		return (value == null ? 0 : value.length()) * size * .55f;
	}
	public RenderBackend backend() {
		return backend;
	}

	@Override
	public synchronized void close() {
		backend.close();
		backend = new NoOpRenderBackend();
	}
}
