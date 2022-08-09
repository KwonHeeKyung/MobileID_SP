package mip.mva.verifier.comm.util;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import mip.mva.verifier.comm.enums.MipErrorEnum;
import mip.mva.verifier.comm.exception.VerifierException;

/**
 * @Project     : 모바일 운전면허증 서비스 구축 사업
 * @PackageName : mip.mva.verifier.comm.util
 * @FileName    : HttpUtil.java
 * @Author      : Min Gi Ju
 * @Date        : 2022. 6. 3.
 * @Description : Http Call Util
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2022. 6. 3.    Min Gi Ju        최초생성
 */
public class HttpUtil {

	/**
	 * Http Call(POST) 실행
	 * 
	 * @MethodName : executeHttpPost
	 * @param url URL
	 * @param param 파라미터
	 * @return 결과
	 * @throws VerifierException
	 */
	public static String executeHttpPost(String url, Object param) throws VerifierException {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = null;

		try {
			response = restTemplate.postForEntity(url, param, String.class);
		} catch (RestClientException e) {
			e.printStackTrace();
			throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, null, e.getMessage());
		}

		return response.getBody();
	}

}
