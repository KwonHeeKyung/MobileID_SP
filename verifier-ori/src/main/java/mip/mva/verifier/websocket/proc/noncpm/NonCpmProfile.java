package mip.mva.verifier.websocket.proc.noncpm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import mip.mva.verifier.comm.enums.ModeEnum;
import mip.mva.verifier.comm.enums.ProxyErrorEnum;
import mip.mva.verifier.comm.enums.TrxStsCodeEnum;
import mip.mva.verifier.comm.exception.VerifierException;
import mip.mva.verifier.comm.service.ProxyService;
import mip.mva.verifier.comm.service.TrxInfoService;
import mip.mva.verifier.comm.util.Base64Util;
import mip.mva.verifier.comm.util.HttpUtil;
import mip.mva.verifier.comm.util.SpringUtil;
import mip.mva.verifier.comm.vo.MipApiDataVO;
import mip.mva.verifier.comm.vo.TrxInfoVO;
import mip.mva.verifier.comm.vo.WsInfoVO;
import mip.mva.verifier.config.ConfigBean;
import mip.mva.verifier.websocket.vo.MsgError;
import mip.mva.verifier.websocket.vo.MsgProfile;
import mip.mva.verifier.websocket.vo.MsgWaitProfile;

/**
 * @Project     : 모바일 운전면허증 서비스 구축 사업
 * @PackageName : mip.mva.verifier.websocket.proc.noncpm
 * @FileName    : NonCpmProfile.java
 * @Author      : Min Gi Ju
 * @Date        : 2022. 5. 31.
 * @Description : Non CPM Profile 메세지 처리 Class
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2022. 5. 31.    Min Gi Ju        최초생성
 */
public class NonCpmProfile {

	private static final Logger LOGGER = LoggerFactory.getLogger(NonCpmProfile.class);

	/**
	 * wait_profile 메세지 처리
	 * 
	 * @MethodName : procWaitProfile
	 * @param message 메세지
	 * @param session Websocket 세션
	 * @param wsInfo Websocket 정보
	 */
	public void procWaitProfile(String message, Session session, WsInfoVO wsInfo) throws VerifierException {
		LOGGER.debug("message : {}", message);

		String trxcode = null;
		String sendMsg = null;

		try {
			ConfigBean configBean = (ConfigBean) SpringUtil.getBean(ConfigBean.class);
			MsgWaitProfile msgWaitProfile = ConfigBean.gson.fromJson(message, MsgWaitProfile.class);

			trxcode = msgWaitProfile.getTrxcode();
			
			if (trxcode == null) {
				throw new VerifierException(ProxyErrorEnum.MISSING_MANDATORY_ITEM, null, "trxcode");
			}

			if (ObjectUtils.isEmpty(trxcode)) { // wait_verify를 수신한 경우 그곳의 거래코드와 일치하는지 비교
				wsInfo.setTrxcode(trxcode);
			} else { // wait_verify를 수신하지 않은 경우 여기서 거래코드를 mo에 설정
				if (!trxcode.equals(wsInfo.getTrxcode())) {
					throw new VerifierException(ProxyErrorEnum.TRXCODE_NOT_FOUND, trxcode, "trxcode");
				}
			}

			String profile = this.getProfile(wsInfo);
			String image = configBean.getSpBiImageBase64();
			Boolean ci = false;

			// Non-CPM에서는 ci, image 설정하지 않고 profile만 설정
			MsgProfile msgProfile = new MsgProfile(trxcode, profile, image, ci);

			if (configBean.getSpApiUse()) { // SP API를 사용 할 경우
				try {
					Map<String, Object> input = new HashMap<String, Object>();

					input.put("msgWaitProfile", msgWaitProfile);
					input.put("msgProfile", msgProfile);

					MipApiDataVO mipApiData = new MipApiDataVO();

					mipApiData.setData(Base64Util.encode(ConfigBean.gson.toJson(input)));

					String resultStr = HttpUtil.executeHttpPost(configBean.getSpApiProfile(), mipApiData);

					mipApiData = ConfigBean.gson.fromJson(resultStr, MipApiDataVO.class);

					if (!mipApiData.getResult()) {
						LOGGER.error("SP API 실패 - trxcode : {}", trxcode);
					}
				} catch (Exception e) {
					LOGGER.error("SP API 호출 오류 - trxcode : {}", trxcode, e);
				}
			}

			sendMsg = ConfigBean.gson.toJson(msgProfile);
		} catch (VerifierException e) {
			MsgError msgError = new MsgError(wsInfo.getTrxcode(), e.getErrcode(), e.getErrmsg());

			LOGGER.error(e.getMessage(), e);

			sendMsg = ConfigBean.gson.toJson(msgError);
		} catch (Exception e) {
			MsgError msgError = new MsgError(wsInfo.getTrxcode(), ProxyErrorEnum.UNKNOWN_ERROR.getCode(), ProxyErrorEnum.UNKNOWN_ERROR.getMsg());

			LOGGER.error(e.getMessage(), e);

			sendMsg = ConfigBean.gson.toJson(msgError);
		}

		LOGGER.debug("sendMsg : {}", sendMsg);

		try {
			session.getRemote().sendString(sendMsg);
			
			wsInfo.setResult(sendMsg);
			wsInfo.setStatus(ConfigBean.WAIT_PROFILE);
		} catch (IOException e) {
			throw new VerifierException(ProxyErrorEnum.UNKNOWN_ERROR, trxcode, "sendString");
		}
	}

	/**
	 * Profile 생성 요청
	 * 
	 * @MethodName : getProfile
	 * @param wsInfo Websocket 정보
	 * @return String Profile 정보
	 * @throws VerifierException
	 */
	private String getProfile(WsInfoVO wsInfo) throws VerifierException {
		ProxyService proxyService = (ProxyService) SpringUtil.getBean(ProxyService.class);
		TrxInfoService trxInfoService = (TrxInfoService) SpringUtil.getBean(TrxInfoService.class);

		// 중계서버로 받은 trxcode 를 insert 한다.
		TrxInfoVO trxInfo = new TrxInfoVO();

		trxInfo.setTrxcode(wsInfo.getTrxcode());
		trxInfo.setSvcCode(wsInfo.getSvcCode());
		trxInfo.setMode(ModeEnum.PROXY.getVal());
		trxInfo.setBranchName(wsInfo.getBranchName());
		trxInfo.setDeviceId(wsInfo.getDeviceId());
		trxInfo.setTrxStsCode(TrxStsCodeEnum.SERCIVE_REQ.getVal());

		// 거래코드 등록
		trxInfoService.registTrxInfo(trxInfo);

		return proxyService.getProfile(wsInfo.getTrxcode());
	}

}
