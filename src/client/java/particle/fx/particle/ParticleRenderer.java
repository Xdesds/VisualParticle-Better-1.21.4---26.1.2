package particle.fx.particle;

import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;

final class ParticleRenderer {
	private static final int CRYSTAL_SIDES = 8;

	private ParticleRenderer() {
	}

	static void drawCube(VertexConsumer buffer, Matrix4f matrix, int color, float size) {
		float half = size / 2.0F;
		int r = color >> 16 & 0xFF;
		int g = color >> 8 & 0xFF;
		int b = color & 0xFF;
		int a = color >> 24 & 0xFF;

		buffer.vertex(matrix, -half, half, -half).color(r, g, b, a);
		buffer.vertex(matrix, -half, half, half).color(r, g, b, a);
		buffer.vertex(matrix, half, half, half).color(r, g, b, a);
		buffer.vertex(matrix, half, half, -half).color(r, g, b, a);

		buffer.vertex(matrix, -half, -half, -half).color(r, g, b, a);
		buffer.vertex(matrix, half, -half, -half).color(r, g, b, a);
		buffer.vertex(matrix, half, -half, half).color(r, g, b, a);
		buffer.vertex(matrix, -half, -half, half).color(r, g, b, a);

		buffer.vertex(matrix, -half, half, half).color(r, g, b, a);
		buffer.vertex(matrix, -half, -half, half).color(r, g, b, a);
		buffer.vertex(matrix, half, -half, half).color(r, g, b, a);
		buffer.vertex(matrix, half, half, half).color(r, g, b, a);

		buffer.vertex(matrix, -half, half, -half).color(r, g, b, a);
		buffer.vertex(matrix, half, half, -half).color(r, g, b, a);
		buffer.vertex(matrix, half, -half, -half).color(r, g, b, a);
		buffer.vertex(matrix, -half, -half, -half).color(r, g, b, a);

		buffer.vertex(matrix, -half, half, -half).color(r, g, b, a);
		buffer.vertex(matrix, -half, -half, -half).color(r, g, b, a);
		buffer.vertex(matrix, -half, -half, half).color(r, g, b, a);
		buffer.vertex(matrix, -half, half, half).color(r, g, b, a);

		buffer.vertex(matrix, half, half, -half).color(r, g, b, a);
		buffer.vertex(matrix, half, half, half).color(r, g, b, a);
		buffer.vertex(matrix, half, -half, half).color(r, g, b, a);
		buffer.vertex(matrix, half, -half, -half).color(r, g, b, a);
	}

	static void drawLines(VertexConsumer buffer, Matrix4f matrix, int color, float size) {
		float half = size / 2.0F;
		int r = color >> 16 & 0xFF;
		int g = color >> 8 & 0xFF;
		int b = color & 0xFF;
		int a = color >> 24 & 0xFF;

		line(buffer, matrix, -half, -half, -half, half, -half, -half, r, g, b, a);
		line(buffer, matrix, half, -half, -half, half, -half, half, r, g, b, a);
		line(buffer, matrix, half, -half, half, -half, -half, half, r, g, b, a);
		line(buffer, matrix, -half, -half, half, -half, -half, -half, r, g, b, a);
		line(buffer, matrix, -half, half, -half, half, half, -half, r, g, b, a);
		line(buffer, matrix, half, half, -half, half, half, half, r, g, b, a);
		line(buffer, matrix, half, half, half, -half, half, half, r, g, b, a);
		line(buffer, matrix, -half, half, half, -half, half, -half, r, g, b, a);
		line(buffer, matrix, -half, -half, -half, -half, half, -half, r, g, b, a);
		line(buffer, matrix, half, -half, -half, half, half, -half, r, g, b, a);
		line(buffer, matrix, half, -half, half, half, half, half, r, g, b, a);
		line(buffer, matrix, -half, -half, half, -half, half, half, r, g, b, a);
	}

	static void drawCrystal(VertexConsumer buffer, Matrix4f matrix, int color, float alpha, float size) {
		float radius = size * 0.24F;
		float prismHeight = size * 0.30F;
		float tipHeight = size * 0.35F;
		int baseAlpha = Math.max(0, Math.min(255, (int) (255.0F * alpha)));
		int darker = multiplyColor(color, 0.68F);
		int lighter = multiplyColor(color, 1.25F);

		for (int i = 0; i < CRYSTAL_SIDES; i++) {
			float angle1 = (float) (Math.PI * 2.0 * i / CRYSTAL_SIDES);
			float angle2 = (float) (Math.PI * 2.0 * (i + 1) / CRYSTAL_SIDES);
			float x1 = (float) Math.cos(angle1) * radius;
			float z1 = (float) Math.sin(angle1) * radius;
			float x2 = (float) Math.cos(angle2) * radius;
			float z2 = (float) Math.sin(angle2) * radius;
			float topY = prismHeight * 0.5F;
			float bottomY = -prismHeight * 0.5F;
			int sideColor = i % 2 == 0 ? color : darker;
			int capColor = i % 2 == 0 ? lighter : color;

			quad(buffer, matrix, x1, bottomY, z1, x2, bottomY, z2, x2, topY, z2, x1, topY, z1, sideColor, baseAlpha);
			triangle(buffer, matrix, 0.0F, topY + tipHeight, 0.0F, x2, topY, z2, x1, topY, z1, capColor, baseAlpha);
			triangle(buffer, matrix, 0.0F, bottomY - tipHeight, 0.0F, x1, bottomY, z1, x2, bottomY, z2, darker, baseAlpha);
		}
	}

	static void drawCrystalLines(VertexConsumer buffer, Matrix4f matrix, int color, float size) {
		float radius = size * 0.24F;
		float prismHeight = size * 0.30F;
		float tipHeight = size * 0.35F;
		float topY = prismHeight * 0.5F;
		float bottomY = -prismHeight * 0.5F;
		float tipTopY = topY + tipHeight;
		float tipBottomY = bottomY - tipHeight;
		int r = color >> 16 & 0xFF;
		int g = color >> 8 & 0xFF;
		int b = color & 0xFF;
		int a = color >> 24 & 0xFF;

		for (int i = 0; i < CRYSTAL_SIDES; i++) {
			float angle1 = (float) (Math.PI * 2.0 * i / CRYSTAL_SIDES);
			float angle2 = (float) (Math.PI * 2.0 * (i + 1) / CRYSTAL_SIDES);
			float x1 = (float) Math.cos(angle1) * radius;
			float z1 = (float) Math.sin(angle1) * radius;
			float x2 = (float) Math.cos(angle2) * radius;
			float z2 = (float) Math.sin(angle2) * radius;

			line(buffer, matrix, x1, topY, z1, x2, topY, z2, r, g, b, a);
			line(buffer, matrix, x1, bottomY, z1, x2, bottomY, z2, r, g, b, a);
			line(buffer, matrix, x1, bottomY, z1, x1, topY, z1, r, g, b, a);
			line(buffer, matrix, 0.0F, tipTopY, 0.0F, x1, topY, z1, r, g, b, a);
			line(buffer, matrix, 0.0F, tipBottomY, 0.0F, x1, bottomY, z1, r, g, b, a);
		}
	}

	private static void line(VertexConsumer buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a) {
		buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
		buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
	}

	private static void triangle(VertexConsumer buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int color, int alpha) {
		vertex(buffer, matrix, x1, y1, z1, color, alpha);
		vertex(buffer, matrix, x2, y2, z2, color, alpha);
		vertex(buffer, matrix, x3, y3, z3, color, alpha);
		vertex(buffer, matrix, x3, y3, z3, color, alpha);
	}

	private static void quad(VertexConsumer buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, int color, int alpha) {
		vertex(buffer, matrix, x1, y1, z1, color, alpha);
		vertex(buffer, matrix, x2, y2, z2, color, alpha);
		vertex(buffer, matrix, x3, y3, z3, color, alpha);
		vertex(buffer, matrix, x4, y4, z4, color, alpha);
	}

	private static void vertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, int color, int alpha) {
		int r = color >> 16 & 0xFF;
		int g = color >> 8 & 0xFF;
		int b = color & 0xFF;
		buffer.vertex(matrix, x, y, z).color(r, g, b, alpha);
	}

	private static int multiplyColor(int color, float factor) {
		int r = Math.min(255, (int) (((color >> 16) & 0xFF) * factor));
		int g = Math.min(255, (int) (((color >> 8) & 0xFF) * factor));
		int b = Math.min(255, (int) ((color & 0xFF) * factor));
		return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
	}

	static void drawGlow(VertexConsumer buffer, Matrix4f matrix, int color, int alpha, float size) {
		int r = color >> 16 & 0xFF;
		int g = color >> 8 & 0xFF;
		int b = color & 0xFF;
		float half = size / 2.0F;

		buffer.vertex(matrix, -half, -half, 0).texture(0, 0).color(r, g, b, alpha);
		buffer.vertex(matrix, -half, half, 0).texture(0, 1).color(r, g, b, alpha);
		buffer.vertex(matrix, half, half, 0).texture(1, 1).color(r, g, b, alpha);
		buffer.vertex(matrix, half, -half, 0).texture(1, 0).color(r, g, b, alpha);
	}
}
