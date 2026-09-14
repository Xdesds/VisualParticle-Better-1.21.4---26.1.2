package visualparticle.better.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import visualparticle.better.client.ClientCore;
import visualparticle.better.client.module.impl.render.ParticlesModule;

@Mixin(MultiPlayerGameMode.class)
public abstract class BlockInteractionMixin {
    private static ParticlesModule visualparticlebetter$particles(){
        return ClientCore.getInstance().modules().find("particles")
                .filter(ParticlesModule.class::isInstance).map(ParticlesModule.class::cast).orElse(null);
    }

    @Inject(method="startDestroyBlock",at=@At("HEAD"))
    private void visualparticlebetter$hit(BlockPos pos,Direction face,CallbackInfoReturnable<Boolean> cir){
        ParticlesModule module=visualparticlebetter$particles();
        if(module!=null)module.spawnBlockHit(pos,face);
    }

    @Inject(method="destroyBlock",at=@At("RETURN"))
    private void visualparticlebetter$break(BlockPos pos,CallbackInfoReturnable<Boolean> cir){
        if(!cir.getReturnValue())return;
        ParticlesModule module=visualparticlebetter$particles();
        if(module!=null)module.spawnBlockBreak(pos);
    }

    @Inject(method="useItemOn",at=@At("RETURN"))
    private void visualparticlebetter$place(LocalPlayer player,InteractionHand hand,BlockHitResult hit,CallbackInfoReturnable<InteractionResult> cir){
        if(!cir.getReturnValue().consumesAction())return;
        ParticlesModule module=visualparticlebetter$particles();
        if(module!=null)module.spawnBlockPlace(hit.getBlockPos().relative(hit.getDirection()),hit.getDirection());
    }
}