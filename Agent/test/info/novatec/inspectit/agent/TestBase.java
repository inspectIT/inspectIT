package info.novatec.inspectit.agent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * Base class for all testing purposes in inspectIT.
 * 
 * Automatically ensures that each testee (annotated with @InjectMock) is reset between each test
 * method invocation.
 * 
 * @author Stefan Siegl
 */
public abstract class TestBase {

	/** The fields annotated @InjectMocks that should be reset after each method invocation. */
	List<String> fieldsToReset = new ArrayList<String>();

	/**
	 * The method will be run before the first test method in the current class is invoked.
	 * <ul>
	 * <li>Scans the Unit Test class for @InjectMock annotations to reset them after each method
	 * invocation.</li>
	 * </ul>
	 */
	@BeforeClass
	public void baseBeforeClass() {
		scanForAnnotations();
	}

	/**
	 * The method will be run before each test method.
	 * <ul>
	 * <li>Initializes any {@link Mock} that is present in the Unit Test.</li>
	 * <li>This also injects the mocks into the testee class if it is annotated using the
	 * <code>@InjectMocks</code> annotation.</li>
	 * </ul>
	 */
	@BeforeMethod
	public void baseBeforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * The method will be run after each test method.
	 * <ul>
	 * <li>It checks for correct mockito usage in each test run.</li>
	 * <li>It resets the field annotated with @InjectMocks.</li>
	 * </ul>
	 */
	@AfterMethod
	public void baseAfterMethod() {
		Mockito.validateMockitoUsage();
		for (String field : this.fieldsToReset) {
			setInstanceValue(this, field, null);
		}
	}

	/**
	 * This method is used to scan for annotations of type {@link InjectMocks} and adding it to the
	 * list of {@link #fieldsToReset}.
	 */
	private void scanForAnnotations() {
		Field[] allFields = getAllFieldsForClass(this.getClass());
		for (Field field : allFields) {
			Annotation[] annotations = field.getAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation.annotationType().equals(InjectMocks.class)) {
					this.fieldsToReset.add(field.getName());
				}
			}
		}
	}

	/** get the list of all fields of class and all super-classes of them */
	private Field[] getAllFieldsForClass(Class<?> clazz) {
		List<Field> fields = new ArrayList<Field>();
		do {
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
		} while (clazz != Object.class);

		return fields.toArray(new Field[fields.size()]);
	}

	/**
	 * Use reflection to change value of any instance field.
	 * 
	 * @param objectToChange
	 *            An Object instance.
	 * @param fieldName
	 *            The name of a field in the class instantiated by classInstancee
	 * @param newValue
	 *            The value you want the field to be set to.
	 * @throws RuntimeException
	 *             when setting the value for the field failed.
	 */
	public void setInstanceValue(Object objectToChange, String fieldName, Object newValue) {
		String targetClass = null;
		try {
			// Get the private field
			Field field;
			if (objectToChange instanceof Class<?>) {
				targetClass = ((Class<?>) objectToChange).getCanonicalName();
				field = findField((Class<?>) objectToChange, fieldName);
			} else {
				targetClass = objectToChange.getClass().getCanonicalName();
				field = findField(objectToChange.getClass(), fieldName);
			}
			// Allow modification on the field
			field.setAccessible(true);
			// Sets the field to the new value for this instance
			field.set(objectToChange, newValue);
		} catch (Exception e) {
			throw new RuntimeException("Error setting field [" + fieldName + "] in class [" + targetClass + "]", e); // NOPMD
		}
	}

	private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		if (clazz == null) {
			throw new NoSuchFieldException("Field could not be found in the class (and superclasses).");
		}

		Field field = null;
		try {
			field = clazz.getDeclaredField(fieldName);
		} catch (Exception e) { // NOPMD intended, as we go up the superclasses
			// NOCHECKSTYLE
		}
		if (field != null) {
			return field;
		} else {
			return findField(clazz.getSuperclass(), fieldName);
		}
	}
}
