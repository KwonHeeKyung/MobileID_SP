package mip.mva.verifier.websocket.proc.cpm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Base64;

import mip.mva.verifier.comm.enums.ProxyErrorEnum;
import mip.mva.verifier.comm.exception.VerifierException;
import mip.mva.verifier.comm.service.ProxyService;
import mip.mva.verifier.comm.util.Base64Util;
import mip.mva.verifier.comm.util.HttpUtil;
import mip.mva.verifier.comm.util.SpringUtil;
import mip.mva.verifier.comm.vo.MipApiDataVO;
import mip.mva.verifier.comm.vo.VP;
import mip.mva.verifier.comm.vo.WsInfoVO;
import mip.mva.verifier.config.ConfigBean;
import mip.mva.verifier.websocket.vo.MsgError;
import mip.mva.verifier.websocket.vo.MsgFinish;
import mip.mva.verifier.websocket.vo.MsgVp;

/**
 * @Project     : 모바일 운전면허증 서비스 구축 사업
 * @PackageName : mip.mva.sp.websocket.proc.cpm
 * @FileName    : CpmVp.java
 * @Author      : Min Gi Ju
 * @Date        : 2022. 5. 31.
 * @Description : CPM 검증 메세지 처리 Class
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2022. 5. 31.    Min Gi Ju        최초생성
 */
public class CpmVp {

	private static final Logger LOGGER = LoggerFactory.getLogger(CpmVp.class);

	/**
	 * vp 메세지 처리
	 * 
	 * @MethodName : procVp
	 * @param message 메세지
	 * @param session Websocket 세션
	 * @param wsInfo vp 정보
	 */
	public void procVp(String message, Session session, WsInfoVO wsInfo) throws VerifierException {
		LOGGER.debug("message : {}", message);

		String trxcode = null;
		String sendMsg = null;

		try {
			MsgVp msgVp = ConfigBean.gson.fromJson(message, MsgVp.class);

			trxcode = wsInfo.getTrxcode();
			
			VP vp = msgVp.getVp();
			
			if (trxcode == null) {
				throw new VerifierException(ProxyErrorEnum.MISSING_MANDATORY_ITEM, null, "trxcode");
			}

			if (vp == null) {
				throw new VerifierException(ProxyErrorEnum.MISSING_MANDATORY_ITEM, null, "vp");
			}

			if (!trxcode.equals(msgVp.getTrxcode())) {
				throw new VerifierException(ProxyErrorEnum.TRXCODE_NOT_FOUND, null, "trxcode");
			}

			ConfigBean configBean = (ConfigBean) SpringUtil.getBean(ConfigBean.class);

			ProxyService proxyService = (ProxyService) SpringUtil.getBean(ProxyService.class);

			proxyService.verifyVP(trxcode, vp);

			MsgFinish msgFinish = new MsgFinish(trxcode);

			if (configBean.getSpApiUse()) { // SP API를 사용 할 경우
				try {
					Map<String, Object> input = new HashMap<String, Object>();

					input.put("msgVp", msgVp);
					input.put("msgFinish", msgFinish);

					MipApiDataVO mipApiData = new MipApiDataVO();

					mipApiData.setData(Base64Util.encode(ConfigBean.gson.toJson(input)));

					String resultStr = HttpUtil.executeHttpPost(configBean.getSpApiVp(), mipApiData);

					mipApiData = ConfigBean.gson.fromJson(resultStr, MipApiDataVO.class);

					if (!mipApiData.getResult()) {
						LOGGER.error("SP API 실패 - trxcode : {}", trxcode);
					}
				} catch (Exception e) {
					LOGGER.error("SP API 호출 오류 - trxcode : {}", trxcode, e);
				}
			}

			sendMsg = ConfigBean.gson.toJson(msgFinish);
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
			session.close();
			
			wsInfo.setResult(sendMsg);
			wsInfo.setStatus(ConfigBean.VP);
		} catch (IOException e) {
			throw new VerifierException(ProxyErrorEnum.UNKNOWN_ERROR, trxcode, "sendString");
		}
	}

}
