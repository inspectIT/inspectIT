:: Name: installService.bat
:: Owner: NovaTec Solutions GmbH (http://www.novatec-gmbh.de/, http://www.inspectit.eu/)
:: Description: installService.bat installs and configures inspectIT CMR as a Windows Service.

@ECHO OFF

:: Procrun execution binary
SET PR_INSTALL=$INSTALL_PATH\CMR\$PR_EXE

:: Windows Service meta information
SET SERVICE_NAME=$SERVICE_NAME
SET PR_DESCRIPTION=$PR_DESCRIPTION
SET PR_DISPLAYNAME=$PR_DISPLAYNAME

:: Java installation path
SET PR_JVM=$INSTALL_PATH\CMR\jre\bin\server\$PR_JVM

:: Procrun classpath
SET PR_CLASSPATH=$INSTALL_PATH\CMR\$PR_CLASSPATH

:: Startup configuration
SET PR_STARTUP=$PR_STARTUP
SET PR_STARTMODE=$PR_STARTMODE
SET PR_STARTCLASS=$PR_STARTCLASS
SET PR_STARTMETHOD=$PR_STARTMETHOD
SET PR_STARTPARAMS=$PR_STARTPARAMS

:: Shutdown configuration
SET PR_STOPMODE=$PR_STOPMODE
SET PR_STOPCLASS=$PR_STOPCLASS
SET PR_STOPMETHOD=$PR_STOPMETHOD
SET PR_STOPPARAMS=$PR_STOPPARAMS

:: Windows Service installation
"%PR_INSTALL%" //IS//%SERVICE_NAME% #COMMAND_OPTS#