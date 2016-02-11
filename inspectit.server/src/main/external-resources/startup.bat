@echo off

if not exist "logs" mkdir logs
:RESTART
#COMMAND#

if ERRORLEVEL 10 (
	echo Restarting CMR...
	goto RESTART
)