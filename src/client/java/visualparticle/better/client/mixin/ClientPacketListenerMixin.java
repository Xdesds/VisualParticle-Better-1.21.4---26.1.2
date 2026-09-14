package visualparticle.better.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import visualparticle.better.client.ClientCore;
import visualparticle.better.client.module.impl.render.ParticlesModule;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    private static ParticlesModule visualparticlebetter$particles(){
        return ClientCore.getInstance().modules().find("particles")
                .filter(ParticlesModule.class::isInstance).map(ParticlesModule.class::cast).orElse(null);
    }

    @Inject(method="handleEntityEvent",at=@At("HEAD"))
    private void visualparticlebetter$entityEvent(ClientboundEntityEventPacket packet,CallbackInfo ci){
        Minecraft mc=Minecraft.getInstance();
        if(packet.getEventId()!=35||mc.level==null)return;
        var entity=packet.getEntity(mc.level);
        ParticlesModule module=visualparticlebetter$particles();
        if(entity!=null&&module!=null)module.spawnTotem(entity);
    }

    @Inject(method="handleTakeItemEntity",at=@At("HEAD"))
    private void visualparticlebetter$itemPickup(ClientboundTakeItemEntityPacket packet,CallbackInfo ci){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||packet.getPlayerId()!=mc.player.getId())return;
        var entity=mc.level.getEntity(packet.getItemId());
        ParticlesModule module=visualparticlebetter$particles();
        if(entity!=null&&module!=null)module.spawnItemPickup(entity);
    }
}