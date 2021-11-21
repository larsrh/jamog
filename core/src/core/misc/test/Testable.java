
package core.misc.test;

import core.exception.ModuleException;
import core.exception.InstantiationException;
import core.misc.module.Module;
import core.misc.module.ModuleHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public interface Testable
{

	/**
	 *
	 * @author torben
	 */
	public static final class Extension extends Module<Testable> {

		private final Constructor<? extends Testable> constructor;
		private TestResult result;

		private Extension(Class<? extends Testable> c) throws ModuleException {
			super(c);

			result = null;

			try {
				constructor = c.getDeclaredConstructor();
				constructor.setAccessible(true);
			}
			catch (NoSuchMethodException ex) {
				throw new ModuleException(ex);
			}
		}

		/**
		 * Runs this test in a {@link TestSuite} and caches the result. To get the
		 * same test results again, invoke {@link #getResult() getResult()}.
		 *
		 * @param testsuite The {@link TestSuite} to run the test in
		 *
		 * @return The {@link TestResult} of this test
		 *
		 * @throws InstantiationException if the tests constructor has thrown an
		 *                                exception
		 */
		public final TestResult runTest(TestSuite testsuite) throws InstantiationException
		{
			try { return result = testsuite.runTest(constructor.newInstance()); }
			catch (final java.lang.InstantiationException ex) { return null; } // will not happen
			catch (final IllegalAccessException ex) { return null; } // will not happen
			catch (final InvocationTargetException ex) { throw new InstantiationException(constructor.getDeclaringClass().getName(), ex.getCause()); }
		}

		/**
		 * @return The {@link TestResult} of the last invokation of {@link
		 * #runTest(core.test.TestSuite) runTest(TestSuite)}.
		 */
		public final TestResult getResult()
		{
			return result;
		}

	}

	public static final class Handler extends ModuleHandler<Testable,Extension> {

		private static Handler handler = new Handler();

		private Handler() {
			super(Testable.class);
		}

		@Override
		protected Extension getSpecificModule(Class<? extends Testable> clazz) throws ModuleException {
			return new Extension(clazz);
		}

		public static final ModuleHandler<Testable,Extension> getHandler() {
			return handler;
		}

	}


	public void test(TestSuite testsuite);
}
