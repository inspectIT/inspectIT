package info.novatec.inspectit.cmr.dao;

import info.novatec.inspectit.communication.data.InvocationSequenceData;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * This layer is used to access the stored invocations.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface InvocationDataDao {

	/**
	 * Returns a list of {@link InvocationSequenceData} objects which contain no associations to
	 * other objects. Thus this list can be used to get an overview of the available invocation
	 * sequences. The limit defines the size of the list.
	 * 
	 * @param platformId
	 *            The ID of the platform.
	 * @param methodId
	 *            The ID of the method.
	 * @param limit
	 *            The limit/size of the list.
	 * @param comparator
	 *            Comparator to compare results with. If <code>null</code> is passed default
	 *            comparator will be used (in this case Timestamp comparator).
	 * 
	 * @return Returns the list of invocation sequences.
	 */
	List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, Comparator<? super InvocationSequenceData> comparator);

	/**
	 * Returns a list of {@link InvocationSequenceData} objects which contain no associations to
	 * other objects. Thus this list can be used to get an overview of the available invocation
	 * sequences. The limit defines the size of the list.
	 * <p>
	 * Compared to the above method, this service method returns all invocations for a specific
	 * agent, not only the invocations for specific methods.
	 * 
	 * @param platformId
	 *            The ID of the platform.
	 * @param limit
	 *            The limit/size of the list.
	 * @param comparator
	 *            Comparator to compare results with. If <code>null</code> is passed default
	 *            comparator will be used (in this case Timestamp comparator).
	 * 
	 * @return Returns the list of invocation sequences.
	 */
	List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, Comparator<? super InvocationSequenceData> comparator);

	/**
	 * Returns a list of {@link InvocationSequenceData} objects which contain no associations to
	 * other objects in given time frame. Thus this list can be used to get an overview of the
	 * available invocation sequences. The limit defines the size of the list.
	 * 
	 * @param platformId
	 *            The ID of the platform.
	 * @param methodId
	 *            The ID of the method.
	 * @param limit
	 *            The limit/size of the list.
	 * @param fromDate
	 *            Date include invocation from.
	 * @param toDate
	 *            Date include invocation to.
	 * @param comparator
	 *            Comparator to compare results with. If <code>null</code> is passed default
	 *            comparator will be used (in this case Timestamp comparator).
	 * 
	 * @return Returns the list of invocation sequences.
	 */
	List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, Date fromDate, Date toDate, Comparator<? super InvocationSequenceData> comparator);

	/**
	 * Returns a list of {@link InvocationSequenceData} objects which contain no associations to
	 * other objects in given time frame. Thus this list can be used to get an overview of the
	 * available invocation sequences. The limit defines the size of the list.
	 * <p>
	 * Compared to the above method, this service method returns all invocations for a specific
	 * agent, not only the invocations for specific methods.
	 * 
	 * @param platformId
	 *            The ID of the platform.
	 * @param limit
	 *            The limit/size of the list.
	 * @param fromDate
	 *            Date include invocation from.
	 * @param toDate
	 *            Date include invocation to.
	 * @param comparator
	 *            Comparator to compare results with. If <code>null</code> is passed default
	 *            comparator will be used (in this case Timestamp comparator).
	 * 
	 * @return Returns the list of invocation sequences.
	 */
	List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, Date fromDate, Date toDate, Comparator<? super InvocationSequenceData> comparator);

	/**
	 * Returns a list of {@link InvocationSequenceData} objects which contain no associations to
	 * other objects. Thus this list can be used to get an overview of the available invocation
	 * sequences. The limit defines the size of the list.
	 * <p>
	 * Compared with the method above, this service method returns only the invocations which ID is
	 * in invocation ID collection supplied.
	 * 
	 * @param platformId
	 *            Platform ID where to look for the objects. If the zero value is passed, looking
	 *            for the object will be done in all platforms.
	 * @param invocationIdCollection
	 *            Collections of invocations IDs to search.
	 * @param limit
	 *            The limit/size of the list.
	 * @param comparator
	 *            Comparator to compare results with. If <code>null</code> is passed default
	 *            comparator will be used (in this case Timestamp comparator).
	 * @return Returns the list of invocation sequences.
	 */
	List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, Collection<Long> invocationIdCollection, int limit, Comparator<? super InvocationSequenceData> comparator);

	/**
	 * This service method is used to get all the details of a specific invocation sequence.
	 * 
	 * @param template
	 *            The template data object.
	 * @return The detailed invocation sequence object.
	 */
	InvocationSequenceData getInvocationSequenceDetail(InvocationSequenceData template);

}
