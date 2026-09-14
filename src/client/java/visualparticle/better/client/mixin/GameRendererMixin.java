package visualparticle.better.client.mixin;

import visualparticle.better.client.ClientCore;
import visualparticle.better.client.module.impl.render.AspectRatioModule;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getProjectionMatrix", at = @At("RETURN"), cancellable = true)
    private void visualparticlebetter$aspectRatio(float fov, CallbackInfoReturnable<Matrix4f> cir) {
        ClientCore.getInstance().modules().find("aspect_ratio")
                .filter(AspectRatioModule.class::isInstance).map(AspectRatioModule.class::cast)
                .filter(AspectRatioModule::enabled).ifPresent(module -> {
                    Matrix4f matrix = new Matrix4f(cir.getReturnValue());
                    matrix.m00(matrix.m11() / module.ratio());
                    cir.setReturnValue(matrix);
                });
    }
}