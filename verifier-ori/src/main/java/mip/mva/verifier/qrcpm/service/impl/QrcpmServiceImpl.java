package mip.mva.verifier.qrcpm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.google.gson.JsonSyntaxException;

import mip.mva.verifier.comm.enums.MipErrorEnum;
import mip.mva.verifier.comm.enums.TrxStsCodeEnum;
import mip.mva.verifier.comm.exception.VerifierException;
import mip.mva.verifier.comm.service.TrxInfoService;
import mip.mva.verifier.comm.util.Base64Util;
import mip.mva.verifier.comm.vo.M120VO;
import mip.mva.verifier.comm.vo.T520VO;
import mip.mva.verifier.comm.vo.TrxInfoVO;
import mip.mva.verifier.comm.vo.WsInfoVO;
import mip.mva.verifier.config.ConfigBean;
import mip.mva.verifier.qrcpm.service.QrcpmService;
import mip.mva.verifier.websocket.client.cpm.CpmClient;

/**
 * @Project     : 모바일 운전면허증 서비스 구축 사업
 * @PackageName : mip.mva.sp.qrcpm.service.impl
 * @FileName    : QrcpmServiceImpl.java
 * @Author      : Min Gi Ju
 * @Date        : 2022. 6. 3.
 * @Description : QR-CPM 인터페이스 검증 처리 ServiceImpl
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2022. 6. 3.    Min Gi Ju        최초생성
 */
@Service
public class QrcpmServiceImpl implements QrcpmService {

	private static final Logger LOGGER = LoggerFactory.getLogger(QrcpmServiceImpl.class);

	/** 커스텀 프로퍼티 */
	private final ConfigBean configBean;
	/** 거래정보 Service */
	private final TrxInfoService trxInfoService;

	/**
	 * 생성자
	 * 
	 * @param configBean 커스텀 프로퍼티
	 * @param trxInfoService 거래정보 Service
	 */
	public QrcpmServiceImpl(ConfigBean configBean, TrxInfoService trxInfoService) {
		this.configBean = configBean;
		this.trxInfoService = trxInfoService;
	}

	/**
	 * QR-CPM 시작
	 * 
	 * @MethodName : start
	 * @param t520 QR-CPM 정보
	 * @return QR-CPM 정보
	 * @throws VerifierException
	 */
	@Override
	public TrxInfoVO start(T520VO t520) throws VerifierException {
		
		LOGGER.debug("t520 : {}", ConfigBean.gson.toJson(t520));

		M120VO m120 = null;

		try {
			String m120Str = Base64Util.decode(t520.getM120Base64());

			m120 = ConfigBean.gson.fromJson(m120Str, M120VO.class);
		} catch (JsonSyntaxException e) {
			throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, null, "m120");
		}

		String svcCode = t520.getSvcCode();
		String branchName = t520.getBranchName();
		String deviceId = t520.getDeviceId();

		String mode = m120.getMode();
		String host = m120.getHost();
		String trxcode = m120.getTrxcode();

		if (ObjectUtils.isEmpty(trxcode)) {
			throw new VerifierException(MipErrorEnum.SP_MISSING_MANDATORY_ITEM, trxcode, "t520.trxcode");
		}

		if (ObjectUtils.isEmpty(svcCode)) {
			throw new VerifierException(MipErrorEnum.SP_MISSING_MANDATORY_ITEM, trxcode, "t520.svcCode");
		}

		String trxStsCode = TrxStsCodeEnum.SERCIVE_REQ.getVal();
		Integer timeout = configBean.getProxyConnTimeOut();

		// 거래상태코 등록
		TrxInfoVO trxInfoVO = new TrxInfoVO();

		trxInfoVO.setTrxcode(trxcode);
		trxInfoVO.setSvcCode(svcCode);
		trxInfoVO.setMode(mode);
		trxInfoVO.setTrxStsCode(trxStsCode);
		trxInfoVO.setBranchName(t520.getBranchName());
		trxInfoVO.setDeviceId(t520.getDeviceId());

		LOGGER.debug("trxInfoVO : {}", ConfigBean.gson.toJson(trxInfoVO));

		// 거래코드 등록
		trxInfoService.registTrxInfo(trxInfoVO);

		WsInfoVO wsInfo = new WsInfoVO();

		wsInfo.setConnUrl(host);
		wsInfo.setTrxcode(trxcode);
		wsInfo.setTimeout(timeout);
		wsInfo.setSvcCode(svcCode);
		wsInfo.setBranchName(branchName);
		wsInfo.setDeviceId(deviceId);

		CpmClient client = new CpmClient(wsInfo);

		client.start();
		
		TrxInfoVO result = trxInfoService.getTrxInfo(trxcode);
		LOGGER.debug(result.toString());
		
		trxInfoService.removeTrxInfo(trxcode);
		
		return result;
	}

}
