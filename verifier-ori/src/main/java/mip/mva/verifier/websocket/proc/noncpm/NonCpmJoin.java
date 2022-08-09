package mip.mva.verifier.websocket.proc.noncpm;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import mip.mva.verifier.comm.enums.ProxyErrorEnum;
import mip.mva.verifier.comm.exception.VerifierException;
import mip.mva.verifier.comm.util.Base64Util;
import mip.mva.verifier.comm.util.HttpUtil;
import mip.mva.verifier.comm.util.SpringUtil;
import mip.mva.verifier.comm.vo.MipApiDataVO;
import mip.mva.verifier.comm.vo.WsInfoVO;
import mip.mva.verifier.config.ConfigBean;
import mip.mva.verifier.websocket.vo.MsgWaitJoin;

/**
 * @Project     : 모바일 운전면허증 서비스 구축 사업
 * @PackageName : mip.mva.verifier.websocket.proc.noncpm
 * @FileName    : NonCpmJoin.java
 * @Author      : Min Gi Ju
 * @Date        : 2022. 5. 31.
 * @Description : Non CPM Join 처리
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2022. 5. 31.    Min Gi Ju        최초생성
 */
public class NonCpmJoin {

	private static final Logger LOGGER = LoggerFactory.getLogger(NonCpmJoin.class);

	/**
	 * wait_join 메세지 처리
	 * 
	 * @MethodName : procWaitJoin
	 * @param message 메세지
	 * @param session Websocket 세션
	 * @param wsInfo Websocket 정보
	 * @throws VerifierException
	 */
	public void procWaitJoin(String message, Session session, WsInfoVO wsInfo) throws VerifierException {
		LOGGER.debug("message : {}", message);

		try {
			MsgWaitJoin msgWaitJoin = new Gson().fromJson(message, MsgWaitJoin.class);

			String trxcode = wsInfo.getTrxcode();
			
			// Message validation
			if (trxcode == null) {
				throw new VerifierException(ProxyErrorEnum.MISSING_MANDATORY_ITEM, null, "trxcode");
			}

			// 요청 메시지의 거래코드가 현재 거래코드와 일치하는지 비교
			if (!trxcode.equals(msgWaitJoin.getTrxcode())) {
				throw new VerifierException(ProxyErrorEnum.TRXCODE_NOT_FOUND, null, "trxcode");
			}

			// NonCpmClient.start() 한 WebController 에서 wait_join 을 받아서 QR 을 응대장치에 노출
			// QrMpmController.qrInfoReq --> QrMpmService.qrInfoRequest
			// QR을 신분증앱에 보여주면 앱이 촬여하여 중계서버로 join 메시지 전송 함
			// wait_join에 대한 응답 메시지는 없음

			ConfigBean configBean = (ConfigBean) SpringUtil.getBean(ConfigBean.class);

			if (configBean.getSpApiUse()) { // SP API를 사용 할 경우
				try {
					Map<String, Object> input = new HashMap<String, Object>();

					input.put("msgWaitJoin", msgWaitJoin);

					MipApiDataVO mipApiData = new MipApiDataVO();

					mipApiData.setData(Base64Util.encode(ConfigBean.gson.toJson(input)));

					String resultStr = HttpUtil.executeHttpPost(configBean.getSpApiJoin(), mipApiData);

					mipApiData = ConfigBean.gson.fromJson(resultStr, MipApiDataVO.class);

					if (!mipApiData.getResult()) {
						LOGGER.error("SP API 실패 - trxcode : {}", trxcode);
					}
				} catch (Exception e) {
					LOGGER.error("SP API 호출 오류 - trxcode : {}", trxcode, e);
				}
			}
		} catch (VerifierException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		wsInfo.setStatus(ConfigBean.WAIT_JOIN);
	}

}
