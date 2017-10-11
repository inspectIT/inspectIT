[![OpenTracing Badge](https://img.shields.io/badge/OpenTracing-enabled-blue.svg)](http://opentracing.io)
# Java Agent SDK

The *Java Agent SDK* is a library provided by the open source APM tool [inspectIT](http://inspectIT.rocks). It can be used to enrich the functionality of Java applications which use the inspectIT agent for monitoring and collection of application runtime metrics and infrastructure data.

## Installation
### Maven
```.xml
<dependency>
    <groupId>rocks.inspectit</groupId>
    <artifactId>agent-java-sdk</artifactId>
    <version>1.8.5</version>
</dependency>
```

## OpenTracing Implementation
inspectIT provides an implementation of the [OpenTracing](http://opentracing.io/) API as part of the agent SDK for Java.

_**Note:** If you are not aware with [OpenTracing](http://opentracing.io/) API, please read the [specifications and documentation](http://opentracing.io/documentation/) as to easier understand this SDK._

### Implementation Details

#### Thread context aware
The most important property of the *inspectIT Tracer* implementation is that it's **thread context** aware. This means that consecutive spans created by the same thread will have a *parent-child* relationship by default. Thus, in most cases it is not needed to explicitly connect spans using `asChildOf` methods if two spans are created by same thread.

If you following the API, note that using the `asChildOf` is safe and will not have any side effects if the relationship between spans has already been made by the tracer itself. 

#### No sample rate
Currently, the inspectIT tracer does not provide an option to set a sample rate. This means that **all** of the spans will be "sampled" and reported. The OpenTracing API does not specify a need for a sampling rate, but in the future the inspectIT Tracer will add this feature to the implementation.

#### Log events are ignored
Any events logged using the `Span.log()` methods are ignored and will not be reported.

#### Single parent context reference possible
The OpenTracing specification allows a span to reference more than one parent span. This is currently limited in the inspectIT implementation as span always have one direct parent reference and it will be the one to the first referred parent. However, the baggage from all referred parent contexts is propagated with the span as expected. The inspectIT Tracer may change this in future in order to align with to the specification.

## Usage

### Usage with inspectIT agent
#### Acquire the Tracer
The tracer can be obtained by calling the `get()` or `get(boolean)` method in the `rocks.inspectit.agent.java.sdk.opentracing.TracerProvider` class. If the inspectIT agent is running with the application where `TracerProvider` is used, the `get` methods will always returned a correctly initialized tracer implementation. If the inspectIT is not running, then the caller can control if the returned tracer is a no-operation tracer or `null`.

The `TracerProvider` returns the `rocks.inspectit.agent.java.sdk.opentracing.ExtendedTracer` instance. This interface defines additional methods that the inspectIT tracer provides for usage on top of the OpenTracing API. It's up to user to decide to code against the `io.opentracing` interfaces or use directly `ExtendedTracer` which provides some additional control.

**Note:** Tracer implementation is correctly initialized only if the inspectIT agent is running on the JVM where inspectIT SDK is used.

#### Example
Following code block describes the typical usage. You can find more example of API usage on [opentracing.io Java API](https://github.com/opentracing/opentracing-java/tree/master/opentracing-api) project.

```Java
// get the tracer from the inspectIT TracerProvider
Tracer tracer = TracerProvider.get();
// build new span with some optional custom  tags
Span span = tracer.buildSpan("my operation").withTag("key", "value").start();
// optionally: attach some baggage to the span which are propagated with next remote call
span.setBaggageItem("user-id", "123");
try {
// then do some actual work
} finally {
    // finish span at the end so it's reported
    span.finish();
}
```

### Usage without inspectIT agent
It's possible to use the inspectIT Tracer implementation also without the inspectIT agent is running with the application. However, then the user must provide the `rocks.inspectit.agent.java.sdk.opentracing.Reporter` implementation when constructing the Tracer, which is notified about each finished span. It's up to user to define what will reporter to do with the finished spans (e.g. store them somehow, log them or something else).

```Java
public class LoggingReporter implements Reporter {
    // just log the finished span
    public void report(Span span) {
        log(span.toString())
    }
}
```

## Additional options
The `rocks.inspectit.agent.java.sdk.opentracing.ExtendedTracer` interface provides some additional methods that users can use in order to do following:

Method | Description
--- | --- 
setTimer(Timer) | By default the inspectIT tracer uses `rocks.inspectit.agent.java.sdk.opentracing.util.SystemTimer` which provides millisecond start time precision. This is done so the tracer is compatible with Java 6. Users can provide better timers if they run on higher Java versions or have third party dependencies which could do better.
registerPropagator(Format, Propagator) | This option allows overwriting of the tracer default propagators or registration of additional propagators that work with formats that inspectIT tracer is not aware of.
buildSpan(String, String, String) | As described in the implementation details the inspectIT tracer is thread-context aware. If you would like to create spans that don't have a parent relationship to the current thread context span, then you can explicitly do this in `ExtendedTracer`.
