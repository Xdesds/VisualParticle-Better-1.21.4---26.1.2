package particle.fx.particle;

import net.minecraft.client.render.VertexConsumer;
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

	private static void line(VertexConsumer buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a) {
		buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
		buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
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
