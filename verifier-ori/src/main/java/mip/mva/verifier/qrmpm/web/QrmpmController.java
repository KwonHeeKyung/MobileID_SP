package mip.mva.verifier.qrmpm.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonSyntaxException;

import mip.mva.verifier.comm.enums.MipErrorEnum;
import mip.mva.verifier.comm.exception.VerifierException;
import mip.mva.verifier.comm.util.Base64Util;
import mip.mva.verifier.comm.vo.MipApiDataVO;
import mip.mva.verifier.comm.vo.T510VO;
import mip.mva.verifier.config.ConfigBean;
import mip.mva.verifier.qrmpm.service.QrmpmService;

@RestController
@RequestMapping("/qrmpm")
public class QrmpmController {

	private static final Logger LOGGER = LoggerFactory.getLogger(QrmpmController.class);

	/** QR-MPM Service */
	private final QrmpmService qrmpmService;

	/**
	 * 생성자
	 * 
	 * @param qrmpmService QR-MPM Service
	 */
	public QrmpmController(QrmpmService qrmpmService) {
		this.qrmpmService = qrmpmService;
	}

	/**
	 * QR-MPM 시작
	 * 
	 * @MethodName : start
	 * @param mipApiData {"data":"base64로 인코딩된 QR-MPM 정보"}
	 * @return {"result":true, "data":"base64로 인코딩된 M200 메시지"}
	 * @throws VerifierException
	 */
	@RequestMapping(value = "/start")
	public MipApiDataVO start(@RequestBody MipApiDataVO mipApiData) throws VerifierException {
		String data = Base64Util.decode(mipApiData.getData());

		LOGGER.debug("data : {}", data);

		T510VO t510 = null;

		try {
			t510 = ConfigBean.gson.fromJson(data, T510VO.class);
		} catch (JsonSyntaxException e) {
			throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "t510");
		}

		t510 = qrmpmService.start(t510);

		mipApiData.setResult(true);
		mipApiData.setData(Base64Util.encode(ConfigBean.gson.toJson(t510)));

		return mipApiData;
	}

}
