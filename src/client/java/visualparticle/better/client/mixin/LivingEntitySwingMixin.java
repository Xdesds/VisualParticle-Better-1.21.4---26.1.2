package visualparticle.better.client.mixin;

import visualparticle.better.client.ClientCore;
import visualparticle.better.client.module.impl.render.HoldMyItemsModule;
import visualparticle.better.client.module.impl.render.SwingAnimationModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySwingMixin {
    @Inject(method = "getCurrentSwingDuration", at = @At("HEAD"), cancellable = true)
    private void visualparticlebetter$swingDuration(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this != Minecraft.getInstance().player) return;
        ClientCore.getInstance().modules().find("swing_animation")
                .filter(SwingAnimationModule.class::isInstance).map(SwingAnimationModule.class::cast)
                .filter(SwingAnimationModule::customDuration)
                .ifPresent(module -> cir.setReturnValue(module.swingDuration()));
        if (cir.isCancelled()) return;
        ClientCore.getInstance().modules().find("hold_my_items")
                .filter(HoldMyItemsModule.class::isInstance).map(HoldMyItemsModule.class::cast)
                .filter(HoldMyItemsModule::enabled)
                .ifPresent(module -> cir.setReturnValue(module.swingDuration()));
    }
}
