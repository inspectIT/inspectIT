package info.novatec.inspectit.ci;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class CiXmlTransformationTest {

	private File f = new File("test.xml");;

	@Test
	public void marshalEnvironment() throws JAXBException {
		Environment environment = new Environment();

		JAXBContext context = JAXBContext.newInstance(Environment.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		marshaller.marshal(environment, System.out);
		marshaller.marshal(environment, f);

		Unmarshaller unmarshaller = context.createUnmarshaller();
		Object object = unmarshaller.unmarshal(f);

		assertThat(object, is(instanceOf(Environment.class)));
		assertThat((Environment) object, is(equalTo(environment)));
	}

	@Test
	public void marshalProfile() throws JAXBException {
		Profile profile = CiDataFactory.getProfile();

		JAXBContext context = JAXBContext.newInstance(Profile.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		marshaller.marshal(profile, System.out);
		marshaller.marshal(profile, f);

		Unmarshaller unmarshaller = context.createUnmarshaller();
		Object object = unmarshaller.unmarshal(f);

		assertThat(object, is(instanceOf(Profile.class)));
		assertThat((Profile) object, is(equalTo(profile)));
	}

	@Test
	public void marshalAgentMappings() throws JAXBException {
		List<AgentMapping> list = CiDataFactory.getAgentMappings(5);
		AgentMappings agentMappings = new AgentMappings();
		agentMappings.setMappings(list);

		JAXBContext context = JAXBContext.newInstance(AgentMappings.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		marshaller.marshal(agentMappings, System.out);
		marshaller.marshal(agentMappings, f);

		Unmarshaller unmarshaller = context.createUnmarshaller();
		Object object = unmarshaller.unmarshal(f);

		assertThat(object, is(instanceOf(AgentMappings.class)));
		assertThat((AgentMappings) object, is(equalTo(agentMappings)));
	}

	@BeforeTest
	@AfterTest
	public void deleteFile() {
		if (f.exists()) {
			f.delete();
		}
	}
}
