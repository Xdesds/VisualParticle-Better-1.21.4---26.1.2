package particle.fx.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import particle.fx.particle.ParticleSystem;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
	@Inject(method = "attack", at = @At("HEAD"))
	private void particleFx$onAttackEntity(Player player, Entity target, CallbackInfo ci) {
		ParticleSystem.getInstance().spawnAttackParticles(target);
	}
}
