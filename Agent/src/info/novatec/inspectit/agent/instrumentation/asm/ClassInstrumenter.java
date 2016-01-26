package info.novatec.inspectit.agent.instrumentation.asm;

import info.novatec.inspectit.instrumentation.config.IMethodInstrumentationPoint;
import info.novatec.inspectit.instrumentation.config.impl.MethodInstrumentationConfig;
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
	Collection<MethodInstrumentationConfig> instrumentationConfigs;

	/**
	 * Collection of the applied instrumentation configurations.
	 */
	private final Collection<MethodInstrumentationConfig> appliedInstrumentationConfigs = new ArrayList<MethodInstrumentationConfig>(0);

	/**
	 * Simple constructor. Can be used in testing.
	 *
	 * @param classVisitor
	 *            Parent class visitor.
	 */
	ClassInstrumenter(ClassVisitor classVisitor) {
		super(Opcodes.ASM5, classVisitor);
	}

	/**
	 * Default constructor.
	 *
	 * @param classVisitor
	 *            Parent class visitor.
	 * @param methodInstrumentationConfigs
	 *            Config holding instrumentation points.
	 */
	public ClassInstrumenter(ClassVisitor classVisitor, Collection<MethodInstrumentationConfig> methodInstrumentationConfigs) {
		super(Opcodes.ASM5, classVisitor);
		this.instrumentationConfigs = new ArrayList<MethodInstrumentationConfig>(methodInstrumentationConfigs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		// calling super to ensure the visitor pattern
		MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);

		// using JSR inliner adapter in order to remove JSR/RET instructions
		// see http://mail-archive.ow2.org/asm/2008-11/msg00008.html
		methodVisitor = new JSRInlinerAdapter(methodVisitor, access, name, desc, signature, exceptions);

		MethodInstrumentationConfig instrumentationConfig = shouldInstrument(name, desc);
		if (null != instrumentationConfig) {
			// go over all instrumentation points and create the method visitor
			for (IMethodInstrumentationPoint instrumentationPoint : instrumentationConfig.getAllInstrumentationPoints()) {
				MethodVisitor mv = instrumentationPoint.getMethodVisitor(methodVisitor, access, name, desc);

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
	MethodInstrumentationConfig shouldInstrument(String name, String desc) {
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

		// if both are empty return true (null safety)
		if (CollectionUtils.isEmpty(parameterTypes) && ArrayUtils.isEmpty(argumentTypes)) {
			return true;
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
	 * Returns if the any byte code was added. This effectively checks if the
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
