:: Name: uninstallService.bat
:: Owner: NovaTec Solutions GmbH (http://www.novatec-gmbh.de/, http://www.inspectit.eu/)
:: Description: uninstallService.bat removes inspectIT CMR Windows Service.

@ECHO OFF
SET PR_INSTALL=$INSTALL_PATH\CMR\$PR_EXE

"%PR_INSTALL%" //DS//$SERVICE_NAME 2> NUL

SET ERRORLVL=0