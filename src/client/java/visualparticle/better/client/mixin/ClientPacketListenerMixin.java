package visualparticle.better.client.mixin;

import visualparticle.better.client.ClientCore;
import visualparticle.better.client.module.impl.render.ParticlesModule;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method="handleEntityEvent",at=@At("HEAD"))
    private void visualparticlebetter$entityEvent(ClientboundEntityEventPacket packet,CallbackInfo ci){
        Minecraft mc=Minecraft.getInstance();
        if(packet.getEventId()!=35||mc.level==null)return;
        var entity=packet.getEntity(mc.level);
        if(entity==null)return;
        ClientCore.getInstance().modules().find("particles").filter(ParticlesModule.class::isInstance)
                .map(ParticlesModule.class::cast).ifPresent(module->module.spawnTotem(entity));
    }}