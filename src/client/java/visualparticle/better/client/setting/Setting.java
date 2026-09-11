package visualparticle.better.client.setting;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public abstract class Setting<T> {
	private final String name;
	private final String hint;
	private T value;
	private BooleanSupplier visibility = () -> true;

	protected Setting(String name, String hint, T value) {
		this.name = Objects.requireNonNull(name);
		this.hint = Objects.requireNonNull(hint);
		this.value = value;
	}

	public final String name() { return name; }
	public final String hint() { return hint; }
	public T value() { return value; }
	public void set(T value) { this.value = value; }
	public final boolean visible() { return visibility.getAsBoolean(); }
	public final void visibleWhen(BooleanSupplier condition) { visibility = Objects.requireNonNull(condition); }
}
