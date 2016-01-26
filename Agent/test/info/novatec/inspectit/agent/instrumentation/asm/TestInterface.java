package info.novatec.inspectit.agent.instrumentation.asm;

import java.io.Serializable;

@TestAnnotation
@SuppressWarnings("PMD")
interface TestInterface extends Serializable {

	@TestAnnotation
	String method1(int i, long[] j, String s, Object[][][] array3Dim);

}