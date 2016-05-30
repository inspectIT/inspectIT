[![Gitter](https://img.shields.io/badge/Gitter-join%20chat-brightgreen.svg)](https://gitter.im/inspectIT/chat?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Build Status](http://jenkins.inspectit.rocks/buildStatus/icon?job=inspectIT%20-%20Integration)](http://jenkins.inspectit.rocks/job/inspectIT%20-%20Integration/) [![License](https://img.shields.io/badge/License-Apache v2-brightgreen.svg)](https://github.com/inspectIT/inspectIT/blob/master/license/LICENSE.txt)

## Overview
inspectIT (http://inspectit.rocks) is the leading Open Source APM (application performance management) tool for monitoring and analyzing your Java(EE) software applications.

Various sensors capture end-to-end information for every request from the end user, to the business tier all the way to the backends. inspectIT is based on an application-centric, business-focused approach, where each technical request is mapped to an application and to a business usecase. With inspectIT you always know about the health of your software and can easily analyze any problems that arise.

We hope that together we can build an alternative to the (great!) commercial tools available so that it is up the end user to choose his tooling. **Let's make inspectIT even more awesome!**

| See all requests | Trace-based analysis | SQL details | Charting |
:-------------------------:|:-------------------------:|:-------------------------:|:-------------------------:
|![](http://inspectit.github.io/inspectIT/screenshots/httOverview.png) | ![](http://inspectit.github.io/inspectIT/screenshots/invocWithSQLLocate.png) | ![](http://inspectit.github.io/inspectIT/screenshots/sqlOverviewWithStorage.png) | ![](http://inspectit.github.io/inspectIT/screenshots/graphsRepo.png)|

## Features
- Detailed trace representation (invocation sequence) for every request containing all interactions with the systems.
- Automatic enrichment of every trace with HTTP information, SQL queries, exceptions, parameters and many more.
- Detailed exception capturing allows to analyze functional problems.
- Drill down into one invocation sequence to find and analyze problematic requests.
- Drill up from an problem within an invocation sequence and find business-related information like the URL the  request was sent to.
- Aggregated views for every captured metric, e.g. [aggregated SQL overview](http://inspectit.github.io/inspectIT/screenshots/sqlOverviewWithStorage.png) shows metrics aggregated by SQL query.
- Navigation feature allows to navigate between aggregated views and invocation sequences for advanced analytics.
- Talk in invocation sequences! Send detailed traces with all information instead of noting down which clicks lead to the problem.
- Reproducing problems is a thing of the past! You already have a trace representation that you can analyze.
- No more config files! We have graphical configuration interfaces for everything.
- Easy and transparent integration of the inspectIT agents in the application.
- Monitor your hardware metrics like cpu, memory or threads.
- Optimized for low overhead.
- Production-proof: Used for >8 years at our customers and during performance firefights
- One server is enough for most environments supporting a big number of agents.  
- RESTful API for automation and integration with other tools.
- Fully adaptable user interface.
- Extensible system: Missing something? Write your own extensions.

### Out-of-the-box profiles
inspectIT already ships with out-of-the-box instrumentations for commonly used Java technologies. If your technology is missing, you can easily instrument it manually (or [request a new profile](https://inspectit-performance.atlassian.net/wiki/display/CONTRIBUTE/Add+feature+requests)). We currently support:
- Java Persistence
  - Hibernate
  - JPA
  - JDBC
- JavaEE
  - Enterprise Java Beans (EJB)
  - Servlets API
  - Java Server Faces (JSF)
  - WebServices
  - Java Transaction API (JTA)
- Apache Struts
- Apache MyFaces Trinidad

### Supported application platforms
- IBM WebSphere
- Oracle Weblogic
- JBoss
- Wildfly
- Tomcat
- Glassfish
- Any plain java application


### Related projects
- Experimental features and supporting components of inspectIT are located at [inspectIT Labs]( https://github.com/inspectIT-labs)
- Our Docker integration projects are located at [inspectIT Docker](https://github.com/inspectit-docker)


## Integration
The integration of inspectIT in your application is completely transparent, you do not have to change a single line of code. The only thing you have to do is to place our agent with your application and integrate it into your startup script.

The integration is as simple as adding the following to the startup of your application.

```
-javaagent:[INSPECTIT_AGENT_HOME]/inspectit-agent.jar -Dinspectit.repository=[REPOSITORY_IP]:[REPOSITORY_PORT] -Dinspectit.agent.name=[AGENT_DISPLAY_NAME]
```

The [end user documentation](https://inspectit-performance.atlassian.net/wiki/display/DOC/End+User+Documentation+Home) provides in-depth documentation on the installation of inspectIT for all supported platforms. If you have further questions please get in touch with us.


## Download
[![Docker Pulls](https://img.shields.io/docker/pulls/inspectit/inspectit.svg?label=docker pulls UI)](https://registry.hub.docker.com/u/inspectit/inspectit/) [![Docker Pulls](https://img.shields.io/docker/pulls/inspectit/cmr.svg?label=docker pulls CMR)](https://registry.hub.docker.com/u/inspectit/cmr/) [![Docker Pulls](https://img.shields.io/docker/pulls/inspectit/jetty.svg?label=docker pulls agent jetty)](https://registry.hub.docker.com/u/inspectit/jetty/) [![Docker Pulls](https://img.shields.io/docker/pulls/inspectit/jboss.svg?label=docker pulls agent jboss)](https://registry.hub.docker.com/u/inspectit/jboss/) [![Docker Pulls](https://img.shields.io/docker/pulls/inspectit/glassfish.svg?label=docker pulls agent glassfish)](https://registry.hub.docker.com/u/inspectit/glassfish/) [![Docker Pulls](https://img.shields.io/docker/pulls/inspectit/tomcat.svg?label=docker pulls agent tomcat)](https://registry.hub.docker.com/u/inspectit/tomcat/)


You can get inspectIT in three ways:
- Download the [latest stable release](https://github.com/inspectIT/inspectIT/releases/latest)
- Download a [specific version](https://github.com/inspectIT/inspectIT/releases)
- Use a pre-built [Docker image](https://hub.docker.com/u/inspectit/)

## Get in touch
We are interested in your feedback. Come chat with us and other users on [![Gitter](https://img.shields.io/badge/Gitter-join%20chat-brightgreen.svg)](https://gitter.im/inspectIT/chat?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge). Visit the [forum](https://groups.google.com/forum/#!forum/inspectit) or simply drop us a [line](mailto:info.inspectit@novatec-gmbh.de). You also might want to follow at Twitter: [@inspectIT_apm](https://twitter.com/inspectit_apm)


## End user / Development Documentation
We are running an Atlassian Confluence for all sorts of [documentation](https://inspectit-performance.atlassian.net/wiki) (thanks to Atlassian for the free license):
From [end user documentation](https://inspectit-performance.atlassian.net/wiki/display/DOC/End+User+Documentation+Home) to the [roadmap](https://inspectit-performance.atlassian.net/wiki/display/ROAD/Roadmap+Home).

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
inspectIT is licensed under [Apache License version 2.0](https://github.com/inspectIT/inspectIT/blob/master/license/LICENSE.txt). Please see our [licensing documentation](https://inspectit-performance.atlassian.net/wiki/display/LIC/FAQ%3A+Licensing) for more details.
(Note that releases up to 1.6.7 were licensed using AGPLv3)

## Sponsoring
inspectIT is mainly driven by [NovaTec Consulting GmbH](http://www.novatec-gmbh.de/), a German consultancy firm that specializes in software performance. Sponsoring a feature in inspectIT is always possible and welcome. Just get in touch with us through [Sponsor a feature](https://inspectit-performance.atlassian.net/wiki/display/CONTRIBUTE/Sponsor+a+feature).

### Research / Further readings
inspectIT is the base for the research project [diagnoseIT](http://diagnoseit.github.io/), sponsored by [German federal ministry of education and research](http://www.bmbf.de) with more than 500.000€.

<!-- interesting badges for further integration -->
<!-- coveralls.io badge -->
<!-- [![Coverage Status](https://coveralls.io/repos/OCA/product-attribute/badge.png?branch=8.0)](https://coveralls.io/r/OCA/product-attribute?branch=8.0) -->
<!-- [![Bountysource](https://img.shields.io/bountysource/team/inspectit/activity.svg)]() -->
