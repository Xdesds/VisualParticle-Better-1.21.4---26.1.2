package particle.fx.particle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import particle.fx.ParticleFxMod;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public final class ParticleConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(ParticleFxMod.MOD_ID + ".json");
	private static final LinkedHashMap<String, SettingsData> CUSTOM_PRESETS = new LinkedHashMap<>();

	private ParticleConfig() {
	}

	public static void load(ParticleSystem.Settings settings) {
		CUSTOM_PRESETS.clear();

		if (Files.notExists(CONFIG_PATH)) {
			save(settings);
			return;
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			ConfigData config = GSON.fromJson(reader, ConfigData.class);
			if (config != null && config.currentSettings != null) {
				apply(config.currentSettings, settings);
				if (config.customPresets != null) {
					CUSTOM_PRESETS.putAll(config.customPresets);
				}
				return;
			}
		} catch (IOException | JsonParseException ignored) {
			ParticleFxMod.LOGGER.info("Falling back to legacy particle config format");
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			SettingsData legacySettings = GSON.fromJson(reader, SettingsData.class);
			if (legacySettings != null) {
				apply(legacySettings, settings);
			}
		} catch (IOException exception) {
			ParticleFxMod.LOGGER.warn("Failed to load particle config from {}", CONFIG_PATH, exception);
		}
	}

	public static void save(ParticleSystem.Settings settings) {
		ConfigData config = new ConfigData();
		config.currentSettings = SettingsData.from(settings);
		config.customPresets.putAll(CUSTOM_PRESETS);

		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException exception) {
			ParticleFxMod.LOGGER.warn("Failed to save particle config to {}", CONFIG_PATH, exception);
		}
	}

	public static List<String> getCustomPresetNames() {
		return List.copyOf(CUSTOM_PRESETS.keySet());
	}

	public static boolean hasCustomPreset(String name) {
		return CUSTOM_PRESETS.containsKey(name);
	}

	public static void saveCustomPreset(String name, ParticleSystem.Settings settings) {
		CUSTOM_PRESETS.put(name, SettingsData.from(settings));
	}

	public static boolean loadCustomPreset(String name, ParticleSystem.Settings settings) {
		SettingsData data = CUSTOM_PRESETS.get(name);
		if (data == null) {
			return false;
		}

		apply(data, settings);
		settings.preset = ParticleSystem.Preset.CUSTOM;
		settings.customPresetName = name;
		return true;
	}

	private static void apply(SettingsData data, ParticleSystem.Settings settings) {
		settings.preset = data.preset == null ? ParticleSystem.Preset.CUSTOM : data.preset;
		settings.customPresetName = data.customPresetName == null ? "" : data.customPresetName;
		settings.colorPreset = data.colorPreset == null ? ParticleSystem.ColorPreset.DEFAULT : data.colorPreset;
		settings.enabled = data.enabled;
		settings.attackTrigger = data.attackTrigger;
		settings.totemTrigger = data.totemTrigger;
		settings.walkTrigger = data.walkTrigger;
		settings.elytraTrigger = data.elytraTrigger;
		settings.projectileTrigger = data.projectileTrigger;
		settings.worldParticles = data.worldParticles;
		settings.worldPhysics = data.worldPhysics;
		settings.particleMode = data.particleMode == null ? Particle3D.ParticleMode.STAR : data.particleMode;
		settings.worldMode = data.worldMode == null ? Particle3D.ParticleMode.STAR : data.worldMode;
		settings.glowMode = data.glowMode == null ? Particle3D.GlowMode.BOTH : data.glowMode;
		settings.attackAmount = data.attackAmount;
		settings.walkAmount = data.walkAmount;
		settings.worldAmount = data.worldAmount;
		settings.spread = data.spread;
		settings.speed = data.speed;
		settings.lifeTime = data.lifeTime;
		settings.size = data.size;
		settings.glowSize = data.glowSize;
		settings.worldLifeTime = data.worldLifeTime;
		settings.worldSize = data.worldSize;
		settings.worldGlowSize = data.worldGlowSize;
		settings.customColors = data.customColors;
		settings.animatedGradient = data.animatedGradient;
		settings.uiPrimaryColor = data.uiPrimaryColor;
		settings.uiSecondaryColor = data.uiSecondaryColor;
		settings.attackColor = data.attackColor;
		settings.attackSecondColor = data.attackSecondColor;
		settings.moveColor = data.moveColor;
		settings.moveSecondColor = data.moveSecondColor;
		settings.projectileColor = data.projectileColor;
		settings.projectileSecondColor = data.projectileSecondColor;
		settings.elytraColor = data.elytraColor;
		settings.elytraSecondColor = data.elytraSecondColor;
		settings.worldColor = data.worldColor;
		settings.worldSecondColor = data.worldSecondColor;
		settings.totemColor = data.totemColor;
		settings.totemSecondColor = data.totemSecondColor;
	}

	private static final class ConfigData {
		private SettingsData currentSettings = new SettingsData();
		private LinkedHashMap<String, SettingsData> customPresets = new LinkedHashMap<>();
	}

	private static final class SettingsData {
		private ParticleSystem.Preset preset = ParticleSystem.Preset.CUSTOM;
		private String customPresetName = "";
		private ParticleSystem.ColorPreset colorPreset = ParticleSystem.ColorPreset.DEFAULT;
		private boolean enabled = true;
		private boolean attackTrigger = true;
		private boolean totemTrigger = true;
		private boolean walkTrigger = true;
		private boolean elytraTrigger = true;
		private boolean projectileTrigger = true;
		private boolean worldParticles = true;
		private boolean worldPhysics = false;
		private Particle3D.ParticleMode particleMode = Particle3D.ParticleMode.STAR;
		private Particle3D.ParticleMode worldMode = Particle3D.ParticleMode.STAR;
		private Particle3D.GlowMode glowMode = Particle3D.GlowMode.BOTH;
		private int attackAmount = 40;
		private int walkAmount = 30;
		private int worldAmount = 100;
		private float spread = 1.0F;
		private float speed = 2.0F;
		private float lifeTime = 2.5F;
		private float size = 1.0F;
		private float glowSize = 7.5F;
		private float worldLifeTime = 10.0F;
		private float worldSize = 1.5F;
		private float worldGlowSize = 3.0F;
		private boolean customColors = false;
		private boolean animatedGradient = true;
		private int uiPrimaryColor = 0xFF68AEFF;
		private int uiSecondaryColor = 0xFFFF6DA4;
		private int attackColor = 0xFFFF6D78;
		private int attackSecondColor = 0xFFFFC371;
		private int moveColor = 0xFF68AEFF;
		private int moveSecondColor = 0xFFB46DFF;
		private int projectileColor = 0xFFFFD166;
		private int projectileSecondColor = 0xFFFF6DA4;
		private int elytraColor = 0xFF75F4FF;
		private int elytraSecondColor = 0xFF68AEFF;
		private int worldColor = 0xFF8DFFB3;
		private int worldSecondColor = 0xFF68AEFF;
		private int totemColor = 0xFF7CFC00;
		private int totemSecondColor = 0xFFFFD700;

		private static SettingsData from(ParticleSystem.Settings settings) {
			SettingsData data = new SettingsData();
			data.preset = settings.preset;
			data.customPresetName = settings.customPresetName;
			data.colorPreset = settings.colorPreset;
			data.enabled = settings.enabled;
			data.attackTrigger = settings.attackTrigger;
			data.totemTrigger = settings.totemTrigger;
			data.walkTrigger = settings.walkTrigger;
			data.elytraTrigger = settings.elytraTrigger;
			data.projectileTrigger = settings.projectileTrigger;
			data.worldParticles = settings.worldParticles;
			data.worldPhysics = settings.worldPhysics;
			data.particleMode = settings.particleMode;
			data.worldMode = settings.worldMode;
			data.glowMode = settings.glowMode;
			data.attackAmount = settings.attackAmount;
			data.walkAmount = settings.walkAmount;
			data.worldAmount = settings.worldAmount;
			data.spread = settings.spread;
			data.speed = settings.speed;
			data.lifeTime = settings.lifeTime;
			data.size = settings.size;
			data.glowSize = settings.glowSize;
			data.worldLifeTime = settings.worldLifeTime;
			data.worldSize = settings.worldSize;
			data.worldGlowSize = settings.worldGlowSize;
			data.customColors = settings.customColors;
			data.animatedGradient = settings.animatedGradient;
			data.uiPrimaryColor = settings.uiPrimaryColor;
			data.uiSecondaryColor = settings.uiSecondaryColor;
			data.attackColor = settings.attackColor;
			data.attackSecondColor = settings.attackSecondColor;
			data.moveColor = settings.moveColor;
			data.moveSecondColor = settings.moveSecondColor;
			data.projectileColor = settings.projectileColor;
			data.projectileSecondColor = settings.projectileSecondColor;
			data.elytraColor = settings.elytraColor;
			data.elytraSecondColor = settings.elytraSecondColor;
			data.worldColor = settings.worldColor;
			data.worldSecondColor = settings.worldSecondColor;
			data.totemColor = settings.totemColor;
			data.totemSecondColor = settings.totemSecondColor;
			return data;
		}
	}
}
