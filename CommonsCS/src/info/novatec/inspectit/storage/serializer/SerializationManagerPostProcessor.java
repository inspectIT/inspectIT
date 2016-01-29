package info.novatec.inspectit.storage.serializer;

import info.novatec.inspectit.ci.AgentMapping;
import info.novatec.inspectit.ci.AgentMappings;
import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.ci.assignment.impl.ExceptionSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.MethodSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.TimerMethodSensorAssignment;
import info.novatec.inspectit.ci.context.impl.FieldContextCapture;
import info.novatec.inspectit.ci.context.impl.ParameterContextCapture;
import info.novatec.inspectit.ci.context.impl.ReturnContextCapture;
import info.novatec.inspectit.ci.exclude.ExcludeRule;
import info.novatec.inspectit.ci.sensor.exception.impl.ExceptionSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.ConnectionMetaDataSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.ConnectionSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.HttpSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.InvocationSequenceSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.Log4jLoggingSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.PreparedStatementParameterSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.PreparedStatementSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.StatementSensorConfig;
import info.novatec.inspectit.ci.sensor.method.impl.TimerSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.impl.ClassLoadingSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.impl.CompilationSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.impl.CpuSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.impl.MemorySensorConfig;
import info.novatec.inspectit.ci.sensor.platform.impl.RuntimeSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.impl.SystemSensorConfig;
import info.novatec.inspectit.ci.sensor.platform.impl.ThreadSensorConfig;
import info.novatec.inspectit.ci.strategy.impl.ListSendingStrategyConfig;
import info.novatec.inspectit.ci.strategy.impl.SimpleBufferStrategyConfig;
import info.novatec.inspectit.ci.strategy.impl.SizeBufferStrategyConfig;
import info.novatec.inspectit.ci.strategy.impl.TimeSendingStrategyConfig;
import info.novatec.inspectit.cmr.property.configuration.Configuration;
import info.novatec.inspectit.cmr.property.configuration.GroupedProperty;
import info.novatec.inspectit.cmr.property.configuration.PropertySection;
import info.novatec.inspectit.cmr.property.configuration.impl.BooleanProperty;
import info.novatec.inspectit.cmr.property.configuration.impl.ByteProperty;
import info.novatec.inspectit.cmr.property.configuration.impl.LongProperty;
import info.novatec.inspectit.cmr.property.configuration.impl.PercentageProperty;
import info.novatec.inspectit.cmr.property.configuration.impl.StringProperty;
import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidation;
import info.novatec.inspectit.cmr.property.configuration.validation.PropertyValidationException;
import info.novatec.inspectit.cmr.property.configuration.validation.ValidationError;
import info.novatec.inspectit.cmr.property.configuration.validator.impl.FullyQualifiedClassNameValidator;
import info.novatec.inspectit.cmr.property.configuration.validator.impl.GreaterOrEqualValidator;
import info.novatec.inspectit.cmr.property.configuration.validator.impl.GreaterValidator;
import info.novatec.inspectit.cmr.property.configuration.validator.impl.LessOrEqualValidator;
import info.novatec.inspectit.cmr.property.configuration.validator.impl.LessValidator;
import info.novatec.inspectit.cmr.property.configuration.validator.impl.NegativeValidator;
import info.novatec.inspectit.cmr.property.configuration.validator.impl.NotEmptyValidator;
import info.novatec.inspectit.cmr.property.configuration.validator.impl.PercentageValidator;
import info.novatec.inspectit.cmr.property.configuration.validator.impl.PositiveValidator;
import info.novatec.inspectit.cmr.property.update.configuration.ConfigurationUpdate;
import info.novatec.inspectit.cmr.property.update.impl.BooleanPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.impl.BytePropertyUpdate;
import info.novatec.inspectit.cmr.property.update.impl.LongPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.impl.PercentagePropertyUpdate;
import info.novatec.inspectit.cmr.property.update.impl.RestoreDefaultPropertyUpdate;
import info.novatec.inspectit.cmr.property.update.impl.StringPropertyUpdate;
import info.novatec.inspectit.cmr.service.IServerStatusService.ServerStatus;
import info.novatec.inspectit.communication.data.cmr.RecordingData;
import info.novatec.inspectit.communication.data.cmr.WritingStatus;
import info.novatec.inspectit.indexing.aggregation.impl.ExceptionDataAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.HttpTimerDataAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.SqlStatementDataAggregator;
import info.novatec.inspectit.indexing.aggregation.impl.TimerDataAggregator;
import info.novatec.inspectit.indexing.indexer.impl.InvocationChildrenIndexer;
import info.novatec.inspectit.indexing.indexer.impl.MethodIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.ObjectTypeIndexer;
import info.novatec.inspectit.indexing.indexer.impl.PlatformIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.SensorTypeIdentIndexer;
import info.novatec.inspectit.indexing.indexer.impl.SqlStringIndexer;
import info.novatec.inspectit.indexing.indexer.impl.TimestampIndexer;
import info.novatec.inspectit.indexing.storage.impl.ArrayBasedStorageLeaf;
import info.novatec.inspectit.indexing.storage.impl.LeafWithNoDescriptors;
import info.novatec.inspectit.indexing.storage.impl.SimpleStorageDescriptor;
import info.novatec.inspectit.indexing.storage.impl.StorageBranch;
import info.novatec.inspectit.indexing.storage.impl.StorageBranchIndexer;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.StorageData.StorageState;
import info.novatec.inspectit.storage.label.BooleanStorageLabel;
import info.novatec.inspectit.storage.label.DateStorageLabel;
import info.novatec.inspectit.storage.label.NumberStorageLabel;
import info.novatec.inspectit.storage.label.ObjectStorageLabel;
import info.novatec.inspectit.storage.label.StringStorageLabel;
import info.novatec.inspectit.storage.label.management.impl.AddLabelManagementAction;
import info.novatec.inspectit.storage.label.management.impl.RemoveLabelManagementAction;
import info.novatec.inspectit.storage.label.type.impl.AssigneeLabelType;
import info.novatec.inspectit.storage.label.type.impl.CreationDateLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomBooleanLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomDateLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomNumberLabelType;
import info.novatec.inspectit.storage.label.type.impl.CustomStringLabelType;
import info.novatec.inspectit.storage.label.type.impl.DataTimeFrameLabelType;
import info.novatec.inspectit.storage.label.type.impl.ExploredByLabelType;
import info.novatec.inspectit.storage.label.type.impl.RatingLabelType;
import info.novatec.inspectit.storage.label.type.impl.StatusLabelType;
import info.novatec.inspectit.storage.label.type.impl.UseCaseLabelType;
import info.novatec.inspectit.storage.processor.impl.AgentFilterDataProcessor;
import info.novatec.inspectit.storage.processor.impl.DataAggregatorProcessor;
import info.novatec.inspectit.storage.processor.impl.DataSaverProcessor;
import info.novatec.inspectit.storage.processor.impl.InvocationClonerDataProcessor;
import info.novatec.inspectit.storage.processor.impl.InvocationExtractorDataProcessor;
import info.novatec.inspectit.storage.processor.impl.TimeFrameDataProcessor;
import info.novatec.inspectit.storage.recording.RecordingProperties;
import info.novatec.inspectit.storage.recording.RecordingState;
import info.novatec.inspectit.storage.serializer.impl.CustomCompatibleFieldSerializer;
import info.novatec.inspectit.storage.serializer.impl.SerializationManager;
import info.novatec.inspectit.storage.serializer.impl.ServerStatusSerializer;
import info.novatec.inspectit.storage.serializer.schema.ClassSchemaManager;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.ObjectArraySerializer;
import com.esotericsoftware.kryo.serializers.DefaultSerializers.EnumSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

/**
 * Registers all classes from the CommonsCS project after {@link SerializationManager} has been
 * created.
 *
 * @author Ivan Senic
 *
 */
@Component
public class SerializationManagerPostProcessor implements BeanPostProcessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof SerializationManager) {
			registerClasses((SerializationManager) bean);
		}
		return bean;
	}

	/**
	 * Registers all classes in the CommonsCS project that needed to be registered to any
	 * {@link SerializationManager} instance.
	 *
	 * @param serializationManager
	 *            {@link SerializationManager}.
	 */
	private void registerClasses(SerializationManager serializationManager) {
		/**
		 * To be able to keep the compatibility, we need to register classes with the same ID. Since
		 * the {@link SerializationManager} will perform registration of classes in the CommonsCS
		 * project, we need to make sure that the registration in this processor starts from the far
		 * away ID so that no overlapping can occur if the new classes are registered in the
		 * original {@link SerializationManager}.
		 */
		int nextRegistrationId = 512;

		Kryo kryo = serializationManager.getKryo();
		ClassSchemaManager schemaManager = serializationManager.getSchemaManager();

		/** Arrays */
		kryo.register(SimpleStorageDescriptor[].class, new ObjectArraySerializer(kryo, SimpleStorageDescriptor[].class), nextRegistrationId++);
		/** Storage classes */
		kryo.register(StorageBranch.class, new CustomCompatibleFieldSerializer<StorageBranch<?>>(kryo, StorageBranch.class, schemaManager), nextRegistrationId++);
		kryo.register(StorageBranchIndexer.class, new CustomCompatibleFieldSerializer<StorageBranchIndexer<?>>(kryo, StorageBranchIndexer.class, schemaManager), nextRegistrationId++);
		kryo.register(SimpleStorageDescriptor.class, new CustomCompatibleFieldSerializer<SimpleStorageDescriptor>(kryo, SimpleStorageDescriptor.class, schemaManager), nextRegistrationId++);
		// we must not copy transient fields of leaf serializer (read/write locks)
		CustomCompatibleFieldSerializer<ArrayBasedStorageLeaf<?>> leafSerializer = new CustomCompatibleFieldSerializer<ArrayBasedStorageLeaf<?>>(kryo, ArrayBasedStorageLeaf.class, schemaManager);
		leafSerializer.setCopyTransient(false);
		kryo.register(ArrayBasedStorageLeaf.class, leafSerializer, nextRegistrationId++);
		kryo.register(LeafWithNoDescriptors.class, new CustomCompatibleFieldSerializer<LeafWithNoDescriptors<?>>(kryo, LeafWithNoDescriptors.class, schemaManager), nextRegistrationId++);
		kryo.register(StorageData.class, new CustomCompatibleFieldSerializer<StorageData>(kryo, StorageData.class, schemaManager), nextRegistrationId++);
		kryo.register(LocalStorageData.class, new CustomCompatibleFieldSerializer<LocalStorageData>(kryo, LocalStorageData.class, schemaManager), nextRegistrationId++);
		kryo.register(StorageState.class, new EnumSerializer(StorageState.class));
		/** Storage labels */
		kryo.register(BooleanStorageLabel.class, new CustomCompatibleFieldSerializer<BooleanStorageLabel>(kryo, BooleanStorageLabel.class, schemaManager), nextRegistrationId++);
		kryo.register(DateStorageLabel.class, new CustomCompatibleFieldSerializer<DateStorageLabel>(kryo, DateStorageLabel.class, schemaManager), nextRegistrationId++);
		kryo.register(NumberStorageLabel.class, new CustomCompatibleFieldSerializer<NumberStorageLabel>(kryo, NumberStorageLabel.class, schemaManager), nextRegistrationId++);
		kryo.register(StringStorageLabel.class, new CustomCompatibleFieldSerializer<StringStorageLabel>(kryo, StringStorageLabel.class, schemaManager), nextRegistrationId++);
		/** Storage labels type */
		kryo.register(AssigneeLabelType.class, new CustomCompatibleFieldSerializer<AssigneeLabelType>(kryo, AssigneeLabelType.class, schemaManager, true), nextRegistrationId++);
		kryo.register(CreationDateLabelType.class, new CustomCompatibleFieldSerializer<CreationDateLabelType>(kryo, CreationDateLabelType.class, schemaManager, true), nextRegistrationId++);
		kryo.register(CustomBooleanLabelType.class, new CustomCompatibleFieldSerializer<CustomBooleanLabelType>(kryo, CustomBooleanLabelType.class, schemaManager, true), nextRegistrationId++);
		kryo.register(CustomDateLabelType.class, new CustomCompatibleFieldSerializer<CustomDateLabelType>(kryo, CustomDateLabelType.class, schemaManager, true), nextRegistrationId++);
		kryo.register(CustomNumberLabelType.class, new CustomCompatibleFieldSerializer<CustomNumberLabelType>(kryo, CustomNumberLabelType.class, schemaManager, true), nextRegistrationId++);
		kryo.register(CustomStringLabelType.class, new CustomCompatibleFieldSerializer<CustomStringLabelType>(kryo, CustomStringLabelType.class, schemaManager, true), nextRegistrationId++);
		kryo.register(ExploredByLabelType.class, new CustomCompatibleFieldSerializer<ExploredByLabelType>(kryo, ExploredByLabelType.class, schemaManager, true), nextRegistrationId++);
		kryo.register(RatingLabelType.class, new CustomCompatibleFieldSerializer<RatingLabelType>(kryo, RatingLabelType.class, schemaManager, true), nextRegistrationId++);
		kryo.register(StatusLabelType.class, new CustomCompatibleFieldSerializer<StatusLabelType>(kryo, StatusLabelType.class, schemaManager, true), nextRegistrationId++);
		kryo.register(UseCaseLabelType.class, new CustomCompatibleFieldSerializer<UseCaseLabelType>(kryo, UseCaseLabelType.class, schemaManager, true), nextRegistrationId++);
		/** Branch indexers */
		kryo.register(PlatformIdentIndexer.class, new FieldSerializer<PlatformIdentIndexer<?>>(kryo, PlatformIdentIndexer.class), nextRegistrationId++);
		kryo.register(ObjectTypeIndexer.class, new FieldSerializer<ObjectTypeIndexer<?>>(kryo, ObjectTypeIndexer.class), nextRegistrationId++);
		kryo.register(MethodIdentIndexer.class, new FieldSerializer<MethodIdentIndexer<?>>(kryo, MethodIdentIndexer.class), nextRegistrationId++);
		kryo.register(SensorTypeIdentIndexer.class, new FieldSerializer<SensorTypeIdentIndexer<?>>(kryo, SensorTypeIdentIndexer.class), nextRegistrationId++);
		kryo.register(TimestampIndexer.class, new CustomCompatibleFieldSerializer<TimestampIndexer<?>>(kryo, TimestampIndexer.class, schemaManager), nextRegistrationId++);
		kryo.register(InvocationChildrenIndexer.class, new FieldSerializer<InvocationChildrenIndexer<?>>(kryo, InvocationChildrenIndexer.class), nextRegistrationId++);
		kryo.register(SqlStringIndexer.class, new FieldSerializer<SqlStringIndexer<?>>(kryo, SqlStringIndexer.class), nextRegistrationId++);

		// data classes between CMR and UI
		// this classes can be registered with FieldSerializer since they are not saved to disk
		kryo.register(RecordingData.class, new FieldSerializer<RecordingData>(kryo, RecordingData.class), nextRegistrationId++);
		kryo.register(WritingStatus.class, new EnumSerializer(WritingStatus.class), nextRegistrationId++);
		kryo.register(AddLabelManagementAction.class, new FieldSerializer<AddLabelManagementAction>(kryo, AddLabelManagementAction.class), nextRegistrationId++);
		kryo.register(RemoveLabelManagementAction.class, new FieldSerializer<RemoveLabelManagementAction>(kryo, RemoveLabelManagementAction.class), nextRegistrationId++);
		kryo.register(DataAggregatorProcessor.class, new FieldSerializer<DataAggregatorProcessor<?>>(kryo, DataAggregatorProcessor.class), nextRegistrationId++);
		kryo.register(DataSaverProcessor.class, new FieldSerializer<DataSaverProcessor>(kryo, DataSaverProcessor.class), nextRegistrationId++);
		kryo.register(InvocationClonerDataProcessor.class, new FieldSerializer<InvocationClonerDataProcessor>(kryo, InvocationClonerDataProcessor.class), nextRegistrationId++);
		kryo.register(InvocationExtractorDataProcessor.class, new FieldSerializer<InvocationExtractorDataProcessor>(kryo, InvocationExtractorDataProcessor.class), nextRegistrationId++);
		kryo.register(TimeFrameDataProcessor.class, new FieldSerializer<TimeFrameDataProcessor>(kryo, TimeFrameDataProcessor.class), nextRegistrationId++);
		kryo.register(TimerDataAggregator.class, new FieldSerializer<TimerDataAggregator>(kryo, TimerDataAggregator.class), nextRegistrationId++);
		kryo.register(SqlStatementDataAggregator.class, new FieldSerializer<SqlStatementDataAggregator>(kryo, SqlStatementDataAggregator.class), nextRegistrationId++);
		kryo.register(HttpTimerDataAggregator.class, new FieldSerializer<HttpTimerDataAggregator>(kryo, HttpTimerDataAggregator.class), nextRegistrationId++);
		kryo.register(ExceptionDataAggregator.class, new FieldSerializer<ExceptionDataAggregator>(kryo, ExceptionDataAggregator.class), nextRegistrationId++);

		// added with INSPECTIT-723
		kryo.register(RecordingState.class, new EnumSerializer(RecordingState.class), nextRegistrationId++);
		kryo.register(RecordingProperties.class, new FieldSerializer<RecordingProperties>(kryo, RecordingProperties.class), nextRegistrationId++);

		// added with INSPECTIT-937
		kryo.register(AgentFilterDataProcessor.class, new FieldSerializer<AgentFilterDataProcessor>(kryo, AgentFilterDataProcessor.class), nextRegistrationId++);

		// added with INSPECTIT-950
		kryo.register(ObjectStorageLabel.class, new CustomCompatibleFieldSerializer<ObjectStorageLabel<?>>(kryo, ObjectStorageLabel.class, schemaManager), nextRegistrationId++);
		kryo.register(DataTimeFrameLabelType.class, new CustomCompatibleFieldSerializer<DataTimeFrameLabelType>(kryo, DataTimeFrameLabelType.class, schemaManager, true), nextRegistrationId++);

		// added with INSPECTIT-991
		kryo.register(ServerStatus.class, new ServerStatusSerializer(), nextRegistrationId++);

		// added with INSPECTIT-963
		// CMR Configuration Properties classes
		// this classes can be registered with FieldSerializer since they are not saved to disk
		kryo.register(GroupedProperty.class, new FieldSerializer<GroupedProperty>(kryo, GroupedProperty.class), nextRegistrationId++);
		kryo.register(BooleanProperty.class, new FieldSerializer<BooleanProperty>(kryo, BooleanProperty.class), nextRegistrationId++);
		kryo.register(BooleanPropertyUpdate.class, new FieldSerializer<BooleanPropertyUpdate>(kryo, BooleanPropertyUpdate.class), nextRegistrationId++);
		kryo.register(LongProperty.class, new FieldSerializer<LongProperty>(kryo, LongProperty.class), nextRegistrationId++);
		kryo.register(LongPropertyUpdate.class, new FieldSerializer<LongPropertyUpdate>(kryo, LongPropertyUpdate.class), nextRegistrationId++);
		kryo.register(PercentageProperty.class, new FieldSerializer<PercentageProperty>(kryo, PercentageProperty.class), nextRegistrationId++);
		kryo.register(PercentagePropertyUpdate.class, new FieldSerializer<PercentagePropertyUpdate>(kryo, PercentagePropertyUpdate.class), nextRegistrationId++);
		kryo.register(ByteProperty.class, new FieldSerializer<ByteProperty>(kryo, ByteProperty.class), nextRegistrationId++);
		kryo.register(BytePropertyUpdate.class, new FieldSerializer<BytePropertyUpdate>(kryo, BytePropertyUpdate.class), nextRegistrationId++);
		kryo.register(StringProperty.class, new FieldSerializer<StringProperty>(kryo, StringProperty.class), nextRegistrationId++);
		kryo.register(StringPropertyUpdate.class, new FieldSerializer<StringPropertyUpdate>(kryo, StringPropertyUpdate.class), nextRegistrationId++);
		kryo.register(RestoreDefaultPropertyUpdate.class, new FieldSerializer<RestoreDefaultPropertyUpdate<?>>(kryo, RestoreDefaultPropertyUpdate.class), nextRegistrationId++);
		kryo.register(Configuration.class, new FieldSerializer<Configuration>(kryo, Configuration.class), nextRegistrationId++);
		kryo.register(ConfigurationUpdate.class, new FieldSerializer<ConfigurationUpdate>(kryo, ConfigurationUpdate.class), nextRegistrationId++);
		kryo.register(PropertySection.class, new FieldSerializer<PropertySection>(kryo, PropertySection.class), nextRegistrationId++);
		// validations
		kryo.register(PropertyValidation.class, new FieldSerializer<PropertyValidation>(kryo, PropertyValidation.class), nextRegistrationId++);
		kryo.register(ValidationError.class, new FieldSerializer<ValidationError>(kryo, ValidationError.class), nextRegistrationId++);
		kryo.register(PropertyValidationException.class, new FieldSerializer<PropertyValidationException>(kryo, PropertyValidationException.class), nextRegistrationId++);
		// validators
		kryo.register(FullyQualifiedClassNameValidator.class, new FieldSerializer<FullyQualifiedClassNameValidator>(kryo, FullyQualifiedClassNameValidator.class), nextRegistrationId++);
		kryo.register(GreaterOrEqualValidator.class, new FieldSerializer<GreaterOrEqualValidator<?>>(kryo, GreaterOrEqualValidator.class), nextRegistrationId++);
		kryo.register(GreaterValidator.class, new FieldSerializer<GreaterValidator<?>>(kryo, GreaterValidator.class), nextRegistrationId++);
		kryo.register(LessOrEqualValidator.class, new FieldSerializer<LessOrEqualValidator<?>>(kryo, LessOrEqualValidator.class), nextRegistrationId++);
		kryo.register(LessValidator.class, new FieldSerializer<LessValidator<?>>(kryo, LessValidator.class), nextRegistrationId++);
		kryo.register(NegativeValidator.class, new FieldSerializer<NegativeValidator<?>>(kryo, NegativeValidator.class), nextRegistrationId++);
		kryo.register(NotEmptyValidator.class, new FieldSerializer<NotEmptyValidator<?>>(kryo, NotEmptyValidator.class), nextRegistrationId++);
		kryo.register(PercentageValidator.class, new FieldSerializer<PercentageValidator<?>>(kryo, PercentageValidator.class), nextRegistrationId++);
		kryo.register(PositiveValidator.class, new FieldSerializer<PositiveValidator<?>>(kryo, PositiveValidator.class), nextRegistrationId++);

		// INSPECTIT-658
		// this classes can be registered with FieldSerializer since they are not saved to disk
		kryo.register(AgentMapping.class, new FieldSerializer<AgentMapping>(kryo, AgentMapping.class), nextRegistrationId++);
		kryo.register(AgentMappings.class, new FieldSerializer<AgentMappings>(kryo, AgentMappings.class), nextRegistrationId++);
		kryo.register(Environment.class, new FieldSerializer<Environment>(kryo, Environment.class), nextRegistrationId++);
		kryo.register(Profile.class, new FieldSerializer<Profile>(kryo, Profile.class), nextRegistrationId++);
		// assignments
		kryo.register(ExceptionSensorAssignment.class, new FieldSerializer<ExceptionSensorAssignment>(kryo, ExceptionSensorAssignment.class), nextRegistrationId++);
		kryo.register(MethodSensorAssignment.class, new FieldSerializer<MethodSensorAssignment>(kryo, MethodSensorAssignment.class), nextRegistrationId++);
		kryo.register(TimerMethodSensorAssignment.class, new FieldSerializer<TimerMethodSensorAssignment>(kryo, TimerMethodSensorAssignment.class), nextRegistrationId++);
		// context capture
		kryo.register(FieldContextCapture.class, new FieldSerializer<FieldContextCapture>(kryo, FieldContextCapture.class), nextRegistrationId++);
		kryo.register(ParameterContextCapture.class, new FieldSerializer<ParameterContextCapture>(kryo, ParameterContextCapture.class), nextRegistrationId++);
		kryo.register(ReturnContextCapture.class, new FieldSerializer<ReturnContextCapture>(kryo, ReturnContextCapture.class), nextRegistrationId++);
		// exclude
		kryo.register(ExcludeRule.class, new FieldSerializer<ExcludeRule>(kryo, ExcludeRule.class), nextRegistrationId++);
		// exception sensor config
		kryo.register(ExceptionSensorConfig.class, new FieldSerializer<ExceptionSensorConfig>(kryo, ExceptionSensorConfig.class), nextRegistrationId++);
		// method sensor configs
		kryo.register(ConnectionMetaDataSensorConfig.class, new FieldSerializer<ConnectionMetaDataSensorConfig>(kryo, ConnectionMetaDataSensorConfig.class), nextRegistrationId++);
		kryo.register(ConnectionSensorConfig.class, new FieldSerializer<ConnectionSensorConfig>(kryo, ConnectionSensorConfig.class), nextRegistrationId++);
		kryo.register(HttpSensorConfig.class, new FieldSerializer<HttpSensorConfig>(kryo, HttpSensorConfig.class), nextRegistrationId++);
		kryo.register(InvocationSequenceSensorConfig.class, new FieldSerializer<InvocationSequenceSensorConfig>(kryo, InvocationSequenceSensorConfig.class), nextRegistrationId++);
		kryo.register(PreparedStatementParameterSensorConfig.class, new FieldSerializer<PreparedStatementParameterSensorConfig>(kryo, PreparedStatementParameterSensorConfig.class),
				nextRegistrationId++);
		kryo.register(PreparedStatementSensorConfig.class, new FieldSerializer<PreparedStatementSensorConfig>(kryo, PreparedStatementSensorConfig.class), nextRegistrationId++);
		kryo.register(StatementSensorConfig.class, new FieldSerializer<StatementSensorConfig>(kryo, StatementSensorConfig.class), nextRegistrationId++);
		kryo.register(TimerSensorConfig.class, new FieldSerializer<TimerSensorConfig>(kryo, TimerSensorConfig.class), nextRegistrationId++);
		// platform sensor configs
		kryo.register(ClassLoadingSensorConfig.class, new FieldSerializer<ClassLoadingSensorConfig>(kryo, ClassLoadingSensorConfig.class), nextRegistrationId++);
		kryo.register(CompilationSensorConfig.class, new FieldSerializer<CompilationSensorConfig>(kryo, CompilationSensorConfig.class), nextRegistrationId++);
		kryo.register(CpuSensorConfig.class, new FieldSerializer<CpuSensorConfig>(kryo, CpuSensorConfig.class), nextRegistrationId++);
		kryo.register(MemorySensorConfig.class, new FieldSerializer<MemorySensorConfig>(kryo, MemorySensorConfig.class), nextRegistrationId++);
		kryo.register(RuntimeSensorConfig.class, new FieldSerializer<RuntimeSensorConfig>(kryo, RuntimeSensorConfig.class), nextRegistrationId++);
		kryo.register(SystemSensorConfig.class, new FieldSerializer<SystemSensorConfig>(kryo, SystemSensorConfig.class), nextRegistrationId++);
		kryo.register(ThreadSensorConfig.class, new FieldSerializer<ThreadSensorConfig>(kryo, ThreadSensorConfig.class), nextRegistrationId++);
		kryo.register(Profile.class, new FieldSerializer<Profile>(kryo, Profile.class), nextRegistrationId++);
		kryo.register(Profile.class, new FieldSerializer<Profile>(kryo, Profile.class), nextRegistrationId++);
		kryo.register(Profile.class, new FieldSerializer<Profile>(kryo, Profile.class), nextRegistrationId++);
		// strategies
		kryo.register(TimeSendingStrategyConfig.class, new FieldSerializer<TimeSendingStrategyConfig>(kryo, TimeSendingStrategyConfig.class), nextRegistrationId++);
		kryo.register(ListSendingStrategyConfig.class, new FieldSerializer<ListSendingStrategyConfig>(kryo, ListSendingStrategyConfig.class), nextRegistrationId++);
		kryo.register(SimpleBufferStrategyConfig.class, new FieldSerializer<SimpleBufferStrategyConfig>(kryo, SimpleBufferStrategyConfig.class), nextRegistrationId++);
		kryo.register(SizeBufferStrategyConfig.class, new FieldSerializer<SizeBufferStrategyConfig>(kryo, SizeBufferStrategyConfig.class), nextRegistrationId++);

		// INSPECTIT-2020
		kryo.register(Log4jLoggingSensorConfig.class, new FieldSerializer<Log4jLoggingSensorConfig>(kryo, Log4jLoggingSensorConfig.class), nextRegistrationId++);
	}

}
