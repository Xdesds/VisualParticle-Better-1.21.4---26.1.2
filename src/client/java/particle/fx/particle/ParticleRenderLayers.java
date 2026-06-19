package particle.fx.particle;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

final class ParticleRenderLayers {
	static final RenderType QUADS = RenderTypes.debugQuads();
	static final RenderType LINES = RenderTypes.linesTranslucent();
	static final Function<Identifier, RenderType> GLOW = Util.memoize(RenderTypes::eyes);

	private ParticleRenderLayers() {
	}
}
