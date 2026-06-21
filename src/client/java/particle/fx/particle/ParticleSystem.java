package particle.fx.particle;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class ParticleSystem {
	private static final ParticleSystem INSTANCE = new ParticleSystem();
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final int DEFAULT_COLOR = 0xFF896148;
	private static final int BLUE_COLOR = 0xFF4A90E2;
	private static final int RED_COLOR = 0xFFC54A39;
	private static final int TOTEM_DURATION = 20;
	private static final float GRAVITY_STRENGTH = 0.04F;
	private static final double ELYTRA_SPEED_THRESHOLD = 0.12;
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
	private Vec3d lastPlayerPos = Vec3d.ZERO;
	private Vec3d playerVelocity = Vec3d.ZERO;
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

	public void tick(MinecraftClient client) {
		if (!settings.enabled || client.player == null || client.world == null) {
			clear();
			return;
		}

		if (settings.walkTrigger) {
			handleWalkParticles(client);
		} else {
			walkParticleAccumulator = 0.0F;
		}

		if (settings.elytraTrigger) {
			handleElytraParticles(client);
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
			Vec3d position = new Vec3d(
					target.getX(),
					target.getY() + Math.random() * target.getHeight(),
					target.getZ()
			);
			Vec3d velocity = new Vec3d(
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

	public void render(WorldRenderContext context) {
		if ((particles.isEmpty() && worldParticles.isEmpty()) || CLIENT.player == null || CLIENT.world == null) {
			return;
		}

		MatrixStack matrices = context.matrices();
		VertexConsumerProvider.Immediate immediate = CLIENT.getBufferBuilders().getEntityVertexConsumers();
		float tickDelta = CLIENT.getRenderTickCounter().getTickProgress(true);

		for (Particle3D particle : particles) {
			particle.render(matrices, immediate, settings.glowSize, tickDelta);
		}

		for (Particle3D particle : worldParticles) {
			particle.render(matrices, immediate, settings.worldGlowSize, tickDelta);
		}

		immediate.draw();
	}

	private void clear() {
		particles.clear();
		worldParticles.clear();
		totemEmitters.clear();
		walkParticleAccumulator = 0.0F;
		lastPlayerPos = Vec3d.ZERO;
		playerVelocity = Vec3d.ZERO;
		playerSpeed = 0.0;
	}

	private void handleWalkParticles(MinecraftClient client) {
		double velocitySq = client.player.getVelocity().lengthSquared();
		boolean isMoving = velocitySq > 0.0001 && !client.player.isSneaking();

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

		float yaw = client.player.getYaw();
		double radians = Math.toRadians(yaw + 90.0F);
		double offsetX = Math.cos(radians) * 0.5;
		double offsetZ = Math.sin(radians) * 0.5;
		float spreadValue = settings.spread * 0.05F;

		for (int i = 0; i < particlesToSpawn; i++) {
			Vec3d position = new Vec3d(
					client.player.getX() - offsetX + (Math.random() - 0.5) * 0.3,
					client.player.getY() + 0.3 + Math.random() * (client.player.getHeight() - 0.3),
					client.player.getZ() - offsetZ + (Math.random() - 0.5) * 0.3
			);
			Vec3d velocity = new Vec3d(
					(Math.random() - 0.5) * spreadValue * settings.speed,
					(Math.random() - 0.5) * spreadValue * 0.5 * settings.speed,
					(Math.random() - 0.5) * spreadValue * settings.speed
			);

			particles.add(createParticle(position, velocity, settings.size * 0.6F, settings.lifeTime * 0.5F));
		}
	}

	private void handleProjectileParticles(MinecraftClient client) {
		float spreadValue = settings.spread * 0.03F;

		for (Entity entity : client.world.getEntities()) {
			if (!(entity instanceof ThrownEntity || entity instanceof ArrowEntity || entity instanceof TridentEntity)) {
				continue;
			}

			ProjectileEntity projectile = (ProjectileEntity) entity;
			boolean isMoving = Math.abs(projectile.getX() - projectile.lastX) > 0.01
					|| Math.abs(projectile.getY() - projectile.lastY) > 0.01
					|| Math.abs(projectile.getZ() - projectile.lastZ) > 0.01;

			if (!isMoving && projectile.getVelocity().lengthSquared() <= 0.01) {
				continue;
			}

			for (int i = 0; i < 2; i++) {
				Vec3d position = new Vec3d(
						projectile.getX() + (Math.random() - 0.5) * 0.5,
						projectile.getY() + Math.random() * projectile.getHeight(),
						projectile.getZ() + (Math.random() - 0.5) * 0.5
				);
				Vec3d velocity = new Vec3d(
						(Math.random() - 0.5) * 2.0 * spreadValue * settings.speed,
						(Math.random() - 0.5) * 2.0 * spreadValue * settings.speed,
						(Math.random() - 0.5) * 2.0 * spreadValue * settings.speed
				);

				particles.add(createParticle(position, velocity, settings.size * 0.5F, settings.lifeTime * 0.3F));
			}
		}
	}

	private void handleElytraParticles(MinecraftClient client) {
		if (!client.player.isGliding()) {
			return;
		}

		Vec3d velocity = client.player.getVelocity();
		double speed = velocity.length();
		if (speed < ELYTRA_SPEED_THRESHOLD) {
			return;
		}

		Vec3d direction = speed > 0.001 ? velocity.normalize() : client.player.getRotationVec(1.0F);
		Vec3d center = new Vec3d(
				client.player.getX(),
				client.player.getY() + client.player.getHeight() * 0.55,
				client.player.getZ()
		).subtract(direction.multiply(0.9));
		Vec3d side = direction.crossProduct(new Vec3d(0.0, 1.0, 0.0));
		if (side.lengthSquared() < 0.0001) {
			side = new Vec3d(1.0, 0.0, 0.0);
		} else {
			side = side.normalize();
		}

		int particleCount = Math.max(2, Math.min(6, (int) Math.round(speed * 10.0)));
		float size = settings.size * 0.55F;
		float lifeTime = Math.max(0.2F, settings.lifeTime * 0.45F);
		float spreadValue = settings.spread * 0.04F;

		for (int i = 0; i < particleCount; i++) {
			double sideOffset = (i % 2 == 0 ? -0.45 : 0.45) + (Math.random() - 0.5) * 0.18;
			Vec3d position = center
					.add(side.multiply(sideOffset))
					.add((Math.random() - 0.5) * 0.18, (Math.random() - 0.5) * 0.18, (Math.random() - 0.5) * 0.18);
			Vec3d particleVelocity = velocity.multiply(-0.08).add(
					(Math.random() - 0.5) * spreadValue * settings.speed,
					(Math.random() - 0.5) * spreadValue * 0.6 * settings.speed,
					(Math.random() - 0.5) * spreadValue * settings.speed
			);

			particles.add(createParticle(position, particleVelocity, size, lifeTime));
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

	private void handleWorldParticles(MinecraftClient client) {
		if (!settings.worldParticles) {
			worldParticles.clear();
			lastPlayerPos = Vec3d.ZERO;
			playerVelocity = Vec3d.ZERO;
			playerSpeed = 0.0;
			return;
		}

		Vec3d currentPos = client.player.getEntityPos();
		if (lastPlayerPos != Vec3d.ZERO) {
			playerVelocity = currentPos.subtract(lastPlayerPos);
			playerSpeed = playerVelocity.horizontalLength();
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

			Vec3d position = new Vec3d(
					entity.getX() + x * entity.getWidth() * 0.5,
					entity.getBodyY(0.5) + y * entity.getHeight() * 0.5,
					entity.getZ() + z * entity.getWidth() * 0.5
			);
			double velocityScale = settings.spread * 0.18 * spreadMultiplier * settings.speed;
			double upward = Math.random() < 0.4
					? (0.15 + Math.random() * 0.2) * settings.speed
					: (0.03 + Math.random() * 0.07) * settings.speed;
			Vec3d velocity = new Vec3d(x * velocityScale, upward, z * velocityScale);
			particles.add(createParticle(position, velocity, settings.size * 0.8F, settings.lifeTime * 0.8F, getTotemColor()));
		}
	}

	private Particle3D createParticle(Vec3d position, Vec3d velocity, float size, float lifeTime) {
		return createParticle(position, velocity, size, lifeTime, getParticleColor());
	}

	private Particle3D createParticle(Vec3d position, Vec3d velocity, float size, float lifeTime, int color) {
		return new Particle3D(position, velocity, color, size, lifeTime)
				.setGravity(getGravity())
				.setVelocityMultiplier(0.99F)
				.setMode(settings.particleMode)
				.setGlowMode(settings.glowMode);
	}

	private Particle3D createWorldParticle(Vec3d playerPos) {
		double radius = WORLD_MIN_RADIUS + Math.random() * (WORLD_MAX_RADIUS - WORLD_MIN_RADIUS);
		double angle = Math.random() * Math.PI * 2.0;
		double spawnX = playerPos.x;
		double spawnZ = playerPos.z;

		if (playerSpeed > 0.05 && playerVelocity.horizontalLength() > 0.01) {
			Vec3d normalizedVelocity = playerVelocity.normalize();
			double forwardAngle = Math.atan2(normalizedVelocity.z, normalizedVelocity.x);
			double angleSpread = Math.PI * 0.4;
			angle = forwardAngle + (Math.random() - 0.5) * angleSpread * 2.0;
			double forwardOffset = radius * 0.7 * Math.min(playerSpeed * 8.0, 1.0);
			spawnX += normalizedVelocity.x * forwardOffset;
			spawnZ += normalizedVelocity.z * forwardOffset;
		}

		Vec3d position = new Vec3d(
				spawnX + Math.cos(angle) * radius,
				playerPos.y - 5.0 + Math.random() * WORLD_MAX_HEIGHT,
				spawnZ + Math.sin(angle) * radius
		);
		Vec3d velocity = new Vec3d(
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
		public Preset preset = Preset.CUSTOM;
		public String customPresetName = "";
		public ColorPreset colorPreset = ColorPreset.DEFAULT;
		public boolean enabled = true;
		public boolean attackTrigger = true;
		public boolean totemTrigger = true;
		public boolean walkTrigger = true;
		public boolean elytraTrigger = true;
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

		public void applyPreset(Preset preset) {
			this.preset = preset;
			this.customPresetName = "";

			switch (preset) {
				case CUSTOM -> {
				}
				case PVP -> {
					colorPreset = ColorPreset.RED;
					attackTrigger = true;
					totemTrigger = true;
					walkTrigger = false;
					elytraTrigger = true;
					projectileTrigger = true;
					worldParticles = false;
					worldPhysics = false;
					particleMode = Particle3D.ParticleMode.LIGHTNING;
					worldMode = Particle3D.ParticleMode.STAR;
					glowMode = Particle3D.GlowMode.BOTH;
					attackAmount = 48;
					walkAmount = 10;
					worldAmount = 40;
					spread = 0.9F;
					speed = 2.6F;
					lifeTime = 1.3F;
					size = 0.85F;
					glowSize = 8.5F;
					worldLifeTime = 8.0F;
					worldSize = 1.0F;
					worldGlowSize = 2.0F;
				}
				case MINIMAL -> {
					colorPreset = ColorPreset.DEFAULT;
					attackTrigger = true;
					totemTrigger = true;
					walkTrigger = false;
					elytraTrigger = false;
					projectileTrigger = false;
					worldParticles = false;
					worldPhysics = false;
					particleMode = Particle3D.ParticleMode.LINE;
					worldMode = Particle3D.ParticleMode.STAR;
					glowMode = Particle3D.GlowMode.BLOOM;
					attackAmount = 16;
					walkAmount = 0;
					worldAmount = 20;
					spread = 0.45F;
					speed = 1.2F;
					lifeTime = 0.8F;
					size = 0.45F;
					glowSize = 3.0F;
					worldLifeTime = 6.0F;
					worldSize = 0.6F;
					worldGlowSize = 1.2F;
				}
				case CINEMATIC -> {
					colorPreset = ColorPreset.BLUE;
					attackTrigger = true;
					totemTrigger = true;
					walkTrigger = true;
					elytraTrigger = true;
					projectileTrigger = true;
					worldParticles = true;
					worldPhysics = false;
					particleMode = Particle3D.ParticleMode.STAR_ALT;
					worldMode = Particle3D.ParticleMode.SNOWFLAKE;
					glowMode = Particle3D.GlowMode.BOTH;
					attackAmount = 58;
					walkAmount = 28;
					worldAmount = 180;
					spread = 1.35F;
					speed = 1.75F;
					lifeTime = 3.8F;
					size = 1.15F;
					glowSize = 10.0F;
					worldLifeTime = 16.0F;
					worldSize = 1.35F;
					worldGlowSize = 4.6F;
				}
			}
		}

		public void markCustom() {
			if (preset != Preset.CUSTOM) {
				customPresetName = "";
			}
			preset = Preset.CUSTOM;
		}

		public void selectCustomPreset(String name) {
			preset = Preset.CUSTOM;
			customPresetName = name;
		}

		public void reset() {
			preset = Preset.CUSTOM;
			customPresetName = "";
			colorPreset = ColorPreset.DEFAULT;
			enabled = true;
			attackTrigger = true;
			totemTrigger = true;
			walkTrigger = true;
			elytraTrigger = true;
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

	public enum Preset {
		CUSTOM,
		PVP,
		MINIMAL,
		CINEMATIC
	}
}
