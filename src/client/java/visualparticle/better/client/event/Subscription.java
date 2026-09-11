package visualparticle.better.client.event;

public interface Subscription extends AutoCloseable {
	@Override
	void close();
}
