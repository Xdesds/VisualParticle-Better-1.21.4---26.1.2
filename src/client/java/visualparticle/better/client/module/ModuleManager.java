package visualparticle.better.client.module;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ModuleManager {
	private final Map<String, Module> modules = new LinkedHashMap<>();

	public void register(Module module) {
		if (modules.putIfAbsent(module.id(), module) != null) {
			throw new IllegalArgumentException("Duplicate module id: " + module.id());
		}
	}

	public void enableDefaults() {
		modules.values().stream().filter(Module::enabledByDefault).forEach(module -> module.setEnabled(true));
	}

	public void disableAll() { modules.values().forEach(module -> module.setEnabled(false)); }
	public Optional<Module> find(String id) { return Optional.ofNullable(modules.get(id)); }
	public Collection<Module> all() { return List.copyOf(modules.values()); }
	public int size() { return modules.size(); }
}
