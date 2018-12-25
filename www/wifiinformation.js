var wifiinformation = function() {
};

wifiinformation.getWifiInfo = function(success, fail ) {
    cordova.exec( success, fail, "wifiinformation", "getWifiInfo", [] );
};

wifiinformation.getActiveDevices = function(success, fail ) {
    cordova.exec( success, fail, "wifiinformation", "getActiveDevices", [] );
};

wifiinformation.getDHCPInfo = function(success, fail ) {
    cordova.exec( success, fail, "wifiinformation", "getDHCPInfo", [] );
};

wifiinformation.getSampleInfo = function(success, fail ) {
    cordova.exec( success, fail, "wifiinformation", "getSampleInfo", [] );
};

module.exports = wifiinformation;
