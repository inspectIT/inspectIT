package info.novatec.inspectit.instrumentation.config.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.instrumentation.classcache.ImmutableAnnotationType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableClassType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableInterfaceType;
import info.novatec.inspectit.testbase.TestBase;

import java.util.Collections;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for the {@link ClassSensorAssignmentFilter}.
 *
 * @author Ivan Senic
 *
 */
public class ClassSensorAssignmentFilterTest extends TestBase {

	protected ClassSensorAssignmentFilter filter;

	@Mock
	protected AbstractClassSensorAssignment<?> assignment;

	@Mock
	protected ImmutableClassType classType;

	@BeforeMethod
	public void init() {
		filter = new ClassSensorAssignmentFilter();
	}

	public class Matches extends ClassSensorAssignmentFilterTest {

		@Test
		public void assignmentNull() {
			when(assignment.getClassName()).thenReturn(null);
			when(classType.getFQN()).thenReturn("name");

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(false));
		}

		@Test
		public void nameNull() {
			when(assignment.getClassName()).thenReturn("name");
			when(classType.getFQN()).thenReturn(null);

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(false));
		}

		@Test
		public void name() {
			String name = "name";
			when(assignment.getClassName()).thenReturn(name);
			when(classType.getFQN()).thenReturn(name);

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(true));

			when(classType.getFQN()).thenReturn("someOtherName");
			assertThat(filter.matches(assignment, classType), is(false));
		}

		@Test
		public void nameNotMatching() {
			String name = "name";
			when(assignment.getClassName()).thenReturn(name);
			when(classType.getFQN()).thenReturn("someOtherName");

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(false));
		}

		@Test
		public void nameWildCard() {
			String wildCard = "nam*";
			when(assignment.getClassName()).thenReturn(wildCard);
			when(classType.getFQN()).thenReturn("name");

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(true));
		}

		@Test
		public void nameWildCardNotMatching() {
			String wildCard = "nam*";
			when(assignment.getClassName()).thenReturn(wildCard);
			when(classType.getFQN()).thenReturn("someOtherName");

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(false));
		}

		@Test
		public void superClassName() {
			String name = "name";
			when(assignment.getClassName()).thenReturn(name);
			when(assignment.isSuperclass()).thenReturn(true);
			ImmutableClassType superClass = Mockito.mock(ImmutableClassType.class);
			when(superClass.getFQN()).thenReturn(name);
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(superClass)).when(classType).getImmutableSuperClasses();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(true));
		}

		@Test
		public void superClassNameNotMatching() {
			String name = "name";
			when(assignment.getClassName()).thenReturn(name);
			when(assignment.isSuperclass()).thenReturn(true);
			ImmutableClassType superClass = Mockito.mock(ImmutableClassType.class);
			when(superClass.getFQN()).thenReturn("someOtherName");
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(superClass)).when(classType).getImmutableSuperClasses();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(false));
		}

		@Test
		public void interfaceName() {
			String name = "name";
			when(assignment.getClassName()).thenReturn(name);
			when(assignment.isInterf()).thenReturn(true);
			ImmutableInterfaceType interf = Mockito.mock(ImmutableInterfaceType.class);
			when(interf.getFQN()).thenReturn(name);
			when(interf.isInterface()).thenReturn(true);
			when(interf.castToInterface()).thenReturn(interf);
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(interf)).when(classType).getImmutableRealizedInterfaces();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(true));
		}

		@Test
		public void interfaceNameNotMatches() {
			String name = "name";
			when(assignment.getClassName()).thenReturn(name);
			when(assignment.isInterf()).thenReturn(true);
			ImmutableInterfaceType interf = Mockito.mock(ImmutableInterfaceType.class);
			when(interf.getFQN()).thenReturn("someOtherName");
			when(interf.isInterface()).thenReturn(true);
			when(interf.castToInterface()).thenReturn(interf);
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(interf)).when(classType).getImmutableRealizedInterfaces();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(false));
		}

		@Test
		public void superSuperClassWildCard() {
			String name = "*name";
			when(assignment.getClassName()).thenReturn(name);
			when(assignment.isSuperclass()).thenReturn(true);
			ImmutableClassType superSuperClass = Mockito.mock(ImmutableClassType.class);
			when(superSuperClass.getFQN()).thenReturn("name");
			ImmutableClassType superClass = Mockito.mock(ImmutableClassType.class);
			when(superClass.getFQN()).thenReturn("someOtherSuperName");
			doReturn(Collections.singleton(superSuperClass)).when(superClass).getImmutableSuperClasses();
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(superClass)).when(classType).getImmutableSuperClasses();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(true));

			when(superSuperClass.getFQN()).thenReturn("someOtherName");
			assertThat(filter.matches(assignment, classType), is(false));
		}

		@Test
		public void superSuperClassWildCardNotMatching() {
			String name = "*name";
			when(assignment.getClassName()).thenReturn(name);
			when(assignment.isSuperclass()).thenReturn(true);
			ImmutableClassType superSuperClass = Mockito.mock(ImmutableClassType.class);
			when(superSuperClass.getFQN()).thenReturn("someOtherName");
			ImmutableClassType superClass = Mockito.mock(ImmutableClassType.class);
			when(superClass.getFQN()).thenReturn("someOtherSuperName");
			doReturn(Collections.singleton(superSuperClass)).when(superClass).getImmutableSuperClasses();
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(superClass)).when(classType).getImmutableSuperClasses();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(false));
		}

		@Test
		public void interfaceIndirectly() {
			String name = "name";
			when(assignment.getClassName()).thenReturn(name);
			when(assignment.isInterf()).thenReturn(true);
			ImmutableInterfaceType indirect = Mockito.mock(ImmutableInterfaceType.class);
			when(indirect.getFQN()).thenReturn(name);
			when(indirect.isInterface()).thenReturn(true);
			when(indirect.castToInterface()).thenReturn(indirect);
			ImmutableInterfaceType interf = Mockito.mock(ImmutableInterfaceType.class);
			when(interf.getFQN()).thenReturn("someOtherName");
			when(interf.isInterface()).thenReturn(true);
			when(interf.castToInterface()).thenReturn(interf);
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(interf)).when(classType).getImmutableRealizedInterfaces();
			doReturn(Collections.singleton(indirect)).when(interf).getImmutableSuperInterfaces();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(true));
		}

		@Test
		public void interfaceIndirectlyNotMatching() {
			String name = "name";
			when(assignment.getClassName()).thenReturn(name);
			when(assignment.isInterf()).thenReturn(true);
			ImmutableInterfaceType indirect = Mockito.mock(ImmutableInterfaceType.class);
			when(indirect.getFQN()).thenReturn("someOtherName");
			when(indirect.isInterface()).thenReturn(true);
			when(indirect.castToInterface()).thenReturn(indirect);
			ImmutableInterfaceType interf = Mockito.mock(ImmutableInterfaceType.class);
			when(interf.getFQN()).thenReturn("someOtherName");
			when(interf.isInterface()).thenReturn(true);
			when(interf.castToInterface()).thenReturn(interf);
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(interf)).when(classType).getImmutableRealizedInterfaces();
			doReturn(Collections.singleton(indirect)).when(interf).getImmutableSuperInterfaces();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(false));
		}

		@Test
		public void annotation() {
			String name = "name";
			when(assignment.getClassName()).thenReturn("*");
			when(assignment.getAnnotation()).thenReturn(name);
			ImmutableAnnotationType annotation = Mockito.mock(ImmutableAnnotationType.class);
			when(annotation.getFQN()).thenReturn(name);
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(annotation)).when(classType).getImmutableAnnotations();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(true));

			when(annotation.getFQN()).thenReturn("someOtherName");
			assertThat(filter.matches(assignment, classType), is(false));
		}

		@Test
		public void annotationNotMatching() {
			String name = "name";
			when(assignment.getClassName()).thenReturn("*");
			when(assignment.getAnnotation()).thenReturn(name);
			ImmutableAnnotationType annotation = Mockito.mock(ImmutableAnnotationType.class);
			when(annotation.getFQN()).thenReturn("someOtherName");
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(annotation)).when(classType).getImmutableAnnotations();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(false));
		}

		@Test
		public void annotationWildCard() {
			String name = "n*me";
			when(assignment.getClassName()).thenReturn("*");
			when(assignment.getAnnotation()).thenReturn(name);
			ImmutableAnnotationType annotation = Mockito.mock(ImmutableAnnotationType.class);
			when(annotation.getFQN()).thenReturn(name);
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(annotation)).when(classType).getImmutableAnnotations();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(true));
		}

		@Test
		public void annotationWildCardNotMatching() {
			String name = "n*me";
			when(assignment.getClassName()).thenReturn("*");
			when(assignment.getAnnotation()).thenReturn(name);
			ImmutableAnnotationType annotation = Mockito.mock(ImmutableAnnotationType.class);
			when(annotation.getFQN()).thenReturn("someOtherName");
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(annotation)).when(classType).getImmutableAnnotations();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(false));
		}

		@Test
		public void superClassAnnotation() {
			String name = "name";
			when(assignment.getClassName()).thenReturn("*");
			when(assignment.getAnnotation()).thenReturn(name);
			ImmutableAnnotationType annotation = Mockito.mock(ImmutableAnnotationType.class);
			when(annotation.getFQN()).thenReturn(name);
			ImmutableClassType superClass = Mockito.mock(ImmutableClassType.class);
			when(superClass.getFQN()).thenReturn("someOtherName");
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(superClass)).when(classType).getImmutableSuperClasses();
			doReturn(Collections.singleton(annotation)).when(superClass).getImmutableAnnotations();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(true));

			when(annotation.getFQN()).thenReturn("someOtherName");
			assertThat(filter.matches(assignment, classType), is(false));
		}

		@Test
		public void superClassAnnotationNotMatching() {
			String name = "name";
			when(assignment.getClassName()).thenReturn("*");
			when(assignment.getAnnotation()).thenReturn(name);
			ImmutableAnnotationType annotation = Mockito.mock(ImmutableAnnotationType.class);
			when(annotation.getFQN()).thenReturn("someOtherName");
			ImmutableClassType superClass = Mockito.mock(ImmutableClassType.class);
			when(superClass.getFQN()).thenReturn("someOtherName");
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(superClass)).when(classType).getImmutableSuperClasses();
			doReturn(Collections.singleton(annotation)).when(superClass).getImmutableAnnotations();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(false));
		}

		@Test
		public void interfaceAnnotation() {
			String name = "name";
			when(assignment.getClassName()).thenReturn("*");
			when(assignment.getAnnotation()).thenReturn(name);
			ImmutableAnnotationType annotation = Mockito.mock(ImmutableAnnotationType.class);
			when(annotation.getFQN()).thenReturn(name);
			ImmutableInterfaceType interf = Mockito.mock(ImmutableInterfaceType.class);
			when(interf.isInterface()).thenReturn(true);
			when(interf.castToInterface()).thenReturn(interf);
			when(interf.getFQN()).thenReturn("someOtherName");
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(interf)).when(classType).getImmutableRealizedInterfaces();
			doReturn(Collections.singleton(annotation)).when(interf).getImmutableAnnotations();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(true));
		}

		@Test
		public void interfaceAnnotationNotMatching() {
			String name = "name";
			when(assignment.getClassName()).thenReturn("*");
			when(assignment.getAnnotation()).thenReturn(name);
			ImmutableAnnotationType annotation = Mockito.mock(ImmutableAnnotationType.class);
			when(annotation.getFQN()).thenReturn("someOtherName");
			ImmutableInterfaceType interf = Mockito.mock(ImmutableInterfaceType.class);
			when(interf.isInterface()).thenReturn(true);
			when(interf.castToInterface()).thenReturn(interf);
			when(interf.getFQN()).thenReturn("someOtherName");
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(interf)).when(classType).getImmutableRealizedInterfaces();
			doReturn(Collections.singleton(annotation)).when(interf).getImmutableAnnotations();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(false));
		}

		@Test
		public void interfaceAnnotationIndirectly() {
			String name = "name";
			when(assignment.getClassName()).thenReturn("*");
			when(assignment.getAnnotation()).thenReturn(name);
			ImmutableAnnotationType annotation = Mockito.mock(ImmutableAnnotationType.class);
			when(annotation.getFQN()).thenReturn(name);
			ImmutableInterfaceType indirect = Mockito.mock(ImmutableInterfaceType.class);
			when(indirect.getFQN()).thenReturn("someOtherName");
			when(indirect.isInterface()).thenReturn(true);
			when(indirect.castToInterface()).thenReturn(indirect);
			ImmutableInterfaceType interf = Mockito.mock(ImmutableInterfaceType.class);
			when(interf.getFQN()).thenReturn("someOtherName");
			when(interf.isInterface()).thenReturn(true);
			when(interf.castToInterface()).thenReturn(interf);
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(interf)).when(classType).getImmutableRealizedInterfaces();
			doReturn(Collections.singleton(indirect)).when(interf).getImmutableSuperInterfaces();
			doReturn(Collections.singleton(annotation)).when(indirect).getImmutableAnnotations();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(true));
		}

		@Test
		public void interfaceAnnotationIndirectlyNotMatching() {
			String name = "name";
			when(assignment.getClassName()).thenReturn("*");
			when(assignment.getAnnotation()).thenReturn(name);
			ImmutableAnnotationType annotation = Mockito.mock(ImmutableAnnotationType.class);
			when(annotation.getFQN()).thenReturn("someOtherName");
			ImmutableInterfaceType indirect = Mockito.mock(ImmutableInterfaceType.class);
			when(indirect.getFQN()).thenReturn("someOtherName");
			when(indirect.isInterface()).thenReturn(true);
			when(indirect.castToInterface()).thenReturn(indirect);
			ImmutableInterfaceType interf = Mockito.mock(ImmutableInterfaceType.class);
			when(interf.getFQN()).thenReturn("someOtherName");
			when(interf.isInterface()).thenReturn(true);
			when(interf.castToInterface()).thenReturn(interf);
			when(classType.getFQN()).thenReturn("someOtherName");
			doReturn(Collections.singleton(interf)).when(classType).getImmutableRealizedInterfaces();
			doReturn(Collections.singleton(indirect)).when(interf).getImmutableSuperInterfaces();
			doReturn(Collections.singleton(annotation)).when(indirect).getImmutableAnnotations();

			boolean matches = filter.matches(assignment, classType);

			assertThat(matches, is(false));
		}
	}
}
