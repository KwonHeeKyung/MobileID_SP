package mip.mva.verifier.comm.web;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonSyntaxException;

import mip.mva.verifier.comm.enums.MipErrorEnum;
import mip.mva.verifier.comm.exception.VerifierException;
import mip.mva.verifier.comm.service.DirectService;
import mip.mva.verifier.comm.service.TrxInfoService;
import mip.mva.verifier.comm.util.Base64Util;
import mip.mva.verifier.comm.vo.M310VO;
import mip.mva.verifier.comm.vo.M320VO;
import mip.mva.verifier.comm.vo.M400VO;
import mip.mva.verifier.comm.vo.M900VO;
import mip.mva.verifier.comm.vo.MipApiDataVO;
import mip.mva.verifier.comm.vo.TrxInfoVO;
import mip.mva.verifier.comm.vo.VP;
import mip.mva.verifier.config.ConfigBean;

@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/mip")
public class MipController {

	private static final Logger LOGGER = LoggerFactory.getLogger(MipController.class);
	
	/** Direct Service */
	private final DirectService directService;
	/** 거래정보 Service */
	private final TrxInfoService trxInfoService;

	/**
	 * 생성자
	 * 
	 * @param directService Direct Service
	 * @param trxInfoService 거래정보 Service
	 */
	public MipController(DirectService directService, TrxInfoService trxInfoService) {
		this.directService = directService;
		this.trxInfoService = trxInfoService;
	}

	/**
	 * Profile 요청
	 * 
	 * @param mipApiData {"data":"base64로 인코딩된 M310 메시지"}
	 * @return {"result":true, "data":"base64로 인코딩된 M310 메시지"}
	 * @throws VerifierException
	 */
	@RequestMapping("/profile")
	public MipApiDataVO getProfile(@RequestBody MipApiDataVO mipApiData) throws VerifierException {
		LOGGER.debug("Profile 요청!");
		
		String data = Base64Util.decode(mipApiData.getData());

		M310VO m310 = null;

		try {
			m310 = ConfigBean.gson.fromJson(data, M310VO.class);
		} catch (JsonSyntaxException e) {
			throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "m310");
		}

		m310 = directService.getProfile(m310);

		mipApiData.setResult(true);
		mipApiData.setData(Base64Util.encode(ConfigBean.gson.toJson(m310)));

		return mipApiData;
	}

	/**
	 * 이미지 요청
	 * 
	 * @param mipApiData {"data":"base64로 인코딩된 M320 메시지"}
	 * @return {"result":true, "data":"base64로 인코딩된 Image Data"}
	 * @throws VerifierException
	 */
	@RequestMapping("/image")
	public MipApiDataVO getImage(@RequestBody MipApiDataVO mipApiData) throws VerifierException {
		LOGGER.debug("BI Image 요청!");
		
		String data = Base64Util.decode(mipApiData.getData());

		M320VO m320 = null;

		try {
			m320 = ConfigBean.gson.fromJson(data, M320VO.class);
		} catch (JsonSyntaxException e) {
			throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "m320");
		}

		String image = directService.getImage(m320);

		mipApiData.setResult(false);
		mipApiData.setData(image);

		return mipApiData;
	}

	/**
	 * VP 검증
	 * 
	 * @param mipApiData {"data":"base64로 인코딩된 M400 메시지"}
	 * @return {"result":true}
	 * @throws VerifierException
	 */
	@RequestMapping("/vp")
	public MipApiDataVO verifyVP(@RequestBody MipApiDataVO mipApiData) throws VerifierException {
		LOGGER.debug("VP 검증!");
		
		String data = Base64Util.decode(mipApiData.getData());

		M400VO m400 = null;

		try {
			m400 = ConfigBean.gson.fromJson(data, M400VO.class);
		} catch (JsonSyntaxException e) {
			throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "m400");
		}

		directService.verifyVP(m400);

		mipApiData.setResult(true);

		return mipApiData;
	}

	/**
	 * 오류 전송
	 * 
	 * @param mipApiData {"data":"base64로 인코딩된 오류 메시지"}
	 * @return {"result":true}
	 * @throws VerifierException
	 */
	@RequestMapping("/error")
	public MipApiDataVO sendError(@RequestBody MipApiDataVO mipApiData) throws VerifierException {
		LOGGER.debug("오류 전송!");
		
		String data = Base64Util.decode(mipApiData.getData());

		M900VO m900 = null;

		try {
			m900 = ConfigBean.gson.fromJson(data, M900VO.class);
		} catch (JsonSyntaxException e) {
			throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "m900");
		}

		directService.sendError(m900);

		mipApiData.setResult(true);

		return mipApiData;
	}

	/**
	 * 거래상태 조회
	 * 
	 * @param mipApiData {"data":"base64로 인코딩된 거래정보"}
	 * @return {"result":true, "data":"base64로 인코딩된 거래정보"}
	 * @throws VerifierException
	 */
	@RequestMapping(value = "/trxsts")
	public MipApiDataVO getTrxsts(@RequestBody MipApiDataVO mipApiData) throws VerifierException {
		LOGGER.debug("거래상태 조회!");
		
		String data = Base64Util.decode(mipApiData.getData());

		TrxInfoVO trxInfo = null;

		try {
			trxInfo = ConfigBean.gson.fromJson(data, TrxInfoVO.class);
		} catch (JsonSyntaxException e) {
			throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "trxInfo");
		}

		trxInfo = trxInfoService.getTrxInfo(trxInfo.getTrxcode());

		mipApiData.setResult(true);
		mipApiData.setData(Base64Util.encode(ConfigBean.gson.toJson(trxInfo)));

		return mipApiData;
	}

	/**
	 * VP 재검증
	 * 
	 * @param mipApiData {"data":"base64로 인코딩된 서비스코드 & VP"}
	 * @return {"result":true}
	 * @throws VerifierException
	 */
	@ResponseBody
	@RequestMapping(value = "/revp", method = { RequestMethod.POST })
	public MipApiDataVO reVerifyVP(@RequestBody MipApiDataVO mipApiData) throws VerifierException {
		LOGGER.debug("VP 재검증!");
		
		String data = Base64Util.decode(mipApiData.getData());

		String svcCode = null;
		VP vp = null;

		try {
			Map<String, String> dataMap = ConfigBean.gson.fromJson(data, HashMap.class);

			svcCode = dataMap.get("svcCode");

			vp = ConfigBean.gson.fromJson(dataMap.get("vp"), VP.class);
		} catch (JsonSyntaxException e) {
			throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "svcCode, vp");
		}

		directService.reVerifyVP(svcCode, vp);

		mipApiData.setResult(true);

		return mipApiData;
	}
	
	/**
	 * VP data 조회
	 * 
	 * @param mipApiData {"data":"base64로 인코딩된 VP"}
	 * @return {"result":true, "data":"base64로 인코딩된 VP data"}
	 * @throws VerifierException
	 */
	@ResponseBody
	@RequestMapping(value = "/vpdata", method = { RequestMethod.POST })
	public MipApiDataVO getVPData(@RequestBody MipApiDataVO mipApiData) throws VerifierException {
		LOGGER.debug("VP data 조회!");
		
		String data = Base64Util.decode(mipApiData.getData());

		VP vp = null;

		try {
			Map<String, String> dataMap = ConfigBean.gson.fromJson(data, HashMap.class);

			vp = ConfigBean.gson.fromJson(dataMap.get("vp"), VP.class);
		} catch (JsonSyntaxException e) {
			throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "vp");
		}

		String vpData = directService.getVPData(vp);

		mipApiData.setResult(true);
		mipApiData.setData(Base64Util.encode(vpData));

		return mipApiData;
	}
	
	/**
	 * CI 조회
	 * 
	 * @param mipApiData {"data":"base64로 인코딩된 VP"}
	 * @return {"result":true, "data":"base64로 인코딩된 CI"}
	 * @throws VerifierException
	 */
	@ResponseBody
	@RequestMapping(value = "/ci", method = { RequestMethod.POST })
	public MipApiDataVO getCI(@RequestBody MipApiDataVO mipApiData) throws VerifierException {
		LOGGER.debug("CI 조회!");
		
		String data = Base64Util.decode(mipApiData.getData());

		VP vp = null;

		try {
			Map<String, String> dataMap = ConfigBean.gson.fromJson(data, HashMap.class);

			vp = ConfigBean.gson.fromJson(dataMap.get("vp"), VP.class);
		} catch (JsonSyntaxException e) {
			throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "vp");
		}

		String ci = directService.getCI(vp);

		mipApiData.setResult(true);
		mipApiData.setData(Base64Util.encode(ci));

		return mipApiData;
	}

}
