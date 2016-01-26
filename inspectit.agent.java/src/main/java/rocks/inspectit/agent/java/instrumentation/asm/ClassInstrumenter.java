package rocks.inspectit.agent.java.instrumentation.asm;

import info.novatec.inspectit.org.objectweb.asm.ClassVisitor;
import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;
import info.novatec.inspectit.org.objectweb.asm.Opcodes;
import info.novatec.inspectit.org.objectweb.asm.Type;
import info.novatec.inspectit.org.objectweb.asm.commons.JSRInlinerAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import rocks.inspectit.shared.all.instrumentation.config.IMethodInstrumentationPoint;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;

/**
 * Used to instrument classes via ASM.
 *
 * @author Ivan Senic
 *
 */
public class ClassInstrumenter extends ClassVisitor {

	/**
	 * All instrumentation configurations that should be added as instrumentation points.
	 */
	private final Collection<MethodInstrumentationConfig> instrumentationConfigs;

	/**
	 * Collection of the applied instrumentation configurations.
	 */
	private final Collection<MethodInstrumentationConfig> appliedInstrumentationConfigs = new ArrayList<MethodInstrumentationConfig>(0);

	/**
	 * Loader aware class writer we delegate calls to.
	 */
	private final LoaderAwareClassWriter loaderAwareClassWriter;

	/**
	 * If enhanced exception sensor is active.
	 */
	private final boolean enhancedExceptionSensor;

	/**
	 * Default constructor.
	 *
	 * @param loaderAwareClassWriter
	 *            LoaderAwareClassWriter
	 * @param methodInstrumentationConfigs
	 *            Config holding instrumentation points.
	 * @param enhancedExceptionSensor
	 *            If enhanced exception sensor is active.
	 */
	public ClassInstrumenter(LoaderAwareClassWriter loaderAwareClassWriter, Collection<MethodInstrumentationConfig> methodInstrumentationConfigs, boolean enhancedExceptionSensor) {
		super(Opcodes.ASM5, loaderAwareClassWriter);
		this.instrumentationConfigs = new ArrayList<MethodInstrumentationConfig>(methodInstrumentationConfigs);
		this.enhancedExceptionSensor = enhancedExceptionSensor;
		this.loaderAwareClassWriter = loaderAwareClassWriter;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		// calling super to ensure the visitor pattern
		super.visit(version, access, name, signature, superName, interfaces);

		loaderAwareClassWriter.setClassName(name);
		loaderAwareClassWriter.setSuperClassName(superName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		// calling super to ensure the visitor pattern
		MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);

		MethodInstrumentationConfig instrumentationConfig = shouldInstrument(name, desc);
		if (null != instrumentationConfig) {
			// using JSR inliner adapter in order to remove JSR/RET instructions
			// see http://mail-archive.ow2.org/asm/2008-11/msg00008.html
			// using only if we add byte code
			methodVisitor = new JSRInlinerAdapter(methodVisitor, access, name, desc, signature, exceptions);

			// go over all instrumentation points and create the method visitor
			for (IMethodInstrumentationPoint instrumentationPoint : instrumentationConfig.getAllInstrumentationPoints()) {
				// note that here we create a chain of method visitor by passing the current one to
				// the one being created, thus following the visitor pattern of ASM
				MethodVisitor mv = instrumentationPoint.getMethodVisitor(methodVisitor, access, name, desc, enhancedExceptionSensor);

				// safety for the null returned method visitor
				if (null != mv) {
					methodVisitor = mv;
				}
			}

			// add to list so that we know which are applied
			appliedInstrumentationConfigs.add(instrumentationConfig);
		}

		return methodVisitor;
	}

	/**
	 * If method should be instrumented. If there is appropriate {@link MethodInstrumentationConfig}
	 * that denotes that method should be instrumented this will be removed from the
	 * {@link #instrumentationConfigs} and returned as a result.
	 *
	 * @param name
	 *            Name of the method.
	 * @param desc
	 *            ASM description of the method.
	 * @return {@link MethodInstrumentationConfig} if method should be instrumented, otherwise
	 *         <code>null</code>
	 */
	private MethodInstrumentationConfig shouldInstrument(String name, String desc) {
		for (Iterator<MethodInstrumentationConfig> it = instrumentationConfigs.iterator(); it.hasNext();) {
			MethodInstrumentationConfig config = it.next();

			if (matches(name, desc, config)) {
				it.remove();
				return config;
			}
		}

		return null;
	}

	/**
	 * If method name and description matches the {@link MethodInstrumentationConfig}.
	 *
	 * @param name
	 *            method name
	 * @param desc
	 *            method ASM description
	 * @param instrumentationConfig
	 *            {@link MethodInstrumentationConfig}
	 * @return <code>true</code> if name and desc matches the instrumentation config
	 */
	private boolean matches(String name, String desc, MethodInstrumentationConfig instrumentationConfig) {
		if (!name.equals(instrumentationConfig.getTargetMethodName())) {
			return false;
		}

		Type methodType = Type.getMethodType(desc);
		if (!methodType.getReturnType().getClassName().equals(instrumentationConfig.getReturnType())) {
			return false;
		}

		Type[] argumentTypes = methodType.getArgumentTypes();
		List<String> parameterTypes = instrumentationConfig.getParameterTypes();

		// if both are empty return true, if only one is empty return false (null safety)
		if (CollectionUtils.isEmpty(parameterTypes) && ArrayUtils.isEmpty(argumentTypes)) {
			return true;
		} else if (CollectionUtils.isEmpty(parameterTypes) || ArrayUtils.isEmpty(argumentTypes)) {
			return false;
		}

		// if not same size return false
		if (argumentTypes.length != parameterTypes.size()) {
			return false;
		}

		// check then one by one
		for (int i = 0; i < argumentTypes.length; i++) {
			if (!argumentTypes[i].getClassName().equals(parameterTypes.get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns if the byte code was added. This effectively checks if the
	 * {@link #appliedInstrumentationConfigs} is not empty.
	 *
	 * @return Returns if any byte code was added.
	 */
	public boolean isByteCodeAdded() {
		return CollectionUtils.isNotEmpty(appliedInstrumentationConfigs);
	}

	/**
	 * Gets {@link #appliedInstrumentationConfigs}.
	 *
	 * @return {@link #appliedInstrumentationConfigs}
	 */
	public Collection<MethodInstrumentationConfig> getAppliedInstrumentationConfigs() {
		return appliedInstrumentationConfigs;
	}

}
