window.mocking = (function() {

	var sentElements = [];
	function preAgentCore() {


	}

	function preAgentModules() {

		inspectIT.beaconService.init = function () {};
		inspectIT.beaconService.send = function (data) {
			sentElements.push(data);
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
		sentElements : sentElements,
	};

})();
