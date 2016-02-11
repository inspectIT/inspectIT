package info.novatec.inspectit.cmr.property;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.jaxb.JAXBTransformator;
import info.novatec.inspectit.cmr.property.configuration.AbstractProperty;
import info.novatec.inspectit.cmr.property.configuration.Configuration;
import info.novatec.inspectit.cmr.property.configuration.PropertySection;
import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.configuration.impl.LongProperty;
import info.novatec.inspectit.cmr.property.configuration.impl.StringProperty;
import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidation;
import info.novatec.inspectit.cmr.property.update.AbstractPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.IPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.configuration.ConfigurationUpdate;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.collections.MapUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

@SuppressWarnings("PMD")
public class PropertyManagerTest {

	@InjectMocks
	private PropertyManager propertyManager;

	@Mock
	private ConfigurationUpdate configurationUpdate;

	@Mock
	private Configuration configuration;

	@Mock
	private PropertyUpdateExecutor propertyUpdateExecutor;

	@Mock
	private JAXBTransformator transformator;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests that the loading of default configuration and updates can be executed with no
	 * exceptions.
	 */
	@Test
	public void loadDefaultConfiguration() throws JAXBException, IOException, SAXException {
		propertyManager.loadConfigurationAndUpdates();
	}

	/**
	 * Test that the {@link Properties} returned by the {@link PropertyManager} are correctly take
	 * from {@link Configuration}.
	 */
	@Test
	public void propertyInDefaultConfiguration() throws JAXBException, IOException, SAXException {
		doReturn(configuration).when(transformator).unmarshall(Mockito.<Path> anyObject(), Mockito.<Path> anyObject(), eq(Configuration.class));
		doReturn(null).when(transformator).unmarshall(Mockito.<Path> anyObject(), Mockito.<Path> anyObject(), eq(ConfigurationUpdate.class));
		when(configuration.validate()).thenReturn(Collections.<AbstractProperty, PropertyValidation> emptyMap());

		SingleProperty<?> property = mock(SingleProperty.class);

		Answer<Object> answer = new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Properties properties = (Properties) invocation.getArguments()[0];
				properties.put("property1", "value1");
				return null;
			}
		};
		doAnswer(answer).when(property).register(Mockito.<Properties> anyObject());
		when(configuration.getAllProperties()).thenReturn(Collections.<AbstractProperty> singleton(property));

		Properties properties = propertyManager.getProperties();
		assertThat(properties.getProperty("property1"), is(equalTo("value1")));
		assertThat(properties.size(), is(1));
	}

	/**
	 * Test that if validation fails for the property it won't be included in the {@link Properties}
	 * returned by {@link PropertyManager}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void propertyNotValidInDefaultConfiguration() throws JAXBException, IOException, SAXException {
		doReturn(configuration).when(transformator).unmarshall(Mockito.<Path> anyObject(), Mockito.<Path> anyObject(), eq(Configuration.class));
		doReturn(null).when(transformator).unmarshall(Mockito.<Path> anyObject(), Mockito.<Path> anyObject(), eq(ConfigurationUpdate.class));

		SingleProperty<?> property = mock(SingleProperty.class);
		PropertyValidation propertyValidation = mock(PropertyValidation.class, Mockito.RETURNS_SMART_NULLS);
		when(configuration.getAllProperties()).thenReturn(Collections.<AbstractProperty> singleton(property));
		when(configuration.validate()).thenReturn(MapUtils.putAll(new HashMap<AbstractProperty, PropertyValidation>(), new Object[][] { { property, propertyValidation } }));

		Properties properties = propertyManager.getProperties();
		verify(property, times(0)).register(Mockito.<Properties> anyObject());
		assertThat(properties.size(), is(0));
	}

	/**
	 * Test that {@link Properties} will hold an updated value of the property if it's included in
	 * the {@link ConfigurationUpdate}.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void savedPropertyUpdateFromTheDefaultConfiguration() throws JAXBException, IOException, SAXException {
		Configuration configuration = new Configuration();
		PropertySection section = new PropertySection();
		SingleProperty<String> property = new StringProperty("", "", "property1", "value1", false, false);
		section.addProperty(property);
		configuration.addSection(section);

		doReturn(configuration).when(transformator).unmarshall(Mockito.<Path> anyObject(), Mockito.<Path> anyObject(), eq(Configuration.class));
		doReturn(configurationUpdate).when(transformator).unmarshall(Mockito.<Path> anyObject(), Mockito.<Path> anyObject(), eq(ConfigurationUpdate.class));

		AbstractPropertyUpdate<String> propertyUpdate = mock(AbstractPropertyUpdate.class);
		when(propertyUpdate.getPropertyLogicalName()).thenReturn("property1");
		when(propertyUpdate.getUpdateValue()).thenReturn("updatedValue");
		when(configurationUpdate.getPropertyUpdates()).thenReturn(Collections.<IPropertyUpdate<?>> singleton(propertyUpdate));

		Properties properties = propertyManager.getProperties();
		assertThat(properties.getProperty("property1"), is(equalTo("updatedValue")));
		assertThat(properties.size(), is(1));
	}

	/**
	 * Test that not matching property update will not be taken into account.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void savedPropertyUpdateNotValid() throws JAXBException, IOException, SAXException {
		Configuration configuration = new Configuration();
		PropertySection section = new PropertySection();
		SingleProperty<Long> property = new LongProperty("", "", "property1", 10L, false, false);
		section.addProperty(property);
		configuration.addSection(section);

		doReturn(configuration).when(transformator).unmarshall(Mockito.<Path> anyObject(), Mockito.<Path> anyObject(), eq(Configuration.class));
		doReturn(configurationUpdate).when(transformator).unmarshall(Mockito.<Path> anyObject(), Mockito.<Path> anyObject(), eq(ConfigurationUpdate.class));

		AbstractPropertyUpdate<String> propertyUpdate = mock(AbstractPropertyUpdate.class);
		when(propertyUpdate.getPropertyLogicalName()).thenReturn("property1");
		when(propertyUpdate.getUpdateValue()).thenReturn("updatedValue");
		when(configurationUpdate.getPropertyUpdates()).thenReturn(Collections.<IPropertyUpdate<?>> singleton(propertyUpdate));

		Properties properties = propertyManager.getProperties();
		assertThat(Long.valueOf(properties.getProperty("property1")), is(10L));
		assertThat(properties.size(), is(1));
	}

	/**
	 * Check that Exception will be thrown if the {@link ConfigurationUpdate} has update that can
	 * not be applied.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expectedExceptions = { Exception.class })
	public void noUpdateIfCanNotUpdateProperty() throws Exception {
		doReturn(configuration).when(transformator).unmarshall(Mockito.<Path> anyObject(), Mockito.<Path> anyObject(), eq(Configuration.class));
		doReturn(null).when(transformator).unmarshall(Mockito.<Path> anyObject(), Mockito.<Path> anyObject(), eq(ConfigurationUpdate.class));

		ConfigurationUpdate configurationUpdate = mock(ConfigurationUpdate.class);
		AbstractPropertyUpdate<Long> propertyUpdate = mock(AbstractPropertyUpdate.class);
		when(propertyUpdate.getPropertyLogicalName()).thenReturn("property1");
		when(configurationUpdate.getPropertyUpdates()).thenReturn(Collections.<IPropertyUpdate<?>> singleton(propertyUpdate));

		SingleProperty singleProperty = mock(SingleProperty.class);
		when(singleProperty.canUpdate(propertyUpdate)).thenReturn(false);
		when(configuration.forLogicalName(Mockito.<String> anyObject())).thenReturn(singleProperty);

		propertyManager.loadConfigurationAndUpdates();
		propertyManager.updateConfiguration(configurationUpdate, false);
	}

	/**
	 * Check that Exception will be thrown if the {@link ConfigurationUpdate} has update for non
	 * existing property.
	 */
	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = { Exception.class })
	public void noUpdateIfPropertyNotFound() throws Exception {
		doReturn(configuration).when(transformator).unmarshall(Mockito.<Path> anyObject(), Mockito.<Path> anyObject(), eq(Configuration.class));
		doReturn(null).when(transformator).unmarshall(Mockito.<Path> anyObject(), Mockito.<Path> anyObject(), eq(ConfigurationUpdate.class));
		when(configuration.forLogicalName(Mockito.<String> anyObject())).thenReturn(null);
		ConfigurationUpdate configurationUpdate = mock(ConfigurationUpdate.class);
		AbstractPropertyUpdate<Long> propertyUpdate = mock(AbstractPropertyUpdate.class);
		when(propertyUpdate.getPropertyLogicalName()).thenReturn("property1");
		when(configurationUpdate.getPropertyUpdates()).thenReturn(Collections.<IPropertyUpdate<?>> singleton(propertyUpdate));

		propertyManager.loadConfigurationAndUpdates();
		propertyManager.updateConfiguration(configurationUpdate, false);
	}

	/**
	 * Check that property will be correctly updated during runtime.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void runtimePropertyUpdate() throws Exception {
		Configuration configuration = new Configuration();
		PropertySection section = new PropertySection();
		SingleProperty<Long> property = new LongProperty("", "", "property1", 10L, false, false);
		section.addProperty(property);
		configuration.addSection(section);

		doReturn(configuration).when(transformator).unmarshall(Mockito.<Path> anyObject(), Mockito.<Path> anyObject(), eq(Configuration.class));
		doReturn(null).when(transformator).unmarshall(Mockito.<Path> anyObject(), Mockito.<Path> anyObject(), eq(ConfigurationUpdate.class));
		doNothing().when(transformator).marshall(Mockito.<Path> anyObject(), any(), anyString());

		ConfigurationUpdate configurationUpdate = mock(ConfigurationUpdate.class);
		AbstractPropertyUpdate<Long> propertyUpdate = mock(AbstractPropertyUpdate.class);
		when(propertyUpdate.getPropertyLogicalName()).thenReturn("property1");
		when(propertyUpdate.getUpdateValue()).thenReturn(20L);
		when(configurationUpdate.getPropertyUpdates()).thenReturn(Collections.<IPropertyUpdate<?>> singleton(propertyUpdate));

		propertyManager.loadConfigurationAndUpdates();
		propertyManager.updateConfiguration(configurationUpdate, false);

		assertThat(property.getValue(), is(20L));
		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
		verify(propertyUpdateExecutor, times(1)).executePropertyUpdates(captor.capture());
		List<SingleProperty<?>> list = captor.getValue();
		assertThat(list, hasSize(1));
		assertThat(list, hasItem(property));

		// confirm configuration update write
		verify(transformator, times(1)).marshall(Mockito.<Path> anyObject(), eq(configurationUpdate), anyString());
	}

	/**
	 * Confirm restore to default of already existing property.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void runtimePropertyUpdateOfAlreadyUpdatedProperty() throws Exception {
		Configuration configuration = new Configuration();
		PropertySection section = new PropertySection();
		SingleProperty<Long> property = new LongProperty("", "", "property1", 10L, false, false);
		section.addProperty(property);
		configuration.addSection(section);

		ConfigurationUpdate configurationUpdate = mock(ConfigurationUpdate.class, Mockito.RETURNS_MOCKS);
		AbstractPropertyUpdate<Long> propertyUpdate = mock(AbstractPropertyUpdate.class);
		when(propertyUpdate.getPropertyLogicalName()).thenReturn("property1");
		when(propertyUpdate.getUpdateValue()).thenReturn(20L);
		Set<IPropertyUpdate<?>> propertyUpdates = Mockito.spy(new HashSet<IPropertyUpdate<?>>());
		propertyUpdates.add(propertyUpdate);
		when(configurationUpdate.getPropertyUpdates()).thenReturn(propertyUpdates);

		doReturn(configuration).when(transformator).unmarshall(Mockito.<Path> anyObject(), Mockito.<Path> anyObject(), eq(Configuration.class));
		doReturn(configurationUpdate).when(transformator).unmarshall(Mockito.<Path> anyObject(), Mockito.<Path> anyObject(), eq(ConfigurationUpdate.class));
		doNothing().when(transformator).marshall(Mockito.<Path> anyObject(), any(), anyString());

		ConfigurationUpdate configurationUpdateRuntime = mock(ConfigurationUpdate.class);
		AbstractPropertyUpdate<Long> propertyUpdateRuntime = mock(AbstractPropertyUpdate.class);
		when(propertyUpdateRuntime.getPropertyLogicalName()).thenReturn("property1");
		when(propertyUpdateRuntime.getUpdateValue()).thenReturn(10L); // set back to default value
		when(configurationUpdateRuntime.getPropertyUpdates()).thenReturn(Collections.<IPropertyUpdate<?>> singleton(propertyUpdateRuntime));

		propertyManager.getProperties();
		propertyManager.updateConfiguration(configurationUpdateRuntime, false);

		// confirm property update and value
		assertThat(property.getValue(), is(10L)); // returned to the default value
		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
		verify(propertyUpdateExecutor, times(1)).executePropertyUpdates(captor.capture());
		List<SingleProperty<?>> list = captor.getValue();
		assertThat(list, hasSize(1));
		assertThat(list, hasItem(property));

		// confirm merging of configurations and write
		verify(configurationUpdate, times(1)).merge(configurationUpdateRuntime, true);
		verify(transformator, times(1)).marshall(Mockito.<Path> anyObject(), eq(configurationUpdate), anyString());
	}
}
