package rocks.inspectit.agent.java.proxy.impl;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.proxy.IProxyBuildPlan;
import rocks.inspectit.agent.java.proxy.IProxyBuilder;
import rocks.inspectit.agent.java.proxy.IProxyClassInfo;
import rocks.inspectit.agent.java.proxy.IProxySubject;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.shared.all.spring.logger.Log;


/**
 * A RuntimeLinker implementation with caching.
 * @see {@link IRuntimeLinker}
 * @author Jonas Kunz
 */
@Component
public final class RuntimeLinker implements IRuntimeLinker {



	/**
	 * logger.
	 */
	@Log
	private Logger log;


	/**
	 * The proxy builder used for creating hte bytecode based on build plans.
	 */
	@Autowired
	private IProxyBuilder proxyBuilder;


	/**
	 * The suffix used for naming proxy classes.
	 */
	private static final String PROXY_CLASS_NAME_SUFFIX = "$inspectiT_proxy";

	/**
	 * Caches the generated Proxy classes
	 * 1st key (Class<?>) the class of the proxy subject (e.g. TagInjectionResponseWrapper)
	 * 2nd key (ClassLoader) the classloader which owns the proxy class
	 */
	private ConcurrentHashMap<Class<?>, HashMap<ClassLoader, IProxyClassInfo>> linkedClassCache;

	/**
	 * Placed in the cache when there was an error building the proxy class.
	 */
	private static final IProxyClassInfo UNLINKEABLE_MARKER = new IProxyClassInfo() {
		// CHECKSTYLE:OFF
		@Override
		public Class<?> getProxyClass() {
			return null;
		}

		@Override
		public Object createProxy(IProxySubject proxySubject) {
			return null;
		}
		// CHECKSTYLE:ON
	};

	/**
	 * Creates an Runtime Linker.
	 * Only one RuntimeLinker should exist in the entire VM.
	 */
	public RuntimeLinker() {
		linkedClassCache = new ConcurrentHashMap<Class<?>, HashMap<ClassLoader, IProxyClassInfo>>();
	}



	/**
	 * Naming of the proxyclass for a given proxy subject type.
	 * @param unlinkedClassName the full qualified name of the proxy-subject type
	 * @return the name of the proxy class
	 */
	private String getProxyClassName(String unlinkedClassName) {
		return unlinkedClassName + PROXY_CLASS_NAME_SUFFIX;
	}

	/**
	 * Creates a proxy for a given proxySubject instance.
	 * The ProxySubject is the object that recives the calls, the proxy redirects them to it.
	 *
	 * @param <T> the type of the ProxySubject (e.g. TagInjectionResponseWrapper)
	 * @param proxySubjectType the class of <T>
	 * @param proxySubject the proxySubject instance to link
	 * @param context a classloader which has access to the api the proxy is linked to
	 * @return the proxy instance, if the linkage was successfull,
	 */
	@Override
	public <T extends IProxySubject> Object createProxy(Class<T> proxySubjectType, T proxySubject, ClassLoader context) {
		IProxyClassInfo proxyClassInfo = getProxyClass(proxySubjectType, context, true);
		if (proxyClassInfo == null) {
			return null;
		} else {
			try {
				//create the isntance
				Object proxy = proxyClassInfo.createProxy(proxySubject);
				//let the subject know about the new proxy
				proxySubject.proxyLinked(proxy, this);

				return proxy;
			} catch (Exception e) {
				log.error("Error creating proxy: " + e.getMessage());
				return null;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends IProxySubject> boolean isProxyInstance(Object inst, Class<T> proxySubjectType) {
		if (inst == null) {
			return false;
		}
		ClassLoader context = inst.getClass().getClassLoader();
		IProxyClassInfo proxyClass = getProxyClass(proxySubjectType, context, false);
		if (proxyClass == null) {
			return false;
		} else {
			return proxyClass.getProxyClass().isInstance(inst);
		}
	}

	/**
	 * Checks the cache for a proxy class.
	 * If it does not exist, it is created.
	 * @param <T> the type of the ProxySubject
	 * @param subjectType the type of the ProxySubject
	 * @param context the context classloader
	 * @param create true, if the proxy class should be generated if it is not found
	 * @return the found proxy class, or null if non was found and it could not be created.
	 */
	private <T> IProxyClassInfo getProxyClass(Class<T> subjectType, ClassLoader context, boolean create)  {
		//we are not interested in the bootstrap loader
		if (context == null) {
			context = ClassLoader.getSystemClassLoader();
		}
		HashMap<ClassLoader, IProxyClassInfo> classLoaderMap = linkedClassCache.get(subjectType);
		if (classLoaderMap == null) {
			linkedClassCache.putIfAbsent(subjectType, new HashMap<ClassLoader, IProxyClassInfo>());
			classLoaderMap = linkedClassCache.get(subjectType);
		}
		IProxyClassInfo proxyClassInfo = classLoaderMap.get(context);
		if (proxyClassInfo == null) {
			synchronized (classLoaderMap) {
				//check again (double check is used to avoid synchronization every call)
				proxyClassInfo = classLoaderMap.get(context);
				if (proxyClassInfo == null) {
					//maybe the proxy was created in a parent class loader, so check for it
					try {
						Class<?> proxyClazz = Class.forName(getProxyClassName(subjectType.getName()), false, context);
						ClassLoader owner = proxyClazz.getClassLoader();
						proxyClassInfo = classLoaderMap.get(owner);
						//place it in the cache for faster checking next time
						classLoaderMap.put(context, proxyClassInfo);
					} catch (ClassNotFoundException e) {
						//class was not instantiated by a parent class loader. We really have to create it (if requested)
						if (create) {
							try {
								String name = getProxyClassName(subjectType.getName());
								IProxyBuildPlan plan = ProxyBuildPlanImpl.create(subjectType, name, context);
								//final check: is it really not in the target classloader? might be the case if
								//someone implemented a classloader with some strange delegation strategy
								ClassLoader target = plan.getTargetClassLoader();
								try {
									Class<?> proxyClazz = Class.forName(name, false, target);
									ClassLoader owner = proxyClazz.getClassLoader();
									proxyClassInfo = classLoaderMap.get(owner);
									//place it in the cache for faster checking next time
									classLoaderMap.put(context, proxyClassInfo);
								} catch (ClassNotFoundException e2) {
									proxyClassInfo = proxyBuilder.createProxyClass(plan);
									classLoaderMap.put(proxyClassInfo.getProxyClass().getClassLoader(), proxyClassInfo);
									classLoaderMap.put(context, proxyClassInfo);
								}
							} catch (Exception e2) {
								log.error("Could not create proxy for " + subjectType.getName() + " in classloader " + context, e2);
								classLoaderMap.put(context, UNLINKEABLE_MARKER);
								proxyClassInfo = UNLINKEABLE_MARKER;
							}
						} else {
							return null;
						}
					}
				}
			}
		}
		if (UNLINKEABLE_MARKER.equals(proxyClassInfo)) {
			return null;
		} else {
			return proxyClassInfo;
		}

	}

}
