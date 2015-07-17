package info.novatec.inspectit.agent.analyzer.impl;

import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatcher;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;

import java.util.Iterator;
import java.util.List;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * The super class matcher implementation is used to check if the class name of the configuration is
 * equal to one of the super classes of the passed class. All of the calls to these methods are
 * mainly delegated to either an {@link DirectMatcher} or an {@link IndirectMatcher}, depending if
 * the configuration is virtual (contains a pattern).
 * 
 * @author Patrice Bouillet
 * 
 */
public class SuperclassMatcher extends AbstractMatcher {

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
	 * @see AbstractMatcher
	 */
	public SuperclassMatcher(IInheritanceAnalyzer inheritanceAnalyzer, IClassPoolAnalyzer classPoolAnalyzer, UnregisteredSensorConfig unregisteredSensorConfig) {
		super(classPoolAnalyzer, unregisteredSensorConfig);

		this.inheritanceAnalyzer = inheritanceAnalyzer;

		if (unregisteredSensorConfig.isVirtual()) {
			delegateMatcher = new IndirectMatcher(classPoolAnalyzer, unregisteredSensorConfig);
		} else {
			delegateMatcher = new DirectMatcher(classPoolAnalyzer, unregisteredSensorConfig);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean compareClassName(ClassLoader classLoader, String className) throws NotFoundException {
		Iterator<CtClass> i = inheritanceAnalyzer.getSuperclassIterator(classLoader, className);
		while (i.hasNext()) {
			CtClass clazz = i.next();
			if (delegateMatcher.compareClassName(classLoader, clazz.getName())) {
				return true;
			}
		}

		return false;
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
		return delegateMatcher.getMatchingConstructors(classLoader, className);
	}

	/**
	 * {@inheritDoc}
	 */
	public void checkParameters(List<? extends CtBehavior> methods) throws NotFoundException {
		delegateMatcher.checkParameters(methods);
	}

}
