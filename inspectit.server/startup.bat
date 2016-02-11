@echo off

:RESTART
#COMMAND#

if ERRORLEVEL 10 (
	echo Restarting CMR...
	goto RESTART
)