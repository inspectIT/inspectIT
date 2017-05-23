/**
 * Mocks the sending of beacons to make it possibly to read back the results.
 * This allows unit tests to validate their conditions based on the "sent" data.
 */

window.mocking = (function() {

	function preAgentCore() {

	}

	function preAgentModules() {

		BeaconService.init = function() {
		};
		BeaconService.send = function(data) {
			window.mocking.sentElements.push(data);
		};
	}

	function preAgentInit() {

	}

	function postAgentInit() {

	}

	return {
		preAgentCore : preAgentCore,
		preAgentModules : preAgentModules,
		preAgentInit : preAgentInit,
		postAgentInit : postAgentInit,
		sentElements : [],
	};

})();
