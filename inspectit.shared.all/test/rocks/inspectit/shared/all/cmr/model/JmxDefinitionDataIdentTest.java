package info.novatec.inspectit.cmr.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.Test;

/**
 * This tests checks the "getDerived..()" methods of the {@link JmxDefinitionDataIdent} class.
 * 
 * @author Marius Oehler
 *
 */
public class JmxDefinitionDataIdentTest {

	/**
	 * Tests the {@link JmxDefinitionDataIdent#getDerivedDomainName()} method.
	 */
	@Test
	public void getDerivedDomainName() {
		JmxDefinitionDataIdent dataIdent = new JmxDefinitionDataIdent();
		dataIdent.setmBeanObjectName("java.lang:type=GarbageCollector");

		assertThat(dataIdent.getDerivedDomainName(), is(equalTo("java.lang")));
	}

	/**
	 * Tests the {@link JmxDefinitionDataIdent#getDerivedDomainName()} method with a more complex
	 * bean-object name.
	 */
	@Test
	public void getDerivedDomainNameComplex() {
		JmxDefinitionDataIdent dataIdent = new JmxDefinitionDataIdent();
		dataIdent.setmBeanObjectName("java.lang:type=GarbageCollector,name=PS MarkSweep");

		assertThat(dataIdent.getDerivedDomainName(), is(equalTo("java.lang.GarbageCollector")));
	}

	/**
	 * Tests the {@link JmxDefinitionDataIdent#getDerivedTypeName()} method.
	 */
	@Test
	public void getDerivedTypeName() {
		JmxDefinitionDataIdent dataIdent = new JmxDefinitionDataIdent();
		dataIdent.setmBeanObjectName("java.lang:type=GarbageCollector");

		assertThat(dataIdent.getDerivedTypeName(), is(equalTo("GarbageCollector")));
	}

	/**
	 * Tests the {@link JmxDefinitionDataIdent#getDerivedTypeName()} method with a more complex
	 * bean-object name.
	 */
	@Test
	public void getDerivedTypeNameComplex() {
		JmxDefinitionDataIdent dataIdent = new JmxDefinitionDataIdent();
		dataIdent.setmBeanObjectName("java.lang:type=GarbageCollector,name=PS MarkSweep");

		assertThat(dataIdent.getDerivedTypeName(), is(equalTo("PS MarkSweep")));
	}

	/**
	 * Tests the {@link JmxDefinitionDataIdent#getDerivedFullName()} method.
	 */
	@Test
	public void getDerivedFullName() {
		JmxDefinitionDataIdent dataIdent = new JmxDefinitionDataIdent();
		dataIdent.setmBeanObjectName("java.lang:type=GarbageCollector");
		dataIdent.setmBeanAttributeName("SystemCpuLoad");

		assertThat(dataIdent.getDerivedFullName(), is(equalTo("java.lang.GarbageCollector:SystemCpuLoad")));
	}
}
