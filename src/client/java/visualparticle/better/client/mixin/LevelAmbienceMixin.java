package visualparticle.better.client.mixin;

import visualparticle.better.client.ClientCore;
import visualparticle.better.client.module.impl.world.AmbienceModule;
import visualparticle.better.client.module.impl.world.CinematicWeatherModule;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelAmbienceMixin {
    private CinematicWeatherModule visualparticlebetter$cinematic() {
		return ClientCore.getInstance().modules().find("cinematic_weather").filter(CinematicWeatherModule.class::isInstance).map(CinematicWeatherModule.class::cast).filter(CinematicWeatherModule::enabled).orElse(null);
	}

	private AmbienceModule visualparticlebetter$ambience() {
        return ClientCore.getInstance().modules().find("ambience")
                .filter(AmbienceModule.class::isInstance).map(AmbienceModule.class::cast).orElse(null);
    }

    @Inject(method = "getDayTime", at = @At("RETURN"), cancellable = true)
    private void visualparticlebetter$time(CallbackInfoReturnable<Long> cir) {
        AmbienceModule module = visualparticlebetter$ambience();
        if (module != null) cir.setReturnValue(module.time(cir.getReturnValue()));
    }

    @Inject(method = "getRainLevel", at = @At("HEAD"), cancellable = true)
    private void visualparticlebetter$rain(float partialTick, CallbackInfoReturnable<Float> cir) {
        AmbienceModule module = visualparticlebetter$ambience();
        if (module != null && module.rain() != null) cir.setReturnValue(module.rain());
    }

    @Inject(method = "getThunderLevel", at = @At("HEAD"), cancellable = true)
    private void visualparticlebetter$thunder(float partialTick, CallbackInfoReturnable<Float> cir) {
        AmbienceModule module = visualparticlebetter$ambience();
        if (module != null && module.thunder() != null) cir.setReturnValue(module.thunder());
    }
}