/*
 * 1. 장치 생성
 * 2. 장치 연결
 * 3. 데이터 전송
 */

package org.thingsboard;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.thingsboard.rest.client.RestClient;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.security.DeviceCredentials;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DeviceCreate {
    static String deviceCreationMessage = "장치 생성 완료 !!";
    static String startSendingData = "데이터 전송 시작 !!";
    static String dataTransferComplete = "데이터 전송 완료 !!";
    
    private static final String devicePrefixName = JsonParser.getDevicePrefixName();             // 장치이름
    private static final String USER_ID = JsonParser.getUserId();                                // 사용자이름
    private static final String USER_PW = JsonParser.getUserPw();                                // 사용자비밀번호
    private static final long DEVICE_CREATE_COUNT = JsonParser.getDeviceCreateCount();           // 생성할장치개수
    private static final String URL = JsonParser.getUrl();                                       // http://192.168.160.90:8080
    private static final String MQTT_URL = JsonParser.getBrokerUrl();                            // tcp://192.168.160.90:1883
    private static final String DEVICE_TELEMETRY_TOPIC = JsonParser.getDeviceTelemetryTopic();   // 장치텔레메트리

    /*
    DeviceController 클래스의 main 메서드.
    1.장치(디바이스)를 지정한 값(DEVICE_CREATE_COUNT)만큼 생성.
    2.MQTT 프로토콜로 각 장치에 연결하고 데이터 값을 각 장치에 랜덤 값으로 전송.
    3.deviceInfo.json 파일 생성 및 장치명과 엑세스 토큰 저장
    */
    public static void main(String[] args) throws IOException {

        Map<String, String> deviceInfo = new TreeMap<>();		// 장치 정보를 저장하기 위한 TreeMap, 자동 오름차순.
        List<DeviceId> deviceIdList = new ArrayList<>();		// 생성된 각 장치의 DeviceId르 저장.

        /*RestClient 객체를 생성하고 로그인.*/
        try (RestClient client = new RestClient(URL)) {
            client.login(USER_ID, USER_PW);

            // DEVICE_CREATE_COUNT 값만큼 반복하여 장치를 생성.
            for (int i = 0; i < DEVICE_CREATE_COUNT; i++) {
                Device device = new Device();
                device.setName(devicePrefixName + "_" + String.format("%05d", (i + 1)));	// 장비 이름
                device.setType("Emulator");													// 장비 프로파일
                device.setLabel("smartX-" + (i + 1));										// 라벨 이름
                device = client.saveDevice(device);											// 장치를 저장하고 반환된 장치 객체를 다시 device 변수에 저장.

                // 장치 ID를 사용하여 장치의 엑세스 토큰을 가져옴.
                Optional<DeviceCredentials> access_token = client.getDeviceCredentialsByDeviceId(device.getId());
                String accessToken = access_token.get().getCredentialsId();
                deviceInfo.put(device.getName(), accessToken); // 장치명과 엑세스 토큰을 deviceInfo에 저장

                // 생성된 장치의 정보를 출력.
                System.out.println("Device created: " + device.getName() + " (ID: " + device.getId() + ")");

                // 장치명을 deviceNameList에 추가.
                deviceIdList.add(new DeviceId(device.getId()));
            }

            client.logout();    // 로그아웃
        }
        System.out.println(deviceCreationMessage);    // 장치 생성 완료 메시지를 출력.

        System.out.println(startSendingData);    // 데이터 전송 시작 메시지 출력.

        /*MQTT 프로토콜로 연결하고 데이터를 각 장치에 랜덤 값으로 쏨*/
        try (
            MqttClient client = new MqttClient(MQTT_URL, MqttClient.generateClientId())) {
            MqttConnectOptions options = new MqttConnectOptions();

            // deviceInfo의 각 항목에 대해 반복합니다.
            for (Map.Entry<String, String> entry : deviceInfo.entrySet()) {
                String deviceName = entry.getKey();
                String accessToken = entry.getValue();

                // MQTT 연결에 사용할 사용자 이름을 엑세스 토큰값을 가져와서 설정.
                options.setUserName(accessToken);

                // MQTT 클라이언트와 연결.
                client.connect(options);

                // 온도와 습도 랜덤값 생성.
                float temperature = generateRandomTemperature();
                float humidity = generateRandomHumidity();

                // 텔레메트리 데이터를 JSON 형식으로 생성.
                String telemetryData = String.format("{\"temperature\": %.2f, \"humidity\": %.2f}", temperature, humidity);

                // MqttMessage 객체를 생성하고 텔레메트리 데이터를 설정.
                MqttMessage message = new MqttMessage(telemetryData.getBytes());
                message.setQos(0);

                // 장치의 텔레메트리 토픽으로 메시지를 전송 or 발행 .
                client.publish(DEVICE_TELEMETRY_TOPIC, message);

                System.out.println("Data published for device: " + deviceName); // 메시지 전송or발행 출력.

                // MQTT 클라이언트와 연결을 종료.
                client.disconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        System.out.println(dataTransferComplete);    // 데이터 전송 완료 메시지 출력.

        /* 
        deviceInfo.json 파일 생성 - 장치명과 엑세스 토큰 
        */
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonDeviceInfo = gson.toJson(deviceInfo);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("deviceInfo.json", false))) {
            writer.write(jsonDeviceInfo);
        }

        /* 
        deviceName.json 파일 생성 - 장치 Id 
        */
        String jsonDeviceId = gson.toJson(deviceIdList);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("deviceId.json", false))) {
            writer.write(jsonDeviceId);
        }
    }

    /*
    generateRandomHumidity 메서드.
    난수로 습도 값을 생성하여 반환.
    */
    private static float generateRandomHumidity() {
        return (float) (Math.random() * 100);
    }

    /*
    generateRandomTemperature 메서드.
    난수로 온도 값을 생성하여 반환.
    */
    private static float generateRandomTemperature() {
        return (float) (Math.random() * 70 - 20);
    }

    /*
    DeviceId 클래스.
    deviceId 속성을 가지는 클래스.
    */
    private static class DeviceId {
        private final org.thingsboard.server.common.data.id.DeviceId deviceId;

        public DeviceId(org.thingsboard.server.common.data.id.DeviceId deviceId) {
            this.deviceId = deviceId;
        }

        @SuppressWarnings("unused")
		public org.thingsboard.server.common.data.id.DeviceId getDeviceId() {
            return deviceId;
        }
    }
}
