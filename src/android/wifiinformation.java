package com.mrustudio.plugin.wifiinformation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class wifiinformation extends CordovaPlugin {
  private static final String GET_SAMPLE_WIFI_INFO = "getSampleInfo";
  private static final String GET_WIFI_INFORMATION = "getWifiInfo";
  private static final String GET_ACTIVE_DEVICES = "getActiveDevices";
  private static final String GET_DHCP_INFO = "getDHCPInfo";
  private static final String GET_Permission = "getPermission";
  private static final String check_permission = "checkPermission";
  private static final String get_host_ip_address = "getHostIp";

  protected final static String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE};

  private static final int PERMISSION_REQUEST_CODE = 200;

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
    try {
      if (GET_Permission.equals(action)) {
        requestPermission();
        return true;

      } else if (check_permission.equals(action)) {
        return checkPermission2(callbackContext);

      } else if (GET_SAMPLE_WIFI_INFO.equals(action)) {
        return getSampleInfo(callbackContext);

      } else if (GET_WIFI_INFORMATION.equals(action)) {
        return getWifiInformation(callbackContext);

      } else if (GET_ACTIVE_DEVICES.equals(action)) {
        return getActiveDeviceList(callbackContext);

      } else if (GET_DHCP_INFO.equals(action)) {
        return getDHCPInformation(callbackContext);

        // get Real Host IP Address
      } else if (get_host_ip_address.equals(action)) {
        cordova.getThreadPool().execute(() -> {
          try {
            InetAddress inetAddress = InetAddress.getByName("checkip.amazonaws.com");
            if (inetAddress != null && !inetAddress.toString().equals("")) {
              RequestQueue queue = Volley.newRequestQueue(cordova.getContext());
              String url_ip = "http://checkip.amazonaws.com/";
              StringRequest stringRequest = new StringRequest(Request.Method.GET, url_ip, ip -> {
                JSONObject obj = new JSONObject();
                try {
                  obj.put("host_ip", ip);
                  callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
                  callbackContext.success();

                } catch (JSONException e) {
                  e.printStackTrace();
                  callbackContext.error("Host IP Not Found!");
                  callbackContext.success();
                }
              }, error -> {
                callbackContext.error("Host IP Not Found!");
                callbackContext.success();
              });
              queue.add(stringRequest);
            }

          } catch (Exception ex) {
            // Log.d("--------------", "No Internet Connection");
            callbackContext.error("Wi-fi Connected but Internet not available!");
            callbackContext.success();
          }
        });
        // cordova.getThreadPool().execute(() -> {
        //   RequestQueue queue = Volley.newRequestQueue(cordova.getContext());
        //   String url_ip = "http://checkip.amazonaws.com/";
        //   StringRequest stringRequest = new StringRequest(Request.Method.GET, url_ip, ip -> {
        //     JSONObject obj = new JSONObject();
        //     try {
        //       obj.put("host_ip", ip);
        //       callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
        //       callbackContext.success();

        //     } catch (JSONException e) {
        //       e.printStackTrace();
        //       callbackContext.error("Host IP Not Found!");
        //       callbackContext.success();
        //     }
        //   }, error -> {
        //     callbackContext.error("Host IP Not Found!");
        //     callbackContext.success();
        //   });
        //   queue.add(stringRequest);
        // });
        return true;
      }

      callbackContext.error("Error no such method '" + action + "'");
      return false;
    } catch (Exception e) {
      callbackContext.error("Error while calling ''" + action + "' '" + e.getMessage());
      return false;
    }
  }

  // get Wi-Fi Info
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @SuppressLint("HardwareIds")
  private boolean getWifiInformation(CallbackContext callbackContext) throws JSONException {
    WifiManager wifi_Manager = (WifiManager) cordova.getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    assert wifi_Manager != null;
    WifiInfo connectionInfo = wifi_Manager.getConnectionInfo();
    ConnectivityManager cm = (ConnectivityManager) cordova.getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    assert cm != null;
    NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    DhcpInfo dhcpInfo = wifi_Manager.getDhcpInfo();

    String fail = "0.0.0.0";
    String wifiSSID = null, wifiBSSID = null, wifiFrequency = null, wifiChannel = null, wifiLinkSpeed = null, wifiSignal = null, wifiIP = null, wifiMAC = null;
    String dhcp_ip = null, dhcp_server = null, dhcp_gateway = null, dhcp_netmask = null, dhcp_dns1 = null, dhcp_dns2 = null;
    int dhcp_lease = 0;

    if (networkInfo.isConnected()) {
      if (connectionInfo.getSupplicantState() == SupplicantState.COMPLETED) {
        // get WIFI Info
        try {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (connectionInfo.getSSID() != null) {
              wifiSSID = connectionInfo.getSSID();
            } else if (networkInfo.getExtraInfo() != null) {
              wifiSSID = networkInfo.getExtraInfo();
            } else {
              wifiSSID = getSSIDForWifiInfo(wifi_Manager, connectionInfo);
            }

          } else {
            wifiSSID = getSSIDForWifiInfo(wifi_Manager, connectionInfo);
          }

          wifiBSSID = connectionInfo.getBSSID();
          wifiFrequency = connectionInfo.getFrequency() + " MHz";
          wifiChannel = String.valueOf(frequencyToChannel(connectionInfo.getFrequency()));
          wifiSignal = signalStrengthToString(connectionInfo.getRssi()) + " (" + connectionInfo.getRssi() + " dBm)";
          wifiLinkSpeed = connectionInfo.getLinkSpeed() + " Mbps";
          wifiIP = formatIPAddress(connectionInfo.getIpAddress());
          wifiMAC = connectionInfo.getMacAddress();
        } catch (Exception exception) {
          exception.printStackTrace();
        }

        // get WIFI DHCP Info
        dhcp_lease = dhcpInfo.leaseDuration;
        dhcp_ip = formatIPAddress(dhcpInfo.ipAddress);
        dhcp_gateway = formatIPAddress(dhcpInfo.gateway);
        dhcp_netmask = formatIPAddress(dhcpInfo.netmask);
        dhcp_dns1 = formatIPAddress(dhcpInfo.dns1);
        dhcp_dns2 = formatIPAddress(dhcpInfo.dns2);
        dhcp_server = formatIPAddress(dhcpInfo.serverAddress);
      }
    }

    // check wifi connection
    if (dhcp_gateway == null || dhcp_gateway.equals(fail)) {
      callbackContext.error("No valid Gateway/IP address identified");
      return false;
    }
//    else if (connectionInfo.getNetworkId() == -1) {
//      Log.i("----------------", "Test 22: " + connectionInfo.getNetworkId());
//      callbackContext.error("No network currently connected.");
//      return false;
//    }

    JSONObject obj = new JSONObject();
    JSONObject wifi_info = new JSONObject();
    JSONObject wifi_dhcp = new JSONObject();

    // set Wi-Fi info
    wifi_info.put("ssid", wifiSSID);
    wifi_info.put("bssid", wifiBSSID);
    wifi_info.put("frequency", wifiFrequency);
    wifi_info.put("channel", wifiChannel);
    wifi_info.put("link_speed", wifiLinkSpeed);
    wifi_info.put("signal_strength", wifiSignal);
    wifi_info.put("ip", wifiIP);
    wifi_info.put("mac", wifiMAC);

    // set Wi-Fi DHCP info
    wifi_dhcp.put("ip", dhcp_ip);
    wifi_dhcp.put("gateway", dhcp_gateway);
    wifi_dhcp.put("netmask", dhcp_netmask);
    wifi_dhcp.put("dns1", dhcp_dns1);
    wifi_dhcp.put("dns2", dhcp_dns2);
    wifi_dhcp.put("dhcp_server", dhcp_server);
    wifi_dhcp.put("lease", dhcp_lease);

    obj.put("wifi_info", wifi_info);
    obj.put("wifi_dhcp", wifi_dhcp);
    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
    return true;
    // WifiManager wifi_Manager = (WifiManager) cordova.getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    // assert wifi_Manager != null;
    // WifiInfo connectionInfo = wifi_Manager.getConnectionInfo();
    // ConnectivityManager cm = (ConnectivityManager) cordova.getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    // assert cm != null;
    // NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    // DhcpInfo dhcpInfo = wifi_Manager.getDhcpInfo();

    // String fail = "0.0.0.0";
    // String wifiSSID = null, wifiBSSID = null, wifiFrequency = null, wifiChannel = null, wifiLinkSpeed = null, wifiSignal = null, wifiIP = null, wifiMAC = null;
    // String dhcp_ip = null, dhcp_server = null, dhcp_gateway = null, dhcp_netmask = null, dhcp_dns1 = null, dhcp_dns2 = null;
    // int dhcp_lease = 0;

    // assert networkInfo != null;
    // if (networkInfo.isConnected()) {
    //   if (connectionInfo.getSupplicantState() == SupplicantState.COMPLETED) {
    //     // get WIFI Info
    //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    //       wifiSSID = connectionInfo.getSSID();
    //     } else {
    //       wifiSSID = getSSIDForWifiInfo(wifi_Manager, connectionInfo);
    //     }
    //     wifiBSSID = connectionInfo.getBSSID();
    //     wifiFrequency = connectionInfo.getFrequency() + " MHz";
    //     wifiChannel = String.valueOf(frequencyToChannel(connectionInfo.getFrequency()));
    //     wifiSignal = signalStrengthToString(connectionInfo.getRssi())+" ("+connectionInfo.getRssi()+" dBm)";
    //     wifiLinkSpeed = connectionInfo.getLinkSpeed() + " Mbps";
    //     wifiIP = formatIPAddress(connectionInfo.getIpAddress());
    //     wifiMAC = connectionInfo.getMacAddress();

    //     // get WIFI DHCP Info
    //     dhcp_lease = dhcpInfo.leaseDuration;
    //     dhcp_ip = formatIPAddress(dhcpInfo.ipAddress);
    //     dhcp_gateway = formatIPAddress(dhcpInfo.gateway);
    //     dhcp_netmask = formatIPAddress(dhcpInfo.netmask);
    //     dhcp_dns1 = formatIPAddress(dhcpInfo.dns1);
    //     dhcp_dns2 = formatIPAddress(dhcpInfo.dns2);
    //     dhcp_server = formatIPAddress(dhcpInfo.serverAddress);
    //   }
    // }

    // // check wifi connection
    // if (dhcp_gateway == null || dhcp_gateway.equals(fail)) {
    //   callbackContext.error("No valid Gateway/IP address identified");
    //   return false;
    // } else if (connectionInfo.getNetworkId() == -1) {
    //   callbackContext.error("No network currently connected.");
    //   return false;
    // }

    // JSONObject obj = new JSONObject();
    // JSONObject wifi_info = new JSONObject();
    // JSONObject wifi_dhcp = new JSONObject();

    // // set Wi-Fi info
    // wifi_info.put("ssid", wifiSSID);
    // wifi_info.put("bssid", wifiBSSID);
    // wifi_info.put("frequency", wifiFrequency);
    // wifi_info.put("channel", wifiChannel);
    // wifi_info.put("link_speed", wifiLinkSpeed);
    // wifi_info.put("signal_strength", wifiSignal);
    // wifi_info.put("ip", wifiIP);
    // wifi_info.put("mac", wifiMAC);

    // // set Wi-Fi DHCP info
    // wifi_dhcp.put("ip", dhcp_ip);
    // wifi_dhcp.put("gateway", dhcp_gateway);
    // wifi_dhcp.put("netmask", dhcp_netmask);
    // wifi_dhcp.put("dns1", dhcp_dns1);
    // wifi_dhcp.put("dns2", dhcp_dns2);
    // wifi_dhcp.put("dhcp_server", dhcp_server);
    // wifi_dhcp.put("lease", dhcp_lease);

    // obj.put("wifi_info", wifi_info);
    // obj.put("wifi_dhcp", wifi_dhcp);

    // callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
    // return true;
  }

  // get ActiveDevices
  private boolean getActiveDeviceList(CallbackContext callbackContext) throws JSONException {
    WifiManager wifi_Manager = (WifiManager) cordova.getActivity().getApplicationContext()
      .getSystemService(Context.WIFI_SERVICE);
    assert wifi_Manager != null;
    WifiInfo connectionInfo = wifi_Manager.getConnectionInfo();

    byte[] myIPAddress = BigInteger.valueOf(connectionInfo.getIpAddress()).toByteArray();
    ArrayUtils.reverse(myIPAddress);

    InetAddress netAddress;
    String fail = "0.0.0.0";
    String hostIpAddress = null;
    ArrayList<String> activeDevices = new ArrayList<>();

    try {
      netAddress = InetAddress.getByAddress(myIPAddress);
      hostIpAddress = netAddress.getHostAddress();
      String subnet = hostIpAddress.substring(0, hostIpAddress.lastIndexOf(".") + 1);
      // ArrayList<String> host = new ArrayList<String>();
      // Log.d(TAG, "subnet: " + subnet);

      for (int i = 0; i < 255; i++) {
        String testIp = subnet + i;
        // Log.d(TAG, "Trying ip: " + testIp);
        InetAddress address = InetAddress.getByName(testIp);
        boolean reachable = address.isReachable(400);
        String hostName = address.getHostName();
        String cHostName = address.getCanonicalHostName();
        // security.checkConnect (hostName, 80);

        if (reachable) {
          activeDevices.add("IP: " + testIp + "\nHost Name: " + hostName + "\nCanonical Host Name: " + cHostName);
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
    assert wifi_Manager != null;
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
    assert wifi_Manager != null;
    WifiInfo connectionInfo = wifi_Manager.getConnectionInfo();
    DhcpInfo dhcp = wifi_Manager.getDhcpInfo();
    InetAddress inetAddress;

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

  private boolean checkPermission() {
    int result = ContextCompat.checkSelfPermission(cordova.getContext(), ACCESS_FINE_LOCATION);
    int result1 = ContextCompat.checkSelfPermission(cordova.getContext(), ACCESS_COARSE_LOCATION);

    return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
  }

  private boolean checkPermission2(CallbackContext callbackContext) {
    JSONObject obj = new JSONObject();
    try {
      int result = ContextCompat.checkSelfPermission(cordova.getContext(), ACCESS_FINE_LOCATION);
      int result1 = ContextCompat.checkSelfPermission(cordova.getContext(), ACCESS_COARSE_LOCATION);

      obj.put("PERMISSION_GRANTED", result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED);

    } catch (Exception e) {
      callbackContext.error("No network currently connected.");
      return false;
    }

    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, obj));
    return true;
  }

  private void requestPermission() {
    ActivityCompat.requestPermissions(cordova.getActivity(),
      new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
  }

  // convert raw ip in string ip
  private String formatIPAddress(int rawIp) {
    try {
      byte[] myIPAddress = BigInteger.valueOf(rawIp).toByteArray();
      ArrayUtils.reverse(myIPAddress);
      InetAddress inetAddress = InetAddress.getByAddress(myIPAddress);
      return inetAddress.getHostAddress();
    } catch (UnknownHostException ignored) {
    }
    return "0.0.0.0";
  }

  // get Wi-FI name
  public String getSSIDForWifiInfo(WifiManager manager, WifiInfo wifiInfo) {
    @SuppressLint("MissingPermission") List<WifiConfiguration> listOfConfigurations = manager.getConfiguredNetworks();
    for (int index = 0; index < listOfConfigurations.size(); index++) {
      WifiConfiguration configuration = listOfConfigurations.get(index);
      if (configuration.networkId == wifiInfo.getNetworkId()) {
        return configuration.SSID;
      }
    }
    return null;
  }

  // convert frequency to channel number
  private int frequencyToChannel(int freq) {
    if (freq == 2484) return 14;

    if (freq < 2484) return (freq - 2407) / 5;

    return freq / 5 - 1000;
  }

  // Wifi Signal Strength Indicator String
  private String signalStrengthToString(int signal) {
    String signal_int = String.valueOf(signal);
    String signal_level;
    int num1 = Integer.parseInt(signal_int.replace("-", ""));
    if (num1 <= 50) {
      signal_level = "Excellent";
      //color code #245dbc
    } else if (num1 <= 60) {
      signal_level = "Good";
      //color code #02a315
    } else if (num1 <= 70) {
      signal_level = "Fair";
      //color code #e4e21e
    } else {
      signal_level = "Week";
      //color code #cd0101
    }
    return signal_level;
  }

  // get Device MAC Address
  private static String getDeviceWifiMacAddress() {
    try {
      List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface nif : all) {
        if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

        byte[] macBytes = nif.getHardwareAddress();
        if (macBytes == null) {
          return "";
        }

        StringBuilder res1 = new StringBuilder();
        for (byte b : macBytes) {
          res1.append(String.format("%02X:", b));
        }

        if (res1.length() > 0) {
          res1.deleteCharAt(res1.length() - 1);
        }
        return res1.toString();
      }
    } catch (Exception ignored) {
    }
    return "02:00:00:00:00:00";
  }

}
