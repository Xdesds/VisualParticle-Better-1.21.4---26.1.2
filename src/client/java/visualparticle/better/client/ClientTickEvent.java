package visualparticle.better.client;

import visualparticle.better.client.event.Event;

public record ClientTickEvent(long tick) implements Event {
}
