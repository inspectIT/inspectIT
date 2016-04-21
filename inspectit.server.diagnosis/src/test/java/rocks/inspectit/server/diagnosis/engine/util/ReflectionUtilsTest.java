package rocks.inspectit.server.diagnosis.engine.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.testng.annotations.Test;

import rocks.inspectit.server.diagnosis.engine.rule.annotation.Action;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.Rule;
import rocks.inspectit.server.diagnosis.engine.rule.annotation.TagValue;
import rocks.inspectit.server.diagnosis.engine.rule.exception.RuleDefinitionException;
import rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils.Visitor;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link ReflectionUtils} class.
 *
 * @author Alexander Wert
 *
 */
public class ReflectionUtilsTest extends TestBase {

	/**
	 * Tests the {@link ReflectionUtils#tryInstantiate(Class)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class TryInstantiate extends ReflectionUtilsTest {

		@Test
		public void validClass() {
			TestClass instance = ReflectionUtils.tryInstantiate(TestClass.class);
			assertThat(instance.par1, is(1));
		}

		@Test(expectedExceptions = { RuntimeException.class })
		public void noDefaultConstructor() {
			ReflectionUtils.tryInstantiate(NoDefaultConstructorClass.class);
		}
	}

	/**
	 * Tests the {@link ReflectionUtils#hasNoArgsConstructor(Class)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class HasNoArgsConstructor extends ReflectionUtilsTest {

		@Test
		public void defaultConstructor() {
			boolean constructor = ReflectionUtils.hasNoArgsConstructor(TestClass.class);
			assertThat(constructor, is(true));
		}

		@Test
		public void noDefaultConstructor() {
			boolean constructor = ReflectionUtils.hasNoArgsConstructor(NoDefaultConstructorClass.class);
			assertThat(constructor, is(false));
		}

		@Test
		public void privateConstructor() {
			boolean constructor = ReflectionUtils.hasNoArgsConstructor(PrivateConstructorClass.class);
			assertThat(constructor, is(false));
		}
	}

	/**
	 * Tests the {@link ReflectionUtils#findAnnotation(Class, Class)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class FindAnnotation extends ReflectionUtilsTest {

		@Test
		public void availableAnnotation() {
			Rule ruleAnnotation = ReflectionUtils.findAnnotation(TestClass.class, Rule.class);
			assertThat(ruleAnnotation, not(equalTo(null)));
		}

		@Test
		public void unavailableAnnotation() {
			Rule ruleAnnotation = ReflectionUtils.findAnnotation(NoDefaultConstructorClass.class, Rule.class);
			assertThat(ruleAnnotation, equalTo(null));
		}
	}

	/**
	 * Tests the
	 * {@link ReflectionUtils#visitFieldsAnnotatedWith(Class, Class, rocks.inspectit.server.diagnosis.engine.util.ReflectionUtils.Visitor)}
	 * method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class VisitFieldsAnnotatedWith extends ReflectionUtilsTest {

		@Test
		public void collectAllFields() throws RuleDefinitionException {
			List<String> fieldNames = ReflectionUtils.visitFieldsAnnotatedWith(TagValue.class, TestClass.class, new Visitor<TagValue, Field, String>() {
				@Override
				public String visit(TagValue annotation, Field field) {
					return field.getName();
				}
			});

			assertThat(fieldNames, containsInAnyOrder("par1", "par2", "par3", "par4"));
		}

		@Test(expectedExceptions = { NullPointerException.class })
		public void visitorReturnsNull() throws RuleDefinitionException {
			ReflectionUtils.visitFieldsAnnotatedWith(TagValue.class, TestClass.class, new Visitor<TagValue, Field, String>() {
				@Override
				public String visit(TagValue annotation, Field field) {
					return field.getName().endsWith("2") ? null : "ok";
				}
			});
		}

		@Test(expectedExceptions = { RuleDefinitionException.class })
		public void explicitVisitorException() throws RuleDefinitionException {
			ReflectionUtils.visitFieldsAnnotatedWith(TagValue.class, TestClass.class, new Visitor<TagValue, Field, String>() {
				@Override
				public String visit(TagValue annotation, Field field) throws RuleDefinitionException {
					if (field.getName().endsWith("2")) {
						throw new RuleDefinitionException("test exception");
					} else {
						return "ok";
					}
				}
			});
		}
	}

	/**
	 * Tests the {@link ReflectionUtils#visitMethodsAnnotatedWith(Class, Class, Visitor)} method.
	 *
	 * @author Alexander Wert
	 *
	 */
	public class VisitMethodsAnnotatedWith extends ReflectionUtilsTest {

		@Test
		public void collectAllMethods() throws RuleDefinitionException {
			List<String> methodNames = ReflectionUtils.visitMethodsAnnotatedWith(Action.class, TestClass.class, new Visitor<Action, Method, String>() {
				@Override
				public String visit(Action annotation, Method method) {
					return method.getName();
				}
			});

			assertThat(methodNames, containsInAnyOrder("actionA", "actionB", "actionC", "actionD"));
		}

		@Test(expectedExceptions = { NullPointerException.class })
		public void visitorReturnsNull() throws RuleDefinitionException {
			ReflectionUtils.visitMethodsAnnotatedWith(Action.class, TestClass.class, new Visitor<Action, Method, String>() {
				@Override
				public String visit(Action annotation, Method method) {
					return method.getName().endsWith("C") ? null : "ok";
				}
			});
		}

		@Test(expectedExceptions = { RuleDefinitionException.class })
		public void explicitVisitorException() throws RuleDefinitionException {
			ReflectionUtils.visitMethodsAnnotatedWith(Action.class, TestClass.class, new Visitor<Action, Method, String>() {
				@Override
				public String visit(Action annotation, Method method) throws RuleDefinitionException {
					if (method.getName().endsWith("C")) {
						throw new RuleDefinitionException("test exception");
					} else {
						return "ok";
					}
				}
			});
		}
	}
}
