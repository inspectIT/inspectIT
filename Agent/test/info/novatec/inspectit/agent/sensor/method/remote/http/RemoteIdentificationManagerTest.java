package info.novatec.inspectit.agent.sensor.method.remote.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.sensor.method.remote.inserter.RemoteIdentificationManager;

import java.util.HashSet;
import java.util.Set;

import org.mockito.InjectMocks;
import org.testng.annotations.Test;

public class RemoteIdentificationManagerTest extends AbstractLogSupport {

	@InjectMocks
	RemoteIdentificationManager remoteIdentificationManager;
	
	public static class GetNextIdentification extends RemoteIdentificationManagerTest {
		
		@Test
		public void retrieveOneRemoteId() throws Exception {
			long id = remoteIdentificationManager.getNextIdentification();
			
			assertThat(id, is(equalTo(1l)));
		}
		
		@Test
		public void retrieveMultipleRemoteId() throws Exception{
			
			for (int i = 1; i < 11; i++){
				
				long id = remoteIdentificationManager.getNextIdentification();
				assertThat(id, is(equalTo((long)i)));
			}
			
		}
		
		/**
		 * Check if the recived IDs are unique.
		 * @throws Exception
		 */
		@Test
		public void retrieveMultipleUniqueRemoteId() throws Exception{
			
			Set<Long> set = new HashSet<Long>();
			
			for (int i = 1; i < 1000; i++){
				
				long id = remoteIdentificationManager.getNextIdentification();
				assertThat(true, is(equalTo(set.add(id))));
			}
			
		}
		
	}
	
	
}
