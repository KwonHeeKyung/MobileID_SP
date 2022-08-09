package mip.mva.verifier.app2app.web;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonSyntaxException;

import mip.mva.verifier.app2app.service.App2AppService;
import mip.mva.verifier.comm.enums.MipErrorEnum;
import mip.mva.verifier.comm.exception.VerifierException;
import mip.mva.verifier.comm.util.Base64Util;
import mip.mva.verifier.comm.vo.MipApiDataVO;
import mip.mva.verifier.comm.vo.T530VO;
import mip.mva.verifier.config.ConfigBean;

/**
 * @Project     : 모바일 운전면허증 서비스 구축 사업
 * @PackageName : mip.mva.verifier.app2app.web
 * @FileName    : App2AppController.java
 * @Author      : Min Gi Ju
 * @Date        : 2022. 6. 8.
 * @Description : App to App 인터페이스 검증 처리 Controller 
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2022. 6. 8.    Min Gi Ju        최초생성
 */
@RestController
@RequestMapping("/app2app")
public class App2AppController {

	/** App to App Service */
	private final App2AppService app2AppService;

	/**
	 * 생성자
	 * 
	 * @param app2AppService App to App Service
	 */
	public App2AppController(App2AppService app2AppService) {
		this.app2AppService = app2AppService;
	}

	/**
	 * App to App 시작
	 * 
	 * @MethodName : start
	 * @param mipApiData {"data":"base64로 인코딩된 푸시 정보"}
	 * @return {"result":true, "data":"base64로 인코딩된 M200 메시지"}
	 * @throws VerifierException
	 */
	@RequestMapping(value = "/start")
	public MipApiDataVO start(@RequestBody MipApiDataVO mipApiData) throws VerifierException {
		String data = Base64Util.decode(mipApiData.getData());

		T530VO t530 = null;

		try {
			t530 = ConfigBean.gson.fromJson(data, T530VO.class);
		} catch (JsonSyntaxException e) {
			throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "t530");
		}

		t530 = app2AppService.start(t530);

		mipApiData.setResult(true);
		mipApiData.setData(Base64Util.encode(ConfigBean.gson.toJson(t530)));

		return mipApiData;
	}

}
