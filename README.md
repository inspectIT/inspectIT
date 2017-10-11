[![Website](https://img.shields.io/badge/Website-visit-brightgreen.svg)](http://www.inspectit.rocks)  [![Twitter Account](https://img.shields.io/badge/Twitter-follow%20us-brightgreen.svg)](https://twitter.com/inspectIT_APM) [![Gitter](https://img.shields.io/badge/Gitter-join%20chat-brightgreen.svg)](https://gitter.im/inspectIT/chat?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![LinkedIn Group](https://img.shields.io/badge/LinkedIn-join%20group-brightgreen.svg)](https://www.linkedin.com/groups/inspectIT-APM-User-Group-8533412/about) [![YouTube](https://img.shields.io/badge/YouTube-watch-brightgreen.svg)](https://www.youtube.com/channel/UCcE-Z-Yndk67PjObEn071fg) [![Build Status](http://jenkins.inspectit.rocks/buildStatus/icon?job=inspectIT%20-%20Integration)](http://jenkins.inspectit.rocks/job/inspectIT%20-%20Integration/) [![License](https://img.shields.io/badge/License-Apache/v2-brightgreen.svg)](https://github.com/inspectIT/inspectIT/blob/master/license/LICENSE.txt)

## Overview
[inspectIT](http://inspectit.rocks) is the leading Open Source APM (application performance management) tool for monitoring and analyzing your Java(EE) software applications.

Various sensors capture end-to-end information for every request from the end user, to the business tier all the way to the backends. inspectIT is based on an application-centric, business-focused approach, where each technical request is mapped to an application and to a business use-case. With inspectIT you always know about the health of your software and can easily analyze any problems that arise.

For Web applications the tool integrates the [End user monitoring](#user-content-end-user-monitoring) using automatic JavaScript agent injection. This allows easy monitoring of the performance that real users are facing in the browser. In addition, the inspectIT can correlate all user actions in the browser to the backend traces, thus providing a complete picture on the user experience.

We hope that together we can build an alternative to the (great!) commercial tools available so that it is up the end user to choose his tooling. **Let's make inspectIT even more awesome!**

## Features

| See all requests | Trace-based analysis | Remote traces | Details (SQL) |
| --- | --- | --- | --- |
| ![](http://inspectit.github.io/inspectIT/screenshots/httOverview.png) | ![](http://inspectit.github.io/inspectIT/screenshots/invocWithSQLLocate.png) | ![](http://inspectit.github.io/inspectIT/screenshots/tracingOverview.png) | ![](http://inspectit.github.io/inspectIT/screenshots/sqlOverviewWithStorage.png) |

| Details (Exceptions) | Sensor configuration | Business context | Alerting |
| --- | --- | --- | --- |
| ![](http://inspectit.github.io/inspectIT/screenshots/detailsExceptions.png) | ![](http://inspectit.github.io/inspectIT/screenshots/sensorConfiguration.png) | ![](http://inspectit.github.io/inspectIT/screenshots/businessContext.png) | ![](http://inspectit.github.io/inspectIT/screenshots/alerting.png) | 

| Monitoring dashboards | EUM Summary | EUM Single Page | EUM 3rd Party Content |
| --- | --- | --- | --- |
| ![](http://inspectit.github.io/inspectIT/screenshots/grafanaDashboards.png) | ![](http://inspectit.github.io/inspectIT/screenshots/EUM_Summary.png) | ![](http://inspectit.github.io/inspectIT/screenshots/EUM_One_page.png) | ![](http://inspectit.github.io/inspectIT/screenshots/EUM-3rd-Party.png) |

- [**Browser End User Monitoring**](#user-content-end-user-monitoring): automatic injection of a JavaScript agent into your application HTML code.
- [**Trace Based**](#user-content-tracing)
  - Detailed trace representation (invocation sequence) for every request containing all interactions with the systems.
  - Support for inter-JVM communication based on HTTP and JMS: each trace shows interaction with all correlated JVMs.
  - Support for browser-JVM communication: correlation between user actions in the browser and backend requests.
  - SDK which implements the OpenTracing.io API. All user spans are combined with inspectIT measurements in a single trace.
- **Variety of Information**
  - Automatic enrichment of every trace with HTTP information, SQL queries, exceptions, parameters and many more.
  - Detailed exception capturing allows to analyze functional problems.
  - Drill down into one invocation sequence to find and analyze problematic requests.
  - Drill up from an problem within an invocation sequence and find business-related information like the URL the request was sent to.
  - Aggregated views for every captured metric, e.g. [aggregated SQL overview](http://inspectit.github.io/inspectIT/screenshots/sqlOverviewWithStorage.png) shows metrics aggregated by SQL query.
- **Business Context**
  - Have a view on your application's business transactions by using flexible business context definitions.
  - Monitor and analyze requests by the functional use-case.
- **Monitoring**
  - Monitor your hardware metrics like cpu, memory or threads.
  - Monitor metrics exposed via JMX beans.
  - Integration with influxDB and Grafana for easy monitoring with [pre-defined dashboards](https://github.com/inspectit-labs/dashboards).
  - Simple e-mail alerting is possible on all long-term monitoring data.
- [**Diagnosis Service**](#user-content-automatic-performance-problems-detection) for automatic performance problem detection. 
- **RESTful API** for automation and integration with other tools.
- **Usability at Its Best**
  - Easy and transparent [integration](#user-content-integration) of the inspectIT agent in your application (compatible with Java 6, 7, 8, and 9).
  - [Out-of-the-box profiles](#user-content-out-of-the-box-profiles) for a quick start.
  - Dynamic instrumentation - change measurement points on the monitored JVM without a need for restart.
  - Store, import and export detailed traces with all information instead of noting down which user-clicks led to a problem.
  - No more config-files! We have fully adaptable, graphical configuration interfaces for everything.
- **Optimized for Low Overhead**
  - Production-proof: Used for >8 years at our customers and during performance firefights.
  - One server is enough for most environments supporting a medium number of agents.
- **Extendable system**: Missing something? Write your own extensions.

### End User Monitoring
<sup>:warning: *We recommend to try this feature on a system configuration in a safe environment first.*</sup>

The inspectIT 1.8 line comes with a new feature: Browser End User Experience Monitoring (EUM). This feature allows you to measure the performance at the browser side of the end user, giving more detailed insights than the back-end instrumentation alone. This is done by automatically injecting a JavaScript agent into your applications HTML code, which in turn captures relevant metrics at the client-side and sends them back to inspectIT using AJAX Requests. Check the [official feature documentation](https://inspectit-performance.atlassian.net/wiki/display/DOC18/Working+with+End+User+Monitoring) to help you started.

### Tracing
[![OpenTracing Badge](https://img.shields.io/badge/OpenTracing-enabled-blue.svg)](http://opentracing.io)

inspectIT provides a set of remote sensors in order to create traces and correlate calls made between JVM nodes in your application. These traces can provide an end-to-end view on the user request execution, even they are spanning over multiple JVMs. In addition, if end-user-monitoring is active inspectIT is capable of correlating browser side actions, like page loads or clicks, to resulting back-end requests ([screenshot](http://inspectit.github.io/inspectIT/screenshots/tracingOverview.png)).

The remote tracing is done in inspectIT as per [OpenTracing.io specification](https://github.com/opentracing/specification/blob/master/specification.md), with a similar data model based on spans. Furthermore, inspectIT combines the captured spans with the invocation sequences created on each JVM and, thus, is able to provide not only the landscape of the interacting JVMs but also provide detailed information on the invocation execution during complete traces. This includes all the data that is captured as a part of invocation sequence, SQL statements, exceptions, method calls, etc. Visit the [official feature documentation](https://inspectit-performance.atlassian.net/wiki/display/DOC18/Working+with+remote+traces) to help you get started.

With a set of default inspectIT profiles you get automatic tracing for following technologies and libraries without a need to change your source code nor dependencies. Currently, we support:

- Java
  - Executor service (Java 6+)
- HTTP
  - Apache HttpComponents Async Client (version 4.x)
  - Apache HttpComponents Client (version 4.x)
  - Java Servlet API (version 2.x & up)
  - Java URL Connection (Java 6+)
  - Jetty Http Client (versions 7.x & 8.x)
  - Spring Rest Template (version 3.x & 4.x)	
- JMS
  - Java Message Service API (version 1.x & up)

The [OpenTracing.io API](http://opentracing.io/) implementation is provided as a part of inspectIT's [inspectit.agent.java.sdk](inspectit.agent.java.sdk) project. Users of the OpenTracing.io API can easily use inspectIT as the implementation.

### Automatic Performance Problems Detection 
<sup>:warning: *Experimental*</sup>

As s result of the [diagnoseIT research project](https://diagnoseit.github.io/), inspectIT's version line 1.8 offers an integrated diagnosis service for automatic performance problems detection. This service can analyze requests lasting longer than a user-defined baseline and provides insights on where the performance problems can be. An overview of found problems is provided as a part of the [monitoring dashboards](https://github.com/inspectit-labs/dashboards). Visit the [official feature documentation](https://inspectit-performance.atlassian.net/wiki/display/DOC18/Working+with+automatic+problem+diagnosis) to help you started.

### Out-Of-The-Box Profiles
inspectIT already ships with out-of-the-box instrumentations for commonly used Java technologies. If your technology is missing, you can easily instrument it manually (or [request a new profile](https://inspectit-performance.atlassian.net/wiki/display/CONTRIBUTE/Add+feature+requests)). We currently support:
- JavaEE
  - Enterprise Java Beans (EJB)
  - Java Server Faces (JSF)
  - Java Transaction API (JTA)
  - Servlets API
  - WebServices
  - WebSockets
- Java Persistence
  - JDBC
  - JPA
  - Hibernate
- Remote Communication Tracing
  - HTTP
  - JMS
- Others
  - Apache Struts
  - Apache MyFaces Trinidad
  - Executor service tracing

### Supported Application Platforms
- Glassfish
- IBM WebSphere
- Jetty
- JBoss / Wildfly
- Oracle Weblogic
- Resin
- Tomcat
- ..
- and any plain Java application

### Project Structure
Project | Description
--- | ---
[inspectit.agent.java](inspectit.agent.java) | Agent for instrumenting and monitoring Java 6+ applications.
[inspectit.agent.java.sdk](inspectit.agent.java.sdk) | Java SDK that implements opentracing.io API.
[inspectit.server](inspectit.server) | Server component know as Central Measurement Repository (CMR).
[inspectit.server.diagnosis](inspectit.server.diagnosis) | Component for automatic performance problems diagnosis (see [diagnoseIT](https://diagnoseit.github.io/) project). 
[inspectit.shared.all](inspectit.shared.all) | Classes shared between all projects. 
[inspectit.shared.cs](inspectit.shared.cs) | Classes shared between server and UI projects. 
[inspectit.ui.rcp](inspectit.shared.cs) | inspectIT user interface based on Eclipse RCP.

### Related Projects
- Experimental features and supporting components of inspectIT are located at [inspectIT Labs]( https://github.com/inspectIT-labs)
- Our Docker integration projects are located at [inspectIT Docker](https://github.com/inspectit-docker)

## Integration
The integration of inspectIT in your application is completely transparent, you do not have to change a single line of code. The only thing you have to do is to place our agent with your application and integrate it into your startup script.

The integration is as simple as adding the following to the startup of your application.

```
-javaagent:[INSPECTIT_AGENT_HOME]/inspectit-agent.jar -Dinspectit.repository=[REPOSITORY_IP]:[REPOSITORY_PORT] -Dinspectit.agent.name=[AGENT_DISPLAY_NAME]
```

The [end user documentation](https://inspectit-performance.atlassian.net/wiki/display/DOC/End+User+Documentation+Home) provides in-depth documentation on the installation of inspectIT for all supported platforms. If you have further questions please get in touch with us.

## Getting Started

### Download
[![Docker Pulls](https://img.shields.io/docker/pulls/inspectit/inspectit.svg?label=docker%20pulls%20UI)](https://registry.hub.docker.com/u/inspectit/inspectit/) [![Docker Pulls](https://img.shields.io/docker/pulls/inspectit/cmr.svg?label=docker%20pulls%20CMR)](https://registry.hub.docker.com/u/inspectit/cmr/) [![Docker Pulls](https://img.shields.io/docker/pulls/inspectit/jetty.svg?label=docker%20pulls%20agent%20jetty)](https://registry.hub.docker.com/u/inspectit/jetty/) [![Docker Pulls](https://img.shields.io/docker/pulls/inspectit/jboss.svg?label=docker%20pulls%20agent%20jboss)](https://registry.hub.docker.com/u/inspectit/jboss/) [![Docker Pulls](https://img.shields.io/docker/pulls/inspectit/glassfish.svg?label=docker%20pulls%20agent%20glassfish)](https://registry.hub.docker.com/u/inspectit/glassfish/) [![Docker Pulls](https://img.shields.io/docker/pulls/inspectit/tomcat.svg?label=docker%20pulls%20agent%20tomcat)](https://registry.hub.docker.com/u/inspectit/tomcat/)

You can get inspectIT in three ways:
- Download the [latest stable release](https://github.com/inspectIT/inspectIT/releases/latest)
- Download a [specific version](https://github.com/inspectIT/inspectIT/releases)
- Use a pre-built [Docker image](https://hub.docker.com/u/inspectit/)

### Try it out with a demo application!
You can easily test the inspectIT features by starting out the demo based on the Spring Petclinic application. The repository [inspectit-labs/spring-petclinic-microservices](https://github.com/inspectit-labs/spring-petclinic-microservices) contains start-up and docker scripts that integrate the inspectIT into the famous Spring demo app. Also check the [inspectit-labs/workshop](https://github.com/inspectit-labs/workshop) for the in-detail step-by-step workshop that will walk you through using inspectIT with the demo application. 

## Get in Touch
[![Twitter Account](https://img.shields.io/badge/Twitter-follow%20us-brightgreen.svg)](https://twitter.com/inspectIT_APM) [![Gitter](https://img.shields.io/badge/Gitter-join%20chat-brightgreen.svg)](https://gitter.im/inspectIT/chat?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![LinkedIn Group](https://img.shields.io/badge/LinkedIn-join%20group-brightgreen.svg)](https://www.linkedin.com/groups/inspectIT-APM-User-Group-8533412/about) 

We are interested in your feedback. Come chat with us and other users on [Gitter](https://gitter.im/inspectIT/chat?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge). Visit the [forum](https://groups.google.com/forum/#!forum/inspectit) or simply drop us a [line](mailto:info.inspectit@novatec-gmbh.de). You also might want to follow us at Twitter [@inspectIT_apm](https://twitter.com/inspectit_apm) or join discussions in our [LinkedIn group](https://www.linkedin.com/groups/inspectIT-APM-User-Group-8533412/about).


## Documentation
We are running an Atlassian Confluence for all sorts of [documentation](https://inspectit-performance.atlassian.net/wiki) (thanks to Atlassian for the free license):
From [end user documentation](https://inspectit-performance.atlassian.net/wiki/display/DOC/End+User+Documentation+Home) to the [road-map](https://inspectit-performance.atlassian.net/wiki/display/ROAD/Roadmap+Home).

## Contribute
You do not have to be a programmer to contribute to inspectIT, but if you are you are certainly welcome. Here is a short list of how you can contribute. Please see our [Contribution Documentation](https://inspectit-performance.atlassian.net/wiki/display/CONTRIBUTE/Contribute+Home)
- [Improve the documentation](https://inspectit-performance.atlassian.net/wiki/display/CONTRIBUTE/Contribute+documentation)
- [Let us know about bugs](https://inspectit-performance.atlassian.net/wiki/display/CONTRIBUTE/Contribute+a+bug+report)
- [Request new features](https://inspectit-performance.atlassian.net/wiki/display/CONTRIBUTE/Add+feature+requests)
- [Prioritize through voting](https://inspectit-performance.atlassian.net/wiki/display/CONTRIBUTE/Vote+for+features+and+bugs)
- [Fork and improve](https://inspectit-performance.atlassian.net/wiki/display/CONTRIBUTE/Contribute+source+code)

### Developers
If you want to fix Bugs or implement Features on your own, you're very welcome to do so! For the easiest start, you just need to follow our [Development Environment Documentation](https://inspectit-performance.atlassian.net/wiki/display/DEV/Development+Environment) for installing Eclipse with all projects and settings already pre-configured via the new official Oomph installer.

### Ticketing
We primarily use [Atlassian JIRA](https://inspectit-performance.atlassian.net/secure/Dashboard.jspa) for ticketing. But if you are in a hurry, please feel free to open a GitHub issue.

## Licensing
inspectIT is licensed under [Apache License version 2.0](license/LICENSE.txt). Please see our [licensing documentation](https://inspectit-performance.atlassian.net/wiki/display/LIC/Licensing) for more details.
(Note that releases up to 1.6.7 were licensed using AGPLv3)

## Commercial Support
If you need the commercial support for the inspectIT please check the [transparent package offering](http://www.inspectit.rocks/#support) by NovaTec Consulting GmbH and feel free to [contact us](http://www.inspectit.rocks/support/) if you are interested.

## Sponsoring
inspectIT is mainly driven by [NovaTec Consulting GmbH](http://www.novatec-gmbh.de/), a German consultancy firm that specializes in software performance. Sponsoring a feature in inspectIT is always possible and welcome. Just get in touch with us through [Sponsor a feature](https://inspectit-performance.atlassian.net/wiki/display/CONTRIBUTE/Sponsor+a+feature).

### Research / Further Readings
inspectIT is the base for the research project [diagnoseIT](http://diagnoseit.github.io/), sponsored by [German federal ministry of education and research](http://www.bmbf.de) with more than 500.000€.

<!-- interesting badges for further integration -->
<!-- coveralls.io badge -->
<!-- [![Coverage Status](https://coveralls.io/repos/OCA/product-attribute/badge.png?branch=8.0)](https://coveralls.io/r/OCA/product-attribute?branch=8.0) -->
<!-- [![Bountysource](https://img.shields.io/bountysource/team/inspectit/activity.svg)]() -->
