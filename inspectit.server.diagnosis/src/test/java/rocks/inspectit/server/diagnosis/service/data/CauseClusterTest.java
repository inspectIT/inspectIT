package rocks.inspectit.server.diagnosis.service.data;

import static org.testng.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;

/**
 * @author Christian Voegele
 *
 */
@SuppressWarnings("PMD")
public class CauseClusterTest {

	public static class Constructor extends CauseClusterTest {

		private static final long METHOD_IDENT = 108L;
		private static final Timestamp DEF_DATE = new Timestamp(new Date().getTime());

		@Test
		public void isReturningTheProperValue() {
			InvocationSequenceData root = new InvocationSequenceData(DEF_DATE, 10, 10, METHOD_IDENT);
			InvocationSequenceData firstInvocationSequenceData = new InvocationSequenceData(DEF_DATE, 10, 10, METHOD_IDENT);
			firstInvocationSequenceData.setParentSequence(root);
			InvocationSequenceData secondInvocationSequenceData = new InvocationSequenceData(DEF_DATE, 10, 10, METHOD_IDENT);
			secondInvocationSequenceData.setParentSequence(root);
			root.getNestedSequences().add(firstInvocationSequenceData);
			root.getNestedSequences().add(secondInvocationSequenceData);
			CauseCluster one = new CauseCluster(firstInvocationSequenceData);
			CauseCluster two = new CauseCluster(secondInvocationSequenceData);
			one.setDistanceToNextCluster(1);
			one.setDepthOfCommonContext(1);
			two.setDistanceToNextCluster(1);
			two.setDepthOfCommonContext(1);
			List<CauseCluster> clustersToMerge = new ArrayList<CauseCluster>();
			clustersToMerge.add(one);
			clustersToMerge.add(two);

			CauseCluster merged = new CauseCluster(clustersToMerge, root);

			assertEquals(merged.getCauseInvocations().size(), 2);
			assertEquals(merged.getCommonContext() == root, true);
		}

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void failureNull() {
			CauseCluster merged = new CauseCluster(null, null);

			assertEquals(merged.getCauseInvocations().size(), 0);
		}

		@Test
		public void commonContextEqualsGlobalContext() {
			InvocationSequenceData root = new InvocationSequenceData(DEF_DATE, 10, 10, METHOD_IDENT);
			InvocationSequenceData firstInvocationSequenceData = new InvocationSequenceData(DEF_DATE, 10, 10, METHOD_IDENT);
			firstInvocationSequenceData.setParentSequence(root);
			InvocationSequenceData secondInvocationSequenceData = new InvocationSequenceData(DEF_DATE, 10, 10, METHOD_IDENT);
			secondInvocationSequenceData.setParentSequence(root);
			root.getNestedSequences().add(firstInvocationSequenceData);
			root.getNestedSequences().add(secondInvocationSequenceData);
			CauseCluster one = new CauseCluster(root);
			CauseCluster two = new CauseCluster(secondInvocationSequenceData);
			one.setDistanceToNextCluster(1);
			one.setDepthOfCommonContext(1);
			two.setDistanceToNextCluster(1);
			two.setDepthOfCommonContext(1);
			List<CauseCluster> clustersToMerge = new ArrayList<CauseCluster>();
			clustersToMerge.add(one);
			clustersToMerge.add(two);

			CauseCluster merged = new CauseCluster(clustersToMerge, root);

			assertEquals(merged.getCauseInvocations().size(), 2);
			assertEquals(merged.getCommonContext() == root, true);
		}

		@Test
		public void wrongGlobalContextResultsInWrongCommonContext() {
			InvocationSequenceData root = new InvocationSequenceData(DEF_DATE, 10, 10, METHOD_IDENT);
			InvocationSequenceData firstInvocationSequenceData = new InvocationSequenceData(DEF_DATE, 10, 10, METHOD_IDENT);
			firstInvocationSequenceData.setParentSequence(root);
			InvocationSequenceData secondInvocationSequenceData = new InvocationSequenceData(DEF_DATE, 10, 10, METHOD_IDENT);
			secondInvocationSequenceData.setParentSequence(root);
			root.getNestedSequences().add(firstInvocationSequenceData);
			root.getNestedSequences().add(secondInvocationSequenceData);
			CauseCluster one = new CauseCluster(root);
			CauseCluster two = new CauseCluster(secondInvocationSequenceData);
			one.setDistanceToNextCluster(1);
			one.setDepthOfCommonContext(1);
			two.setDistanceToNextCluster(1);
			two.setDepthOfCommonContext(1);
			List<CauseCluster> clustersToMerge = new ArrayList<CauseCluster>();
			clustersToMerge.add(one);
			clustersToMerge.add(two);

			CauseCluster merged = new CauseCluster(clustersToMerge, secondInvocationSequenceData);

			assertEquals(merged.getCauseInvocations().size(), 2);
			assertEquals(merged.getCommonContext() == null, true);
		}

	}

}
