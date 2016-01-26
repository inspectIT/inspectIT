package info.novatec.inspectit.agent.instrumentation.asm;

import info.novatec.inspectit.instrumentation.classcache.AnnotationType;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableType;
import info.novatec.inspectit.instrumentation.classcache.InterfaceType;
import info.novatec.inspectit.instrumentation.classcache.MethodType;
import info.novatec.inspectit.instrumentation.classcache.TypeWithAnnotations;
import info.novatec.inspectit.instrumentation.classcache.TypeWithMethods;
import info.novatec.inspectit.org.objectweb.asm.AnnotationVisitor;
import info.novatec.inspectit.org.objectweb.asm.ClassVisitor;
import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;
import info.novatec.inspectit.org.objectweb.asm.Opcodes;
import info.novatec.inspectit.org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

/**
 * Clas for parsing byte code of a class and creating a
 * {@link info.novatec.inspectit.instrumentation.classcache.Type} object.
 *
 * @author Ivan Senic
 *
 */
public class ClassAnalyzer extends ClassVisitor {

	/**
	 * {@link ImmutableType} being created by this Analyzer.
	 */
	private ImmutableType type;

	/**
	 * Hash of the class that s being parsed.
	 */
	private String hash;

	/**
	 * Should prior to setting the FQNs to the created type {@link String#intern()} be called to
	 * optimize the strings in memory. It's expected that this operation provide memory benefits but
	 * to cost additional processing time.
	 */
	private final boolean internFQNs;

	/**
	 * Constructor. Using this constructor requires calling
	 * {@link #prepare(String, IClassCacheModification)} before class analyzing.
	 *
	 * @param classVisitor
	 *            Parent class visitor.
	 * @param internFQNs
	 *            Should prior to setting the FQNs to the created type {@link String#intern()} be
	 *            called to optimize the strings in memory. It's expected that this operation
	 *            provide memory benefits but to cost additional processing time.
	 */
	public ClassAnalyzer(ClassVisitor classVisitor, boolean internFQNs) {
		super(Opcodes.ASM5, classVisitor);
		this.internFQNs = internFQNs;
	}

	/**
	 * Default constructor.
	 *
	 * @param hash
	 *            Hash for the class to be analyzed.
	 * @param classVisitor
	 *            Parent class visitor.
	 * @param internFQNs
	 *            Should prior to setting the FQNs to the created type {@link String#intern()} be
	 *            called to optimize the strings in memory. It's expected that this operation
	 *            provide memory benefits but to cost additional processing time.
	 */
	public ClassAnalyzer(String hash, ClassVisitor classVisitor, boolean internFQNs) {
		super(Opcodes.ASM5, classVisitor);
		this.internFQNs = internFQNs;
		prepare(hash);
	}

	/**
	 * Prepares the {@link ClassAnalyzer} for new class analyzing. Use this method to prior to start
	 * analyzing the class. This way no new instance of the {@link ClassAnalyzer} must be created.
	 *
	 * @param hash
	 *            Hash for the class to be analyzed.
	 */
	public final void prepare(String hash) {
		this.hash = hash;
		this.type = null; // NOPMD
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
			this.type = new AnnotationType(AsmUtil.getFqn(name, internFQNs), hash, modifiers);
		} else if ((Opcodes.ACC_INTERFACE & access) > 0) {
			// if interface create type and add super interface
			InterfaceType interfaceType = new InterfaceType(AsmUtil.getFqn(name, internFQNs), hash, modifiers);

			if (ArrayUtils.isNotEmpty(interfaces)) {
				for (String interfaceName : interfaces) {
					interfaceType.addSuperInterface(new InterfaceType(AsmUtil.getFqn(interfaceName, internFQNs)));
				}
			}

			this.type = interfaceType;
		} else {
			// if class create type and add superclass and interfaces
			ClassType classType = new ClassType(AsmUtil.getFqn(name, internFQNs), hash, modifiers);

			if (null != superName) {
				ClassType superClassType = new ClassType(AsmUtil.getFqn(superName, internFQNs));
				classType.addSuperClass(superClassType);
			}

			if (ArrayUtils.isNotEmpty(interfaces)) {
				for (String interfaceName : interfaces) {
					classType.addInterface(new InterfaceType(AsmUtil.getFqn(interfaceName, internFQNs)));
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
			return annotationVisitor;
		}

		String internalName = Type.getType(desc).getInternalName();
		((TypeWithAnnotations) type).addAnnotation(new AnnotationType(AsmUtil.getFqn(internalName, internFQNs)));

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
			return superMethodVisitor;
		}

		Type methodTypeHelper = Type.getMethodType(desc);

		MethodType methodType = new MethodType();
		methodType.setModifiers(AsmUtil.getModifiers(access));
		if (internFQNs) {
			methodType.setName(name.intern());
		} else {
			methodType.setName(name);
		}

		// exceptions
		if (ArrayUtils.isNotEmpty(exceptions)) {
			for (String exceptionName : exceptions) {
				methodType.addException(new ClassType(AsmUtil.getFqn(exceptionName, internFQNs)));
			}
		}

		// return value
		if (internFQNs) {
			methodType.setReturnType(methodTypeHelper.getReturnType().getClassName().intern());
		} else {
			methodType.setReturnType(methodTypeHelper.getReturnType().getClassName());
		}

		// parameters
		int params = methodTypeHelper.getArgumentTypes().length;
		if (params > 0) {
			List<String> parameters = new ArrayList<String>(params);
			for (Type parameterType : methodTypeHelper.getArgumentTypes()) {
				if (internFQNs) {
					parameters.add(parameterType.getClassName().intern());
				} else {
					parameters.add(parameterType.getClassName());
				}

			}
			methodType.setParameters(parameters);
		}

		((TypeWithMethods) type).addMethod(methodType);

		return new MethodAnalyzer(methodType, internFQNs, superMethodVisitor);
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
