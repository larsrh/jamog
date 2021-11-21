
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                           *
 * Copyright 2009 Lars Hupel, Torben Maack, Sylvester Tremmel                *
 *                                                                           *
 * This file is part of Jamog.                                               *
 *                                                                           *
 * Jamog is free software: you can redistribute it and/or modify             *
 * it under the terms of the GNU General Public License as published by      *
 * the Free Software Foundation; version 3.                                  *
 *                                                                           *
 * Jamog is distributed in the hope that it will be useful,                  *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of            *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the              *
 * GNU General Public License for more details.                              *
 *                                                                           *
 * You should have received a copy of the GNU General Public License         *
 * along with Jamog. If not, see <http://www.gnu.org/licenses/>.             *
 *                                                                           *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package core.build;

import core.exception.BuildException;
import core.exception.DeserializingException;
import core.exception.InstantiationException;
import core.exception.ModuleException;
import core.exception.SerializingException;
import core.misc.module.Module;
import core.misc.module.ModuleHandler;
import core.misc.serial.SerializingStream;
import core.monitor.ComponentListener;
import core.signal.Signal;
import core.signal.SignalBit;
import core.sim.Calculator;
import core.misc.serial.Serializable;
import core.misc.serial.DeserializingStream;
import core.monitor.CalculatorListener;
import core.sim.Simulator;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The {@code Component} class is the superclass of all components.
 * A component performs an action, most likely it will take one or more input
 * {@link Signal}s and run one or more output {@link Signal}s.
 * Basically, there are two types of Components: "blackboxing" components which
 * have no inner structure, like AND, OR etc., represented by
 * their superclass {@link Calculator}, and {@link Composite} components which
 * have an inner structure. All logic of composite componentes results solely from
 * this structure.
 * Subclasses have to provide a constructor taking two parameters: a parent
 * object ({@link ComponentCollection}) and a name ({@link String}).
 * @author lars
 */
public abstract class Component implements CalculatorListener, Serializable {

	public static final class Extension extends Module<Component> {

		private final Constructor<? extends Component> constructor;

		private Extension(Class<? extends Component> c) throws ModuleException {
			super(c);

			try {
				constructor = c.getDeclaredConstructor(ComponentCollection.class, String.class);
				constructor.setAccessible(true);
			}
			catch (NoSuchMethodException ex) {
				throw new ModuleException(ex);
			}
		}

		/**
		 * Creates a new instance of the underlying class by invoking a constructor
		 * with the same syntax as {@link
		 * Component#Component(core.build.ComponentCollection, java.lang.String)
		 * Component(ComponentCollection, String)}, and by convention every
		 * component just calls this function.
		 *
		 * @see Component#Component(core.build.ComponentCollection,
		 *      java.lang.String) Component(ComponentCollection, String)
		 *
		 * @param parent The first parameter for {@link
		 *               Component#Component(core.build.ComponentCollection,
		 *               java.lang.String) Component(ComponentCollection, String)}
		 * @param name   The second parameter for {@link
		 *               Component#Component(core.build.ComponentCollection,
		 *               java.lang.String) Component(ComponentCollection, String)}
		 *
		 * @return A new instance of the component
		 *
		 * @throws InstantiationException if the components constructor has thrown
		 *                                an exception
		 */
		public final Component newInstance(final ComponentCollection parent, final String name) throws InstantiationException
		{
			try { return constructor.newInstance(parent, name); }
			catch (final java.lang.InstantiationException ex) { return null; } // will not happen
			catch (final IllegalAccessException ex) { return null; } // will not happen
			catch (final InvocationTargetException ex) { throw new InstantiationException(constructor.getDeclaringClass().getName(), ex.getCause()); }
		}
		
	}

	public static final class Handler extends ModuleHandler<Component,Extension> {

		private static final Handler handler = new Handler();

		private Handler() {
			super(Component.class);
		}

		@Override
		protected Extension getSpecificModule(Class<? extends Component> clazz) throws ModuleException {
			return new Extension(clazz);
		}

		public static final ModuleHandler<Component,Extension> getHandler() {
			return handler;
		}
		
	}

	private final Map<String,Object> parameters;
	private String flavor;
	private boolean constructMode;
	private final Set<SignalBit> outputSignalBits;
	private final Set<SignalBit> inputSignalBits;
	private final Map<SignalBit,Set<SignalBit>> priorities;

	private String fullName;
	private String shortName;

	ComponentCollection parent;

	private volatile ComponentListener[] listeners;
	private final AtomicBoolean lock;

	/**
	 * Creates a new component with no signals, parameters and flavor set.
	 * This constructor will add the component automatically to its parent,
	 * but this may produce exceptions depending on the implementation of
	 * the {@link ComponentCollection}.
	 * @param parent the collection to which this component will be added.
	 * {@code null} values are permitted, but note that only {@link Environment}
	 * will be able to handle this component. Other {@link ComponentCollection}s
	 * are not allowed to manipulate the assignment later.
	 * @param name the short name of this component which must not contain
	 * {@link ComponentCollection#NAME_SEPARATOR}. The full name will be computed
	 * by concatenating parent's full name, {@link ComponentCollection#NAME_SEPARATOR}
	 * and the given short name
	 */
	protected Component(ComponentCollection parent,String name) {
		assert !name.contains(Composite.NAME_SEPARATOR) : "name '"+name+"' contains name separator '"+Composite.NAME_SEPARATOR;

		this.parent = null;
		this.shortName = name;

		if (parent != null) {
			this.fullName = ("".equals(parent.getName()) ? name : (parent.getName()+Composite.NAME_SEPARATOR+name));
			parent.addComponent(this);
			this.parent = parent;
		}
		else {
			this.fullName = name;
		}

		outputSignalBits = new LinkedHashSet<SignalBit>();
		inputSignalBits = new LinkedHashSet<SignalBit>();
		priorities = new LinkedHashMap<SignalBit, Set<SignalBit>>();
		flavor = null;
		parameters = new HashMap<String,Object>();
		constructMode = false;
		listeners = new ComponentListener[0];
		lock = new AtomicBoolean(false);
	}

	@SuppressWarnings("unchecked")
	protected Component(DeserializingStream in) throws IOException, DeserializingException, InstantiationException
	{
		shortName = in.readString();
		parent = in.readObject();
		flavor = in.readString();
		parameters = in.readObject(HashMap.class, String.class);
		inputSignalBits = in.readObject(LinkedHashSet.class, SignalBit.class);
		outputSignalBits = in.readObject(LinkedHashSet.class, SignalBit.class);
		priorities = in.readObject(LinkedHashMap.class, SignalBit.class, LinkedHashSet.class, SignalBit.class);
		listeners = in.readObject(ComponentListener[].class);

		if (parent != null)
			fullName = "".equals(parent.getName()) ? shortName : (parent.getName() + Composite.NAME_SEPARATOR + shortName);
		else
			fullName = shortName;
		constructMode = false;
		lock = new AtomicBoolean(false);
	}

	/**
	 * Checks whether this component is assigned to a parent component. In most
	 * cases this method is used by {@link ComponentCollection} only.
	 * @return {@code true} if a parent is assigned, {@code false} otherwise
	 * @see ComponentCollection#addComponent(core.build.Component)
	 */
	public final boolean hasParent() {
		return parent != null;
	}

	/**
	 * Renames this component and updates all children if necessary. This method
	 * is intended to be called by {@link Environment} only.
	 * @param name the new short name
	 */
	final void setName(String name) {
		this.shortName = name;
		updateName();
	}

	/**
	 * Recursively computes new names for each child of this component.
	 */
	private final void updateName() {
		if (parent != null)
			fullName = ("".equals(parent.getName()) ? shortName : (parent.getName()+Composite.NAME_SEPARATOR+shortName));
		else
			fullName = shortName;

		if (this instanceof Composite)
			for (Component c : ((Composite)this).getComponents().values())
				c.updateName();
	}

	/**
	 * Returns the short name of this component
	 * @return short name which has been passed to the constructor
	 */
	public final String getShortName() {
		return shortName;
	}

	/**
	 * Returns the full name of this component
	 * @return full name, separated by {@link ComponentCollection#NAME_SEPARATOR}
	 */
	public final String getName() {
		return fullName;
	}

	@Override
	public final String toString() {
		return getName();
	}

	/**
	 * Adds a {@link CalculatorListener} to this {@link Component}. Depending
	 * on the inner structure of this object, it may be necessary to recurse.
	 * @param listener the {@link CalculatorListener} which will be notified
	 * when one of the {@link Calculator}s in this object changed
	 */
	protected abstract void addCalculatorListener(CalculatorListener listener);

	/**
	 * Removes a {@link CalculatorListener} from this {@link Component}. Depending
	 * on the inner structure of this object, it may be necessary to recurse.
	 * @param listener the {@link CalculatorListener} which won't be notified
	 * when one of the {@link Calculator}s in this object changed
	 */
	protected abstract void removeCalculatorListener(CalculatorListener listener);

	/**
	 * Adds a {@link ComponentListener} to this {@link Component}. This listener
	 * will be called, when any inner {@link Calculator} changed.
	 * If this is a top level component ({@code parent instanceof Environment}),
	 * all {@link ComponentListener}s will be removed when a rebuild is done.
	 * @param listener the listener to add
	 */
	public final void addComponentListener(ComponentListener listener)
	{
		while(!lock.compareAndSet(false, true));

		outer:
		{
			ComponentListener[] new_listeners = new ComponentListener[listeners.length + 1];
			for(int i = 0; i < listeners.length; ++i)
				if(listeners[i] == listener)
					break outer;
				else
					new_listeners[i] = listeners[i];
			new_listeners[listeners.length] = listener;
			
			if (listeners.length == 0)
				addCalculatorListener(this);

			listeners = new_listeners;
		}

		lock.set(false);
	}

	public final void removeComponentListener(ComponentListener listener)
	{
		while(!lock.compareAndSet(false, true));

		ComponentListener[] new_listeners = new ComponentListener[listeners.length - 1];
		int i, j;
		for(i = 0, j = 0; i < listeners.length; ++i)
			if(listeners[i] != listener)
				new_listeners[j++] = listeners[i];
		if(j != i) {
			if(new_listeners.length == 0)
				removeCalculatorListener(this);

			listeners = new_listeners;
		}

		lock.set(false);
	}

	final void removeAllComponentListeners()
	{
		while(!lock.compareAndSet(false, true));

		listeners = new ComponentListener[0];
		// we don't call
		//   removeCalculatorListener(this);
		// here, because this method will only be called by Environment
		// before build; aller inner components will be garbage collected :)
		
		lock.set(false);
	}

	@Override
	public void calculateFinished(Calculator finished_calculator) {
		ComponentListener[] current_listeners = listeners;
		for(ComponentListener listener : current_listeners)
			listener.componentChanged(finished_calculator, this);
	}

	@Override public void serialize(SerializingStream out) throws IOException, SerializingException
	{
		out.writeString(shortName);
		out.writeObject(parent);
		out.writeString(flavor);
		out.writeObject(parameters, false, false);
		out.writeObject(inputSignalBits, false, false);
		out.writeObject(outputSignalBits, false, false);
		out.writeObject(priorities, false, false, false, false);
		out.writeObject(listeners, false);
	}

	/**
	 * Returns a set of all output {@link SignalBit}s of this component
	 * @return all output {@link SignalBit}s of this components
	 */
	public final Set<SignalBit> getOutputSignalBits() {
		return Collections.unmodifiableSet(outputSignalBits);
	}

	/**
	 * Returns a set of all input {@link SignalBit}s of this component
	 * @return all input {@link SignalBit}s of this component
	 */
	public final Set<SignalBit> getInputSignalBits() {
		return Collections.unmodifiableSet(inputSignalBits);
	}

	/**
	 * Test whether this component is in construct mode
	 * @return {@code true} if this component is in construct mode
	 */
	public final boolean isInConstructMode() {
		return constructMode;
	}

	/**
	 * Returns all associated parameters
	 * @return a mapping of all parameters which are set
	 */
	public final Map<String,Object> getParameters() {
		return Collections.unmodifiableMap(parameters);
	}

	/**
	 * Returns the selected flavor
	 * @return the selected flavor, or a default one if no one is selected
	 */
	public final String getFlavor() {
		return flavor == null ? Flavor.DEFAULT : flavor;
	}

	/**
	 * Provides access to a stored parameter
	 * @param name the name of the parameter
	 * @return the parameter
	 */
	protected final Object get(String name) {
		return parameters.get(name);
	}

	/**
	 * Provides access to a stored parameter and checks whether it is a signal
	 * @param name the name of the signal parameter
	 * @return the signal parameter
	 * @see Component#get(java.lang.String) 
	 */
	protected final Signal getSignal(String name) {
		assert parameters.containsKey(name) && (parameters.get(name) instanceof Signal)
			: "Signal '"+name+"' not found";
		return (Signal)parameters.get(name);
	}

	/**
	 * Changes the flavor a component uses to be built
	 * @param flavor the flavor string
	 * @return the component itself
	 * @see Component#useAndSet(java.lang.String, java.lang.String[], java.lang.Object[]) 
	 */
	public final Component use(String flavor) {
		assert !constructMode;
		assert flavor != null;
		assert getFlavors().containsKey(flavor);
		
		if (!flavor.equals(this.flavor))
			reset();
		this.flavor = flavor;
		return this;
	}

	/**
	 * Adds a priority pair for later respect in analyze phase.
	 * @param high signal with higher priority
	 * @param low signal with lower priority
	 * @see Simulator
	 * @see AnalyzeException.DirectedPriorityCycleException
	 */
	protected final void addPriority(Signal high,Signal low) {
		for (int i=0;i<high.size();++i) {
			Set<SignalBit> newValue;
			if (!priorities.containsKey(high.getSignalBit(i)))
				priorities.put(high.getSignalBit(i),newValue = new LinkedHashSet<SignalBit>());
			else
				newValue = priorities.get(high.getSignalBit(i));

			for (int j=0;j<low.size();++j)
				newValue.add(low.getSignalBit(j));
		}
	}

	/**
	 * Changes a parameter of the component
	 * @param <T> the type of the parameter
	 * @param name the name of the parameter
	 * @param parameter the parameter
	 * @return the parameter
	 * @see Component#setAll(java.lang.String[], java.lang.Object[])
	 * @see Component#useAndSet(java.lang.String, java.lang.String[], java.lang.Object[]) 
	 */
	public final <T> T set(String name,T parameter) {
		assert !constructMode;

		reset();
		parameters.put(name, parameter);
		return parameter;
	}

	/**
	 * Removes a parameter of the component
	 * @param name the name of the parameter
	 * @return the parameter
	 * @see Component#set(java.lang.String, java.lang.Object) 
	 */
	public final Object unset(String name) {
		assert !constructMode;
			
		reset();
		return parameters.remove(name);
	}

	/**
	 * Changes multiple parameters at the same time
	 * <p>Make sure that {@code names.length == parameters.length}</p>
	 * @param names the names of the parameters
	 * @param parameters the parameters
	 * @return the component itself
	 * @see Component#set(java.lang.String, java.lang.Object)
	 * @see Component#useAndSet(java.lang.String, java.lang.String[], java.lang.Object[]) 
	 */
	public final Component setAll(String names[],Object... parameters) {
		assert !constructMode;
		assert names.length == parameters.length;

		reset();
		for (int i=0;i<names.length;++i)
			this.parameters.put(names[i],parameters[i]);
		return this;
	}

	/**
	 * Changes multiple parameters and the flavor at the same time
	 * <p>Make sure that {@code names.length == parameters.length}</p>
	 * @param flavor the flavor string
	 * @param names the names of the parameters
	 * @param parameters the parameters
	 * @return the component itself
	 * @see Component#set(java.lang.String, java.lang.Object)
	 * @see Component#setAll(java.lang.String[], java.lang.Object[])
	 * @see Component#use(java.lang.String) 
	 */
	public final Component useAndSet(String flavor,String names[],Object... parameters) {
		use(flavor);
		return setAll(names,parameters);
	}

	/**
	 * Adds input and output signals to appropriate sets.
	 * @param flavor the flavor
	 */
	private final void processSignals(Flavor flavor) {
		for (String input : flavor.getInputs().keySet())
			addSignalBitsToSet(inputSignalBits,get(input));
		for (String output : flavor.getOutputs().keySet())
			addSignalBitsToSet(outputSignalBits,get(output));
	}

	/**
	 * Adds {@link SignalBit}s and {@link SignalBit}s of arbitrary-deep nested
	 * arrays of {@link Signal}s to a given {@link Set}.
	 * @param set the set
	 * @param objs an arbitrary-deep nested array of signals (different levels per element are allowed)
	 */
	private final void addSignalBitsToSet(Set<SignalBit> set,Object... objs) {
		for (Object o : objs)
			if (o.getClass().isArray())
				addSignalBitsToSet(set,(Object[])o);
			else if (o instanceof Signal)
				for (int i=0;i<((Signal)o).size();++i)
					set.add(((Signal)o).getSignalBit(i));
			else if (o instanceof SignalBit)
				set.add((SignalBit)o);
			else
				assert false : "cannot add a non-signal";
	}

	/**
	 * Checks whether this component is in a buildable state.
	 * @return {@code true} if a {@link #construct(java.util.Set) construct}-
	 * call will probably succeed. Note that {@link #construct(java.util.Set)
	 * construct} cannot be directly called by user. Use {@link Environment#build()}
	 * instead.
	 * @see Environment#build()
	 */
	public final boolean checkReady() {
		Flavor f = getFlavors().get(flavor == null ? Flavor.DEFAULT : flavor);
		return f.check(parameters);
	}

	/**
	 * Composes a detailed exception message.
	 * @param description exception/error description
	 * @return a detailed string, including class name and the {@link #getName() name}
	 * of this component
	 */
	protected final String getExceptionMessage(String description) {
		return new StringBuilder("In class ")
			.append(getClass().getSimpleName())
			.append(" at component ")
			.append(fullName)
			.append(": ")
			.append(description)
			.toString();
	}

	/**
	 * Constructs this component, this will invoke
	 * <ul>
	 *   <li>selecting flavor from the string passed to {@link #use(java.lang.String)}</li>
	 *   <li>checking arguments</li>
	 *   <li>cleaning up previous constructions</li>
	 *   <li>invoking a chain of build methods from {@link Component} down to the concrete class</li>
	 * </ul>
	 * @param innerComponents an empty set
	 * @return a priority map for {@link Simulator}
	 */
	final Map<SignalBit,Set<SignalBit>> construct(Set<Component> innerComponents) throws BuildException {
		assert !constructMode;
		
		if (flavor == null)
			flavor = Flavor.DEFAULT;

		constructMode = true;

		try {
			// prepare call
			reset();

			// check types and constrains
			Flavor f = getFlavors().get(flavor);
			f.checkAndThrow(parameters);

			// add signals
			processSignals(f);

			if (parent != null && parent instanceof Component) {
				Component cp = (Component)parent;
				for (SignalBit sb : outputSignalBits)
					if (cp.getInputSignalBits().contains(sb))
						throw new BuildException(getExceptionMessage("tried to use a input SignalBit from parent as output"));
				// output as input is o.k.
			}

			// build
			f.build(this);

			// add inner structure to the environment
			if (this instanceof Composite) {
				Composite c = (Composite)this;
				for (Component comp : c.getComponents().values())
					innerComponents.add(comp);
			}
			
			// completed
		}
		catch (BuildException ex) {
			reset();
			throw ex;
		}
		catch (Exception ex) {
			reset();
			throw new BuildException(getExceptionMessage("an exception occured"),ex);
		}
		finally {
			constructMode = false;
		}

		return Collections.unmodifiableMap(priorities);
	}

	/**
	 * Cleans input and output signals
	 */
	private final void reset() {
		outputSignalBits.clear();
		inputSignalBits.clear();
		cleanup();
	}

	/**
	 * This function will be called by {@link Environment#build(java.util.Set, java.util.Set, java.util.Map, core.monitor.EnvironmentListener)}
	 * after this component has been built. By default, it does nothing.
	 * All implementations have to make sure that the whole chain of
	 * {@code init}-methods have to be called from the top to the bottom,
	 * usually by prepending {@code super.init()} in your implementation.
	 * @see Composite#cleanup()
	 */
	protected void init() { }

	/**
	 * This function will be called by {@link Component#reset()}
	 * before this component will be rebuilt. By default, it does nothing.
	 * All implementations have to make sure that the whole chain of
	 * {@code cleanup}-methods have to be called from the bottom to the top,
	 * usually by appending {@code super.cleanup()} to your implementation.
	 * @see #construct(java.util.Set)
	 */
	protected void cleanup() { }

	/**
	 * @return a {@link Map} of {@link Flavor}s which are provided by this
	 * {@link Component}. For each entry {@code e} of this {@link Map}, the
	 * following property must be satisfied:
	 * <pre>e.getKey().equals(e.getValue().{@link Flavor#getName() getName()})</pre>
	 */
	public abstract Map<String, Flavor> getFlavors();
}
