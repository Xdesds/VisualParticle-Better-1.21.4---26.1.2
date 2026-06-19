package particle.fx.particle;

final class ColorUtil {
	private ColorUtil() {
	}

	static int withAlpha(int color, float alphaMultiplier) {
		int alpha = (color >>> 24) & 0xFF;
		int scaledAlpha = Math.max(0, Math.min(255, (int) (alpha * alphaMultiplier)));
		return (color & 0x00FFFFFF) | (scaledAlpha << 24);
	}
}
