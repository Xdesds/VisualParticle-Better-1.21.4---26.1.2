package particle.fx.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class ParticleSystem {
	private static final ParticleSystem INSTANCE = new ParticleSystem();
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final int DEFAULT_COLOR = 0xFF896148;
	private static final int BLUE_COLOR = 0xFF4A90E2;
	private static final int RED_COLOR = 0xFFC54A39;
	private static final int TOTEM_DURATION = 20;
	private static final float GRAVITY_STRENGTH = 0.04F;
	private static final double WORLD_MIN_RADIUS = 3.0;
	private static final double WORLD_MAX_RADIUS = 60.0;
	private static final double WORLD_MAX_HEIGHT = 25.0;
	private static final double WORLD_DESPAWN_DISTANCE = 65.0;
	private static final int[] RANDOM_COLORS = {
			0xFFFF0000,
			0xFFFF7F00,
			0xFFFFFF00,
			0xFF00FF00,
			0xFF00FFFF,
			0xFF0000FF,
			0xFF8B00FF,
			0xFFFF00FF,
			0xFFFF1493,
			0xFFFFFFFF,
			0xFF00FF7F,
			0xFFFF6347
	};

	private final List<Particle3D> particles = new ArrayList<>();
	private final List<Particle3D> worldParticles = new ArrayList<>();
	private final List<TotemEmitter> totemEmitters = new ArrayList<>();
	private final Settings settings = new Settings();
	private float walkParticleAccumulator;
	private Vec3 lastPlayerPos = Vec3.ZERO;
	private Vec3 playerVelocity = Vec3.ZERO;
	private double playerSpeed;
	private long lastWorldSpawnTime;

	private ParticleSystem() {
	}

	public static ParticleSystem getInstance() {
		return INSTANCE;
	}

	public Settings getSettings() {
		return settings;
	}

	public void tick(Minecraft client) {
		if (!settings.enabled || client.player == null || client.level == null) {
			clear();
			return;
		}

		if (settings.walkTrigger) {
			handleWalkParticles(client);
		} else {
			walkParticleAccumulator = 0.0F;
		}

		if (settings.projectileTrigger) {
			handleProjectileParticles(client);
		}

		handleTotemEmitters();
		handleWorldParticles(client);
		updateParticles(particles);
		updateParticles(worldParticles);
	}

	public void spawnAttackParticles(Entity target) {
		if (!settings.enabled || !settings.attackTrigger || target == null) {
			return;
		}

		float spreadValue = settings.spread * 0.15F;
		for (int i = 0; i < settings.attackAmount; i++) {
			Vec3 position = new Vec3(
					target.getX(),
					target.getY() + Math.random() * target.getBbHeight(),
					target.getZ()
			);
			Vec3 velocity = new Vec3(
					(Math.random() - 0.5) * 2.0 * spreadValue * settings.speed,
					(Math.random() - 0.5) * 2.0 * spreadValue * settings.speed,
					(Math.random() - 0.5) * 2.0 * spreadValue * settings.speed
			);

			particles.add(createParticle(position, velocity, settings.size, settings.lifeTime));
		}
	}

	public void spawnTotemParticles(Entity entity) {
		if (settings.enabled && settings.totemTrigger && entity != null) {
			totemEmitters.add(new TotemEmitter(entity, TOTEM_DURATION));
		}
	}

	public void render(LevelRenderContext context) {
		if ((particles.isEmpty() && worldParticles.isEmpty()) || CLIENT.player == null || CLIENT.level == null) {
			return;
		}

		PoseStack matrices = context.poseStack();
		MultiBufferSource.BufferSource immediate = context.bufferSource();
		float tickDelta = CLIENT.getDeltaTracker().getGameTimeDeltaPartialTick(true);

		for (Particle3D particle : particles) {
			particle.render(matrices, immediate, settings.glowSize, tickDelta);
		}

		for (Particle3D particle : worldParticles) {
			particle.render(matrices, immediate, settings.worldGlowSize, tickDelta);
		}

		immediate.endBatch();
	}

	private void clear() {
		particles.clear();
		worldParticles.clear();
		totemEmitters.clear();
		walkParticleAccumulator = 0.0F;
		lastPlayerPos = Vec3.ZERO;
		playerVelocity = Vec3.ZERO;
		playerSpeed = 0.0;
	}

	private void handleWalkParticles(Minecraft client) {
		double velocitySq = client.player.getDeltaMovement().lengthSqr();
		boolean isMoving = velocitySq > 0.0001 && !client.player.isShiftKeyDown();

		if (!isMoving) {
			walkParticleAccumulator = 0.0F;
			return;
		}

		float particlesPerTick = settings.walkAmount / 20.0F;
		walkParticleAccumulator += particlesPerTick;
		int particlesToSpawn = (int) walkParticleAccumulator;
		walkParticleAccumulator -= particlesToSpawn;

		if (particlesToSpawn <= 0) {
			return;
		}

		float yaw = client.player.getYRot();
		double radians = Math.toRadians(yaw + 90.0F);
		double offsetX = Math.cos(radians) * 0.5;
		double offsetZ = Math.sin(radians) * 0.5;
		float spreadValue = settings.spread * 0.05F;

		for (int i = 0; i < particlesToSpawn; i++) {
			Vec3 position = new Vec3(
					client.player.getX() - offsetX + (Math.random() - 0.5) * 0.3,
					client.player.getY() + 0.3 + Math.random() * (client.player.getBbHeight() - 0.3),
					client.player.getZ() - offsetZ + (Math.random() - 0.5) * 0.3
			);
			Vec3 velocity = new Vec3(
					(Math.random() - 0.5) * spreadValue * settings.speed,
					(Math.random() - 0.5) * spreadValue * 0.5 * settings.speed,
					(Math.random() - 0.5) * spreadValue * settings.speed
			);

			particles.add(createParticle(position, velocity, settings.size * 0.6F, settings.lifeTime * 0.5F));
		}
	}

	private void handleProjectileParticles(Minecraft client) {
		float spreadValue = settings.spread * 0.03F;

		for (Entity entity : client.level.entitiesForRendering()) {
			if (!(entity instanceof Projectile projectile)) {
				continue;
			}

			boolean isMoving = Math.abs(projectile.getX() - projectile.xo) > 0.01
					|| Math.abs(projectile.getY() - projectile.yo) > 0.01
					|| Math.abs(projectile.getZ() - projectile.zo) > 0.01;

			if (!isMoving && projectile.getDeltaMovement().lengthSqr() <= 0.01) {
				continue;
			}

			for (int i = 0; i < 2; i++) {
				Vec3 position = new Vec3(
						projectile.getX() + (Math.random() - 0.5) * 0.5,
						projectile.getY() + Math.random() * projectile.getBbHeight(),
						projectile.getZ() + (Math.random() - 0.5) * 0.5
				);
				Vec3 velocity = new Vec3(
						(Math.random() - 0.5) * 2.0 * spreadValue * settings.speed,
						(Math.random() - 0.5) * 2.0 * spreadValue * settings.speed,
						(Math.random() - 0.5) * 2.0 * spreadValue * settings.speed
				);

				particles.add(createParticle(position, velocity, settings.size * 0.5F, settings.lifeTime * 0.3F));
			}
		}
	}

	private void handleTotemEmitters() {
		Iterator<TotemEmitter> iterator = totemEmitters.iterator();
		while (iterator.hasNext()) {
			TotemEmitter emitter = iterator.next();
			emitter.tick();

			if (emitter.isAlive()) {
				spawnTotemBurst(emitter.getEntity(), emitter.getProgress());
			} else {
				iterator.remove();
			}
		}
	}

	private void handleWorldParticles(Minecraft client) {
		if (!settings.worldParticles) {
			worldParticles.clear();
			lastPlayerPos = Vec3.ZERO;
			playerVelocity = Vec3.ZERO;
			playerSpeed = 0.0;
			return;
		}

		Vec3 currentPos = client.player.position();
		if (lastPlayerPos != Vec3.ZERO) {
			playerVelocity = currentPos.subtract(lastPlayerPos);
			playerSpeed = playerVelocity.horizontalDistance();
		}
		lastPlayerPos = currentPos;

		double despawnDistanceSq = WORLD_DESPAWN_DISTANCE * WORLD_DESPAWN_DISTANCE;
		Iterator<Particle3D> iterator = worldParticles.iterator();
		while (iterator.hasNext()) {
			Particle3D particle = iterator.next();
			if (particle.getHorizontalDistanceSquaredTo(currentPos) > despawnDistanceSq) {
				iterator.remove();
			}
		}

		int delay = calculateWorldSpawnDelay(playerSpeed);
		long now = System.currentTimeMillis();
		if (worldParticles.size() >= settings.worldAmount || now - lastWorldSpawnTime < delay) {
			return;
		}

		int spawnCount = calculateWorldSpawnCount(playerSpeed, worldParticles.size(), settings.worldAmount);
		for (int i = 0; i < spawnCount && worldParticles.size() < settings.worldAmount; i++) {
			worldParticles.add(createWorldParticle(currentPos));
		}

		lastWorldSpawnTime = now;
	}

	private void updateParticles(List<Particle3D> targetParticles) {
		Iterator<Particle3D> iterator = targetParticles.iterator();
		while (iterator.hasNext()) {
			Particle3D particle = iterator.next();
			particle.update();

			if (particle.isDead()) {
				iterator.remove();
			}
		}
	}

	private void spawnTotemBurst(Entity entity, float progress) {
		if (entity == null || entity.isRemoved()) {
			return;
		}

		float spreadMultiplier = 1.0F - progress * 0.5F;
		for (int i = 0; i < 4; i++) {
			double x = Math.random() * 2.0 - 1.0;
			double y = Math.random() * 2.0 - 1.0;
			double z = Math.random() * 2.0 - 1.0;

			if (x * x + y * y + z * z > 1.0) {
				continue;
			}

			Vec3 position = new Vec3(
					entity.getX() + x * entity.getBbWidth() * 0.5,
					entity.getY(0.5) + y * entity.getBbHeight() * 0.5,
					entity.getZ() + z * entity.getBbWidth() * 0.5
			);
			double velocityScale = settings.spread * 0.18 * spreadMultiplier * settings.speed;
			double upward = Math.random() < 0.4
					? (0.15 + Math.random() * 0.2) * settings.speed
					: (0.03 + Math.random() * 0.07) * settings.speed;
			Vec3 velocity = new Vec3(x * velocityScale, upward, z * velocityScale);
			particles.add(createParticle(position, velocity, settings.size * 0.8F, settings.lifeTime * 0.8F, getTotemColor()));
		}
	}

	private Particle3D createParticle(Vec3 position, Vec3 velocity, float size, float lifeTime) {
		return createParticle(position, velocity, size, lifeTime, getParticleColor());
	}

	private Particle3D createParticle(Vec3 position, Vec3 velocity, float size, float lifeTime, int color) {
		return new Particle3D(position, velocity, color, size, lifeTime)
				.setGravity(getGravity())
				.setVelocityMultiplier(0.99F)
				.setMode(settings.particleMode)
				.setGlowMode(settings.glowMode);
	}

	private Particle3D createWorldParticle(Vec3 playerPos) {
		double radius = WORLD_MIN_RADIUS + Math.random() * (WORLD_MAX_RADIUS - WORLD_MIN_RADIUS);
		double angle = Math.random() * Math.PI * 2.0;
		double spawnX = playerPos.x;
		double spawnZ = playerPos.z;

		if (playerSpeed > 0.05 && playerVelocity.horizontalDistance() > 0.01) {
			Vec3 normalizedVelocity = playerVelocity.normalize();
			double forwardAngle = Math.atan2(normalizedVelocity.z, normalizedVelocity.x);
			double angleSpread = Math.PI * 0.4;
			angle = forwardAngle + (Math.random() - 0.5) * angleSpread * 2.0;
			double forwardOffset = radius * 0.7 * Math.min(playerSpeed * 8.0, 1.0);
			spawnX += normalizedVelocity.x * forwardOffset;
			spawnZ += normalizedVelocity.z * forwardOffset;
		}

		Vec3 position = new Vec3(
				spawnX + Math.cos(angle) * radius,
				playerPos.y - 5.0 + Math.random() * WORLD_MAX_HEIGHT,
				spawnZ + Math.sin(angle) * radius
		);
		Vec3 velocity = new Vec3(
				(Math.random() - 0.5) * 0.08,
				(Math.random() - 0.5) * 0.02,
				(Math.random() - 0.5) * 0.08
		);

		float gravity = settings.worldPhysics ? 0.0002F : 0.0F;
		return new Particle3D(position, velocity, getParticleColor(), settings.worldSize, settings.worldLifeTime)
				.setGravity(gravity)
				.setVelocityMultiplier(0.99F)
				.setMode(settings.worldMode)
				.setGlowMode(settings.glowMode)
				.setCollision(settings.worldPhysics);
	}

	private static int calculateWorldSpawnDelay(double playerSpeed) {
		int baseDelay = 40;
		if (playerSpeed <= 0.05) {
			return baseDelay;
		}

		double speedFactor = Math.min(playerSpeed * 5.0, 4.0);
		return Math.max((int) (baseDelay / (1.0 + speedFactor)), 8);
	}

	private static int calculateWorldSpawnCount(double playerSpeed, int currentCount, int maxCount) {
		int spawnCount = 1;
		if (playerSpeed > 0.1) {
			spawnCount = (int) Math.min(8, maxCount - currentCount);
			spawnCount = Math.max(1, (int) (spawnCount * Math.min(playerSpeed * 5.0, 1.0)));
		}
		return spawnCount;
	}

	private int getParticleColor() {
		return switch (settings.colorPreset) {
			case DEFAULT -> DEFAULT_COLOR;
			case BLUE -> BLUE_COLOR;
			case RED -> RED_COLOR;
			case RANDOM -> RANDOM_COLORS[ThreadLocalRandom.current().nextInt(RANDOM_COLORS.length)];
		};
	}

	private int getTotemColor() {
		int[] totemColors = {
				0xFF7CFC00,
				0xFFFFD700,
				0xFF32CD32,
				0xFFFFA500,
				0xFF00FF00,
				0xFFADFF2F
		};
		return totemColors[ThreadLocalRandom.current().nextInt(totemColors.length)];
	}

	private float getGravity() {
		return (1.0F - 0.9F) * GRAVITY_STRENGTH;
	}

	public static final class Settings {
		public ColorPreset colorPreset = ColorPreset.DEFAULT;
		public boolean enabled = true;
		public boolean attackTrigger = true;
		public boolean totemTrigger = true;
		public boolean walkTrigger = true;
		public boolean projectileTrigger = true;
		public boolean worldParticles = true;
		public boolean worldPhysics = false;
		public Particle3D.ParticleMode particleMode = Particle3D.ParticleMode.STAR;
		public Particle3D.ParticleMode worldMode = Particle3D.ParticleMode.STAR;
		public Particle3D.GlowMode glowMode = Particle3D.GlowMode.BOTH;
		public int attackAmount = 40;
		public int walkAmount = 30;
		public int worldAmount = 100;
		public float spread = 1.0F;
		public float speed = 2.0F;
		public float lifeTime = 2.5F;
		public float size = 1.0F;
		public float glowSize = 7.5F;
		public float worldLifeTime = 10.0F;
		public float worldSize = 1.5F;
		public float worldGlowSize = 3.0F;
		public void reset() {
			colorPreset = ColorPreset.DEFAULT;
			enabled = true;
			attackTrigger = true;
			totemTrigger = true;
			walkTrigger = true;
			projectileTrigger = true;
			worldParticles = true;
			worldPhysics = false;
			particleMode = Particle3D.ParticleMode.STAR;
			worldMode = Particle3D.ParticleMode.STAR;
			glowMode = Particle3D.GlowMode.BOTH;
			attackAmount = 40;
			walkAmount = 30;
			worldAmount = 100;
			spread = 1.0F;
			speed = 2.0F;
			lifeTime = 2.5F;
			size = 1.0F;
			glowSize = 7.5F;
			worldLifeTime = 10.0F;
			worldSize = 1.5F;
			worldGlowSize = 3.0F;
		}
	}

	public enum ColorPreset {
		DEFAULT,
		BLUE,
		RED,
		RANDOM
	}
}
