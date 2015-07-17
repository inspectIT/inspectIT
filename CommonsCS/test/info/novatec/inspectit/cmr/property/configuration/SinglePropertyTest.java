package info.novatec.inspectit.cmr.property.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidation;
import info.novatec.inspectit.cmr.property.configuration.validation.ValidationError;
import info.novatec.inspectit.cmr.property.configuration.validator.ISinglePropertyValidator;
import info.novatec.inspectit.cmr.property.update.AbstractPropertyUpdate;

import java.util.Collections;
import java.util.Properties;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class SinglePropertyTest {

	@Mock
	ISinglePropertyValidator<Object> validator1;

	@Mock
	ISinglePropertyValidator<Object> validator2;

	@BeforeMethod
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void singlePropertyValidation() {
		final SingleProperty<Object> singleProperty = new SingleProperty<Object>() {
			@Override
			public Object getDefaultValue() {
				return null;
			}

			@Override
			protected void setDefaultValue(Object defaultValue) {
			}

			@Override
			protected Object getUsedValue() {
				return null;
			}

			@Override
			protected void setUsedValue(Object usedValue) {
			}

			@Override
			public Object parseLiteral(String literal) {
				return null;
			}

			@Override
			public AbstractPropertyUpdate<Object> createPropertyUpdate(Object updateValue) {
				return null;
			}
		};

		final String validationMsg = "My validation error message";

		doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				PropertyValidation propertyValidation = (PropertyValidation) args[1];
				propertyValidation.addValidationError(new ValidationError(Collections.<SingleProperty<?>> singletonList(singleProperty), validationMsg));
				return null;
			}
		}).when(validator1).validate(eq(singleProperty), Mockito.<PropertyValidation> anyObject());

		singleProperty.addValidator(validator1);
		singleProperty.addValidator(validator2);
		PropertyValidation propertyValidation = singleProperty.validate();

		ArgumentCaptor<PropertyValidation> captor1 = ArgumentCaptor.forClass(PropertyValidation.class);
		verify(validator1, times(1)).validate(eq(singleProperty), captor1.capture());
		assertThat(captor1.getValue().getProperty(), is(equalTo((AbstractProperty) singleProperty)));

		ArgumentCaptor<PropertyValidation> captor2 = ArgumentCaptor.forClass(PropertyValidation.class);
		verify(validator2, times(1)).validate(eq(singleProperty), captor2.capture());
		assertThat(captor2.getValue().getProperty(), is(equalTo((AbstractProperty) singleProperty)));

		assertThat(propertyValidation.hasErrors(), is(true));
		assertThat(propertyValidation.getErrorCount(), is(1));
		assertThat(propertyValidation.getErrors(), hasSize(1));
		assertThat(propertyValidation.getErrors().iterator().next().getMessage(), is(equalTo(validationMsg)));
	}

	@Test
	public void register() {
		SingleProperty<Object> singleProperty = new SingleProperty<Object>("", "", "property1", "defaultValue", false, false) {
			@Override
			public Object getDefaultValue() {
				return null;
			}

			@Override
			protected void setDefaultValue(Object defaultValue) {
			}

			@Override
			protected Object getUsedValue() {
				return "myValue";
			}

			@Override
			protected void setUsedValue(Object usedValue) {
			}

			@Override
			public AbstractPropertyUpdate<Object> createPropertyUpdate(Object updateValue) {
				return null;
			}

			@Override
			public Object parseLiteral(String literal) {
				return null;
			}
		};

		Properties properties = new Properties();
		singleProperty.register(properties);
		assertThat(properties.getProperty("property1"), is(equalTo("myValue")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void canUpdate() {
		SingleProperty<Object> singleProperty = new SingleProperty<Object>("", "", "property1", "defaultValue", false, false) {
			@Override
			public Object getDefaultValue() {
				return null;
			}

			@Override
			protected void setDefaultValue(Object defaultValue) {
			}

			@Override
			protected Object getUsedValue() {
				return "myValue";
			}

			@Override
			protected void setUsedValue(Object usedValue) {
			}

			@Override
			public AbstractPropertyUpdate<Object> createPropertyUpdate(Object updateValue) {
				return null;
			}

			@Override
			public Object parseLiteral(String literal) {
				return null;
			}
		};

		AbstractPropertyUpdate<Object> propertyUpdate = Mockito.mock(AbstractPropertyUpdate.class);

		// can not update if different logical name
		when(propertyUpdate.getPropertyLogicalName()).thenReturn("someOtherName");
		assertThat(singleProperty.canUpdate(propertyUpdate), is(false));
		when(propertyUpdate.getPropertyLogicalName()).thenReturn("property1");

		// can not update if value class is not the same
		when(propertyUpdate.getUpdateValue()).thenReturn(new Object());
		assertThat(singleProperty.canUpdate(propertyUpdate), is(false));

		// can not update if value is same
		when(propertyUpdate.getUpdateValue()).thenReturn("myValue");
		assertThat(singleProperty.canUpdate(propertyUpdate), is(false));

		// can update
		when(propertyUpdate.getUpdateValue()).thenReturn("updateValue");
		assertThat(singleProperty.canUpdate(propertyUpdate), is(true));
	}
}
