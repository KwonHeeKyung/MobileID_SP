package mip.mva.verifier.push.web;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonSyntaxException;

import mip.mva.verifier.comm.enums.MipErrorEnum;
import mip.mva.verifier.comm.exception.VerifierException;
import mip.mva.verifier.comm.util.Base64Util;
import mip.mva.verifier.comm.vo.MipApiDataVO;
import mip.mva.verifier.comm.vo.T540VO;
import mip.mva.verifier.config.ConfigBean;
import mip.mva.verifier.push.service.PushService;

@RestController
@RequestMapping("/push")
public class PushController {

	/** 푸시 Service */
	private final PushService pushService;

	/**
	 * 생성자
	 * 
	 * @param pushService 푸시 Service
	 */
	public PushController(PushService pushService) {
		this.pushService = pushService;
	}

	/**
	 * 푸시 시작
	 * 
	 * @MethodName : start
	 * @param mipApiData {"data":"base64로 인코딩된 푸시 정보"}
	 * @return {"result":true, "data":"base64로 인코딩된 M200 메시지"}
	 * @throws VerifierException
	 */
	@RequestMapping(value = "/start")
	public MipApiDataVO sendPush(@RequestBody MipApiDataVO mipApiData) throws VerifierException {
		String data = Base64Util.decode(mipApiData.getData());

		T540VO t540 = null;

		try {
			t540 = ConfigBean.gson.fromJson(data, T540VO.class);
		} catch (JsonSyntaxException e) {
			throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "t540");
		}

		t540 = pushService.start(t540);

		mipApiData.setResult(true);
		mipApiData.setData(Base64Util.encode(ConfigBean.gson.toJson(t540)));

		return mipApiData;
	}

}
