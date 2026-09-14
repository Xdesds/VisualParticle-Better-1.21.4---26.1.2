package particle.fx.discord;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import particle.fx.ParticleFxMod;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class DiscordPresenceManager {
	private static final String APPLICATION_ID = "1526834171962458153";
	private static final String LARGE_IMAGE_KEY = "eabf19f42961fba1866c017e60a0c7a41a07320e120540d88524a2e632e3c885";
	private static final String MODRINTH_URL = "https://modrinth.com/mod/visualparticles-better";
	private static final String CURSEFORGE_URL = "https://www.curseforge.com/minecraft/mc-mods/visual-particle-better";
	private static final String GITHUB_URL = "https://github.com/Xdesds/VisualParticle-Better-1.21.4---26.1.2";
	private static final long UPDATE_INTERVAL_MS = 5_000L;
	private static final int OPCODE_HANDSHAKE = 0;
	private static final int OPCODE_FRAME = 1;
	private static final int OPCODE_CLOSE = 2;

	private final long startedAt = System.currentTimeMillis() / 1000L;
	private RandomAccessFile pipe;
	private boolean active;
	private long lastUpdateMs;
	private String lastState = "";

	public void start() {
		if (active) {
			return;
		}

		try {
			pipe = openPipe();
			sendHandshake();
			active = true;
			lastUpdateMs = 0L;
			lastState = "";
			updatePresence(MinecraftClient.getInstance(), true);
			ParticleFxMod.LOGGER.info("Discord RPC started for VisualParticle Better");
		} catch (IOException | RuntimeException exception) {
			closePipe();
			active = false;
			ParticleFxMod.LOGGER.warn("Discord RPC is disabled: {}", exception.getMessage());
		}
	}

	public void tick(MinecraftClient client) {
		if (!active) {
			return;
		}
		updatePresence(client, false);
	}

	public void stop() {
		if (!active && pipe == null) {
			return;
		}

		try {
			if (pipe != null) {
				writeFrame(OPCODE_CLOSE, new JsonObject());
			}
		} catch (IOException exception) {
			ParticleFxMod.LOGGER.warn("Discord RPC close frame failed: {}", exception.getMessage());
		} finally {
			closePipe();
			active = false;
		}
	}

	private void updatePresence(MinecraftClient client, boolean force) {
		long now = System.currentTimeMillis();
		String state = currentState(client);
		if (!force && state.equals(lastState) && now - lastUpdateMs < UPDATE_INTERVAL_MS) {
			return;
		}

		try {
			writeFrame(OPCODE_FRAME, presencePayload(state));
			lastState = state;
			lastUpdateMs = now;
		} catch (IOException exception) {
			closePipe();
			active = false;
			ParticleFxMod.LOGGER.warn("Discord RPC connection lost: {}", exception.getMessage());
		}
	}

	private void sendHandshake() throws IOException {
		JsonObject handshake = new JsonObject();
		handshake.addProperty("v", 1);
		handshake.addProperty("client_id", APPLICATION_ID);
		writeFrame(OPCODE_HANDSHAKE, handshake);
	}

	private JsonObject presencePayload(String state) {
		JsonObject activity = new JsonObject();
		activity.addProperty("details", "Mod Build " + modVersion());
		activity.addProperty("state", state);

		JsonObject timestamps = new JsonObject();
		timestamps.addProperty("start", startedAt);
		activity.add("timestamps", timestamps);

		JsonObject assets = new JsonObject();
		assets.addProperty("large_image", LARGE_IMAGE_KEY);
		assets.addProperty("large_text", "VisualParticle Better");
		activity.add("assets", assets);

		JsonArray buttons = new JsonArray();
		buttons.add(button("Modrinth / CurseForge", MODRINTH_URL));
		buttons.add(button("GitHub", GITHUB_URL));
		activity.add("buttons", buttons);

		JsonObject args = new JsonObject();
		args.addProperty("pid", ProcessHandle.current().pid());
		args.add("activity", activity);

		JsonObject payload = new JsonObject();
		payload.addProperty("cmd", "SET_ACTIVITY");
		payload.add("args", args);
		payload.addProperty("nonce", UUID.randomUUID().toString());
		return payload;
	}

	private static JsonObject button(String label, String url) {
		JsonObject button = new JsonObject();
		button.addProperty("label", label);
		button.addProperty("url", url);
		return button;
	}

	private void writeFrame(int opcode, JsonObject payload) throws IOException {
		if (pipe == null) {
			throw new IOException("Discord IPC pipe is not open");
		}

		byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
		ByteBuffer frame = ByteBuffer.allocate(8 + body.length).order(ByteOrder.LITTLE_ENDIAN);
		frame.putInt(opcode);
		frame.putInt(body.length);
		frame.put(body);
		pipe.write(frame.array());
	}

	private static RandomAccessFile openPipe() throws IOException {
		IOException last = null;
		for (int i = 0; i < 10; i++) {
			try {
				return new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + i, "rw");
			} catch (IOException exception) {
				last = exception;
			}
		}
		throw last == null ? new IOException("Discord IPC pipe was not found") : last;
	}

	private void closePipe() {
		if (pipe == null) {
			return;
		}

		try {
			pipe.close();
		} catch (IOException exception) {
			ParticleFxMod.LOGGER.warn("Discord RPC pipe close failed: {}", exception.getMessage());
		} finally {
			pipe = null;
		}
	}

	private static String currentState(MinecraftClient client) {
		if (client == null || client.world == null || client.player == null) {
			return "In menus";
		}
		if (client.isInSingleplayer()) {
			return "Singleplayer";
		}

		ServerInfo server = client.getCurrentServerEntry();
		if (server == null || server.address == null || server.address.isBlank()) {
			return "Multiplayer";
		}
		return truncate("Playing on " + server.address, 128);
	}

	private static String modVersion() {
		return FabricLoader.getInstance()
				.getModContainer(ParticleFxMod.MOD_ID)
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.orElse("0.0.7");
	}

	private static String truncate(String value, int maxLength) {
		if (value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, Math.max(0, maxLength - 3)) + "...";
	}

	@SuppressWarnings("unused")
	private static String curseForgeUrl() {
		return CURSEFORGE_URL;
	}
}
