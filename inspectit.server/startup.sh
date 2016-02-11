while true; do
	#COMMAND#
	CMR_STATUS=$?
	if [ "$CMR_STATUS" -eq 10 ]; then
		echo "Restarting CMR..."
	else
      		exit $CMR_STATUS
  	 fi
done
