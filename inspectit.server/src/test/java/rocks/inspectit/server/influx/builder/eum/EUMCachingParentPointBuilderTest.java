package rocks.inspectit.server.influx.builder.eum;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.eum.AbstractEUMSpanDetails;
import rocks.inspectit.shared.all.communication.data.eum.Beacon;
import rocks.inspectit.shared.all.communication.data.eum.EUMBeaconElement;
import rocks.inspectit.shared.all.communication.data.eum.EUMSpan;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;
import rocks.inspectit.shared.all.instrumentation.config.impl.JSAgentModule;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.data.PropagationType;

/**
 * @author Jonas Kunz
 *
 */
public class EUMCachingParentPointBuilderTest extends TestBase {

	@SuppressWarnings("serial")
	static class DummyBeaconElement extends AbstractEUMSpanDetails {

		public EUMSpan owner;

		@Override
		public EUMSpan getOwningSpan() {
			return owner;
		}

		@Override
		public boolean isAsyncCall() {
			return false;
		}

		@Override
		public boolean isExternalCall() {
			return false;
		}

		@Override
		public void collectTags(Map<String, String> tags) {

		}

		@Override
		public PropagationType getPropagationType() {
			return null;
		}
	}

	static class DummyPointBuilder extends AbstractEUMPointBuilder<DummyBeaconElement> {

		@Override
		public Collection<Class<? extends AbstractEUMSpanDetails>> getSupportedTypes() {
			return Collections.<Class<? extends AbstractEUMSpanDetails>> singleton(DummyBeaconElement.class);
		}

		public boolean requiresSessionMetaInfoFlag = true;
		public boolean requiresPageLoadRequestFlag = true;

		public final Builder returnedBuilder = Point.measurement("test");

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean requiresSessionMetaInfo() {
			return requiresSessionMetaInfoFlag;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean requiresPageLoadRequest() {
			return requiresPageLoadRequestFlag;
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

		when(sessionInfo.getSessionId()).thenReturn(SESSION_ID);

		EUMSpan plrSpan = Mockito.mock(EUMSpan.class);
		when(plrSpan.getDetails()).thenReturn(pageLoadRequest);
		when(pageLoadRequest.getOwningSpan()).thenReturn(plrSpan);

		when(plrSpan.getTabId()).thenReturn(TAB_ID);
		when(plrSpan.getSessionId()).thenReturn(SESSION_ID);
	}

	protected Beacon generateBeacon(AbstractEUMSpanDetails elementToSend, boolean containsSessionInfo, boolean containsPLR, String modules) {

		ArrayList<EUMBeaconElement> data = new ArrayList<>();
		if (containsSessionInfo) {
			data.add(sessionInfo);
		}
		if (containsPLR) {
			data.add(pageLoadRequest.getOwningSpan());
			when(pageLoadRequest.getOwningSpan().getActiveAgentModules()).thenReturn(modules);
		}
		if (elementToSend != null) {
			EUMSpan span = Mockito.mock(EUMSpan.class);
			when(span.getTabId()).thenReturn(TAB_ID);
			when(span.getSessionId()).thenReturn(SESSION_ID);

			when(span.getDetails()).thenReturn(elementToSend);
			if (elementToSend instanceof DummyBeaconElement) {
				((DummyBeaconElement) elementToSend).owner = span;
			} else {
				when(elementToSend.getOwningSpan()).thenReturn(span);
			}
			when(span.getActiveAgentModules()).thenReturn(modules);
			data.add(span);
		}

		return new Beacon(SESSION_ID, TAB_ID, modules, data);

	}

	public static class CreateBuidlers extends EUMCachingParentPointBuilderTest {

		@Test
		public void testUnkownElementTypehandling() {
			String modules = JSAgentModule.BROWSERINFO_MODULE.getIdentifier() + "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			AbstractEUMSpanDetails dummy = mock(AbstractEUMSpanDetails.class);
			this.subBuilder.requiresPageLoadRequestFlag = true;
			this.subBuilder.requiresSessionMetaInfoFlag = true;

			Collection<Builder> result = new ArrayList<>();
			for (EUMBeaconElement elem : generateBeacon(dummy, true, true, modules).getData()) {
				result.addAll(builder.createBuilders((DefaultData) elem));
			}

			assertThat(result.isEmpty(), equalTo(true));
		}

		@Test
		public void testWaitForSessionInfo() {
			String modules = JSAgentModule.BROWSERINFO_MODULE.getIdentifier() + "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			DummyBeaconElement dummy = new DummyBeaconElement();
			this.subBuilder.requiresPageLoadRequestFlag = false;
			this.subBuilder.requiresSessionMetaInfoFlag = true;

			Collection<Builder> resultA = new ArrayList<>();
			for (EUMBeaconElement elem : generateBeacon(dummy, false, false, modules).getData()) {
				resultA.addAll(builder.createBuilders((DefaultData) elem));
			}
			Collection<Builder> resultB = new ArrayList<>();
			for (EUMBeaconElement elem : generateBeacon(null, true, false, modules).getData()) {
				resultB.addAll(builder.createBuilders((DefaultData) elem));
			}

			assertThat(resultA.isEmpty(), equalTo(true));
			assertThat(resultB.isEmpty(), equalTo(false));
		}

		@Test
		public void testWaitForPageLoadRequest() {
			String modules = JSAgentModule.BROWSERINFO_MODULE.getIdentifier() + "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			DummyBeaconElement dummy = new DummyBeaconElement();
			this.subBuilder.requiresPageLoadRequestFlag = true;
			this.subBuilder.requiresSessionMetaInfoFlag = false;

			Collection<Builder> resultA = new ArrayList<>();
			for (EUMBeaconElement elem : generateBeacon(dummy, true, false, modules).getData()) {
				resultA.addAll(builder.createBuilders((DefaultData) elem));
			}
			Collection<Builder> resultB = new ArrayList<>();
			for (EUMBeaconElement elem : generateBeacon(null, false, true, modules).getData()) {
				resultB.addAll(builder.createBuilders((DefaultData) elem));
			}

			assertThat(resultA.isEmpty(), equalTo(true));
			assertThat(resultB.isEmpty(), equalTo(false));
		}

		@Test
		public void testWaitForPageLoadRequestAndThenSessionInfo() {
			String modules = JSAgentModule.BROWSERINFO_MODULE.getIdentifier() + "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			DummyBeaconElement dummy = new DummyBeaconElement();
			this.subBuilder.requiresPageLoadRequestFlag = true;
			this.subBuilder.requiresSessionMetaInfoFlag = true;

			Collection<Builder> resultA = new ArrayList<>();
			for (EUMBeaconElement elem : generateBeacon(dummy, false, false, modules).getData()) {
				resultA.addAll(builder.createBuilders((DefaultData) elem));
			}
			Collection<Builder> resultB = new ArrayList<>();
			for (EUMBeaconElement elem : generateBeacon(null, false, true, modules).getData()) {
				resultB.addAll(builder.createBuilders((DefaultData) elem));
			}
			Collection<Builder> resultC = new ArrayList<>();
			for (EUMBeaconElement elem : generateBeacon(null, true, false, modules).getData()) {
				resultC.addAll(builder.createBuilders((DefaultData) elem));
			}

			assertThat(resultA.isEmpty(), equalTo(true));
			assertThat(resultB.isEmpty(), equalTo(true));
			assertThat(resultC.isEmpty(), equalTo(false));
		}

		@Test
		public void testWaitForSessionInfoAndThenPageLoadRequest() {
			String modules = JSAgentModule.BROWSERINFO_MODULE.getIdentifier() + "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			DummyBeaconElement dummy = new DummyBeaconElement();
			this.subBuilder.requiresPageLoadRequestFlag = true;
			this.subBuilder.requiresSessionMetaInfoFlag = true;

			Collection<Builder> resultA = new ArrayList<>();
			for (EUMBeaconElement elem : generateBeacon(dummy, false, false, modules).getData()) {
				resultA.addAll(builder.createBuilders((DefaultData) elem));
			}
			Collection<Builder> resultB = new ArrayList<>();
			for (EUMBeaconElement elem : generateBeacon(null, true, false, modules).getData()) {
				resultB.addAll(builder.createBuilders((DefaultData) elem));
			}
			Collection<Builder> resultC = new ArrayList<>();
			for (EUMBeaconElement elem : generateBeacon(null, false, true, modules).getData()) {
				resultC.addAll(builder.createBuilders((DefaultData) elem));
			}
			assertThat(resultA.isEmpty(), equalTo(true));
			assertThat(resultB.isEmpty(), equalTo(true));
			assertThat(resultC.isEmpty(), equalTo(false));
		}

		@Test
		public void testIgnoreSessionInfoAndThenWaitForPageLoadRequest() {
			String modules = "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			DummyBeaconElement dummy = new DummyBeaconElement();
			this.subBuilder.requiresPageLoadRequestFlag = true;
			this.subBuilder.requiresSessionMetaInfoFlag = true;

			Collection<Builder> resultA = new ArrayList<>();
			for (EUMBeaconElement elem : generateBeacon(dummy, false, false, modules).getData()) {
				resultA.addAll(builder.createBuilders((DefaultData) elem));
			}
			Collection<Builder> resultB = new ArrayList<>();
			for (EUMBeaconElement elem : generateBeacon(null, false, true, modules).getData()) {
				resultB.addAll(builder.createBuilders((DefaultData) elem));
			}

			assertThat(resultA.isEmpty(), equalTo(true));
			assertThat(resultB.isEmpty(), equalTo(false));
		}

		@Test
		public void testIgnoreMissingSessionInfo() {
			String modules = "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			DummyBeaconElement dummy = new DummyBeaconElement();
			this.subBuilder.requiresPageLoadRequestFlag = true;
			this.subBuilder.requiresSessionMetaInfoFlag = true;

			Collection<Builder> result = new ArrayList<>();
			for (EUMBeaconElement elem : generateBeacon(dummy, false, true, modules).getData()) {
				result.addAll(builder.createBuilders((DefaultData) elem));
			}

			assertThat(result.isEmpty(), equalTo(false));
		}

		@Test
		public void testSessionInfoNotRequired() {

			String modules = JSAgentModule.BROWSERINFO_MODULE.getIdentifier() + "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			DummyBeaconElement dummy = new DummyBeaconElement();
			this.subBuilder.requiresPageLoadRequestFlag = true;
			this.subBuilder.requiresSessionMetaInfoFlag = false;

			Collection<Builder> result = new ArrayList<>();
			for (EUMBeaconElement elem : generateBeacon(dummy, false, true, modules).getData()) {
				result.addAll(builder.createBuilders((DefaultData) elem));
			}

			assertThat(result.isEmpty(), equalTo(false));

		}

		@Test
		public void testPageLoadRequestNotRequired() {

			String modules = JSAgentModule.BROWSERINFO_MODULE.getIdentifier() + "" + JSAgentModule.NAVTIMINGS_MODULE.getIdentifier();
			DummyBeaconElement dummy = new DummyBeaconElement();
			this.subBuilder.requiresPageLoadRequestFlag = false;
			this.subBuilder.requiresSessionMetaInfoFlag = true;

			Collection<Builder> result = new ArrayList<>();
			for (EUMBeaconElement elem : generateBeacon(dummy, true, false, modules).getData()) {
				result.addAll(builder.createBuilders((DefaultData) elem));
			}

			assertThat(result.isEmpty(), equalTo(false));

		}

	}

}
