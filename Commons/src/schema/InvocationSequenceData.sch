class: info.novatec.inspectit.communication.data.InvocationSequenceData

# Default Data
1: id
2: platformIdent
3: sensorTypeIdent
4: timeStamp

# Method Sensor Data
5: methodIdent
6: parameterContentData

# Invocation Sequence Data
7: nestedSequences
# parentSequence is not serialized because it represents an overhead
# instead we have a special Serializer for invocations that connects parent-child relationship after de-serialization
8: timerData
9: sqlStatementData
10: exceptionSensorDataObjects
11: position
12: duration
13: start
14: end
15: childCount
16: nestedSqlStatements
17: nestedExceptions