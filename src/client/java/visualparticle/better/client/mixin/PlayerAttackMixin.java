package visualparticle.better.client.mixin;

import visualparticle.better.client.ClientCore;
import visualparticle.better.client.event.AttackTargetEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Direct 1.21.11 port of D:\VisualParticle Better's MixinPlayerEntity attack hook. */
@Mixin(Player.class)
public abstract class PlayerAttackMixin {
	@Inject(method = "attack", at = @At("HEAD"))
	private void visualParticleBetter$trackActualAttack(Entity target, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if ((Object) this != client.player || !(target instanceof LivingEntity living)) return;
		ClientCore.getInstance().events().post(new AttackTargetEvent(living, System.currentTimeMillis()));

	}
}
