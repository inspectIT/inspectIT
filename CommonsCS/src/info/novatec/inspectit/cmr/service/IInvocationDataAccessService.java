package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.communication.comparator.ResultComparator;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Service interface which defines the methods to retrieve data objects based on the invocation
 * recordings.
 * 
 * @author Patrice Bouillet
 * 
 */
@ServiceInterface(exporter = ServiceExporterType.HTTP)
public interface IInvocationDataAccessService {

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
	 * @param resultComparator
	 *            Comparator that will be used to sort the results. Can be <code>null</code> and in
	 *            that case no sorting will be done.
	 * @return Returns the list of invocation sequences.
	 */
	List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, ResultComparator<InvocationSequenceData> resultComparator);

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
	 * @param resultComparator
	 *            Comparator that will be used to sort the results. Can be <code>null</code> and in
	 *            that case no sorting will be done.
	 * @return Returns the list of invocation sequences.
	 */
	List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, ResultComparator<InvocationSequenceData> resultComparator);

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
	 * @param resultComparator
	 *            Comparator that will be used to sort the results. Can be <code>null</code> and in
	 *            that case no sorting will be done.
	 * @return Returns the list of invocation sequences.
	 */
	List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, long methodId, int limit, Date fromDate, Date toDate, ResultComparator<InvocationSequenceData> resultComparator);

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
	 * @param resultComparator
	 *            Comparator that will be used to sort the results. Can be <code>null</code> and in
	 *            that case no sorting will be done.
	 * @return Returns the list of invocation sequences.
	 */
	List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, int limit, Date fromDate, Date toDate, ResultComparator<InvocationSequenceData> resultComparator);

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
	 * @param resultComparator
	 *            Comparator that will be used to sort the results. Can be <code>null</code> and in
	 *            that case no sorting will be done.
	 * @return Returns the list of invocation sequences.
	 */
	List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, Collection<Long> invocationIdCollection, int limit, ResultComparator<InvocationSequenceData> resultComparator);

	/**
	 * This service method is used to get all the details of a specific invocation sequence.
	 * 
	 * @param template
	 *            The template data object.
	 * @return The detailed invocation sequence object.
	 */
	InvocationSequenceData getInvocationSequenceDetail(InvocationSequenceData template);

}
