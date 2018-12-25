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

### Demo App:
`git clone https://github.com/Riyaz0001/cordova-plugin-wifi-info-ionic4-demo.git`

## License

The MIT License (MIT)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
