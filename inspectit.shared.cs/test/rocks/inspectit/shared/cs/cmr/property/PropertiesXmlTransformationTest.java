package info.novatec.inspectit.cmr.property;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import info.novatec.inspectit.cmr.property.configuration.Configuration;
import info.novatec.inspectit.cmr.property.configuration.GroupedProperty;
import info.novatec.inspectit.cmr.property.configuration.PropertySection;
import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.configuration.impl.BooleanProperty;
import info.novatec.inspectit.cmr.property.configuration.impl.ByteProperty;
import info.novatec.inspectit.cmr.property.configuration.impl.LongProperty;
import info.novatec.inspectit.cmr.property.configuration.impl.PercentageProperty;
import info.novatec.inspectit.cmr.property.configuration.impl.StringProperty;
import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidationException;
import info.novatec.inspectit.cmr.property.configuration.validator.impl.LessValidator;
import info.novatec.inspectit.cmr.property.configuration.validator.impl.NotEmptyValidator;
import info.novatec.inspectit.cmr.property.configuration.validator.impl.PositiveValidator;
import info.novatec.inspectit.cmr.property.update.AbstractPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.configuration.ConfigurationUpdate;
import info.novatec.inspectit.cmr.property.update.impl.BooleanPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.impl.BytePropertyUpdate;
import info.novatec.inspectit.cmr.property.update.impl.LongPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.impl.PercentagePropertyUpdate;
import info.novatec.inspectit.cmr.property.update.impl.StringPropertyUpdate;

import java.io.File;
import java.util.Random;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class PropertiesXmlTransformationTest {

	private File f = new File("test.xml");;

	@Test
	public void marshalConfiguration() throws JAXBException {
		Configuration configuration = new Configuration();

		PropertySection section = new PropertySection("MySection");
		configuration.addSection(section);

		SingleProperty<String> property1 = new StringProperty("title", "Define title", "properties.title", "Lorem ipsum", true, false);
		property1.addValidator(new NotEmptyValidator<String>());
		section.addProperty(property1);

		SingleProperty<Long> property2 = new LongProperty("speed", "Define speed", "properties.speed", 10L, true, true);
		property2.addValidator(new PositiveValidator<Long>());
		section.addProperty(property2);

		GroupedProperty groupedProperty = new GroupedProperty("myGroup", "Lets show how can you group properties");
		SingleProperty<Long> property3 = new LongProperty("Max rotation", "Define max rotation", "properties.rotation.max", 90L, false, false);
		groupedProperty.addSingleProperty(property3);
		SingleProperty<Long> property4 = new LongProperty("Min rotation", "Define min rotation", "properties.rotation.min", 0L, false, false);
		groupedProperty.addSingleProperty(property4);
		LessValidator<Long> lessValidator = new LessValidator<Long>();
		lessValidator.setProperty("properties.rotation.min");
		lessValidator.setThan("properties.rotation.max");
		groupedProperty.addValidator(lessValidator);

		section.addProperty(groupedProperty);

		JAXBContext context = JAXBContext.newInstance(Configuration.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		marshaller.marshal(configuration, System.out);
		marshaller.marshal(configuration, f);

		Unmarshaller unmarshaller = context.createUnmarshaller();
		Object object = unmarshaller.unmarshal(f);

		assertThat(object, is(instanceOf(Configuration.class)));
		assertThat((Configuration) object, is(equalTo(configuration)));
	}

	@Test
	public void marshalConfigurationUpdate() throws JAXBException, PropertyValidationException {
		ConfigurationUpdate configurationUpdate = new ConfigurationUpdate();

		SingleProperty<String> property1 = new StringProperty("title", "Define title", "properties.title", "Lorem ipsum", true, false);
		configurationUpdate.addPropertyUpdate(property1.createAndValidatePropertyUpdate("New value"));

		SingleProperty<Long> property2 = new LongProperty("speed", "Define speed", "properties.speed", 10L, true, true);
		configurationUpdate.addPropertyUpdate(property2.createAndValidatePropertyUpdate(1000L));

		SingleProperty<Boolean> property3 = new BooleanProperty("boolean", "Define boolean", "properties.boolean", true, true, true);
		configurationUpdate.addPropertyUpdate(property3.createAndValidatePropertyUpdate(false));

		JAXBContext context = JAXBContext.newInstance(ConfigurationUpdate.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		marshaller.marshal(configurationUpdate, System.out);
		marshaller.marshal(configurationUpdate, f);

		Unmarshaller unmarshaller = context.createUnmarshaller();
		Object object = unmarshaller.unmarshal(f);

		assertThat(object, is(instanceOf(ConfigurationUpdate.class)));
		assertThat((ConfigurationUpdate) object, is(equalTo(configurationUpdate)));
	}

	@Test
	public void booleanPropertyXmlSaveLoad() throws JAXBException {
		boolean value = new Random().nextBoolean();
		BooleanProperty booleanProperty = new BooleanProperty("name", "description", "logical-name", Boolean.valueOf(value), false, false);

		JAXBContext context = JAXBContext.newInstance(ByteProperty.class);
		Marshaller marshaller = context.createMarshaller();
		Unmarshaller unmarshaller = context.createUnmarshaller();

		marshaller.marshal(booleanProperty, f);
		Object object = unmarshaller.unmarshal(f);

		assertThat(object, is(instanceOf(BooleanProperty.class)));

		BooleanProperty unmarsalled = (BooleanProperty) object;
		assertThat(unmarsalled.getDefaultValue(), is(Boolean.valueOf(value)));
	}

	@Test
	public void booleanPropertyUpdateXmlSaveLoad() throws PropertyValidationException, JAXBException {
		boolean value = new Random().nextBoolean();
		BooleanProperty booleanProperty = new BooleanProperty("name", "description", "logical-name", Boolean.valueOf(!value), false, false);
		AbstractPropertyUpdate<Boolean> booleanPropertyUpdate = booleanProperty.createAndValidatePropertyUpdate(Boolean.valueOf(value));

		JAXBContext context = JAXBContext.newInstance(BooleanPropertyUpdate.class);
		Marshaller marshaller = context.createMarshaller();
		Unmarshaller unmarshaller = context.createUnmarshaller();

		marshaller.marshal(booleanPropertyUpdate, f);
		Object object = unmarshaller.unmarshal(f);

		assertThat(object, is(instanceOf(BooleanPropertyUpdate.class)));
		BooleanPropertyUpdate unmarsalled = (BooleanPropertyUpdate) object;

		assertThat(booleanProperty.canUpdate(unmarsalled), is(true));
		booleanProperty.setValue(unmarsalled.getUpdateValue());
		assertThat(booleanProperty.getValue().booleanValue(), is(value));
	}

	@Test
	public void longPropertyXmlSaveLoad() throws JAXBException {
		long value = new Random().nextLong();
		LongProperty longProperty = new LongProperty("name", "description", "logical-name", Long.valueOf(value), false, false);

		JAXBContext context = JAXBContext.newInstance(ByteProperty.class);
		Marshaller marshaller = context.createMarshaller();
		Unmarshaller unmarshaller = context.createUnmarshaller();

		marshaller.marshal(longProperty, f);
		Object object = unmarshaller.unmarshal(f);

		assertThat(object, is(instanceOf(LongProperty.class)));

		LongProperty unmarsalled = (LongProperty) object;
		assertThat(unmarsalled.getDefaultValue(), is(Long.valueOf(value)));
	}

	@Test
	public void longPropertyUpdateXmlSaveLoad() throws PropertyValidationException, JAXBException {
		long value = new Random().nextLong();
		if (1 == value) {
			// make sure we don't update with same value
			value++;
		}
		LongProperty longProperty = new LongProperty("name", "description", "logical-name", Long.valueOf(1), false, false);
		AbstractPropertyUpdate<Long> longPropertyUpdate = longProperty.createAndValidatePropertyUpdate(Long.valueOf(value));

		JAXBContext context = JAXBContext.newInstance(LongPropertyUpdate.class);
		Marshaller marshaller = context.createMarshaller();
		Unmarshaller unmarshaller = context.createUnmarshaller();

		marshaller.marshal(longPropertyUpdate, f);
		Object object = unmarshaller.unmarshal(f);

		assertThat(object, is(instanceOf(LongPropertyUpdate.class)));
		LongPropertyUpdate unmarsalled = (LongPropertyUpdate) object;

		assertThat(longProperty.canUpdate(unmarsalled), is(true));
		longProperty.setValue(unmarsalled.getUpdateValue());
		assertThat(longProperty.getValue().longValue(), is(value));
	}

	@Test
	public void stringPropertyXmlSaveLoad() throws JAXBException {
		String value = RandomStringUtils.randomAlphabetic(20);
		StringProperty stringProperty = new StringProperty("name", "description", "logical-name", value, false, false);

		JAXBContext context = JAXBContext.newInstance(ByteProperty.class);
		Marshaller marshaller = context.createMarshaller();
		Unmarshaller unmarshaller = context.createUnmarshaller();

		marshaller.marshal(stringProperty, f);
		Object object = unmarshaller.unmarshal(f);

		assertThat(object, is(instanceOf(StringProperty.class)));

		StringProperty unmarsalled = (StringProperty) object;
		assertThat(unmarsalled.getDefaultValue(), is(value));
	}

	@Test
	public void stringPropertyUpdateXmlSaveLoad() throws PropertyValidationException, JAXBException {
		String value = RandomStringUtils.randomAlphabetic(20);
		StringProperty stringProperty = new StringProperty("name", "description", "logical-name", "balbla", false, false);
		AbstractPropertyUpdate<String> stringPropertyUpdate = stringProperty.createAndValidatePropertyUpdate(value);

		JAXBContext context = JAXBContext.newInstance(StringPropertyUpdate.class);
		Marshaller marshaller = context.createMarshaller();
		Unmarshaller unmarshaller = context.createUnmarshaller();

		marshaller.marshal(stringPropertyUpdate, f);
		Object object = unmarshaller.unmarshal(f);

		assertThat(object, is(instanceOf(StringPropertyUpdate.class)));
		StringPropertyUpdate unmarsalled = (StringPropertyUpdate) object;

		assertThat(stringProperty.canUpdate(unmarsalled), is(true));
		stringProperty.setValue(unmarsalled.getUpdateValue());
		assertThat(stringProperty.getValue(), is(value));
	}

	@Test
	public void bytePropertyXmlSaveLoad() throws JAXBException {
		long bytes = 10 * 1024 * 1024 + 2; // 10MBs + 2 bytes
		ByteProperty byteProperty = new ByteProperty("name", "description", "logical-name", Long.valueOf(bytes), false, false);

		JAXBContext context = JAXBContext.newInstance(ByteProperty.class);
		Marshaller marshaller = context.createMarshaller();
		Unmarshaller unmarshaller = context.createUnmarshaller();

		marshaller.marshal(byteProperty, f);
		Object object = unmarshaller.unmarshal(f);

		assertThat(object, is(instanceOf(ByteProperty.class)));

		ByteProperty unmarsalled = (ByteProperty) object;
		assertThat(unmarsalled.getDefaultValue(), is(Long.valueOf(bytes)));
	}

	@Test
	public void bytePropertyUpdateXmlSaveLoad() throws PropertyValidationException, JAXBException {
		long bytes = 10 * 1024 * 1024 + 2; // 10MBs + 2 bytes
		ByteProperty byteProperty = new ByteProperty("name", "description", "logical-name", Long.valueOf(1), false, false);
		AbstractPropertyUpdate<Long> bytePropertyUpdate = byteProperty.createAndValidatePropertyUpdate(Long.valueOf(bytes));

		JAXBContext context = JAXBContext.newInstance(BytePropertyUpdate.class);
		Marshaller marshaller = context.createMarshaller();
		Unmarshaller unmarshaller = context.createUnmarshaller();

		marshaller.marshal(bytePropertyUpdate, f);
		Object object = unmarshaller.unmarshal(f);

		assertThat(object, is(instanceOf(BytePropertyUpdate.class)));
		BytePropertyUpdate unmarsalled = (BytePropertyUpdate) object;

		assertThat(byteProperty.canUpdate(unmarsalled), is(true));
		byteProperty.setValue(unmarsalled.getUpdateValue());
		assertThat(byteProperty.getValue().longValue(), is(bytes));
	}

	@Test
	public void percentagePropertyXmlSaveLoad() throws JAXBException {
		float value = new Random().nextInt(100);
		PercentageProperty floatProperty = new PercentageProperty("name", "description", "logical-name", Float.valueOf(value), false, false);

		JAXBContext context = JAXBContext.newInstance(ByteProperty.class);
		Marshaller marshaller = context.createMarshaller();
		Unmarshaller unmarshaller = context.createUnmarshaller();

		marshaller.marshal(floatProperty, f);
		Object object = unmarshaller.unmarshal(f);

		assertThat(object, is(instanceOf(PercentageProperty.class)));

		PercentageProperty unmarsalled = (PercentageProperty) object;
		assertThat(unmarsalled.getDefaultValue(), is(Float.valueOf(value)));
	}

	@Test
	public void floatPropertyUpdateXmlSaveLoad() throws PropertyValidationException, JAXBException {
		float value = new Random().nextInt(100);
		if (1 == value) {
			// make sure we don't update with same value
			value++;
		}
		PercentageProperty floatProperty = new PercentageProperty("name", "description", "logical-name", Float.valueOf(1), false, false);
		AbstractPropertyUpdate<Float> floatPropertyUpdate = floatProperty.createAndValidatePropertyUpdate(Float.valueOf(value));

		JAXBContext context = JAXBContext.newInstance(PercentagePropertyUpdate.class);
		Marshaller marshaller = context.createMarshaller();
		Unmarshaller unmarshaller = context.createUnmarshaller();

		marshaller.marshal(floatPropertyUpdate, f);
		Object object = unmarshaller.unmarshal(f);

		assertThat(object, is(instanceOf(PercentagePropertyUpdate.class)));
		PercentagePropertyUpdate unmarsalled = (PercentagePropertyUpdate) object;

		assertThat(floatProperty.canUpdate(unmarsalled), is(true));
		floatProperty.setValue(unmarsalled.getUpdateValue());
		assertThat(floatProperty.getValue().floatValue(), is(value));
	}

	@BeforeTest
	@AfterTest
	public void deleteFile() {
		if (f.exists()) {
			f.delete();
		}
	}
}
