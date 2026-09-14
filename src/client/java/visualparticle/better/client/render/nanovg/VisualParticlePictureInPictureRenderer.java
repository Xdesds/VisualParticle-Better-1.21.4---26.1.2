package visualparticle.better.client.render.nanovg;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.lwjgl.opengl.GL33C;

public final class VisualParticlePictureInPictureRenderer extends PictureInPictureRenderer<VisualParticleRenderState> {
	private final NanoVGCanvas canvas;

	public VisualParticlePictureInPictureRenderer(MultiBufferSource.BufferSource bufferSource, NanoVGCanvas canvas) {
		super(bufferSource);
		this.canvas = canvas;
	}

	@Override
	protected boolean textureIsReadyToBlit(VisualParticleRenderState state) {
		return false;
	}

	@Override
	protected float getTranslateY(int height, int windowScaleFactor) {
		return height / 2.0f;
	}

	@Override
	public Class<VisualParticleRenderState> getRenderStateClass() {
		return VisualParticleRenderState.class;
	}

	@Override
	protected String getTextureLabel() {
		return "visualparticlebetter-ui";
	}

	@Override
	protected void renderToTexture(VisualParticleRenderState state, PoseStack poseStack) {
		var window = Minecraft.getInstance().getWindow();
		if (window.isIconified()) return;

		var colorTexture = RenderSystem.outputColorTextureOverride;
		if (colorTexture == null) return;
		int width = colorTexture.getWidth(0);
		int height = colorTexture.getHeight(0);
		if (width <= 0 || height <= 0) return;
		if (!(RenderSystem.getDevice() instanceof GlDevice glDevice)) return;
		var depthView = RenderSystem.outputDepthTextureOverride;
		if (depthView == null || !(depthView.texture() instanceof GlTexture depthTexture)) return;
		if (!(colorTexture.texture() instanceof GlTexture glColorTexture)) return;

		int framebuffer = glColorTexture.getFbo(glDevice.directStateAccess(), depthTexture);
		GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, framebuffer);
		GlStateManager._viewport(0, 0, width, height);
		GL33C.glBindSampler(0, 0);

		float rawWidth = width;
		float rawHeight = height;
		float guiWidth = Math.max(1.0f, window.getGuiScaledWidth());
		float pixelRatio = rawWidth / guiWidth;

		canvas.begin(state.width(), state.height(), pixelRatio, state.poseMatrix());
		canvas.render(state.commands());
		canvas.end();

		GlStateManager._disableDepthTest();
		GlStateManager._disableCull();
		GlStateManager._enableBlend();
		GlStateManager._blendFuncSeparate(770, 771, 1, 0);
	}
}
