

[![](https://img.shields.io/npm/v/cordova-plugin-google-nearby-connections.svg)](https://www.npmjs.com/package/cordova-plugin-google-nearby)

# Description

A Cordova plugin for the [Google Nearby Connections API](https://developers.google.com/nearby/connections/overview).  

### Supported Platforms

Android (4.1+)

### Limitations

- Currently only supports sending/receiving string payloads (no file or stream support)

# Plugin Installation

Cordova:

```
cordova plugin add cordova-plugin-google-nearby-connections
```

  

Ionic (2+):

```
ionic cordova plugin add cordova-plugin-google-nearby-connections
```

Install from GitHub:

```
cordova plugin add https://github.com/kspierson/cordova-plugin-google-nearby-connections
```

  

# Plugin Usage

  

## Initialize

```
var GoogleNearbyConnections = cordova.plugins.GoogleNearbyConnections;
```

## Methods

### Start Advertising

```
GoogleNearbyConnections.startAdvertising(deviceName, strategyType, serviceId, success, error);
```

### Start Discovery

```
GoogleNearbyConnections.startDiscovery(strategyType, serviceId, success, error);
```

### On Endpoint Found

```
GoogleNearbyConnections.onEndpointFound(success, error);
```

### On Endpoint Lost

```
GoogleNearbyConnections.onEndpointLost(success, error);
```

### On Connection Found

```
GoogleNearbyConnections.onConnectionFound(success, error);
```

### On Connection

```
GoogleNearbyConnections.onConnection(success, error);
```


### On Connection Lost

```
GoogleNearbyConnections.onConnectionLost(success, error);
```

### Accept Connection

```
GoogleNearbyConnections.acceptConnection(endpointId, success, error);
```

### Deny Connection

```
GoogleNearbyConnections.denyConnection(endpointId, success, error);
```

### Send Payload

```
GoogleNearbyConnections.sendPayload(endpointId, payload, success, error);
```

### On Payload Received

```
GoogleNearbyConnections.onPayloadReceived(success, error);
```


# Remove Plugin

Cordova

```
cordova plugin rm cordova-plugin-google-nearby-connections
```

  

Ionic

```
ionic cordova plugin rm cordova-plugin-google-nearby-connections
```
