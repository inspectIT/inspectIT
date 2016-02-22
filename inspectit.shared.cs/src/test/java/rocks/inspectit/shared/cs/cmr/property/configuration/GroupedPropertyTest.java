package rocks.inspectit.shared.cs.cmr.property.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.cs.cmr.property.configuration.GroupedProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;
import rocks.inspectit.shared.cs.cmr.property.configuration.validation.PropertyValidation;
import rocks.inspectit.shared.cs.cmr.property.configuration.validation.ValidationError;
import rocks.inspectit.shared.cs.cmr.property.configuration.validator.IGroupedProperyValidator;

@SuppressWarnings("PMD")
public class GroupedPropertyTest {

	@Mock
	private SingleProperty<?> singleProperty1;

	@Mock
	private SingleProperty<?> singleProperty2;

	@Mock
	private IGroupedProperyValidator validator1;

	@Mock
	private IGroupedProperyValidator validator2;

	@BeforeMethod
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void groupValidation() {
		GroupedProperty groupedProperty = new GroupedProperty();
		groupedProperty.addSingleProperty(singleProperty1);
		groupedProperty.addSingleProperty(singleProperty2);
		groupedProperty.addValidator(validator1);
		groupedProperty.addValidator(validator2);

		PropertyValidation propertyValidation = groupedProperty.validate();
		verify(singleProperty1, times(1)).validate(propertyValidation);
		verify(singleProperty2, times(1)).validate(propertyValidation);
		verify(validator1, times(1)).validate(groupedProperty, propertyValidation);
		verify(validator2, times(1)).validate(groupedProperty, propertyValidation);
		verifyNoMoreInteractions(singleProperty1, singleProperty2, validator1, validator2);
	}

	@Test
	public void validationFailsOnSingleProperty() {
		GroupedProperty groupedProperty = new GroupedProperty();
		groupedProperty.addSingleProperty(singleProperty1);
		groupedProperty.addSingleProperty(singleProperty2);
		groupedProperty.addValidator(validator1);
		groupedProperty.addValidator(validator2);

		doAnswer(new Answer<Object>() {

			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				PropertyValidation propertyValidation = (PropertyValidation) args[0];
				propertyValidation.addValidationError(new ValidationError());
				return null;
			}
		}).when(singleProperty1).validate(Mockito.<PropertyValidation> anyObject());

		PropertyValidation propertyValidation = groupedProperty.validate();
		assertThat(propertyValidation.hasErrors(), is(true));
		assertThat(propertyValidation.getErrorCount(), is(1));

		verify(singleProperty1, times(1)).validate(propertyValidation);
		verify(singleProperty2, times(1)).validate(propertyValidation);
		verify(validator1, times(1)).validate(groupedProperty, propertyValidation);
		verify(validator2, times(1)).validate(groupedProperty, propertyValidation);
		verifyNoMoreInteractions(singleProperty1, singleProperty2, validator1, validator2);
	}

	@Test
	public void validationFailsOnSinglePropertyAndGroupValidation() {
		GroupedProperty groupedProperty = new GroupedProperty();
		groupedProperty.addSingleProperty(singleProperty1);
		groupedProperty.addSingleProperty(singleProperty2);
		groupedProperty.addValidator(validator1);
		groupedProperty.addValidator(validator2);

		doAnswer(new Answer<Object>() {

			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				PropertyValidation propertyValidation = (PropertyValidation) args[0];
				propertyValidation.addValidationError(new ValidationError());
				return null;
			}
		}).when(singleProperty1).validate(Mockito.<PropertyValidation> anyObject());

		doAnswer(new Answer<Object>() {

			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				PropertyValidation propertyValidation = (PropertyValidation) args[1];
				propertyValidation.addValidationError(new ValidationError());
				return null;
			}
		}).when(validator1).validate(eq(groupedProperty), Mockito.<PropertyValidation> anyObject());

		PropertyValidation propertyValidation = groupedProperty.validate();
		assertThat(propertyValidation.hasErrors(), is(true));
		assertThat(propertyValidation.getErrorCount(), is(2));

		verify(singleProperty1, times(1)).validate(propertyValidation);
		verify(singleProperty2, times(1)).validate(propertyValidation);
		verify(validator1, times(1)).validate(groupedProperty, propertyValidation);
		verify(validator2, times(1)).validate(groupedProperty, propertyValidation);
		verifyNoMoreInteractions(singleProperty1, singleProperty2, validator1, validator2);
	}

	@Test
	public void advanced() {
		GroupedProperty groupedProperty = new GroupedProperty();
		groupedProperty.addSingleProperty(singleProperty1);
		groupedProperty.addSingleProperty(singleProperty2);

		when(singleProperty1.isAdvanced()).thenReturn(true);
		when(singleProperty2.isAdvanced()).thenReturn(false);

		assertThat(groupedProperty.isAdvanced(), is(true));

		when(singleProperty1.isAdvanced()).thenReturn(false);
		when(singleProperty2.isAdvanced()).thenReturn(false);

		assertThat(groupedProperty.isAdvanced(), is(false));
	}

	@Test
	public void serverRestartRequired() {
		GroupedProperty groupedProperty = new GroupedProperty();
		groupedProperty.addSingleProperty(singleProperty1);
		groupedProperty.addSingleProperty(singleProperty2);

		when(singleProperty1.isServerRestartRequired()).thenReturn(true);
		when(singleProperty2.isServerRestartRequired()).thenReturn(false);

		assertThat(groupedProperty.isServerRestartRequired(), is(true));

		when(singleProperty1.isServerRestartRequired()).thenReturn(false);
		when(singleProperty2.isServerRestartRequired()).thenReturn(false);

		assertThat(groupedProperty.isServerRestartRequired(), is(false));
	}

	@Test
	public void register() {
		GroupedProperty groupedProperty = new GroupedProperty();
		groupedProperty.addSingleProperty(singleProperty1);
		groupedProperty.addSingleProperty(singleProperty2);

		Properties properties = new Properties();
		groupedProperty.register(properties);

		verify(singleProperty1, times(1)).register(properties);
		verify(singleProperty2, times(1)).register(properties);
		verifyNoMoreInteractions(singleProperty1, singleProperty2);
	}
}
