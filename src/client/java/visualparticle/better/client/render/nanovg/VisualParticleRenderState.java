package visualparticle.better.client.render.nanovg;

import visualparticle.better.client.render.ui.UiCommand;
import java.util.List;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.joml.Matrix3x2f;

public record VisualParticleRenderState(
		int width,
		int height,
		Matrix3x2f poseMatrix,
		ScreenRectangle scissorArea,
		ScreenRectangle bounds,
		List<UiCommand> commands
) implements PictureInPictureRenderState {
	@Override public int x0() { return 0; }
	@Override public int y0() { return 0; }
	@Override public int x1() { return width; }
	@Override public int y1() { return height; }
	@Override public float scale() { return 1.0f; }
}
