package rocks.inspectit.shared.cs.ci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.mockito.InjectMocks;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

import nl.jqno.equalsverifier.EqualsVerifier;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition.ThresholdType;

/**
 * @author Marius Oehler
 *
 */
public class AlertingDefinitionTest extends TestBase {

	@InjectMocks
	AlertingDefinition alertingDefinition;

	Map<String, String> tagMap = ImmutableMap.of("key1", "val1", "key2", "val2", "key3", "val3");

	Map<String, String> tagMapBroken = ImmutableMap.of("key1", "val1", "", "val2", "key3", "");

	List<String> emailList = Arrays.asList("test@example.com", "test2@example.com", "test3@example.com");

	List<String> emailListBroken = Arrays.asList("test@example.com", null, "test3@example.com", "not_an_email@");

	protected AlertingDefinition createAlertingDefinition() throws BusinessException {
		AlertingDefinition definition = new AlertingDefinition();
		definition.setField("field");
		definition.setMeasurement("measurement");
		definition.setThreshold(1D);
		definition.setThresholdType(ThresholdType.LOWER_THRESHOLD);
		definition.setTimeRange(1L, TimeUnit.MINUTES);
		definition.addNotificationEmailAddress("test@example.com");
		definition.putTag("tagKey", "tagVal");
		definition.setThreshold(10);
		definition.setThresholdType(ThresholdType.UPPER_THRESHOLD);
		definition.setTimeRange(0L, TimeUnit.MINUTES);
		return definition;
	}

	/**
	 * Test the {@link AlertingDefinition#putTag(String, String)},
	 * {@link AlertingDefinition#getTags()} and {@link AlertingDefinition#removeTag(String)}
	 * methods.
	 */
	public static class PutTag extends AlertingDefinitionTest {

		@Test
		public void putValidTag() throws BusinessException {
			String tagKey = "tagKey";
			String tagValue = "tagValue";

			alertingDefinition.putTag(tagKey, tagValue);
			String returnedTagValue = alertingDefinition.getTags().get(tagKey);

			assertThat(alertingDefinition.getTags().size(), equalTo(1));
			assertThat(returnedTagValue, equalTo(tagValue));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void putNullTagKey() throws BusinessException {
			alertingDefinition.putTag(null, "tagValue");
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void putNullTagValue() throws BusinessException {
			alertingDefinition.putTag("tagKey", null);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void putEmptyTagKey() throws BusinessException {
			alertingDefinition.putTag("", "tagValue");
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void putEmptyTagValue() throws BusinessException {
			alertingDefinition.putTag("tagKey", "");
		}

	}

	/**
	 * Test the {@link AlertingDefinition#removeTag(String)} method.
	 */
	public static class RemoveTag extends AlertingDefinitionTest {
		@Test
		public void removeTag() throws BusinessException {
			String key = "tagKey";

			assertThat(alertingDefinition.getTags().size(), equalTo(0));
			alertingDefinition.putTag(key, "tagValue");
			assertThat(alertingDefinition.getTags().size(), equalTo(1));
			alertingDefinition.removeTag(key);
			assertThat(alertingDefinition.getTags().size(), equalTo(0));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void removeNullTag() throws BusinessException {
			assertThat(alertingDefinition.getTags().size(), equalTo(0));
			alertingDefinition.putTag("tagKey", "tagValue");
			assertThat(alertingDefinition.getTags().size(), equalTo(1));
			alertingDefinition.removeTag(null);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void removeEmptyTag() throws BusinessException {
			assertThat(alertingDefinition.getTags().size(), equalTo(0));
			alertingDefinition.putTag("tagKey", "tagValue");
			assertThat(alertingDefinition.getTags().size(), equalTo(1));
			alertingDefinition.removeTag("");
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void removeUnknownTag() throws BusinessException {
			assertThat(alertingDefinition.getTags().size(), equalTo(0));
			alertingDefinition.putTag("tagKey", "tagValue");
			assertThat(alertingDefinition.getTags().size(), equalTo(1));
			alertingDefinition.removeTag("unknownKey");
		}

	}

	/**
	 * Test the {@link AlertingDefinition#replaceTags(Map)} method.
	 */
	public static class ReplaceTag extends AlertingDefinitionTest {
		@Test
		public void replaceTags() throws BusinessException {
			assertThat(alertingDefinition.getTags().size(), equalTo(0));
			alertingDefinition.putTag("tagKey", "tagValue");
			assertThat(alertingDefinition.getTags().size(), equalTo(1));
			alertingDefinition.replaceTags(tagMap);
			assertThat(alertingDefinition.getTags().size(), equalTo(3));

			assertThat(alertingDefinition.getTags().entrySet(), everyItem(isIn(tagMap.entrySet())));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void replaceNullTags() throws BusinessException {
			alertingDefinition.replaceTags(null);
		}

		@Test
		public void replaceBrokenTags() throws BusinessException {
			assertThat(alertingDefinition.getTags().size(), equalTo(0));
			alertingDefinition.putTag("tagKey1", "tagValue");
			alertingDefinition.putTag("tagKey2", "tagValue");
			assertThat(alertingDefinition.getTags().size(), equalTo(2));
			alertingDefinition.replaceTags(tagMapBroken);
			assertThat(alertingDefinition.getTags().size(), equalTo(1));
		}
	}

	/**
	 * Test the {@link AlertingDefinition#addNotificationEmailAddress(String)} method.
	 */
	public static class AddNotificationEmailAddress extends AlertingDefinitionTest {
		@Test
		public void putValidEmail() throws BusinessException {
			String mailAddress = "test@example.com";

			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(0));
			alertingDefinition.addNotificationEmailAddress(mailAddress);
			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(1));

			String returnedEmail = alertingDefinition.getNotificationEmailAddresses().get(0);
			assertThat(returnedEmail, equalTo(mailAddress));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void putDuplicateEmail() throws BusinessException {
			String mailAddress = "test@example.com";

			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(0));
			alertingDefinition.addNotificationEmailAddress(mailAddress);
			alertingDefinition.addNotificationEmailAddress(mailAddress);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void putEmptyEmail() throws BusinessException {
			alertingDefinition.addNotificationEmailAddress("");
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void putNullEmail() throws BusinessException {
			alertingDefinition.addNotificationEmailAddress(null);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void putInvalidEmail() throws BusinessException {
			alertingDefinition.addNotificationEmailAddress("not_an_email@");
		}
	}

	/**
	 * Test the {@link AlertingDefinition#removeNotificationEmailAddress(String)} method.
	 */
	public static class RemoveNotificationEmailAddress extends AlertingDefinitionTest {

		@Test
		public void removeEmail() throws BusinessException {
			String mailAddress = "test@example.com";

			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(0));
			alertingDefinition.addNotificationEmailAddress(mailAddress);
			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(1));

			boolean result = alertingDefinition.removeNotificationEmailAddress(mailAddress);

			assertThat(result, is(true));
			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(0));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void removeNullEmail() throws BusinessException {
			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(0));
			alertingDefinition.addNotificationEmailAddress("test@example.com");
			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(1));
			alertingDefinition.removeNotificationEmailAddress(null);
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void removeEmptyEmail() throws BusinessException {
			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(0));
			alertingDefinition.addNotificationEmailAddress("test@example.com");
			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(1));
			alertingDefinition.removeNotificationEmailAddress("");
		}

		@Test
		public void removeUnknownEmail() throws BusinessException {
			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(0));
			alertingDefinition.addNotificationEmailAddress("test@example.com");
			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(1));
			boolean result = alertingDefinition.removeNotificationEmailAddress("other@example.com");

			assertThat(result, is(false));
		}
	}

	/**
	 * Test the {@link AlertingDefinition#replaceNotificationEmailAddresses(List)} method.
	 */
	public static class ReplaceNotificationEmailAddress extends AlertingDefinitionTest {
		@Test
		public void replaceEmails() throws BusinessException {
			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(0));
			alertingDefinition.addNotificationEmailAddress("test@example.com");
			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(1));
			alertingDefinition.replaceNotificationEmailAddresses(emailList);
			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(3));

			assertThat(alertingDefinition.getNotificationEmailAddresses(), everyItem(isIn(emailList)));
		}

		@Test
		public void replaceBrokenEmails() throws BusinessException {
			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(0));
			alertingDefinition.addNotificationEmailAddress("test@example.com");
			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(1));
			alertingDefinition.replaceNotificationEmailAddresses(emailListBroken);
			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(2));
		}

		@Test(expectedExceptions = { BusinessException.class })
		public void replaceEmailsByNull() throws BusinessException {
			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(0));
			alertingDefinition.addNotificationEmailAddress("test@example.com");
			assertThat(alertingDefinition.getNotificationEmailAddresses(), hasSize(1));
			alertingDefinition.replaceNotificationEmailAddresses(null);
		}
	}

	/**
	 * Test the {@link AlertingDefinition#clone()} method.
	 */
	public static class Clone extends AlertingDefinitionTest {
		@Test
		public void testClone() throws BusinessException {
			AlertingDefinition alertingDefinition = new AlertingDefinition();
			alertingDefinition.setField("field");
			alertingDefinition.setMeasurement("measurement");
			alertingDefinition.setThreshold(1D);
			alertingDefinition.setThresholdType(ThresholdType.LOWER_THRESHOLD);
			alertingDefinition.setTimeRange(1L, TimeUnit.MINUTES);
			alertingDefinition.addNotificationEmailAddress("test@example.com");
			alertingDefinition.putTag("tagKey", "tagVal");

			AlertingDefinition clone = new AlertingDefinition(alertingDefinition);

			assertThat(alertingDefinition, not(sameInstance(clone)));
			assertThat(alertingDefinition, equalTo(clone));
		}

		@Test
		public void testCloneNull() throws BusinessException {
			AlertingDefinition clone = new AlertingDefinition(null);

			assertThat(clone, notNullValue());
		}
	}

	/**
	 * Test the {@link AlertingDefinition#equals(Object)} and {@link AlertingDefinition#hashCode()}
	 * method.
	 */
	public static class EqualsAndHashcode extends AlertingDefinitionTest {

		@Test
		public void testEqualsAndHashcode() throws BusinessException {
			EqualsVerifier.forClass(AlertingDefinition.class).verify();
		}
	}
}
