package info.novatec.inspectit.agent.analyzer.impl;

import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatcher;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;

import java.util.ArrayList;
import java.util.List;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * The throwable matcher implementation is used to check if the class name of the configuration is a
 * subclass of {@link Throwable}. All of the calls to these methods are delegated to either an
 * {@link DirectMatcher}, an {@link IndirectMatcher}, an {@link SuperclassMatcher} or an
 * {@link InterfaceMatcher}, depending on which matcher type is passed to the constructor.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public class ThrowableMatcher extends AbstractMatcher {

	/**
	 * The inheritance checker used to check if a super class matches.
	 */
	private final IInheritanceAnalyzer inheritanceAnalyzer;

	/**
	 * The {@link IMatcher} delegator object to route the calls of all methods to.
	 */
	private IMatcher delegateMatcher;

	/**
	 * The only constructor which needs a reference to the {@link UnregisteredSensorConfig} instance
	 * of the corresponding configuration.
	 * 
	 * @param inheritanceAnalyzer
	 *            The inheritance analyzer.
	 * @param classPoolAnalyzer
	 *            The class pool analyzer.
	 * @param unregisteredSensorConfig
	 *            The sensor configuration.
	 * @param delegateMatcher
	 *            The {@link IMatcher} delegator object to route the calls of all methods to.
	 * @see AbstractMatcher
	 */
	public ThrowableMatcher(IInheritanceAnalyzer inheritanceAnalyzer, IClassPoolAnalyzer classPoolAnalyzer, UnregisteredSensorConfig unregisteredSensorConfig, IMatcher delegateMatcher) {
		super(classPoolAnalyzer, unregisteredSensorConfig);

		this.inheritanceAnalyzer = inheritanceAnalyzer;
		this.delegateMatcher = delegateMatcher;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean compareClassName(ClassLoader classLoader, String className) throws NotFoundException {
		return delegateMatcher.compareClassName(classLoader, className);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CtMethod> getMatchingMethods(ClassLoader classLoader, String className) throws NotFoundException {
		return delegateMatcher.getMatchingMethods(classLoader, className);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CtConstructor> getMatchingConstructors(ClassLoader classLoader, String className) throws NotFoundException {
		List<CtConstructor> matchingConstructors = delegateMatcher.getMatchingConstructors(classLoader, className);

		if (isThrowable(classLoader, className)) {
			if (matchingConstructors.isEmpty()) {
				matchingConstructors = new ArrayList<CtConstructor>();
			}

			// add the throwable constructors to the other constructors
			CtClass clazz = classPoolAnalyzer.getClassPool(classLoader).get(className);
			CtConstructor[] constructors = clazz.getConstructors();
			for (CtConstructor constructor : constructors) {
				if (!matchingConstructors.contains(constructor)) {
					matchingConstructors.add(constructor);
				}
			}
		}

		return matchingConstructors;
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkParameters(List<? extends CtBehavior> methods) throws NotFoundException {
		delegateMatcher.checkParameters(methods);
	}

	/**
	 * Checks whether the current class is of type {@link Throwable}.
	 * 
	 * @param classLoader
	 *            The class loader of the given class.
	 * @param className
	 *            The name of the current class.
	 * @return True if the current class is of type {@link Throwable}, false otherwise.
	 */
	private boolean isThrowable(ClassLoader classLoader, String className) {
		return inheritanceAnalyzer.subclassOf(className, "java.lang.Throwable", classPoolAnalyzer.getClassPool(classLoader));
	}

}
