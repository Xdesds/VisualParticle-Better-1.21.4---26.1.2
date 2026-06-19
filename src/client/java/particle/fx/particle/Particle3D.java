package particle.fx.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

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

	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final String MOD_ID = "visualparticle-better";
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

	Particle3D(Vec3 position, Vec3 velocity, int color, float scale, float maxAgeSeconds) {
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

		if (collidesWithWorld && CLIENT.level != null) {
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

	double getHorizontalDistanceSquaredTo(Vec3 position) {
		double dx = this.x - position.x;
		double dz = this.z - position.z;
		return dx * dx + dz * dz;
	}

	void render(PoseStack matrices, MultiBufferSource vertexConsumers, float glowSize, float tickDelta) {
		long now = System.currentTimeMillis();
		float alpha = getAlpha(now);
		if (alpha <= 0.0F || CLIENT.gameRenderer == null) {
			return;
		}

		Vec3 cameraPos = CLIENT.gameRenderer.getMainCamera().position();
		float cameraYaw = CLIENT.gameRenderer.getMainCamera().yRot();
		float cameraPitch = CLIENT.gameRenderer.getMainCamera().xRot();

		double interpX = Mth.lerp(tickDelta, this.lastX, this.x);
		double interpY = Mth.lerp(tickDelta, this.lastY, this.y);
		double interpZ = Mth.lerp(tickDelta, this.lastZ, this.z);

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
		if (CLIENT.level == null) {
			return false;
		}

		BlockPos pos = BlockPos.containing(x, y, z);
		return CLIENT.level.getBlockState(pos).isSolidRender();
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
		return Identifier.fromNamespaceAndPath(MOD_ID, "textures/world/" + fileName);
	}

	private void renderCube(PoseStack matrices, MultiBufferSource vertexConsumers, float relX, float relY, float relZ, float alpha, float glowSize, float cameraYaw, float cameraPitch) {
		long now = System.currentTimeMillis();
		float rotationAnim = (float) (now % 9000L) / 9000.0F * 360.0F;
		int glowColor = ColorUtil.withAlpha(color, alpha);
		float cubeSize = scale * 0.25F;
		float cubeGlow1 = cubeSize * glowSize * 1.15F;
		float cubeGlow2 = cubeSize * (glowSize / 2.7F);

		matrices.pushPose();
		matrices.translate(relX, relY, relZ);
		matrices.mulPose(Axis.YP.rotationDegrees(rotationAnim + this.phase));
		matrices.mulPose(Axis.XP.rotationDegrees(rotationAnim * 0.5F));
		Matrix4f matrix = matrices.last().pose();
		ParticleRenderer.drawCube(vertexConsumers.getBuffer(ParticleRenderLayers.QUADS), matrix, ColorUtil.withAlpha(color, alpha * 0.2F), cubeSize);
		ParticleRenderer.drawLines(vertexConsumers.getBuffer(ParticleRenderLayers.LINES), matrix, ColorUtil.withAlpha(color, alpha * 0.4F), cubeSize);
		matrices.popPose();

		matrices.pushPose();
		matrices.translate(relX, relY, relZ);
		matrices.mulPose(Axis.YP.rotationDegrees(-cameraYaw));
		matrices.mulPose(Axis.XP.rotationDegrees(cameraPitch));
		Matrix4f glowMatrix = matrices.last().pose();
		renderGlow(vertexConsumers, glowMatrix, glowColor, alpha, cubeGlow1, cubeGlow2);
		matrices.popPose();
	}

	private void renderTextured(PoseStack matrices, MultiBufferSource vertexConsumers, float relX, float relY, float relZ, float alpha, float glowSize, float cameraYaw, float cameraPitch) {
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

		matrices.pushPose();
		matrices.translate(relX, relY, relZ);
		matrices.mulPose(Axis.YP.rotationDegrees(-cameraYaw));
		matrices.mulPose(Axis.XP.rotationDegrees(cameraPitch));

		if (spinning) {
			matrices.mulPose(Axis.ZP.rotationDegrees(rotation));
		}

		Matrix4f matrix = matrices.last().pose();
		RenderType layer = ParticleRenderLayers.GLOW.apply(texture);
		VertexConsumer buffer = vertexConsumers.getBuffer(layer);
		float half = textureSize / 2.0F;

		ParticleRenderer.texturedVertex(buffer, matrix, -half, -half, 0, 0, r, g, b, a);
		ParticleRenderer.texturedVertex(buffer, matrix, -half, half, 0, 1, r, g, b, a);
		ParticleRenderer.texturedVertex(buffer, matrix, half, half, 1, 1, r, g, b, a);
		ParticleRenderer.texturedVertex(buffer, matrix, half, -half, 1, 0, r, g, b, a);
		matrices.popPose();

		matrices.pushPose();
		matrices.translate(relX, relY, relZ);
		matrices.mulPose(Axis.YP.rotationDegrees(-cameraYaw));
		matrices.mulPose(Axis.XP.rotationDegrees(cameraPitch));
		Matrix4f glowMatrix = matrices.last().pose();

		float glowSizePrimary = textureSize * glowSize * 0.58F;
		float glowSizeSecondary = textureSize * glowSize * 0.26F;
		renderGlow(vertexConsumers, glowMatrix, glowColor, alpha, glowSizePrimary, glowSizeSecondary);
		matrices.popPose();
	}

	private void renderGlow(MultiBufferSource vertexConsumers, Matrix4f matrix, int glowColor, float alpha, float sizePrimary, float sizeSecondary) {
		if (glowMode == GlowMode.BLOOM || glowMode == GlowMode.BOTH) {
			ParticleRenderer.drawGlow(vertexConsumers.getBuffer(ParticleRenderLayers.GLOW.apply(GLOW_BLOOM)), matrix, glowColor, (int) (120.0F * alpha), sizePrimary);
		}

		if (glowMode == GlowMode.BLOOM_SAMPLE || glowMode == GlowMode.BOTH) {
			ParticleRenderer.drawGlow(vertexConsumers.getBuffer(ParticleRenderLayers.GLOW.apply(GLOW_BLOOM_SAMPLE)), matrix, glowColor, (int) (210.0F * alpha), sizeSecondary);
		}
	}
}
