package particle.fx.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import particle.fx.particle.ParticleSystem;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {
	@Inject(method = "handleEntityEvent", at = @At("HEAD"))
	private void particleFx$onEntityStatus(ClientboundEntityEventPacket packet, CallbackInfo ci) {
		if (packet.getEventId() != 35) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		if (client.level == null) {
			return;
		}

		Entity entity = packet.getEntity(client.level);
		if (entity != null) {
			ParticleSystem.getInstance().spawnTotemParticles(entity);
		}
	}
}
