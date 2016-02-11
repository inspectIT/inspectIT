package rocks.inspectit.shared.all.cmr.cache;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import rocks.inspectit.shared.all.communication.Sizeable;

/**
 * This interface defines necessary methods that are needed for object size calculations.
 * 
 * @author Ivan Senic
 * 
 */
public interface IObjectSizes {

	/**
	 * Returns the size of the reference in the underlying VM.
	 * 
	 * @return Size of the reference in bytes.
	 */
	long getReferenceSize();

	/**
	 * Returns a size of a header of every object. This value has to be added to all classes that
	 * are subclasses of {@link Object}.
	 * 
	 * @return Size of java object header in bytes.
	 */
	long getSizeOfObjectHeader();

	/**
	 * Returns the size of the sizable object.
	 * 
	 * @param sizeable
	 *            {@link Sizeable}.
	 * @return Size in bytes. If passed object is <code>null</code>, <code>0</code> will be returned
	 *         as result.
	 */
	long getSizeOf(Sizeable sizeable);

	/**
	 * Calculates the approximate size of the {@link String} object based on the number of string's
	 * length.
	 * 
	 * @param str
	 *            String which size has to be calculated.
	 * @return Size of {@link String} object in bytes, or 0 if passed string is null.
	 */
	long getSizeOf(String str);

	/**
	 * Calculates the approximate size of all {@link String} objects. Note that this method has
	 * protection about twice adding the size of the same string referenced with different
	 * references. Thus all the objects having more than one string should use this method for
	 * calculation.
	 * 
	 * @param strings
	 *            Strings which sizes have to be calculated.
	 * @return Size of many {@link String} object sin bytes.
	 */
	long getSizeOf(String... strings);

	/**
	 * Calculates the approximate size of the {@link Timestamp} object.
	 * 
	 * @param timestamp
	 *            Timestamp which size has to be calculated.
	 * @return Size of {@link Timestamp} object in bytes, or 0 if passed object is null.
	 */
	long getSizeOf(Timestamp timestamp);

	/**
	 * Calculates the approximate size of the {@link ArrayList} object. The calculation does not
	 * include the size of elements that are in the list. The calculation may not be correct for
	 * other list types. This method will use default array list capacity for calculation.
	 * 
	 * @param arrayList
	 *            ArrayList which size has to be calculated.
	 * @return Size of {@link ArrayList} object in bytes, or 0 if passed object is null.
	 */
	long getSizeOf(List<?> arrayList);

	/**
	 * Calculates the approximate size of the {@link ArrayList} object. The calculation does not
	 * include the size of elements that are in the list. The calculation may not be correct for
	 * other list types.
	 * 
	 * @param arrayList
	 *            ArrayList which size has to be calculated.
	 * @param initialCapacity
	 *            Capacity {@link ArrayList} was created with. If initial capacity is not know, use
	 *            the {@link #getSizeOf(List)}.
	 * @return Size of {@link ArrayList} object in bytes, or 0 if passed object is null.
	 */
	long getSizeOf(List<?> arrayList, int initialCapacity);

	/**
	 * Calculates the approximate size of the {@link HashSet} object. The calculation does not
	 * include the size of elements that are in the set. The calculation may not be correct for
	 * other set types. This method will use default hash map capacity for calculation.
	 * 
	 * @param hashSetSize
	 *            HashSet size that has to be calculated.
	 * @return Size of {@link HashSet} object in bytes.
	 */
	long getSizeOfHashSet(int hashSetSize);

	/**
	 * Calculates the approximate size of the {@link HashSet} object. The calculation does not
	 * include the size of elements that are in the set. The calculation may not be correct for
	 * other set types.
	 * 
	 * @param hashSetSize
	 *            HashSet size that has to be calculated.
	 * @param initialCapacity
	 *            Initial capacity {@link HashSet} has been created with. If initial capacity is not
	 *            know, use the {@link #getSizeOfHashSet(int)}.
	 * @return Size of {@link HashSet} object in bytes.
	 */
	long getSizeOfHashSet(int hashSetSize, int initialCapacity);

	/**
	 * Calculates the approximate size of the {@link HashMap} object. The calculation does not
	 * include the size of elements that are in the map. The calculation may not be correct for
	 * other map types. This method will use default hash map capacity for calculation.
	 * 
	 * @param hashMapSize
	 *            Size of hash map.
	 * @return Size of {@link HashMap} object in bytes.
	 */
	long getSizeOfHashMap(int hashMapSize);

	/**
	 * Calculates the approximate size of the {@link HashMap} object. The calculation does not
	 * include the size of elements that are in the map. The calculation may not be correct for
	 * other map types.
	 * 
	 * @param hashMapSize
	 *            Size of hash map.
	 * @param initialCapacity
	 *            Initial capacity {@link HashMap} has been created with. If initial capacity is not
	 *            know, use the {@link #getSizeOfHashMap(int)}.
	 * @return Size of {@link HashMap} object in bytes.
	 */
	long getSizeOfHashMap(int hashMapSize, int initialCapacity);
	
	/**
	 * Returns size of HashMap's inner Key or Entry set classes.
	 * 
	 * @return Returns size of HashMap's inner Key or Entry set classes.
	 */
	long getSizeOfHashMapKeyEntrySet();

	/**
	 * Calculates the approximate size of the ConcurrentHashMap object. The calculation does not
	 * include the size of elements that are in the map. The calculation may not be correct for
	 * other map types.
	 * 
	 * @param mapSize
	 *            Map size.
	 * @param concurrencyLevel
	 *            Concurrency level in the map.
	 * @return Size of ConcurrentHashMap object in bytes.
	 */
	long getSizeOfConcurrentHashMap(int mapSize, int concurrencyLevel);

	/**
	 * Calculates the approximate size of the
	 * {@link org.cliffc.high_scale_lib.NonBlockingHashMapLong} object. The calculation does not
	 * include the size of value elements that are in the map. Since keys in this map are
	 * represented as primitive long arrays, the keys are included in the calculation. The
	 * calculation may not be correct for other map types.
	 * 
	 * @param mapSize
	 *            Map size.
	 * @return Size of map object in bytes.
	 */
	long getSizeOfNonBlockingHashMapLong(int mapSize);

	/**
	 * Calculates size of the {@link Object} objects.
	 * 
	 * @return Size of the {@link Object} objects in bytes.
	 */
	long getSizeOfObjectObject();

	/**
	 * Calculates size of the {@link Long} objects.
	 * 
	 * @return Size of the {@link Long} objects in bytes.
	 */
	long getSizeOfLongObject();

	/**
	 * Calculates size of the {@link Integer} objects.
	 * 
	 * @return Size of the {@link Integer} objects in bytes.
	 */
	long getSizeOfIntegerObject();

	/**
	 * Calculates size of the {@link Short} objects.
	 * 
	 * @return Size of the {@link Short} objects in bytes.
	 */
	long getSizeOfShortObject();

	/**
	 * Calculates size of the {@link Character} objects.
	 * 
	 * @return Size of the {@link Character} objects in bytes.
	 */
	long getSizeOfCharacterObject();

	/**
	 * Calculates size of the {@link Boolean} objects.
	 * 
	 * @return Size of the {@link Boolean} objects in bytes.
	 */
	long getSizeOfBooleanObject();

	/**
	 * Returns the object size based on the number of given primitive fields in the object's class.
	 * 
	 * @param referenceCount
	 *            Number of references to objects.
	 * @param booleanCount
	 *            Number of boolean fields.
	 * @param intCount
	 *            Number of int fields.
	 * @param floatCount
	 *            Number of float fields.
	 * @param longCount
	 *            Number of long fields.
	 * @param doubleCount
	 *            Number of double fields.
	 * @return Exact object size in bytes.
	 */
	long getPrimitiveTypesSize(int referenceCount, int booleanCount, int intCount, int floatCount, int longCount, int doubleCount);

	/**
	 * Returns the aligned objects size, because the object size in memory is always a multiple of 8
	 * bytes.
	 * 
	 * @param size
	 *            Initial non-aligned object size.
	 * @return Aligned object size.
	 */
	long alignTo8Bytes(long size);

	/**
	 * Provides the rate in percentages for object size expansion for security regarding the memory.
	 * If the object size is need to expand for 20%, this method will return 0.2.
	 * 
	 * @return Security expansion rate in percentages.
	 */
	float getObjectSecurityExpansionRate();

	/**
	 * Sets the rate in percentages for object size expansion.
	 * 
	 * @param objectSecurityExpansionRate
	 *            Expansion rate. If the expansion rate should be 20%, the given value should be
	 *            0.2.
	 */
	void setObjectSecurityExpansionRate(float objectSecurityExpansionRate);

	/**
	 * Calculates the size of the array with out objects in the array - <b> Can only be used on
	 * non-primitive arrays </b>.
	 * 
	 * @param arraySize
	 *            Size of array (length).
	 * @return Size in bytes.
	 */
	long getSizeOfArray(int arraySize);

	/**
	 * Returns size of our own
	 * {@link rocks.inspectit.shared.all.indexing.buffer.impl.Leaf.CustomWeakReference} without the
	 * referred object.
	 * 
	 * @return Size in bytes.
	 */
	long getSizeOfCustomWeakReference();
	
	/**
	 * Calculates the size of the primitive array with the primitives in the array.
	 * 
	 * @param arraySize
	 *            Size of array (length).
	 * @param primitiveSize
	 *            Size in bytes of the primitive type in array
	 * @return Size in bytes.
	 */
	long getSizeOfPrimitiveArray(int arraySize, long primitiveSize);

}
