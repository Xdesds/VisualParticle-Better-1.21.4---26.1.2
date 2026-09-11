package visualparticle.better.client.render.ui;

public final class AnimatedFloat {
	private float value;
	private float target;

	public AnimatedFloat(float initialValue) {
		value = initialValue;
		target = initialValue;
	}

	public void setTarget(float newTarget) {
		target = newTarget;
	}

	public float update(float frameSeconds, float response) {
		float safeDelta = Math.max(0.0f, Math.min(frameSeconds, 0.1f));
		float amount = 1.0f - (float) Math.exp(-Math.max(0.0f, response) * safeDelta);
		value += (target - value) * amount;
		return value;
	}

	public float value() {
		return value;
	}
}
