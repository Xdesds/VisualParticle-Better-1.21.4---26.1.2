package particle.fx.particle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import particle.fx.ParticleFxMod;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

final class UiRender2D {
	private static final int SCALE = 3;
	static final int CORNER_TOP_LEFT = 1;
	static final int CORNER_TOP_RIGHT = 2;
	static final int CORNER_BOTTOM_RIGHT = 4;
	static final int CORNER_BOTTOM_LEFT = 8;
	static final int CORNERS_RIGHT = CORNER_TOP_RIGHT | CORNER_BOTTOM_RIGHT;
	private static final Map<SurfaceKey, Identifier> SURFACES = new HashMap<>();
	private static int nextTextureId;

	private UiRender2D() {
	}

	static void blurredBackdrop(DrawContext context, int x, int y, int width, int height, int radius, int tint) {
		roundedRect(context, x, y, width, height, radius, tint);
	}

	static void softShadow(DrawContext context, int x, int y, int width, int height, int radius, int spread, int color) {
		if (!visible(width, height, color) || spread <= 0) {
			return;
		}
		drawSurface(context, new SurfaceKey(SurfaceType.SHADOW, width + spread * 2, height + spread * 2, radius + spread, color, color, spread, 0), x - spread, y - spread);
	}

	static void roundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
		roundedRect(context, x, y, width, height, radius, color,
				CORNER_TOP_LEFT | CORNER_TOP_RIGHT | CORNER_BOTTOM_RIGHT | CORNER_BOTTOM_LEFT);
	}

	static void roundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color, int corners) {
		if (!visible(width, height, color)) {
			return;
		}
		drawSurface(context, new SurfaceKey(SurfaceType.RECT, width, height, radius, color, color, 0, corners), x, y);
	}

	static void gradientRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int topColor, int bottomColor) {
		gradientRoundedRect(context, x, y, width, height, radius, topColor, bottomColor,
				CORNER_TOP_LEFT | CORNER_TOP_RIGHT | CORNER_BOTTOM_RIGHT | CORNER_BOTTOM_LEFT);
	}

	static void gradientRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int topColor, int bottomColor, int corners) {
		if (!visible(width, height, topColor) && !visible(width, height, bottomColor)) {
			return;
		}
		drawSurface(context, new SurfaceKey(SurfaceType.GRADIENT, width, height, radius, topColor, bottomColor, 0, corners), x, y);
	}

	static void colorPicker(DrawContext context, int x, int y, int width, int height, int radius, float hue) {
		int hueKey = Math.round(MathHelper.clamp(hue, 0.0F, 1.0F) * 1000.0F);
		drawSurface(context, new SurfaceKey(SurfaceType.COLOR_PICKER, width, height, radius, 0xFFFFFFFF, 0xFF000000, hueKey, 0), x, y);
	}

	static void hueBar(DrawContext context, int x, int y, int width, int height, int radius) {
		drawSurface(context, new SurfaceKey(SurfaceType.HUE_BAR, width, height, radius, 0xFFFFFFFF, 0xFFFFFFFF, 0, 0), x, y);
	}

	static void alphaBar(DrawContext context, int x, int y, int width, int height, int radius, int color) {
		drawSurface(context, new SurfaceKey(SurfaceType.ALPHA_BAR, width, height, radius, color, color, 0, 0), x, y);
	}

	static void border(DrawContext context, int x, int y, int width, int height, int radius, int color) {
		if (!visible(width, height, color)) {
			return;
		}
		drawSurface(context, new SurfaceKey(SurfaceType.BORDER, width, height, radius, color, color, 0,
				CORNER_TOP_LEFT | CORNER_TOP_RIGHT | CORNER_BOTTOM_RIGHT | CORNER_BOTTOM_LEFT), x, y);
	}

	static void pill(DrawContext context, int x, int y, int width, int height, int color) {
		roundedRect(context, x, y, width, height, height / 2, color);
	}

	static void slider(DrawContext context, int x, int y, int width, float progress, int trackColor, int fillColor, int knobColor) {
		progress = MathHelper.clamp(progress, 0.0F, 1.0F);
		pill(context, x, y, width, 4, trackColor);
		pill(context, x, y, Math.max(4, Math.round(width * progress)), 4, fillColor);
		int knobX = x + Math.round(width * progress);
		roundedRect(context, knobX - 3, y - 4, 7, 12, 4, knobColor);
	}

	static int withAlpha(int color, int alpha) {
		return (MathHelper.clamp(alpha, 0, 255) << 24) | (color & 0x00FFFFFF);
	}

	static int multiply(int color, float factor) {
		int a = color & 0xFF000000;
		int r = Math.min(255, Math.max(0, (int) (((color >> 16) & 0xFF) * factor)));
		int g = Math.min(255, Math.max(0, (int) (((color >> 8) & 0xFF) * factor)));
		int b = Math.min(255, Math.max(0, (int) ((color & 0xFF) * factor)));
		return a | (r << 16) | (g << 8) | b;
	}

	private static void drawSurface(DrawContext context, SurfaceKey key, int x, int y) {
		Identifier id = SURFACES.computeIfAbsent(key, UiRender2D::createSurface);
		context.drawTexture(RenderPipelines.GUI_TEXTURED, id, x, y, 0.0F, 0.0F, key.width, key.height,
				key.textureWidth(), key.textureHeight(), key.textureWidth(), key.textureHeight());
	}

	private static Identifier createSurface(SurfaceKey key) {
		int textureWidth = key.textureWidth();
		int textureHeight = key.textureHeight();
		NativeImage image = new NativeImage(textureWidth, textureHeight, false);
		for (int py = 0; py < textureHeight; py++) {
			for (int px = 0; px < textureWidth; px++) {
				image.setColorArgb(px, py, pixel(key, (px + 0.5F) / SCALE, (py + 0.5F) / SCALE));
			}
		}

		Identifier id = Identifier.of(ParticleFxMod.MOD_ID, "ui_surface/" + nextTextureId++);
		NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> id.toString(), image);
		MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
		return id;
	}

	private static int pixel(SurfaceKey key, float px, float py) {
		return switch (key.type) {
			case RECT -> applyCoverage(key.topColor, roundedCoverage(key, px, py));
			case GRADIENT -> applyCoverage(lerpColor(key.topColor, key.bottomColor, py / Math.max(1.0F, key.height - 1.0F)),
					roundedCoverage(key, px, py));
			case BORDER -> borderPixel(key, px, py);
			case SHADOW -> shadowPixel(key, px, py);
			case COLOR_PICKER -> colorPickerPixel(key, px, py);
			case HUE_BAR -> hueBarPixel(key, px, py);
			case ALPHA_BAR -> alphaBarPixel(key, px, py);
		};
	}

	private static int borderPixel(SurfaceKey key, float px, float py) {
		float outer = roundedCoverage(key, px, py);
		float inner = roundedCoverage(px - 1.0F, py - 1.0F, key.width - 2, key.height - 2, Math.max(0, key.radius - 1), key.flags);
		return applyCoverage(key.topColor, MathHelper.clamp(outer - inner, 0.0F, 1.0F));
	}

	private static float roundedCoverage(SurfaceKey key, float px, float py) {
		return roundedCoverage(px, py, key.width, key.height, key.radius, key.flags);
	}

	private static int shadowPixel(SurfaceKey key, float px, float py) {
		float spread = Math.max(1.0F, key.extra);
		float dist = roundedDistance(px - spread, py - spread, key.width - spread * 2.0F, key.height - spread * 2.0F, Math.max(0.0F, key.radius - spread));
		float alpha = 1.0F - MathHelper.clamp(dist / spread, 0.0F, 1.0F);
		return applyCoverage(key.topColor, alpha * alpha * 0.72F);
	}

	private static float roundedCoverage(float px, float py, float width, float height, float radius) {
		return roundedCoverage(px, py, width, height, radius,
				CORNER_TOP_LEFT | CORNER_TOP_RIGHT | CORNER_BOTTOM_RIGHT | CORNER_BOTTOM_LEFT);
	}

	private static float roundedCoverage(float px, float py, float width, float height, float radius, int corners) {
		if (px < 0.0F || px > width || py < 0.0F || py > height) {
			return 0.0F;
		}
		if (radius <= 0.0F || corners == 0) {
			return 1.0F;
		}
		if (px < radius && py < radius && (corners & CORNER_TOP_LEFT) == 0) {
			return 1.0F;
		}
		if (px > width - radius && py < radius && (corners & CORNER_TOP_RIGHT) == 0) {
			return 1.0F;
		}
		if (px > width - radius && py > height - radius && (corners & CORNER_BOTTOM_RIGHT) == 0) {
			return 1.0F;
		}
		if (px < radius && py > height - radius && (corners & CORNER_BOTTOM_LEFT) == 0) {
			return 1.0F;
		}
		float dist = roundedDistance(px, py, width, height, radius);
		return MathHelper.clamp(0.5F - dist, 0.0F, 1.0F);
	}

	private static int colorPickerPixel(SurfaceKey key, float px, float py) {
		float sat = MathHelper.clamp(px / Math.max(1.0F, key.width - 1.0F), 0.0F, 1.0F);
		float bri = 1.0F - MathHelper.clamp(py / Math.max(1.0F, key.height - 1.0F), 0.0F, 1.0F);
		float hue = key.extra / 1000.0F;
		int color = 0xFF000000 | (Color.HSBtoRGB(hue, sat, bri) & 0x00FFFFFF);
		return applyCoverage(color, roundedCoverage(px, py, key.width, key.height, key.radius));
	}

	private static int hueBarPixel(SurfaceKey key, float px, float py) {
		float hue = MathHelper.clamp(px / Math.max(1.0F, key.width - 1.0F), 0.0F, 1.0F);
		int color = 0xFF000000 | (Color.HSBtoRGB(hue, 1.0F, 1.0F) & 0x00FFFFFF);
		return applyCoverage(color, roundedCoverage(px, py, key.width, key.height, key.radius));
	}

	private static int alphaBarPixel(SurfaceKey key, float px, float py) {
		float alpha = MathHelper.clamp(px / Math.max(1.0F, key.width - 1.0F), 0.0F, 1.0F);
		int color = (Math.round(alpha * ((key.topColor >>> 24) & 0xFF)) << 24) | (key.topColor & 0x00FFFFFF);
		return applyCoverage(color, roundedCoverage(px, py, key.width, key.height, key.radius));
	}

	private static float roundedDistance(float px, float py, float width, float height, float radius) {
		radius = Math.min(radius, Math.min(width, height) * 0.5F);
		float qx = Math.abs(px - width * 0.5F) - (width * 0.5F - radius);
		float qy = Math.abs(py - height * 0.5F) - (height * 0.5F - radius);
		float ox = Math.max(qx, 0.0F);
		float oy = Math.max(qy, 0.0F);
		return (float) Math.sqrt(ox * ox + oy * oy) + Math.min(Math.max(qx, qy), 0.0F) - radius;
	}

	private static int applyCoverage(int color, float coverage) {
		int alpha = Math.round(((color >>> 24) & 0xFF) * MathHelper.clamp(coverage, 0.0F, 1.0F));
		return (alpha << 24) | (color & 0x00FFFFFF);
	}

	private static int lerpColor(int from, int to, float progress) {
		progress = MathHelper.clamp(progress, 0.0F, 1.0F);
		int a = lerp((from >>> 24) & 0xFF, (to >>> 24) & 0xFF, progress);
		int r = lerp((from >>> 16) & 0xFF, (to >>> 16) & 0xFF, progress);
		int g = lerp((from >>> 8) & 0xFF, (to >>> 8) & 0xFF, progress);
		int b = lerp(from & 0xFF, to & 0xFF, progress);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private static int lerp(int from, int to, float progress) {
		return Math.round(from + (to - from) * progress);
	}

	private static boolean visible(int width, int height, int color) {
		return width > 0 && height > 0 && ((color >>> 24) & 0xFF) > 0;
	}

	private enum SurfaceType {
		RECT,
		GRADIENT,
		BORDER,
		SHADOW,
		COLOR_PICKER,
		HUE_BAR,
		ALPHA_BAR
	}

	private record SurfaceKey(SurfaceType type, int width, int height, int radius, int topColor, int bottomColor, int extra, int flags) {
		private int textureWidth() {
			return width * SCALE;
		}

		private int textureHeight() {
			return height * SCALE;
		}
	}
}
