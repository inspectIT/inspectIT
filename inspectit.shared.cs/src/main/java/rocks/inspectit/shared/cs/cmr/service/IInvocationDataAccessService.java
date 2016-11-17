package rocks.inspectit.shared.cs.cmr.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import rocks.inspectit.shared.all.cmr.service.ServiceExporterType;
import rocks.inspectit.shared.all.cmr.service.ServiceInterface;
import rocks.inspectit.shared.all.communication.comparator.ResultComparator;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.exception.BusinessException;

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
	 *            The limit/size of the list. Value <code>-1</code> means no limit.
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
	 *            The limit/size of the list. Value <code>-1</code> means no limit.
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
	 *            The limit/size of the list. Value <code>-1</code> means no limit.
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
	 *            The limit/size of the list. Value <code>-1</code> means no limit.
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
	 *            The limit/size of the list. Value <code>-1</code> means no limit.
	 * @param resultComparator
	 *            Comparator that will be used to sort the results. Can be <code>null</code> and in
	 *            that case no sorting will be done.
	 * @return Returns the list of invocation sequences.
	 */
	List<InvocationSequenceData> getInvocationSequenceOverview(long platformId, Collection<Long> invocationIdCollection, int limit, ResultComparator<InvocationSequenceData> resultComparator);

	/**
	 * Returns a list of {@link InvocationSequenceData} objects which contain no associations to
	 * other objects in given time frame. Thus this list can be used to get an overview of the
	 * available invocation sequences. The limit defines the size of the list.
	 *
	 * @param platformId
	 *            Platform ID where to look for the objects. If the zero value is passed, looking
	 *            for the object will be done in all platforms.
	 * @param limit
	 *            The limit/size of the list. Value <code>-1</code> means no limit.
	 * @param startDate
	 *            Date include invocation from.
	 * @param endDate
	 *            Date include invocation to.
	 * @param minId
	 *            Only invocations with equal or higher id are submitted.
	 * @param businessTrxId
	 *            Business transaction ID. If the zero value is passed, looking for the objects will
	 *            be done on all business transactions.
	 * @param applicationId
	 *            Application ID. If the zero value is passed, looking for the objects will be done
	 *            on all applications.
	 * @param resultComparator
	 *            Comparator that will be used to sort the results. Can be <code>null</code> and in
	 *            that case no sorting will be done.
	 * @return Returns the list of invocation sequences.
	 * @throws BusinessException
	 *             If data cannot be retrieved.
	 */
	List<InvocationSequenceData> getInvocationSequenceOverview(Long platformId, int limit, Date startDate, Date endDate, Long minId, int businessTrxId, int applicationId, String alertId, // NOCHK
			ResultComparator<InvocationSequenceData> resultComparator) throws BusinessException;

	/**
	 * Returns a list of {@link InvocationSequenceData} objects which contain no associations to
	 * other objects in given time frame. Thus this list can be used to get an overview of the
	 * available invocation sequences. The limit defines the size of the list.
	 *
	 * @param platformId
	 *            Platform ID where to look for the objects. If the zero value is passed, looking
	 *            for the object will be done in all platforms.
	 * @param limit
	 *            The limit/size of the list. Value <code>-1</code> means no limit.
	 * @param startDate
	 *            Date include invocation from.
	 * @param endDate
	 *            Date include invocation to.
	 * @param minId
	 *            Only invocations with equal or higher id are submitted.
	 * @param businessTrxId
	 *            Business transaction ID. If the zero value is passed, looking for the objects will
	 *            be done on all business transactions.
	 * @param applicationId
	 *            Application ID. If the zero value is passed, looking for the objects will be done
	 *            on all applications.
	 * @param invocationIdCollection
	 *            Collections of invocations IDs to search.
	 * @param resultComparator
	 *            Comparator that will be used to sort the results. Can be <code>null</code> and in
	 *            that case no sorting will be done.
	 * @return Returns the list of invocation sequences.
	 * @throws BusinessException
	 *             If data cannot be retrieved.
	 */
	List<InvocationSequenceData> getInvocationSequenceOverview(Long platformId, int limit, Date startDate, Date endDate, Long minId, int businessTrxId, int applicationId, // NOCHK
			Collection<Long> invocationIdCollection, ResultComparator<InvocationSequenceData> resultComparator) throws BusinessException;

	/**
	 * This service method is used to get all the details of a specific invocation sequence.
	 *
	 * @param template
	 *            The template data object.
	 * @return The detailed invocation sequence object.
	 */
	InvocationSequenceData getInvocationSequenceDetail(InvocationSequenceData template);

	/**
	 * Returns a list of {@link InvocationSequenceData} objects belonging to an alert defined by the
	 * passed alert id. The {@link InvocationSequenceData} objects in this list contain no
	 * associations to other objects. Thus this list can be used to get an overview of the available
	 * invocation sequences. The limit defines the size of the list.
	 *
	 * @param alertId
	 *            The ID of the alert the invocation sequences belong to.
	 * @param limit
	 *            The limit/size of the list. Value <code>-1</code> means no limit.
	 * @param resultComparator
	 *            Comparator that will be used to sort the results. Can be <code>null</code> and in
	 *            that case no sorting will be done.
	 * @return Returns the list of invocation sequences.
	 * @throws BusinessException
	 *             If data cannot be retrieved.
	 */
	List<InvocationSequenceData> getInvocationSequenceOverview(String alertId, int limit, ResultComparator<InvocationSequenceData> resultComparator) throws BusinessException;
}
