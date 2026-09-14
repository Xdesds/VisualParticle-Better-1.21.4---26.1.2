package visualparticle.better.client;

import net.fabricmc.api.ClientModInitializer;

public final class VisualParticleBetterEntrypoint implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientCore.getInstance().start();
	}
}
