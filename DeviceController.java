/* 
 * Thingsboard에 대량의 장치를 생성하고 삭제하는 Java 언어로 작성된 모듈.
 * 
 * @version 1.0
 * @date : 2023-06-13
 * 
 * 1, 2, 3 순으로 한 줄씩 실행. 다른 두 줄은 주석처리. 
*/

package org.thingsboard;

import java.io.IOException;

public class DeviceController {

	public static void main(String[] args) throws IOException {
		TokenGenerator.main(args);	// 1. Thingsboard 로그인. 엑세스 토큰 생성.
//		DeviceCreate.main(args); 	// 2. 장치 생성. 장치 연결. 데이터 전송.
//		DeviceDelete.main(args); 	// 3. 장치 삭제.
	}

}


