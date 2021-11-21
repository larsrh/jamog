
package core.misc.module;

import core.exception.ModuleException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class acts as a central point for classes loaded by runtime, called
 * "modules" (<i>terminology is subject to change</i>). Basically, all existing
 * {@link Classes} are examined and wrapped via instances of subclasses of
 * {@link Module} which provide the functionality (e. g. factory methods).
 * Note that {@link ModuleHandler}s are not loaded automatically by default,
 * which could lead to the problem, that a subclass {@code ModuleHandler} is loaded
 * after the corresponding classes to handle, thus producing an empty
 * result when calling {@link #getModules() getModules()}. To fix this,
 * you might want to add the annotation {@link Loadable} to your own subclass
 * of {@link ModuleHandler} which makes sure that the handler will be properly
 * registered. This will only work if your subclass follows singleton pattern.
 * @param <T> base type of modules which should be handled
 * @param <S> subtype of {@link Module}s which are produced
 * @author lars
 */
public abstract class ModuleHandler<T,S extends Module<T>> {

	private final Class<T> clazz;
	private final Set<S> modules;

	/**
	 * Creates a new {@link ModuleHandler} which cares about a particular
	 * class and all subclasses. Multiple {@link ModuleHandlers} for one class
	 * (or a class and a superclass) are allowed.
	 * The newly created instance will be added to a single registry.
	 * @see #rescanAllLoadedClasses() 
	 * @param clazz the {@link Class} to care about, with type {@code T}
	 */
	public ModuleHandler(Class<T> clazz) {
		this.clazz = clazz;
		this.modules = new LinkedHashSet<S>();
	}

	/**
	 * Asks the {@link ClassLoader} for each loaded class and, if it's a
	 * subclass of {@code T}, invokes {@link #getSpecificModule(java.lang.Class)
	 * getSpecificModule}. Results can be received by calling
	 * {@link #getModules()}.
	 */
	final void rescanLoadedClasses() {
		final Collection<Class<?>> classes = ClassLoader.getLoaded();
		modules.clear();

		for (final Class<?> c : classes)
			if (clazz.isAssignableFrom(c))
				try {
					modules.add(getSpecificModule(c.asSubclass(clazz)));
				}
				catch(final ModuleException ex) {
					continue;
				}
	}

	/**
	 * @return a {@link Set} of {@link Module}s for each loaded subclass
	 * of {@code T}.
	 */
	public final Set<S> getModules() {
		return Collections.unmodifiableSet(modules);
	}

	/**
	 * Must be implemented by subclasses to create a {@link Module} for
	 * a {@link Class}.
	 * @param clazz the {@link Class} to be wrapped
	 * @return a corresponding {@link Module} instance
	 * @throws ModuleException if something went wrong
	 */
	protected abstract S getSpecificModule(Class<? extends T> clazz) throws ModuleException;

}
