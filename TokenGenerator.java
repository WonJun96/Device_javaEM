/*
토큰 생성. 생성된 토큰 token.json에 저장.
 */

package org.thingsboard;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.FileWriter;
import java.io.IOException;

/*
ThingsBoard 호스트 URL, 사용자 ID, 사용자 비밀번호, token.json파일의 경로를 저장
 */
public class TokenGenerator {

    private static final String THINGSBOARD_HOST = JsonParser.getUrl();	// http://192.168.160.90:8080
    private static final String USER_ID = JsonParser.getUserId();		// tenant@thingsboard.org
    private static final String USER_PW = JsonParser.getUserPw();		// tenant
    private static final String TOKEN_JSON_PATH = "token.json";			// 생성할 json 파일 경로

    public static void main(String[] args) {
        generateToken();
    }

    private static void generateToken() {
        OkHttpClient client = new OkHttpClient();

        String loginUrl = THINGSBOARD_HOST + "/api/auth/login"; // 로그인 URL
        
        /*
        JSON 요청 본문 생성. 
        요청 본문에는 사용자 ID와 비밀번호가 포함.
         */
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("username", USER_ID);
        requestBody.addProperty("password", USER_PW);

        @SuppressWarnings("deprecation")
		RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestBody.toString());
        /*
        요청 본문과 함께 POST 메서드를 사용하여 요청 객체 생성.
         */
        Request request = new Request.Builder()
                .url(loginUrl)
                .post(body)
                .build();

        /*
		요청을 실행하고 응답을 받은 후, 응답이 성공적인지 확인. 
		성공적인 경우 JSON 응답을 파싱하여 토큰 값을 추출하고, token.json 파일을 업데이트하여 토큰을 저장. 
		but, 실패한 경우 실패 상태 코드와 메시지를 출력.
         */
        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            if (response.isSuccessful()) {
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                String token = jsonResponse.get("token").getAsString();

                // Update token.json file with the token
                JsonObject tokenJson = gson.fromJson(readTokenJson(), JsonObject.class);
                tokenJson.addProperty("TOKEN", token);
                writeTokenJson(tokenJson.toString());

                System.out.println("토큰 생성 완료 , 정상적으로 token.json 저장 완료 !!");
            } else {
                System.out.println("Failed to generate token: " + response.code() + " - " + response.message());
                System.out.println("Response Body: " + responseBody);
            }
        } catch (IOException e) {
            System.out.println("Failed to generate token: " + e.getMessage());
        }
    }

    /*
    token.json 파일을 읽어와서 해당 파일의 내용을 문자열 형태로 반환. 
    TOKEN_JSON_PATH 변수에 지정된 경로에 있는 파일을 열고, 스캐너를 사용하여 파일 내용을 읽어옴. 
    파일 내용은 문자열로 저장되고, 반환됨.
     */
    private static String readTokenJson() throws IOException {
        String tokenJson = "";
        try (java.util.Scanner scanner = new java.util.Scanner(new java.io.File(TOKEN_JSON_PATH))) {
            scanner.useDelimiter("\\Z");
            tokenJson = scanner.next();
        }
        return tokenJson;
    }

    
    /*
    writeTokenJson() 메서드는 token.json 파일에 주어진 문자열 형태의 데이터를 작성. 
    TOKEN_JSON_PATH 변수에 지정된 경로에 있는 파일을 열고, FileWriter를 사용하여 파일에 데이터를 작성. 
    데이터는 주어진 문자열로 작성됨.
     */
    private static void writeTokenJson(String tokenJson) throws IOException {
        try (FileWriter writer = new FileWriter(TOKEN_JSON_PATH)) {
            writer.write(tokenJson);
        }
    }
}
