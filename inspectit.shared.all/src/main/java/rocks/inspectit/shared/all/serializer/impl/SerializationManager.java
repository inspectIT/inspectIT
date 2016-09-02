package rocks.inspectit.shared.all.serializer.impl;

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
import org.slf4j.Logger;
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
import rocks.inspectit.shared.all.cmr.model.JmxDefinitionDataIdent;
import rocks.inspectit.shared.all.cmr.model.JmxSensorTypeIdent;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.model.MethodIdentToSensorType;
import rocks.inspectit.shared.all.cmr.model.MethodSensorTypeIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformSensorTypeIdent;
import rocks.inspectit.shared.all.cmr.model.SensorTypeIdent;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.ExceptionEvent;
import rocks.inspectit.shared.all.communication.data.AggregatedExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.AggregatedHttpTimerData;
import rocks.inspectit.shared.all.communication.data.AggregatedSqlStatementData;
import rocks.inspectit.shared.all.communication.data.AggregatedTimerData;
import rocks.inspectit.shared.all.communication.data.ClassLoadingInformationData;
import rocks.inspectit.shared.all.communication.data.CompilationInformationData;
import rocks.inspectit.shared.all.communication.data.CpuInformationData;
import rocks.inspectit.shared.all.communication.data.DatabaseAggregatedTimerData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.HttpInfo;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationAwareData.MutableInt;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.JmxSensorValueData;
import rocks.inspectit.shared.all.communication.data.LoggingData;
import rocks.inspectit.shared.all.communication.data.MemoryInformationData;
import rocks.inspectit.shared.all.communication.data.ParameterContentData;
import rocks.inspectit.shared.all.communication.data.ParameterContentType;
import rocks.inspectit.shared.all.communication.data.RuntimeInformationData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;
import rocks.inspectit.shared.all.communication.data.ThreadInformationData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.communication.data.VmArgumentData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData.AgentConnection;
import rocks.inspectit.shared.all.communication.data.cmr.ApplicationData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.all.communication.data.cmr.CmrStatusData;
import rocks.inspectit.shared.all.communication.data.eum.AjaxRequest;
import rocks.inspectit.shared.all.communication.data.eum.ClickAction;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadAction;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.ResourceLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.UserAction;
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.exception.RemoteException;
import rocks.inspectit.shared.all.exception.TechnicalException;
import rocks.inspectit.shared.all.exception.enumeration.AgentManagementErrorCodeEnum;
import rocks.inspectit.shared.all.exception.enumeration.AlertErrorCodeEnum;
import rocks.inspectit.shared.all.exception.enumeration.BusinessContextErrorCodeEnum;
import rocks.inspectit.shared.all.exception.enumeration.ConfigurationInterfaceErrorCodeEnum;
import rocks.inspectit.shared.all.exception.enumeration.StorageErrorCodeEnum;
import rocks.inspectit.shared.all.instrumentation.classcache.AnnotationType;
import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.InterfaceType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.classcache.util.ArraySet;
import rocks.inspectit.shared.all.instrumentation.classcache.util.MethodTypeSet;
import rocks.inspectit.shared.all.instrumentation.classcache.util.SortedArraySet;
import rocks.inspectit.shared.all.instrumentation.classcache.util.TypeSet;
import rocks.inspectit.shared.all.instrumentation.classcache.util.TypeWithAnnotationsSet;
import rocks.inspectit.shared.all.instrumentation.config.PriorityEnum;
import rocks.inspectit.shared.all.instrumentation.config.SpecialInstrumentationType;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentEndUserMonitoringConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.ExceptionSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxAttributeDescriptor;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.PlatformSensorTypeConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.PropertyPath;
import rocks.inspectit.shared.all.instrumentation.config.impl.PropertyPathStart;
import rocks.inspectit.shared.all.instrumentation.config.impl.SensorInstrumentationPoint;
import rocks.inspectit.shared.all.instrumentation.config.impl.SpecialInstrumentationPoint;
import rocks.inspectit.shared.all.instrumentation.config.impl.SubstitutionDescriptor;
import rocks.inspectit.shared.all.pattern.EqualsMatchPattern;
import rocks.inspectit.shared.all.pattern.WildcardMatchPattern;
import rocks.inspectit.shared.all.serializer.HibernateAwareClassResolver;
import rocks.inspectit.shared.all.serializer.IKryoProvider;
import rocks.inspectit.shared.all.serializer.ISerializer;
import rocks.inspectit.shared.all.serializer.SerializationException;
import rocks.inspectit.shared.all.serializer.schema.ClassSchemaManager;
import rocks.inspectit.shared.all.spring.logger.Log;
import rocks.inspectit.shared.all.tracing.data.ClientSpan;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;
import rocks.inspectit.shared.all.util.IHibernateUtil;
import rocks.inspectit.shared.all.util.KryoNetNetwork;
import rocks.inspectit.shared.all.util.TimeFrame;

/**
 * Implementation of the {@link ISerializer} that uses Kryo library for serializing the objects.
 * <br>
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
	 * Logger of this class.
	 */
	@Log
	Logger logger;

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
	public void initKryo() {
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

		// ATTENTION: registration ID has been increased by 9 as there were 9 classes removed from
		// registration at this point with ticket INSPECTIT-2276
		// there is no other way then using reflection here
		int nextRegistrationId = kryo.getNextRegistrationId();
		nextRegistrationId += 9;

		// added with INSPECTIT-950
		kryo.register(TimeFrame.class, new CustomCompatibleFieldSerializer<TimeFrame>(kryo, TimeFrame.class, schemaManager), nextRegistrationId++);

		// added with INSPECTIT-480
		// needed for KryoNet
		nextRegistrationId = kryoNetNetwork.register(kryo, nextRegistrationId);

		// added with INSPECTIT-632
		kryo.register(BusinessException.class, new FieldSerializer<BusinessException>(kryo, BusinessException.class), nextRegistrationId++);
		kryo.register(TechnicalException.class, new FieldSerializer<TechnicalException>(kryo, TechnicalException.class), nextRegistrationId++);
		kryo.register(RemoteException.class, new FieldSerializer<RemoteException>(kryo, RemoteException.class), nextRegistrationId++);
		kryo.register(StorageErrorCodeEnum.class, new EnumSerializer(StorageErrorCodeEnum.class), nextRegistrationId++);
		kryo.register(AgentManagementErrorCodeEnum.class, new EnumSerializer(AgentManagementErrorCodeEnum.class), nextRegistrationId++);
		kryo.register(InvocationTargetException.class, new FieldSerializer<InvocationTargetException>(kryo, InvocationTargetException.class) {
			@Override
			protected InvocationTargetException create(Kryo kryo, Input input, Class<InvocationTargetException> type) {
				return new InvocationTargetException(null);
			}
		}, nextRegistrationId++);

		// added with INSPECTIT-1924
		kryo.register(ConfigurationInterfaceErrorCodeEnum.class, new EnumSerializer(ConfigurationInterfaceErrorCodeEnum.class), nextRegistrationId++);

		// added with INSPECTIT-1971
		kryo.register(LoggingData.class, new InvocationAwareDataSerializer<LoggingData>(kryo, LoggingData.class, schemaManager), nextRegistrationId++);

		// added with INSPECTIT-1915
		kryo.register(JmxSensorTypeIdent.class, new CustomCompatibleFieldSerializer<JmxSensorTypeIdent>(kryo, JmxSensorTypeIdent.class, schemaManager, true), nextRegistrationId++);
		kryo.register(JmxDefinitionDataIdent.class, new CustomCompatibleFieldSerializer<JmxDefinitionDataIdent>(kryo, JmxDefinitionDataIdent.class, schemaManager), nextRegistrationId++);
		kryo.register(JmxSensorValueData.class, new CustomCompatibleFieldSerializer<JmxSensorValueData>(kryo, JmxSensorValueData.class, schemaManager), nextRegistrationId++);

		// added with INSPECTIT-1849
		kryo.register(HttpInfo.class, new CustomCompatibleFieldSerializer<HttpInfo>(kryo, HttpInfo.class, schemaManager), nextRegistrationId++);

		// added with INSPECTIT-1919
		kryo.register(AgentConfig.class, new FieldSerializer<AgentConfig>(kryo, AgentConfig.class), nextRegistrationId++);
		nextRegistrationId++; // removed Sensor config class
		kryo.register(PlatformSensorTypeConfig.class, new FieldSerializer<PlatformSensorTypeConfig>(kryo, PlatformSensorTypeConfig.class), nextRegistrationId++);
		kryo.register(MethodSensorTypeConfig.class, new FieldSerializer<MethodSensorTypeConfig>(kryo, MethodSensorTypeConfig.class), nextRegistrationId++);
		kryo.register(ExceptionSensorTypeConfig.class, new FieldSerializer<ExceptionSensorTypeConfig>(kryo, ExceptionSensorTypeConfig.class), nextRegistrationId++);
		kryo.register(PropertyPath.class, new FieldSerializer<PropertyPath>(kryo, PropertyPath.class), nextRegistrationId++);
		kryo.register(PropertyPathStart.class, new FieldSerializer<PropertyPathStart>(kryo, PropertyPathStart.class), nextRegistrationId++);
		kryo.register(InstrumentationDefinition.class, new FieldSerializer<InstrumentationDefinition>(kryo, InstrumentationDefinition.class), nextRegistrationId++);
		kryo.register(MethodInstrumentationConfig.class, new FieldSerializer<MethodInstrumentationConfig>(kryo, MethodInstrumentationConfig.class), nextRegistrationId++);
		kryo.register(SensorInstrumentationPoint.class, new FieldSerializer<SensorInstrumentationPoint>(kryo, SensorInstrumentationPoint.class), nextRegistrationId++);
		kryo.register(SpecialInstrumentationPoint.class, new FieldSerializer<SpecialInstrumentationPoint>(kryo, SpecialInstrumentationPoint.class), nextRegistrationId++);
		kryo.register(SpecialInstrumentationType.class, new EnumSerializer(SpecialInstrumentationType.class), nextRegistrationId++);
		kryo.register(PriorityEnum.class, new EnumSerializer(PriorityEnum.class), nextRegistrationId++);
		kryo.register(EqualsMatchPattern.class, new FieldSerializer<EqualsMatchPattern>(kryo, EqualsMatchPattern.class), nextRegistrationId++);
		kryo.register(WildcardMatchPattern.class, new FieldSerializer<WildcardMatchPattern>(kryo, WildcardMatchPattern.class), nextRegistrationId++);
		// class cache structures
		kryo.register(ClassType.class, new FieldSerializer<ClassType>(kryo, ClassType.class), nextRegistrationId++);
		kryo.register(InterfaceType.class, new FieldSerializer<InterfaceType>(kryo, InterfaceType.class), nextRegistrationId++);
		kryo.register(AnnotationType.class, new FieldSerializer<AnnotationType>(kryo, AnnotationType.class), nextRegistrationId++);
		kryo.register(MethodType.class, new FieldSerializer<MethodType>(kryo, MethodType.class), nextRegistrationId++);
		kryo.register(ArraySet.class, new CollectionSerializer(), nextRegistrationId++);
		kryo.register(SortedArraySet.class, new CollectionSerializer(), nextRegistrationId++);
		kryo.register(MethodTypeSet.class, new CollectionSerializer(), nextRegistrationId++);
		kryo.register(TypeSet.class, new CollectionSerializer(), nextRegistrationId++);
		kryo.register(TypeWithAnnotationsSet.class, new CollectionSerializer(), nextRegistrationId++);

		// added with INSPECTIT-2071
		kryo.register(JmxAttributeDescriptor.class, new FieldSerializer<JmxAttributeDescriptor>(kryo, JmxAttributeDescriptor.class), nextRegistrationId++);
		kryo.register(JmxSensorTypeConfig.class, new FieldSerializer<JmxSensorTypeConfig>(kryo, JmxSensorTypeConfig.class), nextRegistrationId++);

		// added with INSPECTIT-1804, INSPECTIT-1807
		kryo.register(BusinessContextErrorCodeEnum.class, new EnumSerializer(BusinessContextErrorCodeEnum.class), nextRegistrationId++);
		kryo.register(ApplicationData.class, new CustomCompatibleFieldSerializer<ApplicationData>(kryo, ApplicationData.class, schemaManager), nextRegistrationId++);
		kryo.register(BusinessTransactionData.class, new CustomCompatibleFieldSerializer<BusinessTransactionData>(kryo, BusinessTransactionData.class, schemaManager), nextRegistrationId++);

		// added with INSPECTIT-1953
		kryo.register(AlertErrorCodeEnum.class, new EnumSerializer(AlertErrorCodeEnum.class), nextRegistrationId++);

		// added with INSPECTIT-2226
		kryo.register(SubstitutionDescriptor.class, new FieldSerializer<SubstitutionDescriptor>(kryo, SubstitutionDescriptor.class), nextRegistrationId++);

		// added with INSPECTIT-1921
		kryo.register(SpanIdent.class, new CustomCompatibleFieldSerializer<SpanIdent>(kryo, SpanIdent.class, schemaManager), nextRegistrationId++);
		kryo.register(ClientSpan.class, new CustomCompatibleFieldSerializer<ClientSpan>(kryo, ClientSpan.class, schemaManager, true), nextRegistrationId++);
		kryo.register(ServerSpan.class, new CustomCompatibleFieldSerializer<ServerSpan>(kryo, ServerSpan.class, schemaManager, true), nextRegistrationId++);

		// ADDED with INSPECT-1959
		// TODO: Use the CustomCompatibleFieldSerializer as soon as the data is stable
		kryo.register(AgentEndUserMonitoringConfig.class, new FieldSerializer<AgentEndUserMonitoringConfig>(kryo, AgentEndUserMonitoringConfig.class), nextRegistrationId++);
		kryo.register(AjaxRequest.class, new FieldSerializer<AjaxRequest>(kryo, AjaxRequest.class), nextRegistrationId++);
		kryo.register(ClickAction.class, new FieldSerializer<ClickAction>(kryo, ClickAction.class), nextRegistrationId++);
		kryo.register(PageLoadAction.class, new FieldSerializer<PageLoadAction>(kryo, PageLoadAction.class), nextRegistrationId++);
		kryo.register(PageLoadRequest.class, new FieldSerializer<PageLoadRequest>(kryo, PageLoadRequest.class), nextRegistrationId++);
		kryo.register(ResourceLoadRequest.class, new FieldSerializer<ResourceLoadRequest>(kryo, ResourceLoadRequest.class), nextRegistrationId++);
		kryo.register(UserAction.class, new FieldSerializer<UserAction>(kryo, UserAction.class), nextRegistrationId++);
		kryo.register(UserSessionInfo.class, new FieldSerializer<UserSessionInfo>(kryo, UserSessionInfo.class), nextRegistrationId++);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serialize(Object object, Output output) throws SerializationException {
		serialize(object, output, Collections.emptyMap());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
	@Override
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
	 * {@inheritDoc}
	 */
	@Override
	public <T> T copy(T object) {
		return kryo.copy(object);
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
	@Override
	public Kryo getKryo() {
		return kryo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
