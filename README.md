Wi-Fi Information
=================

Wi-Fi Information plugin for Cordova that supports Android.

## Command Line Install

    cordova plugin add https://github.com/Riyaz0001/cordova-plugin-wifi-info.git

## Global Usage

The plugin creates the global object `wifiinformation`, with the following methods:

* getPermission() - Check Permission Granted or Not
* getSampleInfo(onSuccess, onError)  - Sampley get wifi ip, ssid, mac, or gateway.
* getWifiInfo(onSuccess, onError) - Full wifi information, like: LINK_SPEED_UNITS, FREQUENCY_UNITS, received signal strength (RSSI).
* getActiveDevices(onSuccess, onError) - Get All Active Device List, include: ip, host name.
* getDHCPInfo(onSuccess, onError) - Get WI-FI DHCP information.
* getHostIp(onSuccess, onError) - Get Your Real IP address.

### Using getSampleInfo
The onSuccess() callback has one argument object with the properties `IP, SSID, MAC` and `Gateway`. The onError() callback is provided with a single value describing the error.

```javascript
// Get your wifi router information.
function getWifiInfo() {
    // return Promise
    return new Promise<any>((resolve, reject) => {
      if (this.network.type.toLowerCase() === "wifi") {
        // view wifi router info
        wifiinformation.getWifiInfo(
          (data) => {
          // get wifi basic info
            const wifi_info = {
              ssid:
                data.wifi_info.ssid != undefined
                  ? data.wifi_info.ssid.replace(/"/g, "")
                  : "MyHome Wi-Fi",
              bssid: data.wifi_info.bssid,
              frequency: data.wifi_info.frequency,
              channel: data.wifi_info.channel,
              linkspeed: data.wifi_info.link_speed,
              signal: data.wifi_info.signal_strength,
              ip: data.wifi_info.ip,
              mac: data.wifi_info.mac,
            };

            // get wifi DHCP info
            const wifi_dhcp = {
              server: data.wifi_dhcp.dhcp_server,
              ip: data.wifi_dhcp.ip,
              gateway: data.wifi_dhcp.gateway,
              netmask: data.wifi_dhcp.netmask,
              dns1: data.wifi_dhcp.dns1,
              dns2: data.wifi_dhcp.dns2,
              lease: data.wifi_dhcp.lease,
            };
            // return Promise success.
            resolve([wifi_info, wifi_dhcp]);
          },
          (err) => {
            reject(err);
          }
        );
      } else {
        reject("No Wi-Fi Connected.");
      }
    });
  }
  
  
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

// Get Host IP address
 wifiinformation.getHostIp(
    function(data) => {
      console.log(data);
    },
    function(err) => {
      console.error(err);
    }
 );
```

### Demo App for Ionic v4:
`git clone https://github.com/Riyaz0001/cordova-plugin-wifi-info-ionic4-demo.git`


