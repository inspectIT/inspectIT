package rocks.inspectit.server.instrumentation.config.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableAnnotationType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableMethodType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType.Character;
import rocks.inspectit.shared.all.instrumentation.classcache.Modifiers;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;

/**
 * Test for the {@link MethodSensorAssignmentFilter}.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class MethodSensorAssignmentFilterTest extends TestBase {

	protected MethodSensorAssignmentFilter filter;

	@Mock
	protected MethodSensorAssignment assignment;

	@Mock
	protected ClassType classType;

	@Mock
	protected ImmutableMethodType methodType;

	@BeforeMethod
	public void init() {
		filter = new MethodSensorAssignmentFilter();
	}

	public class Matches extends MethodSensorAssignmentFilterTest {
		@Test
		public void base() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);

			boolean result = filter.matches(assignment, methodType);

			assertThat(result, is(true));
		}

		@Test
		public void constructor() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			// specific
			when(assignment.isConstructor()).thenReturn(true);
			when(methodType.getMethodCharacter()).thenReturn(Character.CONSTRUCTOR);

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(true));
		}

		@Test
		public void constructorNotMatchingMethodType() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			// specific
			when(assignment.isConstructor()).thenReturn(true);
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(false));
		}

		@Test
		public void constructorNotMatchingStaticConstructorType() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			// specific
			when(assignment.isConstructor()).thenReturn(true);
			when(methodType.getMethodCharacter()).thenReturn(Character.STATIC_CONSTRUCTOR);

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(false));
		}

		@Test
		public void constructorNotMatchingAssignment() {
			when(assignment.isConstructor()).thenReturn(false);
			when(methodType.getMethodCharacter()).thenReturn(Character.CONSTRUCTOR);

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(false));
		}

		@Test
		public void constructorNotMatchingAssignmentWildcard() {
			when(assignment.getMethodName()).thenReturn("*");
			when(assignment.isConstructor()).thenReturn(false);
			when(methodType.getMethodCharacter()).thenReturn(Character.CONSTRUCTOR);

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(false));
		}

		@Test
		public void name() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			// specific
			String name = "name";
			when(assignment.getMethodName()).thenReturn(name);
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn(name);

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(true));
		}

		@Test
		public void nameNotMatching() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			// specific
			String name = "name";
			when(assignment.getMethodName()).thenReturn(name);
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("someOtherName");

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(false));
		}

		@Test
		public void nameWildcard() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			// specific
			String name = "n*";
			when(assignment.getMethodName()).thenReturn(name);
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("name");

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(true));
		}

		@Test
		public void nameWildcardNotMatching() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			// specific
			String name = "n*";
			when(assignment.getMethodName()).thenReturn(name);
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("someOtherName");

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(false));
		}

		@Test
		public void publicModifier() {
			when(assignment.getMethodName()).thenReturn("*");
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("name");
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(true));
		}

		@Test
		public void publicModifierNotMatching() {
			when(assignment.getMethodName()).thenReturn("*");
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("name");
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PRIVATE));

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(false));
		}

		@Test
		public void protectedModifier() {
			when(assignment.getMethodName()).thenReturn("*");
			when(assignment.isProtectedModifier()).thenReturn(true);
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("name");
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PROTECTED));

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(true));
		}

		@Test
		public void protectedModifierNotMatching() {
			when(assignment.getMethodName()).thenReturn("*");
			when(assignment.isProtectedModifier()).thenReturn(true);
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("name");
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PRIVATE));

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(false));
		}

		@Test
		public void privateModifier() {
			when(assignment.getMethodName()).thenReturn("*");
			when(assignment.isPrivateModifier()).thenReturn(true);
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("name");
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PRIVATE));

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(true));
		}

		@Test
		public void privateModifierNotMatching() {
			when(assignment.getMethodName()).thenReturn("*");
			when(assignment.isPrivateModifier()).thenReturn(true);
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("name");
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(false));
		}

		@Test
		public void packageModifier() {
			when(assignment.getMethodName()).thenReturn("*");
			when(assignment.isDefaultModifier()).thenReturn(true);
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("name");
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(0));

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(true));
		}

		@Test
		public void packageModifierNotMatching() {
			when(assignment.getMethodName()).thenReturn("*");
			when(assignment.isDefaultModifier()).thenReturn(true);
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("name");
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(false));
		}

		@Test
		public void parameters() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			// specific
			String param = "param";
			when(assignment.getMethodName()).thenReturn("*");
			when(assignment.getParameters()).thenReturn(Collections.singletonList(param));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("name");
			when(methodType.getParameters()).thenReturn(Collections.singletonList(param));

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(true));
		}

		@Test
		public void parametersNull() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			// specific
			String param = "param";
			when(assignment.getMethodName()).thenReturn("*");
			when(assignment.getParameters()).thenReturn(null);
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("name");
			when(methodType.getParameters()).thenReturn(Collections.singletonList(param));

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(true));
		}

		@Test
		public void parametersNotMatchingSize() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			// specific
			String param = "param";
			when(assignment.getMethodName()).thenReturn("*");
			when(assignment.getParameters()).thenReturn(Collections.singletonList(param));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("name");
			when(methodType.getParameters()).thenReturn(Collections.<String> emptyList());

			boolean matches = filter.matches(assignment, methodType);
			assertThat(matches, is(false));

			when(methodType.getParameters()).thenReturn(Arrays.asList(new String[] { "param1", "param2" }));

			matches = filter.matches(assignment, methodType);
			assertThat(matches, is(false));

		}

		@Test
		public void parametersNotMatchingName() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			// specific
			String param = "param";
			when(assignment.getMethodName()).thenReturn("*");
			when(assignment.getParameters()).thenReturn(Collections.singletonList(param));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("name");
			when(methodType.getParameters()).thenReturn(Collections.singletonList("someOtherParam"));

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(false));
		}

		@Test
		public void parametersNotMatchingOrder() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			// specific
			when(assignment.getMethodName()).thenReturn("*");
			when(assignment.getParameters()).thenReturn(Arrays.asList(new String[] { "param2", "param1" }));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("name");
			when(methodType.getParameters()).thenReturn(Arrays.asList(new String[] { "param1", "param2" }));

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(false));
		}

		@Test
		public void parametersWildcard() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			// specific
			String param = "param*";
			when(assignment.getMethodName()).thenReturn("*");
			when(assignment.getParameters()).thenReturn(Collections.singletonList(param));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("name");
			when(methodType.getParameters()).thenReturn(Collections.singletonList("param1"));

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(true));
		}

		@Test
		public void parametersWildcardNotMatching() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			// specific
			String param = "param*";
			when(assignment.getMethodName()).thenReturn("*");
			when(assignment.getParameters()).thenReturn(Collections.singletonList(param));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			when(methodType.getName()).thenReturn("name");
			when(methodType.getParameters()).thenReturn(Collections.singletonList("someOtherParam"));

			boolean matches = filter.matches(assignment, methodType);

			assertThat(matches, is(false));
		}

		@Test
		public void annotation() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			// specific
			when(classType.isClass()).thenReturn(true);
			when(classType.castToClass()).thenReturn(classType);
			doReturn(classType).when(methodType).getImmutableClassOrInterfaceType();
			String name = "name";
			when(assignment.getAnnotation()).thenReturn(name);
			when(assignment.getMethodName()).thenReturn("*");
			ImmutableAnnotationType annotation = Mockito.mock(ImmutableAnnotationType.class);
			when(annotation.getFQN()).thenReturn(name);
			doReturn(Collections.singleton(annotation)).when(methodType).getImmutableAnnotations();

			boolean result = filter.matches(assignment, methodType);

			assertThat(result, is(true));
		}

		@Test
		public void annotationWildcard() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			// specific
			when(classType.isClass()).thenReturn(true);
			when(classType.castToClass()).thenReturn(classType);
			doReturn(classType).when(methodType).getImmutableClassOrInterfaceType();
			String name = "n*";
			when(assignment.getAnnotation()).thenReturn(name);
			when(assignment.getMethodName()).thenReturn("*");
			ImmutableAnnotationType annotation = Mockito.mock(ImmutableAnnotationType.class);
			when(annotation.getFQN()).thenReturn("name");
			doReturn(Collections.singleton(annotation)).when(methodType).getImmutableAnnotations();

			boolean result = filter.matches(assignment, methodType);

			assertThat(result, is(true));
		}

		@Test
		public void annotationInClassType() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			when(methodType.getMethodCharacter()).thenReturn(Character.METHOD);
			// specific
			when(classType.isClass()).thenReturn(true);
			when(classType.castToClass()).thenReturn(classType);
			doReturn(classType).when(methodType).getImmutableClassOrInterfaceType();
			String name = "n*";
			when(assignment.getAnnotation()).thenReturn(name);
			when(assignment.getMethodName()).thenReturn("*");
			ImmutableAnnotationType annotation = Mockito.mock(ImmutableAnnotationType.class);
			when(annotation.getFQN()).thenReturn("name");
			doReturn(Collections.singleton(annotation)).when(classType).getImmutableAnnotations();

			boolean result = filter.matches(assignment, methodType);

			assertThat(result, is(true));
		}

		@Test
		public void notMatchingStaticConstructor() {
			// default
			when(assignment.isPublicModifier()).thenReturn(true);
			when(methodType.getModifiers()).thenReturn(Modifiers.getModifiers(Modifier.PUBLIC));
			when(methodType.getMethodCharacter()).thenReturn(Character.STATIC_CONSTRUCTOR);

			boolean result = filter.matches(assignment, methodType);

			assertThat(result, is(false));
		}
	}
}
