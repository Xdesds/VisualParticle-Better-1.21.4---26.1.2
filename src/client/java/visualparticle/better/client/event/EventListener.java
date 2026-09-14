package visualparticle.better.client.event;

@FunctionalInterface
public interface EventListener<T extends Event> {
	void onEvent(T event);
}
