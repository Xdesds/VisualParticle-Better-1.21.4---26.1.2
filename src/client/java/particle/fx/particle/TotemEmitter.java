package particle.fx.particle;

import net.minecraft.entity.Entity;

final class TotemEmitter {
	private final Entity entity;
	private final int maxAge;
	private int age;

	TotemEmitter(Entity entity, int maxAge) {
		this.entity = entity;
		this.maxAge = maxAge;
	}

	void tick() {
		age++;
	}

	boolean isAlive() {
		return age < maxAge && entity != null && !entity.isRemoved();
	}

	Entity getEntity() {
		return entity;
	}

	float getProgress() {
		return (float) age / (float) maxAge;
	}
}
