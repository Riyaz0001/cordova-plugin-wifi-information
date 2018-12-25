Wi-Fi Information
=================

Wi-Fi Information plugin for Cordova that supports Android.

## Command Line Install

    cordova plugin add https://github.com/Riyaz0001/cordova-plugin-wifi-info.git

## Global Usage

The plugin creates the global object `wifiinformation`, with the following methods:

* getSampleInfo(onSuccess, onError)  - Sampley get wifi ip, ssid, mac, or gateway.
* getWifiInfo(onSuccess, onError) - Full wifi information, like: LINK_SPEED_UNITS, FREQUENCY_UNITS, received signal strength (RSSI).
* getActiveDevices(onSuccess, onError) - Get All Active Device List, include: ip, host name.
* getDHCPInfo(onSuccess, onError) - Get WI-FI DHCP information.

### Using getSampleInfo
The onSuccess() callback has one argument object with the properties `IP, SSID, MAC` and `Gateway`. The onError() callback is provided with a single value describing the error.

```javascript
function onSuccess( wifiInfo ) {
    alert( "IP: " + wifiInfo.ip + 
           "\nSSID:" + wifiInfo.ssid + 
           "\nMAC: " + wifiInfo.mac + 
           "\nGateway: " + wifiInfo.gateway );
}

function onError( error ) {

    // Note: onError() will be called when an IP address & SSID can't be found. eg WiFi is disabled.
    alert( error );
}

wifiinformation.getSampleInfo( onSuccess, onError );
```

### Demo App for Ionic v4:
`git clone https://github.com/Riyaz0001/cordova-plugin-wifi-info-ionic4-demo.git`


