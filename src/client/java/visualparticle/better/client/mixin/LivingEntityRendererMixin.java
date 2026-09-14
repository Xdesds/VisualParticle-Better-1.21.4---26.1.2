package visualparticle.better.client.mixin;

import visualparticle.better.client.ClientCore;
import visualparticle.better.client.module.impl.render.HitColorModule;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity,S extends LivingEntityRenderState,M extends EntityModel<? super S>> {
    @Inject(method="getModelTint",at=@At("RETURN"),cancellable=true)
    private void visualparticlebetter$hitColor(S state,CallbackInfoReturnable<Integer> cir){ClientCore.getInstance().modules().find("hit_color").filter(HitColorModule.class::isInstance).map(HitColorModule.class::cast).filter(HitColorModule::enabled).ifPresent(module->cir.setReturnValue(module.tint(state,cir.getReturnValue())));}
}