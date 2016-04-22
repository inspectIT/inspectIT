package rocks.inspectit.shared.cs.cmr.property.configuration.validator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.cs.cmr.property.configuration.GroupedProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.validation.PropertyValidation;
import rocks.inspectit.shared.cs.cmr.property.configuration.validation.ValidationError;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.FullyQualifiedClassNameValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.GreaterOrEqualValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.GreaterValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.LessOrEqualValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.LessValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.NegativeValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.NotEmptyValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.PercentageValidator;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.impl.PositiveValidator;

@SuppressWarnings({ "PMD", "unchecked", "rawtypes" })
public class ValidatorsTest {

	@Mock
	private PropertyValidation propertyValidation;

	@Mock
	private SingleProperty singleProperty;

	@Mock
	private SingleProperty singleProperty2;

	@Mock
	private GroupedProperty groupedProperty;

	@BeforeMethod
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void validatorGroupValidation() {
		when(singleProperty.getValue()).thenReturn(Integer.valueOf(10));
		when(singleProperty.getLogicalName()).thenReturn("my.property1");

		when(singleProperty2.getValue()).thenReturn(Integer.valueOf(20));
		when(singleProperty2.getLogicalName()).thenReturn("my.property2");

		Set<SingleProperty<?>> properties = new HashSet<>();
		properties.add(singleProperty);
		properties.add(singleProperty2);
		when(groupedProperty.forLogicalname("my.property1")).thenReturn(singleProperty);
		when(groupedProperty.forLogicalname("my.property2")).thenReturn(singleProperty2);

		LessValidator<Number> validator = new LessValidator<>();
		validator.setProperty("my.property1");
		validator.setThan("my.property2");

		validator.validate(groupedProperty, propertyValidation);
		verifyZeroInteractions(propertyValidation);

		// make it fail
		when(singleProperty.getValue()).thenReturn(Integer.valueOf(30));
		validator.validate(groupedProperty, propertyValidation);

		verify(propertyValidation, times(1)).addValidationError(Matchers.<ValidationError> anyObject());
	}

	@Test
	public void validatorIsLessThan() {
		when(singleProperty.getValue()).thenReturn(Integer.valueOf(10));
		when(singleProperty.getLogicalName()).thenReturn("my.property");

		LessValidator<Number> validator = new LessValidator<>();
		when(singleProperty.parseLiteral(null)).thenReturn(11, 10, 9);

		validator.validate(singleProperty, propertyValidation);

		verifyZeroInteractions(propertyValidation);

		validator.validate(singleProperty, propertyValidation);
		validator.validate(singleProperty, propertyValidation);

		verify(propertyValidation, times(2)).addValidationError(Matchers.<ValidationError> anyObject());
	}

	@Test
	public void validatorIsLessThanOrEqual() {
		when(singleProperty.getValue()).thenReturn(Integer.valueOf(10));
		when(singleProperty.getLogicalName()).thenReturn("my.property");

		LessOrEqualValidator<Number> validator = new LessOrEqualValidator<>();
		when(singleProperty.parseLiteral(null)).thenReturn(11, 10, 9);

		validator.validate(singleProperty, propertyValidation);
		validator.validate(singleProperty, propertyValidation);

		verifyZeroInteractions(propertyValidation);

		validator.validate(singleProperty, propertyValidation);

		verify(propertyValidation, times(1)).addValidationError(Matchers.<ValidationError> anyObject());
	}

	@Test
	public void validatorIsGreaterThan() {
		when(singleProperty.getValue()).thenReturn(Integer.valueOf(10));
		when(singleProperty.getLogicalName()).thenReturn("my.property");

		GreaterValidator<Number> validator = new GreaterValidator<>();
		when(singleProperty.parseLiteral(null)).thenReturn(9, 10, 11);

		validator.validate(singleProperty, propertyValidation);

		verifyZeroInteractions(propertyValidation);

		validator.validate(singleProperty, propertyValidation);
		validator.validate(singleProperty, propertyValidation);

		verify(propertyValidation, times(2)).addValidationError(Matchers.<ValidationError> anyObject());
	}

	@Test
	public void validatorIsGreaterThanOrEqual() {
		when(singleProperty.getValue()).thenReturn(Integer.valueOf(10));
		when(singleProperty.getLogicalName()).thenReturn("my.property");

		GreaterOrEqualValidator<Number> validator = new GreaterOrEqualValidator<>();
		when(singleProperty.parseLiteral(null)).thenReturn(9, 10, 11);

		validator.validate(singleProperty, propertyValidation);

		verifyZeroInteractions(propertyValidation);

		validator.validate(singleProperty, propertyValidation);
		validator.validate(singleProperty, propertyValidation);

		verify(propertyValidation, times(1)).addValidationError(Matchers.<ValidationError> anyObject());
	}

	@Test
	public void validatorIsPositive() {
		when(singleProperty.getLogicalName()).thenReturn("my.property");

		PositiveValidator<Number> validator = new PositiveValidator<>();

		when(singleProperty.getValue()).thenReturn(Integer.valueOf(10));
		validator.validate(singleProperty, propertyValidation);

		verifyZeroInteractions(propertyValidation);

		when(singleProperty.getValue()).thenReturn(Integer.valueOf(-10));
		validator.validate(singleProperty, propertyValidation);

		verify(propertyValidation, times(1)).addValidationError(Matchers.<ValidationError> anyObject());
	}

	@Test
	public void validatorIsNegative() {
		when(singleProperty.getLogicalName()).thenReturn("my.property");

		NegativeValidator<Number> validator = new NegativeValidator<>();

		when(singleProperty.getValue()).thenReturn(Integer.valueOf(-10));
		validator.validate(singleProperty, propertyValidation);

		verifyZeroInteractions(propertyValidation);

		when(singleProperty.getValue()).thenReturn(Integer.valueOf(10));
		validator.validate(singleProperty, propertyValidation);

		verify(propertyValidation, times(1)).addValidationError(Matchers.<ValidationError> anyObject());
	}

	@Test
	public void validatorIsPercentage() {
		when(singleProperty.getValue()).thenReturn(0.75f);
		when(singleProperty.getLogicalName()).thenReturn("my.property");

		PercentageValidator<Number> validator = new PercentageValidator<>();
		validator.validate(singleProperty, propertyValidation);

		verifyZeroInteractions(propertyValidation);
	}

	@Test
	public void validatorIsNotEmpty() {
		when(singleProperty.getLogicalName()).thenReturn("my.property");
		NotEmptyValidator<Object> validator = new NotEmptyValidator<>();

		when(singleProperty.getValue()).thenReturn("Some string");
		verifyZeroInteractions(propertyValidation);

		when(singleProperty.getValue()).thenReturn("");
		validator.validate(singleProperty, propertyValidation);

		when(singleProperty.getValue()).thenReturn(Collections.emptyList());
		validator.validate(singleProperty, propertyValidation);

		when(singleProperty.getValue()).thenReturn(Collections.emptyMap());
		validator.validate(singleProperty, propertyValidation);

		verify(propertyValidation, times(3)).addValidationError(Matchers.<ValidationError> anyObject());
	}

	@Test
	public void validatorIsFullyQualifiedClassName() {
		SingleProperty<String> singleProperty = mock(SingleProperty.class);
		when(singleProperty.getLogicalName()).thenReturn("my.property");

		FullyQualifiedClassNameValidator validator = new FullyQualifiedClassNameValidator();

		when(singleProperty.getValue()).thenReturn(getClass().getName());
		validator.validate(singleProperty, propertyValidation);

		verifyZeroInteractions(propertyValidation);

		// test not FQN
		when(singleProperty.getValue()).thenReturn("package,Class");
		validator.validate(singleProperty, propertyValidation);

		when(singleProperty.getValue()).thenReturn("1canot.start.with.Number");
		validator.validate(singleProperty, propertyValidation);

		verify(propertyValidation, times(2)).addValidationError(Matchers.<ValidationError> anyObject());
	}

}
