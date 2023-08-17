package org.kspierson.cordova;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Context;
// import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

public class GoogleNearbyConnections extends CordovaPlugin {
    private String SERVICE_ID;
    // private Strategy STRATEGY;

    private static CallbackContext endpointFoundCallback;
    private static CallbackContext endpointLostCallback;
    private static CallbackContext connectionCallback;
    private static CallbackContext connectionFoundCallback;
    private static CallbackContext connectionLostCallback;
    private static CallbackContext payloadReceivedCallback;

    private static final int REQUEST_CODE = 1;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static final Map<String, Strategy> strategies = new HashMap<String, Strategy>() {
        {
            put("P2P_CLUSTER", Strategy.P2P_CLUSTER);
            put("P2P_STAR", Strategy.P2P_STAR);
            put("P2P_POINT_TO_POINT", Strategy.P2P_POINT_TO_POINT);
        }
    };

    private Context context;

    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
            JSONObject response = new JSONObject();
            PluginResult pluginResult;
            try {
                response.put("endpointId", endpointId);
                response.put("endpointName", connectionInfo.getEndpointName());
                response.put("authToken", connectionInfo.getAuthenticationToken());
                pluginResult = new PluginResult(PluginResult.Status.OK, response);
                pluginResult.setKeepCallback(true);
            } catch (JSONException e) {
                pluginResult = new PluginResult(PluginResult.Status.OK, "Connected initiated to " + endpointId);
                pluginResult.setKeepCallback(true);
            }
            GoogleNearbyConnections.connectionFoundCallback.sendPluginResult(pluginResult);
        }

        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution connection) {
            PluginResult pluginResult;
            switch (connection.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    pluginResult = new PluginResult(PluginResult.Status.OK, "Connected to " + endpointId);
                    pluginResult.setKeepCallback(true);
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, "Connection Rejected");
                    pluginResult.setKeepCallback(true);
                    break;
                case ConnectionsStatusCodes.STATUS_ERROR:
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, "Connection Error");
                    pluginResult.setKeepCallback(true);
                    break;
                default:
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, "Unknown Error");
                    pluginResult.setKeepCallback(true);
            }
            GoogleNearbyConnections.connectionCallback.sendPluginResult(pluginResult);
        }

        @Override
        public void onDisconnected(String endpointId) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, endpointId + " disconnected");
            pluginResult.setKeepCallback(true); // keep callback
            GoogleNearbyConnections.connectionLostCallback.sendPluginResult(pluginResult);
        }
    };

    private final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
            Nearby.getConnectionsClient(context).requestConnection(SERVICE_ID, endpointId, connectionLifecycleCallback)
                    .addOnSuccessListener((Void unused) -> {
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, endpointId);
                        pluginResult.setKeepCallback(true); // keep callback
                        GoogleNearbyConnections.endpointFoundCallback.sendPluginResult(pluginResult);
                    }).addOnFailureListener((Exception e) -> {
                        PluginResult errorResult = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
                        errorResult.setKeepCallback(true); // keep callback
                        GoogleNearbyConnections.endpointFoundCallback.sendPluginResult(errorResult);
                    }
            );
        }

        @Override
        public void onEndpointLost(String endpointId) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, endpointId + " lost");
            pluginResult.setKeepCallback(true);
            GoogleNearbyConnections.endpointLostCallback.sendPluginResult(pluginResult);
        }
    };

    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            switch (payload.getType()) {
                case (Payload.Type.BYTES):
                    byte[] receivedBytes = payload.asBytes();
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, new String(receivedBytes));
                    pluginResult.setKeepCallback(true); // keep callback
                    GoogleNearbyConnections.payloadReceivedCallback.sendPluginResult(pluginResult);
                    break;
                default:
                    PluginResult errorResult = new PluginResult(PluginResult.Status.ERROR, "Payload type not supported");
                    errorResult.setKeepCallback(true); // keep callback
                    GoogleNearbyConnections.payloadReceivedCallback.sendPluginResult(errorResult);
                    break;
            }
        }

        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
            // TO DO
        }
    };

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.context = cordova.getActivity().getApplicationContext();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (!cordova.hasPermission(permission)) {
                getPermissions(REQUEST_CODE, REQUIRED_PERMISSIONS);
            }
        }
        String deviceName;
        String strategyType;
        String serviceId;
        String endpointId;
        String payload;

        switch (action) {
            case "startAdvertising":
                deviceName = args.getString(0);
                strategyType = args.getString(1);
                serviceId = args.getString(2);
                this.SERVICE_ID = serviceId;
                // this.STRATEGY = strategies.get(strategyType);
                cordova.getThreadPool().execute(() -> {
                    startAdvertising(deviceName, strategyType, serviceId, callbackContext);
                });
                return true;
            case "startDiscovery":
                strategyType = args.getString(0);
                serviceId = args.getString(1);
                this.SERVICE_ID = serviceId;
                cordova.getThreadPool().execute(() -> {
                    startDiscovery(strategyType, serviceId, callbackContext);
                });
                return true;
            case "stopAdvertising":
                cordova.getThreadPool().execute(() -> {
                    stopAdvertising(callbackContext);
                });
            case "stopDiscovery":
                cordova.getThreadPool().execute(() -> {
                    stopDiscovery(callbackContext);
                });
            case "stopAllEndpoints":
                cordova.getThreadPool().execute(() -> {
                    stopAllEndpoints(callbackContext);
                });
            case "onEndpointFound":
                this.endpointFoundCallback = callbackContext;
                return true;
            case "onEndpointLost":
                this.endpointLostCallback = callbackContext;
                return true;
            case "onConnectionFound":
                this.connectionFoundCallback = callbackContext;
                return true;
            case "onConnection":
                this.connectionCallback = callbackContext;
                return true;
            case "onConnectionLost":
                this.connectionLostCallback = callbackContext;
                return true;
            case "acceptConnection":
                endpointId = args.getString(0);
                this.acceptConnection(endpointId, callbackContext);
                return true;
            case "denyConnection":
                endpointId = args.getString(0);
                this.denyConnection(endpointId, callbackContext);
                return true;
            case "sendPayload":
                endpointId = args.getString(0);
                payload = args.getString(1);
                //payload = args.getJSONObject(1);
                this.sendPayload(endpointId, payload, callbackContext);
                return true;
            case "onPayloadReceived":
                this.payloadReceivedCallback = callbackContext;
                return true;
            case "disconnectFromEndpoint":
                endpointId = args.getString(0);
                this.disconnectFromEndpoint(endpointId, callbackContext);
                return true;
            default:
                return false;
        }
    }

    protected void getPermissions(int requestCode, String[] permissions) {
        cordova.requestPermissions(this, requestCode, permissions);
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                // TO DO
                return;
            }
        }
    }

    private void startAdvertising(String deviceName, String strategyType, String serviceId, CallbackContext callbackContext) {
        Strategy STRATEGY = strategies.get(strategyType);
        AdvertisingOptions advertisingOptions = new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(this.context).startAdvertising(deviceName, serviceId, connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener((Void unused) -> {
                    callbackContext.success(deviceName + " is advertising for service " + serviceId);
                }).addOnFailureListener((Exception e) -> {
                    callbackContext.error(e.getMessage());
                }
        );
    }

    private void startDiscovery(String strategyType, String serviceId, CallbackContext callbackContext) {
        Strategy STRATEGY = strategies.get(strategyType);
        DiscoveryOptions discoveryOptions = new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(this.context).startDiscovery(serviceId, endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener((Void unused) -> {
                    callbackContext.success("Discovering for service " + serviceId);
                }).addOnFailureListener((Exception e) -> {
                    callbackContext.error(e.getMessage());
                }
        );
    }

    private void acceptConnection(String endpointId, CallbackContext callbackContext) {
        Nearby.getConnectionsClient(this.context).acceptConnection(endpointId, payloadCallback)
                .addOnSuccessListener((Void unused) -> {
                    callbackContext.success();
                }).addOnFailureListener((Exception e) -> {
                    callbackContext.error(e.getMessage());
                }
        );
    }

    private void denyConnection(String endpointId, CallbackContext callbackContext) {
        Nearby.getConnectionsClient(this.context).rejectConnection(endpointId)
                .addOnSuccessListener((Void unused) -> {
                    callbackContext.success();
                }).addOnFailureListener((Exception e) -> {
                    callbackContext.error(e.getMessage());
                }
        );
    }

    private void sendPayload(String endpointId, String payloadString, CallbackContext callbackContext) {
        byte[] bytesPayload = payloadString.getBytes();
        Payload payload = Payload.fromBytes(bytesPayload);
        Nearby.getConnectionsClient(this.context).sendPayload(endpointId, payload)
                .addOnSuccessListener((Void unused) -> {
                    callbackContext.success();
                }).addOnFailureListener((Exception e) -> {
                    callbackContext.error(e.getMessage());
                }
        );
    }

    private void stopAdvertising(CallbackContext callbackContext) {
        Nearby.getConnectionsClient(this.context).stopAdvertising();
        callbackContext.success();
    }

    private void stopDiscovery(CallbackContext callbackContext) {
        Nearby.getConnectionsClient(this.context).stopDiscovery();
        callbackContext.success();
    }

    private void stopAllEndpoints(CallbackContext callbackContext) {
        Nearby.getConnectionsClient(this.context).stopAllEndpoints();
        callbackContext.success();
    }

    private void disconnectFromEndpoint(String endpointId, CallbackContext callbackContext) {
        Nearby.getConnectionsClient(this.context).disconnectFromEndpoint(endpointId);
        callbackContext.success();
    }
}
