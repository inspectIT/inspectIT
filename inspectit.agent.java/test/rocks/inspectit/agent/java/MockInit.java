package rocks.inspectit.agent.java;

import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;

public abstract class MockInit {

	@BeforeMethod
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

}
