package visualparticle.better.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import visualparticle.better.client.ClientCore;
import visualparticle.better.client.module.impl.render.HoldMyItemsModule;
import visualparticle.better.client.module.impl.render.SwingAnimationModule;
import visualparticle.better.client.module.impl.render.ViewModelModule;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER))
    private void visualparticlebetter$viewModel(AbstractClientPlayer player, float partialTick, float pitch,
                                       InteractionHand hand, float swing, ItemStack stack, float equip,
                                       PoseStack pose, SubmitNodeCollector collector, int light, CallbackInfo ci) {
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        ClientCore.getInstance().modules().find("view_model")
                .filter(ViewModelModule.class::isInstance).map(ViewModelModule.class::cast)
                .filter(ViewModelModule::enabled)
                .ifPresent(module -> pose.translate(module.x(arm), module.y(arm), module.z(arm)));
        ClientCore.getInstance().modules().find("hold_my_items")
                .filter(HoldMyItemsModule.class::isInstance).map(HoldMyItemsModule.class::cast)
                .filter(HoldMyItemsModule::enabled)
                .ifPresent(module -> pose.translate(module.x(), module.y(), module.z()));
    }

    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    private void visualparticlebetter$swing(float progress, PoseStack pose, int direction, HumanoidArm arm, CallbackInfo ci) {
        ClientCore.getInstance().modules().find("swing_animation")
                .filter(SwingAnimationModule.class::isInstance).map(SwingAnimationModule.class::cast)
                .filter(SwingAnimationModule::custom)
                .ifPresent(module -> {
                    module.apply(pose, arm, progress);
                    ci.cancel();
                });
    }
}
