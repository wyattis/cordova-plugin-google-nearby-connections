var exec = require('cordova/exec');

exports.startAdvertising = function(deviceName, strategyType, serviceId, success, error) {
    exec(success, error, 'GoogleNearbyConnections', 'startAdvertising', [deviceName, strategyType, serviceId]);
}

exports.startDiscovery = function(strategyType, serviceId, success, error) {
    exec(success, error, 'GoogleNearbyConnections', 'startDiscovery', [strategyType, serviceId]);
}

exports.onEndpointFound = function(success, error) {
    exec(success, error, 'GoogleNearbyConnections', 'onEndpointFound', []);
}

exports.onEndpointLost = function(success, error) {
    exec(success, error, 'GoogleNearbyConnections', 'onEndpointLost', []);
}

exports.onConnectionFound = function(success, error) {
    exec(success, error, 'GoogleNearbyConnections', 'onConnectionFound', []);
}

exports.onConnection = function(success, error) {
    exec(success, error, 'GoogleNearbyConnections', 'onConnection', []);
}

exports.onConnectionLost = function(success, error) {
    exec(success, error, 'GoogleNearbyConnections', 'onConnectionLost', []);
}

exports.acceptConnection = function(endpointId, success, error) {
    exec(success, error, 'GoogleNearbyConnections', 'acceptConnection', [endpointId]);
}

exports.denyConnection = function(endpointId, success, error) {
    exec(success, error, 'GoogleNearbyConnections', 'denyConnection', [endpointId]);
}

exports.sendPayload = function(endpointId, payload, success, error) {
    exec(success, error, 'GoogleNearbyConnections', 'sendPayload', [endpointId, payload]);
}

exports.onPayloadReceived = function(success, error) {
    exec(success, error, 'GoogleNearbyConnections', 'onPayloadReceived', []);
}



