package rocks.inspectit.server.influx.builder.eum;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.eum.AbstractEUMElement;
import rocks.inspectit.shared.all.communication.data.eum.Beacon;
import rocks.inspectit.shared.all.communication.data.eum.EUMElementID;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;
import rocks.inspectit.shared.all.instrumentation.config.impl.JSAgentModule;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class EUMCachingParentPointBuilderTest extends TestBase {

	@SuppressWarnings("serial")
	static class DummyBeaconElement extends AbstractEUMElement {

	}

	static class DummyPointBuilder extends AbstractEUMPointBuilder<DummyBeaconElement> {

		@Override
		public Collection<Class<? extends AbstractEUMElement>> getSupportedTypes() {
			return Collections.<Class<? extends AbstractEUMElement>> singleton(DummyBeaconElement.class);
		}

		public boolean requiresSessionMetaInfo = true;
		public boolean requiresPageLoadRequest = true;

		public final Builder returnedBuilder = Point.measurement("test");

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean requiresSessionMetaInfo() {
			return requiresSessionMetaInfo;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean requiresPageLoadRequest() {
			return requiresPageLoadRequest;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Collection<Builder> build(UserSessionInfo sessionInfo, PageLoadRequest plr, DummyBeaconElement data) {
			return Collections.singletonList(returnedBuilder);
		}


	}

	EUMCachingParentPointBuilder builder;

	DummyPointBuilder subBuilder;

	private static final long SESSION_ID = 1234;
	private static final long TAB_ID = 56789;


	@Mock
	private UserSessionInfo sessionInfo;

	@Mock
	private PageLoadRequest pageLoadRequest;

	@BeforeMethod
	public void setup() {
		subBuilder = new DummyPointBuilder();
		builder = new EUMCachingParentPointBuilder(Collections.<AbstractEUMPointBuilder<?>> singletonList(subBuilder));

		EUMElementID id = new EUMElementID();
		id.setLocalID(0);
		id.setSessionID(SESSION_ID);
		id.setTabID(TAB_ID);
		when(sessionInfo.getID()).thenReturn(id);

		EUMElementID id2 = new EUMElementID();
		id2.setLocalID(1);
		id2.setSessionID(SESSION_ID);
		id2.setTabID(TAB_ID);
		when(pageLoadRequest.getID()).thenReturn(id2);
	}

	protected Beacon generateBeacon(AbstractEUMElement elementToSend, boolean containsSessionInfo, boolean containsPLR, String modules) {

		ArrayList<AbstractEUMElement> data = new ArrayList<>();
		if (containsSessionInfo) {
			data.add(sessionInfo);
		}
		if (containsPLR) {
			data.add(pageLoadRequest);
		}
		if (elementToSend != null) {
			elementToSend.setLocalID(2);
			elementToSend.setSessionID(SESSION_ID);
			elementToSend.setTabID(TAB_ID);
			data.add(elementToSend);
		}

		return new Beacon(SESSION_ID, TAB_ID, modules, data);

	}

	public static class CreateBuidlers extends EUMCachingParentPointBuilderTest {

		@Test
		public void testUnkownElementTypehandling() {
			String modules = JSAgentModule.BROWSERINFO_MODULE.getIdentifier() + "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			AbstractEUMElement dummy = mock(AbstractEUMElement.class);
			this.subBuilder.requiresPageLoadRequest = true;
			this.subBuilder.requiresSessionMetaInfo = true;

			Collection<Builder> result = builder.createBuilders(generateBeacon(dummy, true, true, modules));

			assertThat(result.isEmpty(), equalTo(true));
		}

		@Test
		public void testWaitForSessionInfo() {
			String modules = JSAgentModule.BROWSERINFO_MODULE.getIdentifier() + "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			DummyBeaconElement dummy = new DummyBeaconElement();
			this.subBuilder.requiresPageLoadRequest = false;
			this.subBuilder.requiresSessionMetaInfo = true;

			Collection<Builder> resultA = builder.createBuilders(generateBeacon(dummy, false, false, modules));
			Collection<Builder> resultB = builder.createBuilders(generateBeacon(null, true, false, modules));

			assertThat(resultA.isEmpty(), equalTo(true));
			assertThat(resultB.isEmpty(), equalTo(false));
		}

		@Test
		public void testWaitForPageLoadRequest() {
			String modules = JSAgentModule.BROWSERINFO_MODULE.getIdentifier() + "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			DummyBeaconElement dummy = new DummyBeaconElement();
			this.subBuilder.requiresPageLoadRequest = true;
			this.subBuilder.requiresSessionMetaInfo = false;

			Collection<Builder> resultA = builder.createBuilders(generateBeacon(dummy, true, false, modules));
			Collection<Builder> resultB = builder.createBuilders(generateBeacon(null, false, true, modules));

			assertThat(resultA.isEmpty(), equalTo(true));
			assertThat(resultB.isEmpty(), equalTo(false));
		}

		@Test
		public void testWaitForPageLoadRequestAndThenSessionInfo() {
			String modules = JSAgentModule.BROWSERINFO_MODULE.getIdentifier() + "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			DummyBeaconElement dummy = new DummyBeaconElement();
			this.subBuilder.requiresPageLoadRequest = true;
			this.subBuilder.requiresSessionMetaInfo = true;

			Collection<Builder> resultA = builder.createBuilders(generateBeacon(dummy, false, false, modules));
			Collection<Builder> resultB = builder.createBuilders(generateBeacon(null, false, true, modules));
			Collection<Builder> resultC = builder.createBuilders(generateBeacon(null, true, false, modules));

			assertThat(resultA.isEmpty(), equalTo(true));
			assertThat(resultB.isEmpty(), equalTo(true));
			assertThat(resultC.isEmpty(), equalTo(false));
		}

		@Test
		public void testWaitForSessionInfoAndThenPageLoadRequest() {
			String modules = JSAgentModule.BROWSERINFO_MODULE.getIdentifier() + "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			DummyBeaconElement dummy = new DummyBeaconElement();
			this.subBuilder.requiresPageLoadRequest = true;
			this.subBuilder.requiresSessionMetaInfo = true;

			Collection<Builder> resultA = builder.createBuilders(generateBeacon(dummy, false, false, modules));
			Collection<Builder> resultB = builder.createBuilders(generateBeacon(null, true, false, modules));
			Collection<Builder> resultC = builder.createBuilders(generateBeacon(null, false, true, modules));

			assertThat(resultA.isEmpty(), equalTo(true));
			assertThat(resultB.isEmpty(), equalTo(true));
			assertThat(resultC.isEmpty(), equalTo(false));
		}

		@Test
		public void testIgnoreSessionInfoAndThenWaitForPageLoadRequest() {
			String modules = "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			DummyBeaconElement dummy = new DummyBeaconElement();
			this.subBuilder.requiresPageLoadRequest = true;
			this.subBuilder.requiresSessionMetaInfo = true;

			Collection<Builder> resultA = builder.createBuilders(generateBeacon(dummy, false, false, modules));
			Collection<Builder> resultB = builder.createBuilders(generateBeacon(null, false, true, modules));

			assertThat(resultA.isEmpty(), equalTo(true));
			assertThat(resultB.isEmpty(), equalTo(false));
		}

		@Test
		public void testIgnoreMissingSessionInfo() {
			String modules = "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			DummyBeaconElement dummy = new DummyBeaconElement();
			this.subBuilder.requiresPageLoadRequest = true;
			this.subBuilder.requiresSessionMetaInfo = true;

			Collection<Builder> result = builder.createBuilders(generateBeacon(dummy, false, true, modules));

			assertThat(result.isEmpty(), equalTo(false));
		}

		@Test
		public void testSessionInfoNotRequired() {

			String modules = JSAgentModule.BROWSERINFO_MODULE.getIdentifier() + "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			DummyBeaconElement dummy = new DummyBeaconElement();
			this.subBuilder.requiresPageLoadRequest = true;
			this.subBuilder.requiresSessionMetaInfo = false;

			Collection<Builder> result = builder.createBuilders(generateBeacon(dummy, false, true, modules));

			assertThat(result.isEmpty(), equalTo(false));

		}

		@Test
		public void testPageLoadRequestNotRequired() {

			String modules = JSAgentModule.BROWSERINFO_MODULE.getIdentifier() + "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			DummyBeaconElement dummy = new DummyBeaconElement();
			this.subBuilder.requiresPageLoadRequest = false;
			this.subBuilder.requiresSessionMetaInfo = true;

			Collection<Builder> result = builder.createBuilders(generateBeacon(dummy, true, false, modules));

			assertThat(result.isEmpty(), equalTo(false));

		}

	}

}
