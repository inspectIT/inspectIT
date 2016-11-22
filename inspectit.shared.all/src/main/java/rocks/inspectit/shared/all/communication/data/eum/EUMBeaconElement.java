package rocks.inspectit.shared.all.communication.data.eum;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import rocks.inspectit.shared.all.communication.DefaultData;

/**
 * Basic interface for any kind of data sent in a {@link Beacon}. <br>
 * Note that data classes have to extend from {@link DefaultData}!
 *
 * @author Jonas Kunz
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = EUMSpan.class, visible = true)
@JsonSubTypes({ @Type(name = "metaInfo", value = UserSessionInfo.class) })
public interface EUMBeaconElement {

	/**
	 * Called after all data of the beacon has been deserialized with Jackson. This method can then
	 * be used to perform post-processing, for example like resolving relative URLs.
	 *
	 * @param beacon
	 *            the beacon to which this element belongs
	 */
	void deserializationComplete(Beacon beacon);

	/**
	 * Every EUMBeaconElement has to extend from {@link DefaultData}. Therefore, implementation of
	 * this method just return "this".
	 *
	 * @return "this" typed as DefaultData
	 */
	DefaultData asDefaultData();

}
