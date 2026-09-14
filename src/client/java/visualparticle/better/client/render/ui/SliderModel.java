package visualparticle.better.client.render.ui;

public final class SliderModel {
	private final float minimum;
	private final float maximum;
	private final AnimatedFloat visualProgress;
	private float value;

	public SliderModel(float minimum, float maximum, float initialValue) {
		if (!(maximum > minimum)) throw new IllegalArgumentException("maximum must be greater than minimum");
		this.minimum = minimum;
		this.maximum = maximum;
		this.value = clamp(initialValue);
		this.visualProgress = new AnimatedFloat(progress());
	}

	public void setValue(float newValue) {
		value = clamp(newValue);
		visualProgress.setTarget(progress());
	}

	public void setFromMouse(float mouseX, float trackX, float trackWidth) {
		float progress = Math.max(0.0f, Math.min(1.0f, (mouseX - trackX) / trackWidth));
		setValue(minimum + (maximum - minimum) * progress);
	}

	public float update(float frameSeconds) {
		return visualProgress.update(frameSeconds, 18.0f);
	}

	public float value() {
		return value;
	}

	public float progress() {
		return (value - minimum) / (maximum - minimum);
	}

	private float clamp(float candidate) {
		return Math.max(minimum, Math.min(maximum, candidate));
	}
}
