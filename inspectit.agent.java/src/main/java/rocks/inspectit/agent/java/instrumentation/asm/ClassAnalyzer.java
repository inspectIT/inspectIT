package rocks.inspectit.agent.java.instrumentation.asm;

import info.novatec.inspectit.org.objectweb.asm.AnnotationVisitor;
import info.novatec.inspectit.org.objectweb.asm.ClassVisitor;
import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;
import info.novatec.inspectit.org.objectweb.asm.Opcodes;
import info.novatec.inspectit.org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.shared.all.instrumentation.classcache.AnnotationType;
import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableType;
import rocks.inspectit.shared.all.instrumentation.classcache.InterfaceType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.classcache.TypeWithAnnotations;
import rocks.inspectit.shared.all.instrumentation.classcache.TypeWithMethods;

/**
 * Class for parsing byte code of a class and creating a
 * {@link info.novatec.inspectit.instrumentation.classcache.Type} object.
 *
 * @author Ivan Senic
 *
 */
public class ClassAnalyzer extends ClassVisitor {

	/**
	 * Logger for the class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ClassAnalyzer.class);

	/**
	 * {@link ImmutableType} being created by this Analyzer.
	 */
	private ImmutableType type;

	/**
	 * Hash of the class that s being parsed.
	 */
	private final String hash;


	/**
	 * Default constructor. ClassVisitor will be set to <code>null</code>.
	 *
	 * @param hash
	 *            Hash for the class to be analyzed.
	 */
	public ClassAnalyzer(String hash) {
		this(hash, null);
	}

	/**
	 * Secondary constructor.
	 *
	 * @param hash
	 *            Hash for the class to be analyzed.
	 * @param classVisitor
	 *            Parent class visitor.
	 */
	public ClassAnalyzer(String hash, ClassVisitor classVisitor) {
		super(Opcodes.ASM5, classVisitor);
		this.hash = hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		// calling super to ensure the visitor pattern
		super.visit(version, access, name, signature, superName, interfaces);

		// calculating modifiers
		int modifiers = AsmUtil.getModifiers(access);

		if ((Opcodes.ACC_ANNOTATION & access) > 0) {
			// if annotation just create type
			this.type = new AnnotationType(AsmUtil.getFqn(name), hash, modifiers);
		} else if ((Opcodes.ACC_INTERFACE & access) > 0) {
			// if interface create type and add super interface
			InterfaceType interfaceType = new InterfaceType(AsmUtil.getFqn(name), hash, modifiers);

			if (ArrayUtils.isNotEmpty(interfaces)) {
				for (String interfaceName : interfaces) {
					interfaceType.addSuperInterface(new InterfaceType(AsmUtil.getFqn(interfaceName)));
				}
			}

			this.type = interfaceType;
		} else {
			// if class create type and add superclass and interfaces
			ClassType classType = new ClassType(AsmUtil.getFqn(name), hash, modifiers);

			if (null != superName) {
				ClassType superClassType = new ClassType(AsmUtil.getFqn(superName));
				classType.addSuperClass(superClassType);
			}

			if (ArrayUtils.isNotEmpty(interfaces)) {
				for (String interfaceName : interfaces) {
					classType.addInterface(new InterfaceType(AsmUtil.getFqn(interfaceName)));
				}
			}

			this.type = classType;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		// calling super to ensure the visitor pattern
		AnnotationVisitor annotationVisitor = super.visitAnnotation(desc, visible);

		// if we don't have correct type return
		if (!(type instanceof TypeWithAnnotations)) {
			LOG.error("Annotation visit failed as the analyzed type " + type + " is not implementing the TypeWithAnnotations interface.");
			return annotationVisitor;
		}

		String internalName = Type.getType(desc).getInternalName();
		((TypeWithAnnotations) type).addAnnotation(new AnnotationType(AsmUtil.getFqn(internalName)));

		return annotationVisitor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		// calling super to ensure the visitor pattern
		MethodVisitor superMethodVisitor = super.visitMethod(access, name, desc, signature, exceptions);

		// if we don't have correct type return
		if (!(type instanceof TypeWithMethods)) {
			// this can happen as for annotation we don't want to store the methods, but they do
			// have them
			return superMethodVisitor;
		}

		Type methodTypeHelper = Type.getMethodType(desc);

		MethodType methodType = new MethodType();
		methodType.setModifiers(AsmUtil.getModifiers(access));
		methodType.setName(name);

		// exceptions
		if (ArrayUtils.isNotEmpty(exceptions)) {
			for (String exceptionName : exceptions) {
				methodType.addException(new ClassType(AsmUtil.getFqn(exceptionName)));
			}
		}

		// return value
		methodType.setReturnType(methodTypeHelper.getReturnType().getClassName());

		// parameters
		int params = methodTypeHelper.getArgumentTypes().length;
		if (params > 0) {
			List<String> parameters = new ArrayList<String>(params);
			for (Type parameterType : methodTypeHelper.getArgumentTypes()) {
				parameters.add(parameterType.getClassName());
			}
			methodType.setParameters(parameters);
		}

		((TypeWithMethods) type).addMethod(methodType);

		return new MethodAnalyzer(methodType, superMethodVisitor);
	}

	/**
	 * Gets {@link #type}.
	 *
	 * @return {@link #type}
	 */
	public ImmutableType getType() {
		return type;
	}

}
