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
// import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.SupplicantState;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;
import android.util.Log;

import android.text.format.Formatter;
import android.support.v4.app.ActivityCompat;

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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import org.apache.commons.lang3.*;

public class wifiinformation extends CordovaPlugin {
  public static final String GET_SAMPLE_WIFI_INFO = "getSampleInfo";
  public static final String GET_WIFI_INFORMATION = "getWifiInfo";
  public static final String GET_ACTIVE_DEVICES = "getActiveDevices";
  public static final String GET_DHCP_INFO = "getDHCPInfo";
  public static final String GET_Permission = "getPermission";
  public static final String check_permission = "checkPermission";

  public static final int TAKE_PIC_SEC = 0;
  private static final int LOCATION = 1;
  protected final static String[] permissions = { Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE };

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    try {
      if (GET_Permission.equals(action)) {
        return checkPermission(callbackContext);

      } else if (check_permission.equals(action)) {
        // return checkPermission(callbackContext);

      } else if (GET_SAMPLE_WIFI_INFO.equals(action)) {
        return getSampleInfo(callbackContext);

      } else if (GET_WIFI_INFORMATION.equals(action)) {
        return getWifiInformation(callbackContext);

      } else if (GET_ACTIVE_DEVICES.equals(action)) {
        return getActiveDeviceList(callbackContext);

      } else if (GET_DHCP_INFO.equals(action)) {
        return getDHCPInformation(callbackContext);
      }

      callbackContext.error("Error no such method '" + action + "'");
      return false;
    } catch (Exception e) {
      callbackContext.error("Error while calling ''" + action + "' '" + e.getMessage());
      return false;
    }
  }

  // @Override
  // public void onRequestPermissionsResult(int requestCode, String[] permissions,
  // int[] grantResults) {
  // if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode ==
  // LOCATION) {
  // // User allowed the location and you can read it now
  // getSampleInfo();
  // }
  // }

  // get Wi-Fi Info
  private boolean getWifiInformation(CallbackContext callbackContext) throws JSONException {
    WifiManager wifi_Manager = (WifiManager) cordova.getActivity().getApplicationContext()
        .getSystemService(Context.WIFI_SERVICE);
    WifiInfo connectionInfo = wifi_Manager.getConnectionInfo();
    ConnectivityManager cm = (ConnectivityManager) cordova.getActivity().getApplicationContext()
        .getSystemService(Context.CONNECTIVITY_SERVICE);

    String routerSSID = null;
    String routerBSSID = null;
    if (connectionInfo.getSupplicantState() == SupplicantState.COMPLETED) {
      routerSSID = connectionInfo.getSSID();
      routerBSSID = connectionInfo.getBSSID();
    }

    try {
      if (routerSSID.equals("unknown ssid") || routerSSID.equals("<unknown ssid>") || routerSSID == null) {
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
          routerSSID = info.getExtraInfo();
        }
      }
    } catch (Exception e) {
    }

    int networkID = connectionInfo.getNetworkId();
    int getFrequencyNumber = 0;
    try {
      getFrequencyNumber = connectionInfo.getFrequency();
    } catch (Exception e) {
    }

    String routerIpAddress = Formatter.formatIpAddress(connectionInfo.getIpAddress());
    String routerMAC = connectionInfo.getMacAddress();
    String routerFrequency = "" + getFrequencyNumber;
    String routerLinkSpeed = "" + connectionInfo.getLinkSpeed();
    String routerRSSI = "" + connectionInfo.getRssi();
    // String routerRSSI = "" +
    // WifiManager.calculateSignalLevel(connectionInfo.getRssi(), 10) + "/10dBm";
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
    wifiObject.put("signalStrength", routerRSSI);

    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, wifiObject));
    return true;
  }

  // get ActiveDevices
  private boolean getActiveDeviceList(CallbackContext callbackContext) throws JSONException {
    WifiManager wifi_Manager = (WifiManager) cordova.getActivity().getApplicationContext()
        .getSystemService(Context.WIFI_SERVICE);
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
        // Log.d(TAG, "Trying ip: " + testIp);
        InetAddress address = InetAddress.getByName(testIp);
        boolean reachable = address.isReachable(400);
        String hostName = address.getHostName();
        String cHostName = address.getCanonicalHostName();
        // security.checkConnect (hostName, 80);

        if (reachable) {
          activeDevices.add("IP: " + testIp + "\nHost Name: " + String.valueOf(hostName) + "\nCanonical Host Name: "
              + String.valueOf(cHostName));
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
    WifiManager wifi_Manager = (WifiManager) cordova.getActivity().getApplicationContext()
        .getSystemService(Context.WIFI_SERVICE);
    DhcpInfo dhcp = wifi_Manager.getDhcpInfo();

    int leaseDuration = dhcp.leaseDuration;
    String ip = Formatter.formatIpAddress(dhcp.ipAddress);
    String gateway = Formatter.formatIpAddress(dhcp.gateway);
    String netmask = Formatter.formatIpAddress(dhcp.netmask);
    String dns1 = Formatter.formatIpAddress(dhcp.dns1);
    String dns2 = Formatter.formatIpAddress(dhcp.dns2);
    String dhcp_server = Formatter.formatIpAddress(dhcp.serverAddress);
    String fullInfo = dhcp.toString();
    String fail = "0.0.0.0";

    if (ip == null || ip.equals(fail)) {
      callbackContext.error("No valid IP address identified");
      return false;
    }

    JSONObject dhcpObject = new JSONObject();

    dhcpObject.put("ip", ip);
    dhcpObject.put("gateway", gateway);
    dhcpObject.put("netmask", netmask);
    dhcpObject.put("dns1", dns1);
    dhcpObject.put("dns2", dns2);
    dhcpObject.put("dhcp_server", dhcp_server);
    dhcpObject.put("leaseDuration", leaseDuration);
    dhcpObject.put("fullInfo", fullInfo);

    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, dhcpObject));
    return true;
  }

  // get Wi-Fi Info
  private boolean getSampleInfo(CallbackContext callbackContext) throws JSONException {
    WifiManager wifi_Manager = (WifiManager) cordova.getActivity().getApplicationContext()
        .getSystemService(Context.WIFI_SERVICE);
    WifiInfo connectionInfo = wifi_Manager.getConnectionInfo();
    DhcpInfo dhcp = wifi_Manager.getDhcpInfo();
    InetAddress inetAddress = null;

    String fail = "0.0.0.0";
    String wifiDeviceSSID = "";
    String wifiDeviceBSSID = "";
    String wifiDeviceGateway = Formatter.formatIpAddress(dhcp.gateway);
    String wifiDeviceIPAddress = null;

    // Permission already granted
    if (connectionInfo.getSupplicantState() == SupplicantState.COMPLETED) {
      wifiDeviceSSID = connectionInfo.getSSID();
      wifiDeviceBSSID = connectionInfo.getBSSID();
    }

    byte[] myIPAddress = BigInteger.valueOf(connectionInfo.getIpAddress()).toByteArray();
    ArrayUtils.reverse(myIPAddress);

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

  public boolean checkPermission(CallbackContext callbackContext) throws JSONException {
    JSONObject permissionList = new JSONObject();
    JSONObject obj = new JSONObject();

    boolean storagePermission = PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        && PermissionHelper.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    boolean locationPermission = PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
    // boolean locationPermission2 = PermissionHelper.hasPermission(this,
    // Manifest.permission.ACCESS_COARSE_LOCATION);

    if (!locationPermission) {
      locationPermission = true;
      try {
        PackageManager packageManager = this.cordova.getActivity().getPackageManager();
        String[] permissionsInPackage = packageManager.getPackageInfo(this.cordova.getActivity().getPackageName(),
            PackageManager.GET_PERMISSIONS).requestedPermissions;
        if (permissionsInPackage != null) {
          for (String permission : permissionsInPackage) {
            if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
              locationPermission = false;
              break;

            }
          }
        }
      } catch (NameNotFoundException e) {
      }
    }

    // if (!locationPermission2) {
    // locationPermission2 = true;
    // try {
    // PackageManager packageManager =
    // this.cordova.getActivity().getPackageManager();
    // String[] permissionsInPackage =
    // packageManager.getPackageInfo(this.cordova.getActivity().getPackageName(),
    // PackageManager.GET_PERMISSIONS).requestedPermissions;
    // if (permissionsInPackage != null) {
    // for (String permission : permissionsInPackage) {
    // if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
    // locationPermission2 = false;
    // break;

    // }
    // }
    // }
    // } catch (NameNotFoundException e) {
    // }
    // }

    if (locationPermission && storagePermission) {
      // takePicture(returnType, encodingType);
      permissionList.put("READ_EXTERNAL_STORAGE", true);
      permissionList.put("WRITE_EXTERNAL_STORAGE", true);
      permissionList.put("ACCESS_FINE_LOCATION", true);

    } else if (storagePermission && !locationPermission) {
      PermissionHelper.requestPermission(this, TAKE_PIC_SEC, Manifest.permission.ACCESS_FINE_LOCATION);

    }
    // else if (storagePermission && !locationPermission2) {
    // PermissionHelper.requestPermission(this, TAKE_PIC_SEC,
    // Manifest.permission.ACCESS_COARSE_LOCATION);

    // }
    else if (!storagePermission && locationPermission) {
      PermissionHelper.requestPermissions(this, TAKE_PIC_SEC,
          new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE });

    }

    // else if (!storagePermission && locationPermission2) {
    // PermissionHelper.requestPermissions(this, TAKE_PIC_SEC,
    // new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
    // Manifest.permission.WRITE_EXTERNAL_STORAGE });

    // }
    else {
      PermissionHelper.requestPermissions(this, TAKE_PIC_SEC, permissions);
    }

    obj.put("permissions", permissionList);
    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
    return true;
  }

}
