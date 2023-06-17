/*
conf.json 파일을 파싱하여 정보드을 가져오는 기능.
*/

package org.thingsboard;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class JsonParser {
    private static JSONObject config;

    static {
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader("conf.json")) {
            config = (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static String getIpAddress() {
        return (String) config.get("IP_ADDRESS");
    }

    public static String getPort() {
        return (String) config.get("PORT");
    }

    public static String getBrokerPort() {
        return (String) config.get("BROKER_PORT");
    }

    public static String getUserId() {
        return (String) config.get("USER_ID");
    }

    public static String getUserPw() {
        return (String) config.get("USER_PW");
    }
    
    public static String getDevicePrefixName() {
        return (String) config.get("DEVICEPREFIXNAME");
    }

    public static String getProfileId() {
        return (String) config.get("PROFILEID");
    }

    public static String getDeviceTelemetryTopic() {
    	return (String) config.get("DEVICE_TELEMETRY_TOPIC");
    }

    public static long getDeviceCreateCount() {
        return (long) config.get("DEVICE_CREATE_COUNT");
    }

    public static long getDeviceDeleteCount() {
        return (long) config.get("DEVICE_DELETE_COUNT");
    }
    

    public static String getUrl() {
        String ipAddress = getIpAddress();
        String port = getPort();
        return "http://" + ipAddress + ":" + port;
    }
    
    public static String getBrokerUrl() {
    	String brokerIpAddress = getIpAddress();
    	String brokerPort = getBrokerPort();
    	return "tcp://" + brokerIpAddress + ":" + brokerPort;
    }
}
