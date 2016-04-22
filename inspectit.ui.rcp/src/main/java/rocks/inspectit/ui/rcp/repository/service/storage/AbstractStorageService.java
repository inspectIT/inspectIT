package rocks.inspectit.ui.rcp.repository.service.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.storage.serializer.SerializationException;
import rocks.inspectit.shared.cs.indexing.aggregation.IAggregator;
import rocks.inspectit.shared.cs.indexing.aggregation.impl.AggregationPerformer;
import rocks.inspectit.shared.cs.indexing.storage.IStorageDescriptor;
import rocks.inspectit.shared.cs.indexing.storage.IStorageTreeComponent;
import rocks.inspectit.shared.cs.indexing.storage.impl.StorageIndexQuery;
import rocks.inspectit.shared.cs.storage.LocalStorageData;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.shared.cs.storage.StorageManager;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.StorageRepositoryDefinition;
import rocks.inspectit.ui.rcp.storage.util.DataRetriever;

/**
 * Abstract class for all storage services.
 *
 * @author Ivan Senic
 *
 * @param <E>
 *            Type of data provided by the service.
 */
public abstract class AbstractStorageService<E extends DefaultData> {

	/**
	 * Default amount of data that will be requested by one HTTP request. 10MB.
	 */
	private static final int MAX_QUERY_SIZE = 1024 * 1024 * 10;

	/**
	 * Storage repository definition.
	 */
	private StorageRepositoryDefinition storageRepositoryDefinition;

	/**
	 * {@link LocalStorageData}.
	 */
	private LocalStorageData localStorageData;

	/**
	 * {@link DataRetriever}.
	 */
	private DataRetriever dataRetriever;

	/**
	 * {@link StorageManager}.
	 */
	private StorageManager storageManager;

	/**
	 * Returns the indexing tree that can be used for querying.
	 *
	 * @return Returns the indexing tree that can be used for querying.
	 */
	protected abstract IStorageTreeComponent<E> getIndexingTree();

	/**
	 * Executes the query on the indexing tree.
	 *
	 * @param storageIndexQuery
	 *            Index query to execute.
	 *
	 * @return Result list.
	 */
	protected List<E> executeQuery(StorageIndexQuery storageIndexQuery) {
		return this.executeQuery(storageIndexQuery, null, null, -1);
	}

	/**
	 * Executes the query on the indexing tree. If the {@link IAggregator} is not <code>null</code>
	 * then the results will be aggregated based on the given {@link IAggregator}.
	 *
	 * @param storageIndexQuery
	 *            Index query to execute.
	 * @param aggregator
	 *            {@link IAggregator}. Pass <code>null</code> if no aggregation is needed.
	 * @return Result list.
	 */
	protected List<E> executeQuery(StorageIndexQuery storageIndexQuery, IAggregator<E> aggregator) {
		return this.executeQuery(storageIndexQuery, aggregator, null, -1);
	}

	/**
	 * Executes the query on the indexing tree. Results can be sorted by comparator.
	 *
	 * @param storageIndexQuery
	 *            Index query to execute.
	 * @param comparator
	 *            If supplied the final result list will be sorted by this comparator.
	 * @return Result list.
	 */
	protected List<E> executeQuery(StorageIndexQuery storageIndexQuery, Comparator<? super E> comparator) {
		return this.executeQuery(storageIndexQuery, null, comparator, -1);
	}

	/**
	 * Executes the query on the indexing tree. Furthermore the result list can be limited.
	 *
	 * @param storageIndexQuery
	 *            Index query to execute.
	 * @param limit
	 *            Limit the number of results by given number. Value <code>-1</code> means no limit.
	 * @return Result list.
	 */
	protected List<E> executeQuery(StorageIndexQuery storageIndexQuery, int limit) {
		return this.executeQuery(storageIndexQuery, null, null, limit);
	}

	/**
	 * Executes the query on the indexing tree. If the {@link IAggregator} is not <code>null</code>
	 * then the results will be aggregated based on the given {@link IAggregator}.
	 *
	 * @param storageIndexQuery
	 *            Index query to execute.
	 * @param aggregator
	 *            {@link IAggregator}. Pass <code>null</code> if no aggregation is needed.
	 * @param comparator
	 *            If supplied the final result list will be sorted by this comparator.
	 * @return Result list.
	 */
	protected List<E> executeQuery(StorageIndexQuery storageIndexQuery, IAggregator<E> aggregator, Comparator<? super E> comparator) {
		return this.executeQuery(storageIndexQuery, aggregator, comparator, -1);
	}

	/**
	 * Executes the query on the indexing tree. If the {@link IAggregator} is not <code>null</code>
	 * then the results will be aggregated based on the given {@link IAggregator}. Furthermore the
	 * result list can be limited.
	 *
	 * @param storageIndexQuery
	 *            Index query to execute.
	 * @param aggregator
	 *            {@link IAggregator}. Pass <code>null</code> if no aggregation is needed.
	 * @param limit
	 *            Limit the number of results by given number. Value <code>-1</code> means no limit.
	 * @return Result list.
	 */
	protected List<E> executeQuery(StorageIndexQuery storageIndexQuery, IAggregator<E> aggregator, int limit) {
		return this.executeQuery(storageIndexQuery, aggregator, null, limit);
	}

	/**
	 * Executes the query on the indexing tree. Results can be sorted by comparator. Furthermore the
	 * result list can be limited.
	 *
	 * @param storageIndexQuery
	 *            Index query to execute.
	 *
	 * @param comparator
	 *            If supplied the final result list will be sorted by this comparator.
	 * @param limit
	 *            Limit the number of results by given number. Value <code>-1</code> means no limit.
	 * @return Result list.
	 */
	protected List<E> executeQuery(StorageIndexQuery storageIndexQuery, Comparator<? super E> comparator, int limit) {
		return this.executeQuery(storageIndexQuery, null, comparator, limit);
	}

	/**
	 * This method executes the query in way that it first checks if wanted data is already cached.
	 * If not method has the ability to load the data via the HTTP or locally and aggregate the data
	 * if the {@link IAggregator} is provided. If the {@link IAggregator} is not provided, the data
	 * will be returned not aggregated.
	 * <P>
	 * In addition it will try to cache the results if they are not yet cached.
	 * <P>
	 * This method should be used by all subclasses, because it guards against massive data loading
	 * that can make out of memory exceptions on the UI.
	 *
	 * @param storageIndexQuery
	 *            Query.
	 * @param aggregator
	 *            {@link IAggregator}
	 * @param comparator
	 *            If supplied the final result list will be sorted by this comparator.
	 * @param limit
	 *            Limit the number of results by given number. Value <code>-1</code> means no limit.
	 * @return Return results of a query.
	 */
	protected List<E> executeQuery(StorageIndexQuery storageIndexQuery, IAggregator<E> aggregator, Comparator<? super E> comparator, int limit) {
		List<E> returnList = null;
		// check if this can be cached
		if (storageManager.canBeCached(storageIndexQuery, aggregator)) {
			int hash = storageManager.getCachedDataHash(storageIndexQuery, aggregator);
			if (!localStorageData.isFullyDownloaded()) {
				// check if it s cached on the CMR
				StorageData storageData = new StorageData(localStorageData);
				try {
					returnList = dataRetriever.getCachedDataViaHttp(getCmrRepositoryDefinition(), storageData, hash);
				} catch (BusinessException | IOException | SerializationException e) { // NOPMD //
																						// NOCHK
					// ignore cause we can still load results in other way
				}

				if (null == returnList) {
					// if not we load data regular way
					returnList = loadData(storageIndexQuery, aggregator);

					// and cache it on the CMR if we get something
					if (CollectionUtils.isNotEmpty(returnList)) {
						cacheQueryResultOnCmr(getCmrRepositoryDefinition(), storageData, returnList, hash);
					}
				}
			} else {
				try {
					returnList = dataRetriever.getCachedDataLocally(localStorageData, hash);
				} catch (IOException | SerializationException e) { // NOPMD NOCHK
					// ignore cause we can still load results in other way
				}

				if (null == returnList) {
					// if not we load data regular way
					returnList = loadData(storageIndexQuery, aggregator);

					// and cache it locally if we get something
					if (CollectionUtils.isNotEmpty(returnList)) {
						cacheQueryResultLocally(localStorageData, returnList, hash);
					}
				}
			}
		} else {
			returnList = loadData(storageIndexQuery, aggregator);
		}

		// sort if needed
		if (null != comparator) {
			Collections.sort(returnList, comparator);
		}

		// limit the size if needed
		if ((limit > -1) && (returnList.size() > limit)) {
			returnList = returnList.subList(0, limit);
		}

		return returnList;
	}

	/**
	 * Caches result set on the CMR for the given storage under given hash.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition} to cache results on.
	 * @param storageData
	 *            {@link StorageData}
	 * @param results
	 *            Results to cache
	 * @param hash
	 *            Hash to use
	 */
	private void cacheQueryResultOnCmr(CmrRepositoryDefinition cmrRepositoryDefinition, StorageData storageData, List<E> results, int hash) {
		try {
			cmrRepositoryDefinition.getStorageService().cacheStorageData(storageData, results, hash);
		} catch (BusinessException e) { // NOPMD NOCHK
			// ignore also if caching fails
		}
	}

	/**
	 * Caches result locally for the given storage under given hash.
	 *
	 * @param localStorageData
	 *            {@link LocalStorageData}
	 * @param results
	 *            Results to cache
	 * @param hash
	 *            Hash to use
	 */
	private void cacheQueryResultLocally(LocalStorageData localStorageData, List<E> results, int hash) {
		try {
			storageManager.cacheStorageData(localStorageData, results, hash);
		} catch (IOException | SerializationException e) { // NOPMD NOCHK
			// ignore also if caching fails
		}

	}

	/**
	 * This method has the ability to load the data via the HTTP and aggregate the data if the
	 * {@link IAggregator} is provided. If the {@link IAggregator} is not provided, the data will be
	 * returned not aggregated.
	 * <P>
	 * This method should be used by all subclasses, because it guards against massive data loading
	 * that can make out of memory exceptions on the UI.
	 *
	 * @param storageIndexQuery
	 *            Query.
	 * @param aggregator
	 *            {@link IAggregator}
	 * @return Return results of a query.
	 */
	private List<E> loadData(StorageIndexQuery storageIndexQuery, IAggregator<E> aggregator) {
		List<IStorageDescriptor> descriptors = getIndexingTree().query(storageIndexQuery);
		// sort the descriptors to optimize the number of read operations
		Collections.sort(descriptors, new Comparator<IStorageDescriptor>() {
			@Override
			public int compare(IStorageDescriptor o1, IStorageDescriptor o2) {
				int channelCompare = Integer.compare(o1.getChannelId(), o2.getChannelId());
				if (channelCompare != 0) {
					return channelCompare;
				} else {
					return Long.compare(o1.getPosition(), o2.getPosition());
				}
			}
		});

		AggregationPerformer<E> aggregationPerformer = null;
		if (null != aggregator) {
			aggregationPerformer = new AggregationPerformer<>(aggregator);
		}
		List<E> returnList = new ArrayList<>();

		int size = 0;
		int count = 0;
		List<IStorageDescriptor> limitedDescriptors = new ArrayList<>();
		for (IStorageDescriptor storageDescriptor : descriptors) {
			// increase count, add descriptor size and update current list
			count++;
			size += storageDescriptor.getSize();
			limitedDescriptors.add(storageDescriptor);

			// if the size is already to big, or we reached end do query
			if ((size > MAX_QUERY_SIZE) || (count == descriptors.size())) {
				// load data and filter with restrictions
				List<E> allData;
				if (localStorageData.isFullyDownloaded()) {
					try {
						allData = dataRetriever.getDataLocally(localStorageData, descriptors);
					} catch (SerializationException e) {
						String msg = "Data in the downloaded storage " + localStorageData + " can not be loaded with this version of the inspectIT. Version of the CMR where storage was created is "
								+ localStorageData.getCmrVersion() + ".";
						InspectIT.getDefault().createErrorDialog(msg, e, -1);
						return Collections.emptyList();
					} catch (IOException e) {
						InspectIT.getDefault().createErrorDialog("Exception occurred trying to load the data.", e, -1);
						return Collections.emptyList();
					}
				} else {
					try {
						allData = dataRetriever.getDataViaHttp(getCmrRepositoryDefinition(), localStorageData, limitedDescriptors);
					} catch (SerializationException e) {
						String msg = "Data in the remote storage " + localStorageData + " can not be loaded with this version of the inspectIT. Version of the CMR where storage was created is "
								+ localStorageData.getCmrVersion() + ".";
						InspectIT.getDefault().createErrorDialog(msg, e, -1);
						return Collections.emptyList();
					} catch (IOException e) {
						InspectIT.getDefault().createErrorDialog("Exception occurred trying to load the data.", e, -1);
						return Collections.emptyList();
					}
				}
				List<E> passedData = getRestrictionsPassedList(allData, storageIndexQuery);

				// if we need to aggregate then do so, otherwise just add to result list
				if (null != aggregationPerformer) {
					aggregationPerformer.processCollection(passedData);
				} else {
					returnList.addAll(passedData);
				}

				// reset the size and current list
				size = 0;
				limitedDescriptors.clear();
			}
		}

		// aggregate if needed
		if (null != aggregator) {
			returnList = aggregationPerformer.getResultList();
		}

		return returnList;
	}

	/**
	 * This utility method is used to create a list of elements that pass all the restrictions in
	 * the {@link StorageIndexQuery}.
	 *
	 * @param notPassedList
	 *            List of all elements.
	 * @param storageIndexQuery
	 *            {@link StorageIndexQuery}.
	 * @return New list only with elements that are passing all restrictions.
	 */
	private List<E> getRestrictionsPassedList(List<E> notPassedList, StorageIndexQuery storageIndexQuery) {
		List<E> passedList = new ArrayList<>();
		for (E element : notPassedList) {
			if ((null != element) && element.isQueryComplied(storageIndexQuery)) {
				passedList.add(element);
			}
		}
		return passedList;
	}

	/**
	 * Gets {@link #storageRepositoryDefinition}.
	 *
	 * @return {@link #storageRepositoryDefinition}
	 */
	public StorageRepositoryDefinition getStorageRepositoryDefinition() {
		return storageRepositoryDefinition;
	}

	/**
	 * Sets {@link #storageRepositoryDefinition}.
	 *
	 * @param storageRepositoryDefinition
	 *            New value for {@link #storageRepositoryDefinition}
	 */
	public void setStorageRepositoryDefinition(StorageRepositoryDefinition storageRepositoryDefinition) {
		this.storageRepositoryDefinition = storageRepositoryDefinition;
	}

	/**
	 * @return the cmrRepositoryDefinition
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return getStorageRepositoryDefinition().getCmrRepositoryDefinition();
	}

	/**
	 * Gets {@link #localStorageData}.
	 *
	 * @return {@link #localStorageData}
	 */
	public LocalStorageData getLocalStorageData() {
		return localStorageData;
	}

	/**
	 * Sets {@link #localStorageData}.
	 *
	 * @param localStorageData
	 *            New value for {@link #localStorageData}
	 */
	public void setLocalStorageData(LocalStorageData localStorageData) {
		this.localStorageData = localStorageData;
	}

	/**
	 * @param dataRetriever
	 *            the httpDataRetriever to set
	 */
	public void setDataRetriever(DataRetriever dataRetriever) {
		this.dataRetriever = dataRetriever;
	}

	/**
	 * Sets {@link #storageManager}.
	 *
	 * @param storageManager
	 *            New value for {@link #storageManager}
	 */
	public void setStorageManager(StorageManager storageManager) {
		this.storageManager = storageManager;
	}

}
