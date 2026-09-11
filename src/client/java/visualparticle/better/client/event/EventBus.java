package visualparticle.better.client.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {
	private final Map<Class<? extends Event>, CopyOnWriteArrayList<EventListener<? extends Event>>> listeners = new ConcurrentHashMap<>();

	public <T extends Event> Subscription subscribe(Class<T> type, EventListener<T> listener) {
		var typedListeners = listeners.computeIfAbsent(type, ignored -> new CopyOnWriteArrayList<>());
		typedListeners.add(listener);
		return () -> typedListeners.remove(listener);
	}

	public <T extends Event> void post(T event) {
		List<EventListener<? extends Event>> typedListeners = listeners.get(event.getClass());
		if (typedListeners == null) return;
		for (EventListener<? extends Event> listener : typedListeners) dispatch(listener, event);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Event> void dispatch(EventListener<? extends Event> listener, T event) {
		((EventListener<T>) listener).onEvent(event);
	}

	public void clear() {
		listeners.clear();
	}
}
