package particle.fx.particle;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import particle.fx.ParticleFxMod;

import java.util.function.Function;

final class ParticleRenderLayers extends RenderPhase {
	static final RenderLayer QUADS = RenderLayer.of(
			ParticleFxMod.MOD_ID + "_world_particles_cube",
			VertexFormats.POSITION_COLOR,
			VertexFormat.DrawMode.QUADS,
			2048,
			RenderLayer.MultiPhaseParameters.builder()
					.program(POSITION_COLOR_PROGRAM)
					.transparency(LIGHTNING_TRANSPARENCY)
					.depthTest(LEQUAL_DEPTH_TEST)
					.cull(DISABLE_CULLING)
					.writeMaskState(COLOR_MASK)
					.build(false)
	);

	static final RenderLayer LINES = RenderLayer.of(
			ParticleFxMod.MOD_ID + "_world_particles_lines",
			VertexFormats.POSITION_COLOR,
			VertexFormat.DrawMode.DEBUG_LINES,
			2048,
			RenderLayer.MultiPhaseParameters.builder()
					.program(POSITION_COLOR_PROGRAM)
					.transparency(LIGHTNING_TRANSPARENCY)
					.depthTest(LEQUAL_DEPTH_TEST)
					.cull(DISABLE_CULLING)
					.writeMaskState(COLOR_MASK)
					.build(false)
	);

	static final Function<Identifier, RenderLayer> GLOW = Util.memoize(texture -> RenderLayer.of(
			ParticleFxMod.MOD_ID + "_world_particles_glow",
			VertexFormats.POSITION_TEXTURE_COLOR,
			VertexFormat.DrawMode.QUADS,
			2048,
			RenderLayer.MultiPhaseParameters.builder()
					.program(POSITION_TEXTURE_COLOR_PROGRAM)
					.texture(new Texture(texture, TriState.FALSE, false))
					.transparency(LIGHTNING_TRANSPARENCY)
					.depthTest(LEQUAL_DEPTH_TEST)
					.cull(DISABLE_CULLING)
					.writeMaskState(COLOR_MASK)
					.build(false)
	));

	private ParticleRenderLayers() {
		super("visualparticle-better_render_layers", () -> {
		}, () -> {
		});
	}
}
