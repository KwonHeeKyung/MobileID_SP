package mip.mva.verifier.comm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import mip.mva.verifier.comm.dao.SvcDAO;
import mip.mva.verifier.comm.enums.MipErrorEnum;
import mip.mva.verifier.comm.exception.VerifierException;
import mip.mva.verifier.comm.service.SvcService;
import mip.mva.verifier.comm.vo.SvcVO;
import mip.mva.verifier.comm.vo.TrxInfoSvcVO;

/**
 * @Project     : 모바일 운전면허증 서비스 구축 사업
 * @PackageName : mip.mva.verifier.comm.service.impl
 * @FileName    : SvcServiceImpl.java
 * @Author      : Min Gi Ju
 * @Date        : 2022. 6. 3.
 * @Description : 서비스 ServiceImpl
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2022. 6. 3.    Min Gi Ju        최초생성
 */
@Service
public class SvcServiceImpl implements SvcService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SvcServiceImpl.class);

	/** 서비스 DAO */
	private final SvcDAO svcDAO;

	/**
	 * 생성자
	 * 
	 * @param svcDAO 서비스 DAO
	 */
	public SvcServiceImpl(SvcDAO svcDAO) {
		this.svcDAO = svcDAO;
	}

	/**
	 * 서비스 조회
	 * 
	 * @MethodName : getSvc
	 * @param svcCode
	 * @return 서비스정보
	 * @throws VerifierException
	 */
	@Override
	public SvcVO getSvc(String svcCode) throws VerifierException {
		LOGGER.debug("svcCode : {}", svcCode);

		SvcVO svc = null;

		try {
			svc = svcDAO.selectSvc(svcCode);

			if (svc == null) {
				throw new VerifierException(MipErrorEnum.SP_INVALID_DATA, null, "Service Code");
			}
		} catch (VerifierException e) {
			throw e;
		} catch (Exception e) {
			throw new VerifierException(MipErrorEnum.SP_DB_ERROR, null, "Service select");
		}

		return svc;
	}

	/**
	 * 거래 & 서비스정보 조회
	 * 
	 * @MethodName : getTrxInfoSvc
	 * @param trxcode 거래코드
	 * @return 거래 & 서비스정보
	 * @throws VerifierException
	 */
	@Override
	public TrxInfoSvcVO getTrxInfoSvc(String trxcode) throws VerifierException {
		LOGGER.debug("trxcode : {}", trxcode);

		TrxInfoSvcVO trxInfoSvc = null;

		try {
			trxInfoSvc = svcDAO.selectTrxInfoSvc(trxcode);
		} catch (Exception e) {
			throw new VerifierException(MipErrorEnum.SP_DB_ERROR, trxcode, "TrxInfo select for Service");
		}

		return trxInfoSvc;
	}

}
