package rocks.inspectit.server.diagnosis.service.data;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

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

		private static final Timestamp DEF_DATE = new Timestamp(new Date().getTime());

		@Test
		public void isReturningTheProperValue() {
			long methodIdent = 108L;
			InvocationSequenceData root = new InvocationSequenceData(DEF_DATE, 10, 10, methodIdent);
			InvocationSequenceData firstInvocationSequenceData = new InvocationSequenceData(DEF_DATE, 10, 10, methodIdent);
			firstInvocationSequenceData.setParentSequence(root);
			InvocationSequenceData secondInvocationSequenceData = new InvocationSequenceData(DEF_DATE, 10, 10, methodIdent);
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

			CauseCluster merged = new CauseCluster(clustersToMerge);

			assertThat("The returned problemContext must have size 2", merged.getCauseInvocations().size(), is(2));
			assertThat("The returned problemContext must be the root one element", merged.getCommonContext(), is(root));
		}

		@Test
		public void wrongGlobalContextResultsInWrongCommonContext() {
			long methodIdent = 108L;
			InvocationSequenceData root = new InvocationSequenceData(DEF_DATE, 10, 10, methodIdent);
			InvocationSequenceData firstInvocationSequenceData = new InvocationSequenceData(DEF_DATE, 10, 10, methodIdent);
			firstInvocationSequenceData.setParentSequence(root);
			InvocationSequenceData secondInvocationSequenceData = new InvocationSequenceData(DEF_DATE, 10, 10, methodIdent);
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

			CauseCluster merged = new CauseCluster(clustersToMerge);

			assertThat("The returned problemContext must has size 2", merged.getCauseInvocations().size(), is(2));
			assertThat("The returned root cause rule must not be null", merged.getCommonContext(), nullValue());
		}

	}

}
