/*
token.json 파일을 파싱하여 토큰을 가져오는 기능.
*/
package org.thingsboard;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class Token_JsonParser {
	private static JSONObject token;

    static {
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader("token.json")) {
        	token = (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static String getToken() {
		return (String) token.get("TOKEN");
	}
}
