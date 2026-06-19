package particle.fx.particle;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import particle.fx.ParticleFxMod;

import java.util.function.Function;

final class ParticleRenderLayers {
	private static final RenderPipeline COLOR_PIPELINE = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
					.withLocation(Identifier.of(ParticleFxMod.MOD_ID, "world_particles_color"))
					.withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
					.withCull(false)
					.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
					.withDepthWrite(false)
					.withBlend(BlendFunction.LIGHTNING)
					.build()
	);

	private static final RenderPipeline LINES_PIPELINE = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
					.withLocation(Identifier.of(ParticleFxMod.MOD_ID, "world_particles_lines"))
					.withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.DEBUG_LINES)
					.withCull(false)
					.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
					.withDepthWrite(false)
					.withBlend(BlendFunction.LIGHTNING)
					.build()
	);

	private static final RenderPipeline GLOW_PIPELINE = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
					.withLocation(Identifier.of(ParticleFxMod.MOD_ID, "world_particles_glow"))
					.withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
					.withCull(false)
					.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
					.withDepthWrite(false)
					.withBlend(BlendFunction.LIGHTNING)
					.withSampler("Sampler0")
					.build()
	);

	static final RenderLayer QUADS = RenderLayer.of(
			ParticleFxMod.MOD_ID + "_world_particles_cube",
			RenderSetup.builder(COLOR_PIPELINE)
					.translucent()
					.expectedBufferSize(2048)
					.build()
	);

	static final RenderLayer LINES = RenderLayer.of(
			ParticleFxMod.MOD_ID + "_world_particles_lines",
			RenderSetup.builder(LINES_PIPELINE)
					.translucent()
					.expectedBufferSize(2048)
					.build()
	);

	static final Function<Identifier, RenderLayer> GLOW = Util.memoize(texture -> {
		RenderSetup setup = RenderSetup.builder(GLOW_PIPELINE)
				.texture("Sampler0", texture)
				.translucent()
				.expectedBufferSize(2048)
				.build();
		return RenderLayer.of(ParticleFxMod.MOD_ID + "_world_particles_glow", setup);
	});

	private ParticleRenderLayers() {
	}
}
