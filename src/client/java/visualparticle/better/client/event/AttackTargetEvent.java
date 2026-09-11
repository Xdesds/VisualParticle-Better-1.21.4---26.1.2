package visualparticle.better.client.event;

import net.minecraft.world.entity.LivingEntity;

public record AttackTargetEvent(LivingEntity target, long attackedAt) implements Event {}