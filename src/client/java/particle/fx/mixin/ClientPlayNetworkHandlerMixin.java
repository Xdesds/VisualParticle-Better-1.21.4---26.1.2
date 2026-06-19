package particle.fx.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import particle.fx.particle.ParticleSystem;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Inject(method = "onEntityStatus", at = @At("HEAD"))
	private void particleFx$onEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci) {
		if (packet.getStatus() != 35) {
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null) {
			return;
		}

		Entity entity = packet.getEntity(client.world);
		if (entity != null) {
			ParticleSystem.getInstance().spawnTotemParticles(entity);
		}
	}
}
