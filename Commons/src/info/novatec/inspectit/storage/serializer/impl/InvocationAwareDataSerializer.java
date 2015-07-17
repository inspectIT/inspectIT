package info.novatec.inspectit.storage.serializer.impl;

import info.novatec.inspectit.communication.data.InvocationAwareData;
import info.novatec.inspectit.communication.data.InvocationAwareData.MutableInt;
import info.novatec.inspectit.storage.serializer.schema.ClassSchemaManager;
import info.novatec.inspectit.storage.serializer.util.KryoSerializationPreferences;

import java.util.Collections;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.ObjectMap;

/**
 * Special serializer that can dismiss writing of the
 * {@link InvocationAwareData#invocationsParentsIdMap} if the kryo context is holding the
 * {@link KryoSerializationPreferences#WRITE_INVOCATION_AFFILIATION_DATA} key and false as a value.
 * 
 * @param <T>
 *            Type of class.
 * 
 * @author Ivan Senic
 * 
 */
public class InvocationAwareDataSerializer<T extends InvocationAwareData> extends CustomCompatibleFieldSerializer<T> {

	/**
	 * Default constructor.
	 * 
	 * @param kryo
	 *            Kryo instance
	 * @param type
	 *            Class to be serialized
	 * @param schemaManager
	 *            {@link ClassSchemaManager} holding information about values.
	 */
	public InvocationAwareDataSerializer(Kryo kryo, Class<T> type, ClassSchemaManager schemaManager) {
		super(kryo, type, schemaManager);
	}

	/**
	 * Default constructor.
	 * 
	 * @param kryo
	 *            Kryo instance
	 * @param type
	 *            Class to be serialized
	 * @param schemaManager
	 *            {@link ClassSchemaManager} holding information about values.
	 * @param useSuperclassSchema
	 *            If the superclass schema should be used if the one for the class is not available.
	 */
	public InvocationAwareDataSerializer(Kryo kryo, Class<T> type, ClassSchemaManager schemaManager, boolean useSuperclassSchema) {
		super(kryo, type, schemaManager, useSuperclassSchema);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void write(Kryo kryo, Output output, T object) {
		ObjectMap<Object, Object> garphContext = kryo.getGraphContext();
		if (Boolean.FALSE.equals(garphContext.get(KryoSerializationPreferences.WRITE_INVOCATION_AFFILIATION_DATA))) {
			Map<Long, MutableInt> temp = object.getInvocationsParentsIdMap();
			object.setInvocationsParentsIdMap(Collections.<Long, MutableInt> emptyMap());
			super.write(kryo, output, object);
			object.setInvocationsParentsIdMap(temp);
		} else {
			super.write(kryo, output, object);
		}
	}
}
