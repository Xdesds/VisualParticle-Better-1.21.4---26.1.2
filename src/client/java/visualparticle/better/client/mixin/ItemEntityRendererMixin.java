package visualparticle.better.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import visualparticle.better.client.ClientCore;
import visualparticle.better.client.module.impl.render.ItemPhysicsModule;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {
    @Unique private final Map<ItemEntityRenderState,Boolean> visualparticlebetter$grounded=Collections.synchronizedMap(new WeakHashMap<>());
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V",at=@At("TAIL"))
    private void visualparticlebetter$capture(ItemEntity entity,ItemEntityRenderState state,float partialTick,CallbackInfo ci){visualparticlebetter$grounded.put(state,entity.onGround());}
    @Inject(method="submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V",shift=At.Shift.AFTER))
    private void visualparticlebetter$layFlat(ItemEntityRenderState state,PoseStack stack,SubmitNodeCollector collector,CameraRenderState camera,CallbackInfo ci){if(Boolean.TRUE.equals(visualparticlebetter$grounded.get(state))&&ClientCore.getInstance().modules().find("item_physics").filter(ItemPhysicsModule.class::isInstance).map(ItemPhysicsModule.class::cast).filter(ItemPhysicsModule::enabled).isPresent()){stack.translate(0,-.035,0);stack.mulPose(Axis.XP.rotationDegrees(90));}}
}