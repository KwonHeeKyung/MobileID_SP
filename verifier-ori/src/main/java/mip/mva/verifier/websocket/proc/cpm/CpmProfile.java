package mip.mva.verifier.websocket.proc.cpm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mip.mva.verifier.comm.enums.ProxyErrorEnum;
import mip.mva.verifier.comm.exception.VerifierException;
import mip.mva.verifier.comm.service.ProxyService;
import mip.mva.verifier.comm.util.Base64Util;
import mip.mva.verifier.comm.util.HttpUtil;
import mip.mva.verifier.comm.util.SpringUtil;
import mip.mva.verifier.comm.vo.MipApiDataVO;
import mip.mva.verifier.comm.vo.WsInfoVO;
import mip.mva.verifier.config.ConfigBean;
import mip.mva.verifier.websocket.vo.MsgError;
import mip.mva.verifier.websocket.vo.MsgProfile;
import mip.mva.verifier.websocket.vo.MsgWaitProfile;

/**
 * @Project     : 모바일 운전면허증 서비스 구축 사업
 * @PackageName : mip.mva.verifier.websocket.proc.cpm
 * @FileName    : CpmProfile.java
 * @Author      : Min Gi Ju
 * @Date        : 2022. 5. 31.
 * @Description : CPM Profile 메세지 처리 Class
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2022. 5. 31.    Min Gi Ju        최초생성
 */
public class CpmProfile {

	private static final Logger LOGGER = LoggerFactory.getLogger(CpmProfile.class);

	/**
	 * wait_profile 메세지 처리
	 * 
	 * @MethodName : procWaitProfile
	 * @param message 메세지
	 * @param session Websocket 세션
	 * @param wsInfo Websocket 정보
	 * @throws VerifierException
	 */
	public void procWaitProfile(String message, Session session, WsInfoVO wsInfo) throws VerifierException {
		LOGGER.debug("message : {}", message);

		String trxcode = null;
		String sendMsg = null;

		try {
			MsgWaitProfile msgWaitProfile = ConfigBean.gson.fromJson(message, MsgWaitProfile.class);

			trxcode = msgWaitProfile.getTrxcode();
			
			if (trxcode == null) {
				throw new VerifierException(ProxyErrorEnum.MISSING_MANDATORY_ITEM, null, "trxcode");
			}

			if (!trxcode.equals(wsInfo.getTrxcode())) {
				throw new VerifierException(ProxyErrorEnum.TRXCODE_NOT_FOUND, trxcode, "trxcode");
			}

			ConfigBean configBean = (ConfigBean) SpringUtil.getBean(ConfigBean.class);

			ProxyService proxyService = (ProxyService) SpringUtil.getBean(ProxyService.class);

			String profile = proxyService.getProfile(trxcode);
			String image = configBean.getSpBiImageBase64();
			Boolean ci = configBean.getSpCi();

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

}
