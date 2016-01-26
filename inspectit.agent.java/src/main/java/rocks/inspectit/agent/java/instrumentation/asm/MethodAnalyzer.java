package rocks.inspectit.agent.java.instrumentation.asm;

import info.novatec.inspectit.org.objectweb.asm.AnnotationVisitor;
import info.novatec.inspectit.org.objectweb.asm.MethodVisitor;
import info.novatec.inspectit.org.objectweb.asm.Opcodes;
import info.novatec.inspectit.org.objectweb.asm.Type;

import rocks.inspectit.shared.all.instrumentation.classcache.AnnotationType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;

/**
 * Method analyzer for picking up annotations of a method.
 *
 * @author Ivan Senic
 *
 */
public class MethodAnalyzer extends MethodVisitor {

	/**
	 * Method type to store information from the byte code.
	 */
	private final MethodType methodType;

	/**
	 * Default constructor.
	 *
	 * @param methodType
	 *            Method type to store the read information to.
	 * @param mv
	 *            Parent method visitor.
	 */
	public MethodAnalyzer(MethodType methodType, MethodVisitor mv) {
		super(Opcodes.ASM5, mv);
		this.methodType = methodType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		// calling super to ensure the visitor pattern
		AnnotationVisitor annotationVisitor = super.visitAnnotation(desc, visible);

		if (null == methodType) {
			return annotationVisitor;
		}

		String internalName = Type.getType(desc).getInternalName();
		methodType.addAnnotation(new AnnotationType(AsmUtil.getFqn(internalName)));

		return annotationVisitor;
	}

}
