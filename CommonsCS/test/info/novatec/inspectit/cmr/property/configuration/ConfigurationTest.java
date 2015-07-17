package info.novatec.inspectit.cmr.property.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidation;

import java.util.Map;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class ConfigurationTest {

	private Configuration configuration;

	@Mock
	private AbstractProperty property1;

	@Mock
	private AbstractProperty property2;

	@Mock
	private PropertyValidation propertyValidation1;

	@Mock
	private PropertyValidation propertyValidation2;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		configuration = new Configuration();
		PropertySection section = new PropertySection();
		section.addProperty(property1);
		section.addProperty(property2);
		configuration.addSection(section);
	}

	@Test
	public void validate() {
		when(propertyValidation1.hasErrors()).thenReturn(true);
		when(property1.validate()).thenReturn(propertyValidation1);
		when(propertyValidation2.hasErrors()).thenReturn(false);
		when(property2.validate()).thenReturn(propertyValidation2);

		Map<AbstractProperty, PropertyValidation> validateMap = configuration.validate();

		assertThat(validateMap, hasEntry(property1, propertyValidation1));
		assertThat(validateMap, not(hasKey(property2)));
	}
}
