package particle.fx.particle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import particle.fx.ParticleFxMod;

import java.util.concurrent.ThreadLocalRandom;

final class Particle3D {
	enum ParticleMode {
		CUBES,
		CROWN,
		CUBE_BLAST,
		DOLLAR,
		HEART,
		LIGHTNING,
		LINE,
		RHOMBUS,
		SNOWFLAKE,
		STAR,
		STAR_ALT,
		TRIANGLE,
		RANDOM
	}

	enum GlowMode {
		BLOOM,
		BLOOM_SAMPLE,
		BOTH
	}

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final int FADE_IN_MS = 150;
	private static final int FADE_OUT_MS = 250;
	private static final ParticleMode[] RANDOM_MODES = {
			ParticleMode.CUBES,
			ParticleMode.CROWN,
			ParticleMode.CUBE_BLAST,
			ParticleMode.DOLLAR,
			ParticleMode.HEART,
			ParticleMode.LIGHTNING,
			ParticleMode.LINE,
			ParticleMode.RHOMBUS,
			ParticleMode.SNOWFLAKE,
			ParticleMode.STAR,
			ParticleMode.STAR_ALT,
			ParticleMode.TRIANGLE
	};

	private static final Identifier TEXTURE_CROWN = texture("crown.png");
	private static final Identifier TEXTURE_CUBE_BLAST = texture("cubeblast1.png");
	private static final Identifier TEXTURE_DOLLAR = texture("dollar.png");
	private static final Identifier TEXTURE_HEART = texture("heart.png");
	private static final Identifier TEXTURE_LIGHTNING = texture("lightning.png");
	private static final Identifier TEXTURE_LINE = texture("line.png");
	private static final Identifier TEXTURE_RHOMBUS = texture("rhombus.png");
	private static final Identifier TEXTURE_SNOWFLAKE = texture("snowflake.png");
	private static final Identifier TEXTURE_STAR = texture("star.png");
	private static final Identifier TEXTURE_STAR_ALT = texture("star1.png");
	private static final Identifier TEXTURE_TRIANGLE = texture("triangle.png");
	private static final Identifier GLOW_BLOOM = texture("dashbloom.png");
	private static final Identifier GLOW_BLOOM_SAMPLE = texture("dashbloomsample.png");

	private double x;
	private double y;
	private double z;
	private double lastX;
	private double lastY;
	private double lastZ;
	private double velocityX;
	private double velocityY;
	private double velocityZ;
	private final long startTime;
	private long fadeOutStart = -1L;
	private final float phase;
	private final int color;
	private final float scale;
	private final long lifeTimeMs;
	private float rotation;
	private float gravityStrength = 0.004F;
	private float velocityMultiplier = 0.99F;
	private boolean collidesWithWorld = true;
	private ParticleMode actualMode = ParticleMode.CUBES;
	private GlowMode glowMode = GlowMode.BOTH;
	private boolean spinning = true;

	Particle3D(Vec3d position, Vec3d velocity, int color, float scale, float maxAgeSeconds) {
		this.startTime = System.currentTimeMillis();
		this.phase = (float) (Math.random() * 100.0);
		this.rotation = (float) (Math.random() * 360.0);
		this.x = position.x;
		this.y = position.y;
		this.z = position.z;
		this.lastX = position.x;
		this.lastY = position.y;
		this.lastZ = position.z;
		this.velocityX = velocity.x;
		this.velocityY = velocity.y;
		this.velocityZ = velocity.z;
		this.color = color;
		this.scale = scale;
		this.lifeTimeMs = (long) (maxAgeSeconds * 1000.0F);
	}

	Particle3D setGravity(float gravity) {
		this.gravityStrength = gravity;
		return this;
	}

	Particle3D setVelocityMultiplier(float multiplier) {
		this.velocityMultiplier = multiplier;
		return this;
	}

	Particle3D setCollision(boolean collision) {
		this.collidesWithWorld = collision;
		return this;
	}

	Particle3D setMode(ParticleMode mode) {
		if (mode == ParticleMode.RANDOM) {
			this.actualMode = RANDOM_MODES[ThreadLocalRandom.current().nextInt(RANDOM_MODES.length)];
		} else {
			this.actualMode = mode;
		}
		return this;
	}

	Particle3D setGlowMode(GlowMode glowMode) {
		this.glowMode = glowMode;
		return this;
	}

	Particle3D setSpinning(boolean spinning) {
		this.spinning = spinning;
		return this;
	}

	void update() {
		long now = System.currentTimeMillis();
		this.lastX = this.x;
		this.lastY = this.y;
		this.lastZ = this.z;

		this.velocityY -= gravityStrength;

		if (collidesWithWorld && CLIENT.world != null) {
			if (isHit(this.x + this.velocityX, this.y, this.z)) {
				this.velocityX *= -0.8;
			} else {
				this.x += this.velocityX;
			}

			if (isHit(this.x, this.y + this.velocityY, this.z)) {
				this.velocityX *= 0.999;
				this.velocityZ *= 0.999;
				this.velocityY *= -0.7;
			} else {
				this.y += this.velocityY;
			}

			if (isHit(this.x, this.y, this.z + this.velocityZ)) {
				this.velocityZ *= -0.8;
			} else {
				this.z += this.velocityZ;
			}
		} else {
			this.x += this.velocityX;
			this.y += this.velocityY;
			this.z += this.velocityZ;
		}

		this.velocityX *= velocityMultiplier;
		this.velocityY *= velocityMultiplier;
		this.velocityZ *= velocityMultiplier;

		if (spinning) {
			this.rotation += 2.0F;
		}

		if (fadeOutStart < 0L && now - startTime > lifeTimeMs) {
			fadeOutStart = now;
		}
	}

	boolean isDead() {
		return fadeOutStart >= 0L && getAlpha(System.currentTimeMillis()) <= 0.001F;
	}

	double getHorizontalDistanceSquaredTo(Vec3d position) {
		double dx = this.x - position.x;
		double dz = this.z - position.z;
		return dx * dx + dz * dz;
	}

	void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float glowSize, float tickDelta) {
		long now = System.currentTimeMillis();
		float alpha = getAlpha(now);
		if (alpha <= 0.0F || CLIENT.gameRenderer == null) {
			return;
		}

		Vec3d cameraPos = CLIENT.gameRenderer.getCamera().getCameraPos();
		float cameraYaw = CLIENT.gameRenderer.getCamera().getYaw();
		float cameraPitch = CLIENT.gameRenderer.getCamera().getPitch();

		double interpX = MathHelper.lerp(tickDelta, this.lastX, this.x);
		double interpY = MathHelper.lerp(tickDelta, this.lastY, this.y);
		double interpZ = MathHelper.lerp(tickDelta, this.lastZ, this.z);

		float relX = (float) (interpX - cameraPos.x);
		float relY = (float) (interpY - cameraPos.y);
		float relZ = (float) (interpZ - cameraPos.z);

		if (actualMode == ParticleMode.CUBES) {
			renderCube(matrices, vertexConsumers, relX, relY, relZ, alpha, glowSize, cameraYaw, cameraPitch);
		} else {
			renderTextured(matrices, vertexConsumers, relX, relY, relZ, alpha, glowSize, cameraYaw, cameraPitch);
		}
	}

	private float getAlpha(long now) {
		float fadeIn = ease(clamp((now - startTime) / (float) FADE_IN_MS));
		if (fadeOutStart < 0L) {
			return fadeIn;
		}

		float fadeOut = 1.0F - ease(clamp((now - fadeOutStart) / (float) FADE_OUT_MS));
		return Math.min(fadeIn, fadeOut);
	}

	private static float ease(float value) {
		if (value < 0.5F) {
			return 2.0F * value * value;
		}
		return 1.0F - (float) Math.pow(-2.0F * value + 2.0F, 2.0F) / 2.0F;
	}

	private static float clamp(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}

	private boolean isHit(double x, double y, double z) {
		if (CLIENT.world == null) {
			return false;
		}

		BlockPos pos = BlockPos.ofFloored(x, y, z);
		return CLIENT.world.getBlockState(pos).isFullCube(CLIENT.world, pos);
	}

	private Identifier getTexture() {
		return switch (actualMode) {
			case CROWN -> TEXTURE_CROWN;
			case CUBE_BLAST -> TEXTURE_CUBE_BLAST;
			case DOLLAR -> TEXTURE_DOLLAR;
			case HEART -> TEXTURE_HEART;
			case LIGHTNING -> TEXTURE_LIGHTNING;
			case LINE -> TEXTURE_LINE;
			case RHOMBUS -> TEXTURE_RHOMBUS;
			case SNOWFLAKE -> TEXTURE_SNOWFLAKE;
			case STAR -> TEXTURE_STAR;
			case STAR_ALT -> TEXTURE_STAR_ALT;
			case TRIANGLE -> TEXTURE_TRIANGLE;
			default -> null;
		};
	}

	private static Identifier texture(String fileName) {
		return Identifier.of(ParticleFxMod.MOD_ID, "textures/world/" + fileName);
	}

	private void renderCube(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float relX, float relY, float relZ, float alpha, float glowSize, float cameraYaw, float cameraPitch) {
		long now = System.currentTimeMillis();
		float rotationAnim = (float) (now % 9000L) / 9000.0F * 360.0F;
		int glowColor = ColorUtil.withAlpha(color, alpha);
		float cubeSize = scale * 0.25F;
		float cubeGlow1 = cubeSize * glowSize;
		float cubeGlow2 = cubeSize * (glowSize / 3.0F);

		matrices.push();
		matrices.translate(relX, relY, relZ);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationAnim + this.phase));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationAnim * 0.5F));
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		ParticleRenderer.drawCube(vertexConsumers.getBuffer(ParticleRenderLayers.QUADS), matrix, ColorUtil.withAlpha(color, alpha * 0.2F), cubeSize);
		ParticleRenderer.drawLines(vertexConsumers.getBuffer(ParticleRenderLayers.LINES), matrix, ColorUtil.withAlpha(color, alpha * 0.4F), cubeSize);
		matrices.pop();

		matrices.push();
		matrices.translate(relX, relY, relZ);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-cameraYaw));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cameraPitch));
		Matrix4f glowMatrix = matrices.peek().getPositionMatrix();
		renderGlow(vertexConsumers, glowMatrix, glowColor, alpha, cubeGlow1, cubeGlow2);
		matrices.pop();
	}

	private void renderTextured(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float relX, float relY, float relZ, float alpha, float glowSize, float cameraYaw, float cameraPitch) {
		Identifier texture = getTexture();
		if (texture == null) {
			return;
		}

		int glowColor = ColorUtil.withAlpha(color, alpha);
		float textureSize = scale * 0.5F;
		int r = glowColor >> 16 & 0xFF;
		int g = glowColor >> 8 & 0xFF;
		int b = glowColor & 0xFF;
		int a = (int) (255 * alpha);

		matrices.push();
		matrices.translate(relX, relY, relZ);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-cameraYaw));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cameraPitch));

		if (spinning) {
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotation));
		}

		Matrix4f matrix = matrices.peek().getPositionMatrix();
		RenderLayer layer = ParticleRenderLayers.GLOW.apply(texture);
		VertexConsumer buffer = vertexConsumers.getBuffer(layer);
		float half = textureSize / 2.0F;

		buffer.vertex(matrix, -half, -half, 0).texture(0, 0).color(r, g, b, a);
		buffer.vertex(matrix, -half, half, 0).texture(0, 1).color(r, g, b, a);
		buffer.vertex(matrix, half, half, 0).texture(1, 1).color(r, g, b, a);
		buffer.vertex(matrix, half, -half, 0).texture(1, 0).color(r, g, b, a);
		matrices.pop();

		matrices.push();
		matrices.translate(relX, relY, relZ);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-cameraYaw));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cameraPitch));
		Matrix4f glowMatrix = matrices.peek().getPositionMatrix();

		float glowSizePrimary = textureSize * glowSize * 0.5F;
		float glowSizeSecondary = textureSize * glowSize * 0.2F;
		renderGlow(vertexConsumers, glowMatrix, glowColor, alpha, glowSizePrimary, glowSizeSecondary);
		matrices.pop();
	}

	private void renderGlow(VertexConsumerProvider vertexConsumers, Matrix4f matrix, int glowColor, float alpha, float sizePrimary, float sizeSecondary) {
		if (glowMode == GlowMode.BLOOM || glowMode == GlowMode.BOTH) {
			ParticleRenderer.drawGlow(vertexConsumers.getBuffer(ParticleRenderLayers.GLOW.apply(GLOW_BLOOM)), matrix, glowColor, (int) (80.0F * alpha), sizePrimary);
		}

		if (glowMode == GlowMode.BLOOM_SAMPLE || glowMode == GlowMode.BOTH) {
			ParticleRenderer.drawGlow(vertexConsumers.getBuffer(ParticleRenderLayers.GLOW.apply(GLOW_BLOOM_SAMPLE)), matrix, glowColor, (int) (140.0F * alpha), sizeSecondary);
		}
	}
}
