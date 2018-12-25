package com.mrustudio.plugin.wifiinformation;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import android.text.format.Formatter;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Proxy.Type;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Enumeration;
import java.util.logging.*;

import java.math.BigInteger;
import java.util.Arrays;
import org.apache.commons.lang3.*; 


public class wifiinformation extends CordovaPlugin {
	public static final String GET_SAMPLE_WIFI_INFO = "getSampleInfo";
    public static final String GET_WIFI_INFORMATION = "getWifiInfo";
    public static final String GET_ACTIVE_DEVICES = "getActiveDevices";
    public static final String GET_DHCP_INFO = "getDHCPInfo";

	

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        boolean permissionGranted = PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_WIFI_STATE);
        boolean permissionGranted2 = PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        
       // Stop always requesting the permission
       if(!permissionGranted && permissionGranted2) {
           // PermissionHelper.requestPermission(this, 2, Manifest.permission.ACCESS_WIFI_STATE);
           PermissionHelper.requestPermissions(this, 2, new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE});
       }

		try {
			if (GET_SAMPLE_WIFI_INFO.equals(action)) {
                return getSampleInfo(callbackContext);
                
            } else if(GET_WIFI_INFORMATION.equals(action)) {
				return getWifiInformation(callbackContext);
            
            } else if(GET_ACTIVE_DEVICES.equals(action)) {
				return getActiveDeviceList(callbackContext);
            
            } else if(GET_DHCP_INFO.equals(action)) {
				return getDHCPInformation(callbackContext);
            }
            
			callbackContext.error("Error no such method '" + action + "'");
			return false;
		} catch(Exception e) {
			callbackContext.error("Error while calling ''" + action + "' '" + e.getMessage());
			return false;
		}
	}


    // get Wi-Fi Info
    private boolean getWifiInformation(CallbackContext callbackContext) throws JSONException {
        WifiManager wifi_Manager = (WifiManager) cordova.getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wifi_Manager.getConnectionInfo();
        
        int networkID = connectionInfo.getNetworkId();
        String routerSSID = connectionInfo.getSSID();
        String routerBSSID = connectionInfo.getBSSID();
        String routerIpAddress = Formatter.formatIpAddress(connectionInfo.getIpAddress());
        String routerMAC = connectionInfo.getMacAddress();
        String routerFrequency = "" + connectionInfo.getFrequency() + "MHz";
        String routerLinkSpeed = "" + connectionInfo.getLinkSpeed() + "Mbps";
        String routerRSSI = "" + WifiManager.calculateSignalLevel(connectionInfo.getRssi(), 10) + "/10dBm";
        String fail = "0.0.0.0";


		if (routerIpAddress == null || routerIpAddress.equals(fail)) {
			callbackContext.error("No valid IP address identified");
			return false;
        
        } else if (routerMAC == null || networkID == -1) {
			callbackContext.error("No network currently connected.");
			return false;
        }
        
        JSONObject wifiObject = new JSONObject();
        
        wifiObject.put("deviceSSID", routerSSID);
        wifiObject.put("deviceBSSID", routerBSSID);
        wifiObject.put("deviceIpAddress", routerIpAddress);
        wifiObject.put("deviceMAC", routerMAC);
        wifiObject.put("deviceFrequency", routerFrequency);
        wifiObject.put("deviceLinkSpeed", routerLinkSpeed);
        wifiObject.put("deviceRSSI", routerRSSI);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, wifiObject));
		return true;
    }



    // get ActiveDevices
    private boolean getActiveDeviceList(CallbackContext callbackContext) throws JSONException {
        WifiManager wifi_Manager = (WifiManager) cordova.getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wifi_Manager.getConnectionInfo();

        
        byte[] myIPAddress = BigInteger.valueOf(connectionInfo.getIpAddress()).toByteArray();
        ArrayUtils.reverse(myIPAddress);

        InetAddress netAddress = null;
        String fail = "0.0.0.0";
        String hostIpAddress = null;
        ArrayList<String> activeDevices = new ArrayList<String>();

        try {
            netAddress = InetAddress.getByAddress(myIPAddress);
            hostIpAddress = netAddress.getHostAddress();
            String subnet = hostIpAddress.substring(0, hostIpAddress.lastIndexOf(".") + 1);
            // ArrayList<String> host = new ArrayList<String>();
            // Log.d(TAG, "subnet: " + subnet);

            for (int i = 0; i < 255; i++) {
                String testIp = subnet + String.valueOf(i);
                //Log.d(TAG, "Trying ip: " + testIp);
                InetAddress address = InetAddress.getByName(testIp);
                boolean reachable = address.isReachable(400);
                String hostName = address.getHostName();
                String cHostName = address.getCanonicalHostName();
                // security.checkConnect (hostName, 80);

                if (reachable) {
                    activeDevices.add("IP: " + testIp + "\nHost Name: " + String.valueOf(hostName) + "\nCanonical Host Name: " + String.valueOf(cHostName));
                }
            }
      
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        
        
		if (hostIpAddress == null || hostIpAddress.equals(fail)) {
			callbackContext.error("No valid IP address identified");
			return false;
        }
        

        JSONObject ipInformation = new JSONObject();
        
        ipInformation.put("ip", hostIpAddress);
        ipInformation.put("deviceList", activeDevices.toString());

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, ipInformation));
		return true;
	}



     // get DHCP Info
     private boolean getDHCPInformation(CallbackContext callbackContext) throws JSONException {
		WifiManager wifi_Manager = (WifiManager) cordova.getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi_Manager.getDhcpInfo();

        String ip = Formatter.formatIpAddress(dhcp.ipAddress);
        String gateway = Formatter.formatIpAddress(dhcp.gateway);
        String fullInfo = dhcp.toString();
        String fail = "0.0.0.0";


		if (ip == null || ip.equals(fail)) {
			callbackContext.error("No valid IP address identified");
			return false;
        }
        
        JSONObject dhcpObject = new JSONObject();
        
        dhcpObject.put("ip", ip);
        dhcpObject.put("gateway", gateway);
        dhcpObject.put("fullInfo", fullInfo);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, dhcpObject));
        return true;
    }


     // get Wi-Fi Info
     private boolean getSampleInfo(CallbackContext callbackContext) throws JSONException {
        WifiManager wifi_Manager = (WifiManager) cordova.getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo connectionInfo = wifi_Manager.getConnectionInfo();
        DhcpInfo dhcp = wifi_Manager.getDhcpInfo();
        InetAddress inetAddress = null;
        
        byte[] myIPAddress = BigInteger.valueOf(connectionInfo.getIpAddress()).toByteArray();
        ArrayUtils.reverse(myIPAddress);


        String fail = "0.0.0.0";
        String wifiDeviceSSID = connectionInfo.getSSID();
        String wifiDeviceBSSID = connectionInfo.getBSSID();
        String wifiDeviceGateway = Formatter.formatIpAddress(dhcp.gateway);
        String wifiDeviceIPAddress = null;

        try {
            inetAddress = InetAddress.getByAddress(myIPAddress);
            wifiDeviceIPAddress = inetAddress.getHostAddress();

        } catch (Exception e1) {
            e1.printStackTrace();
        }


        int networkID = connectionInfo.getNetworkId();
		if (wifiDeviceIPAddress == null || wifiDeviceIPAddress.equals(fail)) {
			callbackContext.error("No valid IP address identified");
			return false;
        
        } else if (wifiDeviceBSSID == null || networkID == -1) {
			callbackContext.error("No network currently connected.");
			return false;
        }
        
        JSONObject wifiObject = new JSONObject();
        
        wifiObject.put("ip", wifiDeviceIPAddress);
        wifiObject.put("ssid", wifiDeviceSSID);
        wifiObject.put("mac", wifiDeviceBSSID);
        wifiObject.put("gateway", wifiDeviceGateway);

        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, wifiObject));
		return true;
    }






	// private JSONObject createProxyInformation (Proxy.Type proxyType, String host, String port) throws JSONException {
	// 	JSONObject proxyInformation = new JSONObject();
	// 	proxyInformation.put("type", proxyType.toString());
	// 	proxyInformation.put("host", host);
	// 	proxyInformation.put("port", port);
	// 	return proxyInformation;
	// }

	// private boolean getHttpProxyInformation(String url, CallbackContext callbackContext) throws JSONException, URISyntaxException {
	// 	JSONArray proxiesInformation = new JSONArray();
	// 	ProxySelector defaultProxySelector = ProxySelector.getDefault();
		
	// 	if(defaultProxySelector != null){
	// 		List<java.net.Proxy> proxyList = defaultProxySelector.select(new URI(url));
	// 		for(java.net.Proxy proxy: proxyList){
	// 			if (java.net.Proxy.Type.DIRECT.equals(proxy.type())) {
    //             	break;
    //         	}
	// 			InetSocketAddress proxyAddress = (InetSocketAddress)proxy.address();
	// 			if(proxyAddress != null){
	// 				proxiesInformation.put(createProxyInformation(proxy.type(), proxyAddress.getHostString(), String.valueOf(proxyAddress.getPort())));
	// 			}
	// 		}
	// 	}

	// 	if(proxiesInformation.length() < 1){
	// 		proxiesInformation.put(createProxyInformation(Proxy.Type.DIRECT, "none", "none"));
	// 	}

	// 	callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, proxiesInformation));
	// 	return true;
	// }

	// private boolean extractIpInfo(String[] ipInfo, CallbackContext callbackContext) throws JSONException {
	// 	String ip = ipInfo[0];
	// 	String subnet = ipInfo[1];
	// 	String fail = "0.0.0.0";
	// 	if (ip == null || ip.equals(fail)) {
	// 		callbackContext.error("No valid IP address identified");
	// 		return false;
	// 	}

	// 	Map<String,String> ipInformation = new HashMap<String,String>();
	// 	ipInformation.put("ip", ip);
	// 	ipInformation.put("subnet", subnet);

	// 	callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject(ipInformation)));
	// 	return true;
	// }

	// private String[] getWiFiIPAddress() {
	// 	WifiManager wifiManager = (WifiManager) cordova.getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
	// 	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	// 	int ip = wifiInfo.getIpAddress();

	// 	String ipString = String.format(
	// 		"%d.%d.%d.%d",
	// 		(ip & 0xff),
	// 		(ip >> 8 & 0xff),
	// 		(ip >> 16 & 0xff),
	// 		(ip >> 24 & 0xff)
	// 		);

	// 	String subnet = "";
	// 	try {
	// 		InetAddress inetAddress = InetAddress.getByName(ipString);
	// 		subnet = getIPv4Subnet(inetAddress);
	// 	} catch (Exception e) {
	// 	}

	// 	return new String[]{ ipString, subnet };
	// }

	// private String[] getCarrierIPAddress() {
	//   try {
	//     for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	//        NetworkInterface intf = (NetworkInterface) en.nextElement();
	//        //Log.e(TAG, "Interface: " + intf.toString() + " name: " + intf.getName() + " display nane: " + intf.getDisplayName() );
	//        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	//           InetAddress inetAddress = enumIpAddr.nextElement();
	// 		   if (!inetAddress.isLoopbackAddress() && (!intf.getName().equals("wlan0")) && inetAddress instanceof Inet4Address) {
	// 			   String ipaddress = inetAddress.getHostAddress().toString();
	// 			   String subnet = getIPv4Subnet(inetAddress);
	// 			   return new String[]{ ipaddress, subnet };
	//           }
	//        }
	//     }
	//   } catch (SocketException ex) {
	//      Log.e(TAG, "Exception in Get IP Address: " + ex.toString());
	//   }
	//   return new String[]{ null, null };
	// }

	// public static String getIPv4Subnet(InetAddress inetAddress) {
	// 	try {
	// 		NetworkInterface ni = NetworkInterface.getByInetAddress(inetAddress);
	// 		List<InterfaceAddress> intAddrs =  ni.getInterfaceAddresses();
	// 		for (InterfaceAddress ia : intAddrs) {
	// 			if (!ia.getAddress().isLoopbackAddress() && ia.getAddress() instanceof Inet4Address) {
	// 				return getIPv4SubnetFromNetPrefixLength(ia.getNetworkPrefixLength()).getHostAddress().toString();
	// 			}
	// 		}
	// 	} catch (Exception e) {
	// 	}
	// 	return "";
	// }

	// public static InetAddress getIPv4SubnetFromNetPrefixLength(int netPrefixLength) {
	// 	try {
	// 		int shift = (1<<31);
	// 		for (int i=netPrefixLength-1; i>0; i--) {
	// 			shift = (shift >> 1);
	// 		}
	// 		String subnet = Integer.toString((shift >> 24) & 255) + "." + Integer.toString((shift >> 16) & 255) + "." + Integer.toString((shift >> 8) & 255) + "." + Integer.toString(shift & 255);
	// 		return InetAddress.getByName(subnet);
	// 	}
	// 	catch(Exception e){
	// 	}
	// 	return null;
	// }
}
