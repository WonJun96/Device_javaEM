/*
 * ThingsBoard 서버에 HTTP DELETE 요청을 보내서 장치를 삭제
 */

package org.thingsboard;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeviceDelete {
    static String DeviceDeletionCompleted = "모든 장치 삭제 완료 !!";
    static String DeletedDeviceInfoJson = "deviceInfo.json 파일 삭제 완료 !!";
    static String DeletedDeviceIdJson = "deviceId.json 파일 삭제 완료 !!";
    static String UnableToDeleteJson = "파일을 삭제할 수 없습니다. 일부 장치는 삭제되지 않습니다...";
    
    private static final String THINGSBOARD_HOST = JsonParser.getUrl();			// http://192.168.160.90:8080
    private static final String TOKEN = Token_JsonParser.getToken();			// 토큰값
    private static final String DEVICE_INFO_JSON_PATH = "deviceInfo.json";		// deviceInfo.json 경로
    private static final String DEVICE_ID_JSON_PATH = "deviceId.json";			// deviceId.json 경로

    public static void main(String[] args) {
        deleteDevices();
    }

    private static void deleteDevices() {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();
        try {
        	// deviceId.json 파일을 읽어서 JsonArray로 변환.
            JsonArray deviceArray = gson.fromJson(new FileReader(DEVICE_ID_JSON_PATH), JsonArray.class);
            
            // deviceArray를 반복하면서 각 장치를 삭제합니다.
            for (int i = 0; i < deviceArray.size(); i++) {
            	// 장치 정보를 JsonObject로 가져옴.
                JsonObject deviceObject = deviceArray.get(i).getAsJsonObject();
                // JsonObject에서 장치 Id를 가져옴.
                JsonElement deviceIdElement = deviceObject.get("deviceId").getAsJsonObject().get("id");
                
                /*
                 * 장치 Id가 null이 아니고 Json 원시 타입의 경우에만 삭제.
                 */
                if (deviceIdElement != null && deviceIdElement.isJsonPrimitive()) {
                    String deviceId = deviceIdElement.getAsString();
                    
                    // 삭제할 장치의 URL 생성.
                    String deleteUrl = THINGSBOARD_HOST + "/api/device/" + deviceId;
                    
                    // 삭제를 위한 HTTP DELETE 요청 생성.
                    Request request = new Request.Builder()
                            .url(deleteUrl)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("X-Authorization", "Bearer " + TOKEN)
                            .delete()
                            .build();

                    // 요청을 시행하고 응답을 받음.
                    Response response = client.newCall(request).execute();

                    // 응답이 성공적으로 받아진다면 장치 삭제 완료 메시지를 출력.
                    if (response.isSuccessful()) {
                        System.out.println("Deleted device: " + deviceId);
                     // 그렇지 않은 경우 실패 메시지와 응답 코드, 응답 본문을 출력.
                    } else {
                        System.out.println("Failed to delete device: " + deviceId);
                        System.out.println("Response code: " + response.code());
                        System.out.println("Response body: " + response.body().string());
                    }
                }
            }

            System.out.println(DeviceDeletionCompleted);

            // deviceInfo.json 파일과 deviceId.json 파일을 삭제.
            File deviceInfoFile = new File(DEVICE_INFO_JSON_PATH);
            File deviceIdFile = new File(DEVICE_ID_JSON_PATH);

            // 모든 장치가 삭제되었는지 확인한 후, 파일을 삭제.
            if (deviceArray.size() != 0) {
                
                if (deviceInfoFile.exists() && deviceIdFile.exists()) {
                    deviceInfoFile.delete();
                    System.out.println(DeletedDeviceInfoJson);
                    deviceIdFile.delete();
                    System.out.println(DeletedDeviceIdJson);
                }
                
            } else {
                System.out.println(UnableToDeleteJson);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}