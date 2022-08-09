package mip.mva.verifier.qrcpm.web;

import java.util.Base64;

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
import mip.mva.verifier.comm.vo.T520VO;
import mip.mva.verifier.comm.vo.TrxInfoVO;
import mip.mva.verifier.config.ConfigBean;
import mip.mva.verifier.qrcpm.service.QrcpmService;

@RestController
@RequestMapping("/qrcpm")
public class QrcpmController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(QrcpmController.class);

	/** QR-CPM Service */
	private final QrcpmService qrcpmService;

	/**
	 * 생성자
	 * 
	 * @param qrcpmService QR-CPM Service
	 */
	public QrcpmController(QrcpmService qrcpmService) {
		this.qrcpmService = qrcpmService;
	}

	/**
	 * QR-CPM 시작
	 * 
	 * @MethodName : start
	 * @param mipApiData {"data":"base64로 인코딩된 QR-CPM 정보"}
	 * @return {"result":true, "data":"base64로 인코딩된 M120 메시지"}
	 * @throws VerifierException
	 */
	@RequestMapping(value = "/start")
	public MipApiDataVO start(@RequestBody MipApiDataVO mipApiData) throws VerifierException {
		LOGGER.debug("::::::::::QR-CPM START!::::::::::");
		String data = Base64Util.decode(mipApiData.getData());
		LOGGER.debug("In Data="+data);
		T520VO t520 = null;

		try {
			t520 = ConfigBean.gson.fromJson(data, T520VO.class);
		} catch (JsonSyntaxException e) {
			throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "t520");
		}

		TrxInfoVO result = qrcpmService.start(t520);
		
		String json = ConfigBean.gson.toJson(result, TrxInfoVO.class);
		String enc = Base64.getEncoder().encodeToString(json.getBytes());

		mipApiData.setResult(true);
		mipApiData.setData(enc);
		
		return mipApiData;
	}

}
