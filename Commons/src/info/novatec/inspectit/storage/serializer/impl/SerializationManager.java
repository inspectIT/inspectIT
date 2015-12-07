package info.novatec.inspectit.storage.serializer.impl;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodIdentToSensorType;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.model.PlatformSensorTypeIdent;
import info.novatec.inspectit.cmr.model.SensorTypeIdent;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.ExceptionEvent;
import info.novatec.inspectit.communication.comparator.AggregatedExceptionSensorDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.DefaultDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.ExceptionSensorDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.HttpTimerDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.InvocationAwareDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.MethodSensorDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.ResultComparator;
import info.novatec.inspectit.communication.comparator.SqlStatementDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.TimerDataComparatorEnum;
import info.novatec.inspectit.communication.data.AggregatedExceptionSensorData;
import info.novatec.inspectit.communication.data.AggregatedHttpTimerData;
import info.novatec.inspectit.communication.data.AggregatedSqlStatementData;
import info.novatec.inspectit.communication.data.AggregatedTimerData;
import info.novatec.inspectit.communication.data.ClassLoadingInformationData;
import info.novatec.inspectit.communication.data.CompilationInformationData;
import info.novatec.inspectit.communication.data.CpuInformationData;
import info.novatec.inspectit.communication.data.DatabaseAggregatedTimerData;
import info.novatec.inspectit.communication.data.ExceptionSensorData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationAwareData.MutableInt;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.MemoryInformationData;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.ParameterContentType;
import info.novatec.inspectit.communication.data.RuntimeInformationData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.communication.data.SystemInformationData;
import info.novatec.inspectit.communication.data.ThreadInformationData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.communication.data.VmArgumentData;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData.AgentConnection;
import info.novatec.inspectit.communication.data.cmr.CmrStatusData;
import info.novatec.inspectit.communication.data.cmr.Permission;
import info.novatec.inspectit.communication.data.cmr.Role;
import info.novatec.inspectit.communication.data.cmr.User;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.exception.RemoteException;
import info.novatec.inspectit.exception.TechnicalException;
import info.novatec.inspectit.exception.enumeration.AgentManagementErrorCodeEnum;
import info.novatec.inspectit.exception.enumeration.StorageErrorCodeEnum;
import info.novatec.inspectit.storage.serializer.HibernateAwareClassResolver;
import info.novatec.inspectit.storage.serializer.IKryoProvider;
import info.novatec.inspectit.storage.serializer.ISerializer;
import info.novatec.inspectit.storage.serializer.SerializationException;
import info.novatec.inspectit.storage.serializer.schema.ClassSchemaManager;
import info.novatec.inspectit.util.IHibernateUtil;
import info.novatec.inspectit.util.KryoNetNetwork;
import info.novatec.inspectit.util.TimeFrame;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.ClassResolver;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ReferenceResolver;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.LongArraySerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.ClassSerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.DateSerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.EnumSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import com.esotericsoftware.kryo.util.DefaultClassResolver;
import com.esotericsoftware.kryo.util.MapReferenceResolver;
import com.esotericsoftware.kryo.util.ObjectMap;

import de.javakaffee.kryoserializers.SynchronizedCollectionsSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;

/**
 * Implementation of the {@link ISerializer} that uses Kryo library for serializing the objects. <br>
 * <br>
 * <b>This class is not thread safe and should be used with special attention. The class can be used
 * only by one thread while the serialization/de-serialization process lasts.</b>
 * 
 * @author Ivan Senic
 * 
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Lazy
public class SerializationManager implements ISerializer, IKryoProvider, InitializingBean {

	/**
	 * Main {@link Kryo} instance.
	 */
	private Kryo kryo;

	/**
	 * {@link KryoNetNetwork} for registering needed classes for communication.
	 */
	@Autowired
	private KryoNetNetwork kryoNetNetwork;

	/**
	 * Schema manager that holds all schemas for the {@link DefaultData} objects to be serialized.
	 */
	@Autowired
	private ClassSchemaManager schemaManager;

	/**
	 * {@link IHibernateUtil} if needed for Hibernate persistent collections/maps solving.
	 */
	@Autowired(required = false)
	IHibernateUtil hibernateUtil;

	/**
	 * Initialize {@link Kryo} properties.
	 */
	protected void initKryo() {
		// if hibernateUtil is provided, we create special kind of class resolver
		ClassResolver classResolver;
		if (null != hibernateUtil) {
			classResolver = new HibernateAwareClassResolver(hibernateUtil);
		} else {
			classResolver = new DefaultClassResolver();
		}

		// we disable references for DefaultData objects because they are not needed
		// invocations will be handled manually
		ReferenceResolver referenceResolver = new MapReferenceResolver() {
			@SuppressWarnings("rawtypes")
			@Override
			public boolean useReferences(Class paramClass) {
				if (DefaultData.class.isAssignableFrom(paramClass)) {
					return false;
				} else {
					return super.useReferences(paramClass);
				}
			}
		};
		kryo = new Kryo(classResolver, referenceResolver);
		kryo.setRegistrationRequired(false);
		registerClasses(kryo);
	}

	/**
	 * Registers all necessary classes to the {@link Kryo} instance;
	 * 
	 * ATTENTION!
	 * 
	 * Please do not change the order of the registered classes. If new classes need to be
	 * registered, please add this registration at the end. Otherwise the old data will not be able
	 * to be de-serialized. If some class is not need to be register any more, do not remove the
	 * registration. If the class is not available any more, add arbitrary class to its position, so
	 * that the order can be maintained. Do not add unnecessary classes to the registration list.
	 * 
	 * NOTE: By default, all primitives (including wrappers) and java.lang.String are registered.
	 * Any other class, including JDK classes like ArrayList and even arrays such as String[] or
	 * int[] must be registered.
	 * 
	 * NOTE: If it is known up front what classes need to be serialized, registering the classes is
	 * ideal. However, in some cases the classes to serialize are not known until it is time to
	 * perform the serialization. When setRegistrationOptional is true, registered classes are still
	 * written as an integer. However, unregistered classes are written as a String, using the name
	 * of the class. This is much less efficient, but can't always be avoided.
	 * 
	 * @param kryo
	 *            Kryo that needs to be prepared.
	 */
	private void registerClasses(Kryo kryo) {
		/** Java native classes */
		kryo.register(Class.class, new ClassSerializer());
		kryo.register(ArrayList.class, new HibernateAwareCollectionSerializer(hibernateUtil)); // NOPMD
		kryo.register(CopyOnWriteArrayList.class, new CollectionSerializer());
		kryo.register(HashSet.class, new HibernateAwareCollectionSerializer(hibernateUtil)); // NOPMD
		kryo.register(HashMap.class, new HibernateAwareMapSerializer(hibernateUtil)); // NOPMD
		kryo.register(ConcurrentHashMap.class, new MapSerializer());
		kryo.register(Timestamp.class, new TimestampSerializer());
		kryo.register(Date.class, new DateSerializer());
		kryo.register(AtomicLong.class, new FieldSerializer<AtomicLong>(kryo, AtomicLong.class));
		/** Arrays */
		kryo.register(long[].class, new LongArraySerializer());
		/** inspectIT model classes */
		kryo.register(PlatformIdent.class, new CustomCompatibleFieldSerializer<PlatformIdent>(kryo, PlatformIdent.class, schemaManager));
		kryo.register(MethodIdent.class, new CustomCompatibleFieldSerializer<MethodIdent>(kryo, MethodIdent.class, schemaManager));
		kryo.register(SensorTypeIdent.class, new CustomCompatibleFieldSerializer<SensorTypeIdent>(kryo, SensorTypeIdent.class, schemaManager));
		kryo.register(MethodSensorTypeIdent.class, new CustomCompatibleFieldSerializer<MethodSensorTypeIdent>(kryo, MethodSensorTypeIdent.class, schemaManager));
		kryo.register(PlatformSensorTypeIdent.class, new CustomCompatibleFieldSerializer<PlatformSensorTypeIdent>(kryo, PlatformSensorTypeIdent.class, schemaManager, true));
		/** Common data classes */
		kryo.register(MutableInt.class, new FieldSerializer<MutableInt>(kryo, MutableInt.class));
		kryo.register(InvocationSequenceData.class, new InvocationSequenceCustomCompatibleFieldSerializer(kryo, InvocationSequenceData.class, schemaManager));
		// TODO Check if we want for these
		kryo.register(TimerData.class, new InvocationAwareDataSerializer<TimerData>(kryo, TimerData.class, schemaManager));
		kryo.register(HttpTimerData.class, new InvocationAwareDataSerializer<HttpTimerData>(kryo, HttpTimerData.class, schemaManager));
		kryo.register(SqlStatementData.class, new InvocationAwareDataSerializer<SqlStatementData>(kryo, SqlStatementData.class, schemaManager));
		kryo.register(ExceptionSensorData.class, new InvocationAwareDataSerializer<ExceptionSensorData>(kryo, ExceptionSensorData.class, schemaManager));
		kryo.register(ExceptionEvent.class, new EnumSerializer(ExceptionEvent.class));
		kryo.register(ParameterContentData.class, new CustomCompatibleFieldSerializer<ParameterContentData>(kryo, ParameterContentData.class, schemaManager));
		kryo.register(MemoryInformationData.class, new CustomCompatibleFieldSerializer<MemoryInformationData>(kryo, MemoryInformationData.class, schemaManager));
		kryo.register(CpuInformationData.class, new CustomCompatibleFieldSerializer<CpuInformationData>(kryo, CpuInformationData.class, schemaManager));
		kryo.register(SystemInformationData.class, new CustomCompatibleFieldSerializer<SystemInformationData>(kryo, SystemInformationData.class, schemaManager));
		kryo.register(VmArgumentData.class, new CustomCompatibleFieldSerializer<VmArgumentData>(kryo, VmArgumentData.class, schemaManager));
		kryo.register(ThreadInformationData.class, new CustomCompatibleFieldSerializer<ThreadInformationData>(kryo, ThreadInformationData.class, schemaManager));
		kryo.register(RuntimeInformationData.class, new CustomCompatibleFieldSerializer<RuntimeInformationData>(kryo, RuntimeInformationData.class, schemaManager));
		kryo.register(CompilationInformationData.class, new CustomCompatibleFieldSerializer<CompilationInformationData>(kryo, CompilationInformationData.class, schemaManager));
		kryo.register(ClassLoadingInformationData.class, new CustomCompatibleFieldSerializer<ClassLoadingInformationData>(kryo, ClassLoadingInformationData.class, schemaManager));
		kryo.register(ParameterContentType.class, new EnumSerializer(ParameterContentType.class));

		// aggregation classes
		kryo.register(AggregatedExceptionSensorData.class, new InvocationAwareDataSerializer<AggregatedExceptionSensorData>(kryo, AggregatedExceptionSensorData.class, schemaManager));
		kryo.register(DatabaseAggregatedTimerData.class, new InvocationAwareDataSerializer<DatabaseAggregatedTimerData>(kryo, DatabaseAggregatedTimerData.class, schemaManager, true));

		// classes needed for the HTTP calls from the UI
		kryo.register(RemoteInvocation.class, new FieldSerializer<RemoteInvocation>(kryo, RemoteInvocation.class));
		kryo.register(RemoteInvocationResult.class, new FieldSerializer<RemoteInvocationResult>(kryo, RemoteInvocationResult.class) {
			@Override
			protected RemoteInvocationResult create(Kryo kryo, Input input, Class<RemoteInvocationResult> type) {
				return new RemoteInvocationResult(null);
			}
		});

		// data classes between CMR and UI
		// this classes can be registered with FieldSerializer since they are not saved to disk
		kryo.register(CmrStatusData.class, new FieldSerializer<CmrStatusData>(kryo, CmrStatusData.class));
		kryo.register(AgentStatusData.class, new FieldSerializer<AgentStatusData>(kryo, AgentStatusData.class));
		kryo.register(AgentConnection.class, new EnumSerializer(AgentConnection.class));

		// INSPECTIT-849 - Hibernate uses Arrays.asList which does not have no-arg constructor
		kryo.register(Arrays.asList().getClass(), new CollectionSerializer() {
			@Override
			@SuppressWarnings("rawtypes")
			protected Collection create(Kryo kryo, Input input, Class<Collection> type) {
				return new ArrayList<Object>();
			}
		});

		// INSPECTIT-846
		kryo.register(AggregatedHttpTimerData.class, new InvocationAwareDataSerializer<AggregatedHttpTimerData>(kryo, AggregatedHttpTimerData.class, schemaManager));
		kryo.register(AggregatedSqlStatementData.class, new InvocationAwareDataSerializer<AggregatedSqlStatementData>(kryo, AggregatedSqlStatementData.class, schemaManager));
		kryo.register(AggregatedTimerData.class, new InvocationAwareDataSerializer<AggregatedTimerData>(kryo, AggregatedTimerData.class, schemaManager));

		// added with INSPECTIT-853
		kryo.register(MethodIdentToSensorType.class, new CustomCompatibleFieldSerializer<MethodIdentToSensorType>(kryo, MethodIdentToSensorType.class, schemaManager));

		// added with INSPECTIT-912
		UnmodifiableCollectionsSerializer.registerSerializers(kryo);
		SynchronizedCollectionsSerializer.registerSerializers(kryo);
		kryo.register(StackTraceElement.class, new StackTraceElementSerializer());

		// added with INSPECTIT-887
		kryo.register(DefaultDataComparatorEnum.class, new EnumSerializer(DefaultDataComparatorEnum.class));
		kryo.register(MethodSensorDataComparatorEnum.class, new EnumSerializer(MethodSensorDataComparatorEnum.class));
		kryo.register(InvocationAwareDataComparatorEnum.class, new EnumSerializer(InvocationAwareDataComparatorEnum.class));
		kryo.register(TimerDataComparatorEnum.class, new EnumSerializer(TimerDataComparatorEnum.class));
		kryo.register(HttpTimerDataComparatorEnum.class, new EnumSerializer(HttpTimerDataComparatorEnum.class));
		kryo.register(SqlStatementDataComparatorEnum.class, new EnumSerializer(SqlStatementDataComparatorEnum.class));
		kryo.register(ExceptionSensorDataComparatorEnum.class, new EnumSerializer(ExceptionSensorDataComparatorEnum.class));
		kryo.register(AggregatedExceptionSensorDataComparatorEnum.class, new EnumSerializer(AggregatedExceptionSensorDataComparatorEnum.class));
		kryo.register(InvocationAwareDataComparatorEnum.class, new EnumSerializer(InvocationAwareDataComparatorEnum.class));
		kryo.register(ResultComparator.class, new FieldSerializer<ResultComparator<?>>(kryo, ResultComparator.class));

		// added with INSPECTIT-950
		kryo.register(TimeFrame.class, new CustomCompatibleFieldSerializer<TimeFrame>(kryo, TimeFrame.class, schemaManager));

		// added with INSPECTIT-480
		// needed for KryoNet
		kryoNetNetwork.register(kryo);

		// added with INSPECTIT-632
		kryo.register(BusinessException.class, new FieldSerializer<BusinessException>(kryo, BusinessException.class));
		kryo.register(TechnicalException.class, new FieldSerializer<TechnicalException>(kryo, TechnicalException.class));
		kryo.register(RemoteException.class, new FieldSerializer<RemoteException>(kryo, RemoteException.class));
		kryo.register(StorageErrorCodeEnum.class, new EnumSerializer(StorageErrorCodeEnum.class));
		kryo.register(AgentManagementErrorCodeEnum.class, new EnumSerializer(AgentManagementErrorCodeEnum.class));
		kryo.register(InvocationTargetException.class, new FieldSerializer<InvocationTargetException>(kryo, InvocationTargetException.class) {
			@Override
			protected InvocationTargetException create(Kryo kryo, Input input, Class<InvocationTargetException> type) {
				return new InvocationTargetException(null);
			}
		});
		
		// added with NTDT <3
		kryo.register(Permission.class, new FieldSerializer<Permission>(kryo, Permission.class));
		kryo.register(Role.class, new FieldSerializer<Role>(kryo, Role.class));
		kryo.register(User.class, new FieldSerializer<User>(kryo, User.class));
	}

	/**
	 * {@inheritDoc}
	 */
	public void serialize(Object object, Output output) throws SerializationException {
		serialize(object, output, Collections.emptyMap());
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void serialize(Object object, Output output, Map<?, ?> kryoPreferences) throws SerializationException {
		if (MapUtils.isNotEmpty(kryoPreferences)) {
			ObjectMap<Object, Object> graphContext = kryo.getGraphContext();
			for (Entry<?, ?> entry : kryoPreferences.entrySet()) {
				graphContext.put(entry.getKey(), entry.getValue());
			}
		}
		try {
			kryo.writeClassAndObject(output, object);
			output.flush();
		} catch (Exception exception) {
			throw new SerializationException("Serialization failed.\n" + exception.getMessage(), exception);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object deserialize(Input input) throws SerializationException {
		Object object = null;
		try {
			object = kryo.readClassAndObject(input);
		} catch (Exception exception) {
			throw new SerializationException("De-serialization failed.\n" + exception.getMessage(), exception);
		}
		return object;
	}

	/**
	 * Gets {@link #schemaManager}.
	 * 
	 * @return {@link #schemaManager}
	 */
	public ClassSchemaManager getSchemaManager() {
		return schemaManager;
	}

	/**
	 * <i>This setter can be removed when the Spring3.0 on the GUI side is working properly.</i>
	 * 
	 * @param schemaManager
	 *            the schemaManager to set
	 */
	public void setSchemaManager(ClassSchemaManager schemaManager) {
		this.schemaManager = schemaManager;
	}

	/**
	 * Sets {@link #kryoNetNetwork}.
	 * 
	 * @param kryoNetNetwork
	 *            New value for {@link #kryoNetNetwork}
	 */
	public void setKryoNetNetwork(KryoNetNetwork kryoNetNetwork) {
		this.kryoNetNetwork = kryoNetNetwork;
	}

	/**
	 * Gets {@link #kryo}.
	 * 
	 * @return {@link #kryo}
	 */
	public Kryo getKryo() {
		return kryo;
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		initKryo();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("schemaManager", schemaManager);
		toStringBuilder.append("kryo", ToStringBuilder.reflectionToString(kryo));
		return toStringBuilder.toString();
	}

}
