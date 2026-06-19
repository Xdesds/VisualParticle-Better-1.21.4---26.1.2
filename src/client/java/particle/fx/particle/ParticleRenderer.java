package particle.fx.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

final class ParticleRenderer {
	private ParticleRenderer() {
	}

	static void drawCube(VertexConsumer buffer, Matrix4f matrix, int color, float size) {
		float half = size / 2.0F;
		int r = color >> 16 & 0xFF;
		int g = color >> 8 & 0xFF;
		int b = color & 0xFF;
		int a = color >> 24 & 0xFF;

		vertex(buffer, matrix, -half, half, -half, 0.0F, 0.0F, r, g, b, a);
		vertex(buffer, matrix, -half, half, half, 0.0F, 1.0F, r, g, b, a);
		vertex(buffer, matrix, half, half, half, 1.0F, 1.0F, r, g, b, a);
		vertex(buffer, matrix, half, half, -half, 1.0F, 0.0F, r, g, b, a);

		vertex(buffer, matrix, -half, -half, -half, 0.0F, 0.0F, r, g, b, a);
		vertex(buffer, matrix, half, -half, -half, 1.0F, 0.0F, r, g, b, a);
		vertex(buffer, matrix, half, -half, half, 1.0F, 1.0F, r, g, b, a);
		vertex(buffer, matrix, -half, -half, half, 0.0F, 1.0F, r, g, b, a);

		vertex(buffer, matrix, -half, half, half, 0.0F, 0.0F, r, g, b, a);
		vertex(buffer, matrix, -half, -half, half, 0.0F, 1.0F, r, g, b, a);
		vertex(buffer, matrix, half, -half, half, 1.0F, 1.0F, r, g, b, a);
		vertex(buffer, matrix, half, half, half, 1.0F, 0.0F, r, g, b, a);

		vertex(buffer, matrix, -half, half, -half, 0.0F, 0.0F, r, g, b, a);
		vertex(buffer, matrix, half, half, -half, 1.0F, 0.0F, r, g, b, a);
		vertex(buffer, matrix, half, -half, -half, 1.0F, 1.0F, r, g, b, a);
		vertex(buffer, matrix, -half, -half, -half, 0.0F, 1.0F, r, g, b, a);

		vertex(buffer, matrix, -half, half, -half, 0.0F, 0.0F, r, g, b, a);
		vertex(buffer, matrix, -half, -half, -half, 0.0F, 1.0F, r, g, b, a);
		vertex(buffer, matrix, -half, -half, half, 1.0F, 1.0F, r, g, b, a);
		vertex(buffer, matrix, -half, half, half, 1.0F, 0.0F, r, g, b, a);

		vertex(buffer, matrix, half, half, -half, 0.0F, 0.0F, r, g, b, a);
		vertex(buffer, matrix, half, half, half, 1.0F, 0.0F, r, g, b, a);
		vertex(buffer, matrix, half, -half, half, 1.0F, 1.0F, r, g, b, a);
		vertex(buffer, matrix, half, -half, -half, 0.0F, 1.0F, r, g, b, a);
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

	private static void line(VertexConsumer buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a) {
		buffer.addVertex(matrix, x1, y1, z1)
				.setColor(r, g, b, a)
				.setUv1(0, 0)
				.setUv2(240, 240)
				.setNormal(0, 1, 0)
				.setLineWidth(2.0F);
		buffer.addVertex(matrix, x2, y2, z2)
				.setColor(r, g, b, a)
				.setUv1(0, 0)
				.setUv2(240, 240)
				.setNormal(0, 1, 0)
				.setLineWidth(2.0F);
	}

	static void drawGlow(VertexConsumer buffer, Matrix4f matrix, int color, int alpha, float size) {
		int r = color >> 16 & 0xFF;
		int g = color >> 8 & 0xFF;
		int b = color & 0xFF;
		float half = size / 2.0F;

		texturedVertex(buffer, matrix, -half, -half, 0, 0, r, g, b, alpha);
		texturedVertex(buffer, matrix, -half, half, 0, 1, r, g, b, alpha);
		texturedVertex(buffer, matrix, half, half, 1, 1, r, g, b, alpha);
		texturedVertex(buffer, matrix, half, -half, 1, 0, r, g, b, alpha);
	}

	static void texturedVertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float u, float v, int r, int g, int b, int a) {
		vertex(buffer, matrix, x, y, 0.0F, u, v, r, g, b, a);
	}

	private static void vertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, float u, float v, int r, int g, int b, int a) {
		buffer.addVertex(matrix, x, y, z)
				.setColor(r, g, b, a)
				.setUv(u, v)
				.setUv1(0, 0)
				.setUv2(240, 240)
				.setNormal(0, 0, 1);
	}
}
