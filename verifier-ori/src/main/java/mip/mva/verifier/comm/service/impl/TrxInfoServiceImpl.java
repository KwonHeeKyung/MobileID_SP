package mip.mva.verifier.comm.service.impl;

import org.springframework.stereotype.Service;

import mip.mva.verifier.comm.dao.TrxInfoDAO;
import mip.mva.verifier.comm.enums.MipErrorEnum;
import mip.mva.verifier.comm.exception.VerifierException;
import mip.mva.verifier.comm.service.TrxInfoService;
import mip.mva.verifier.comm.vo.TrxInfoVO;

/**
 * @Project     : 모바일 운전면허증 서비스 구축 사업
 * @PackageName : mip.mva.verifier.comm.service.impl
 * @FileName    : TrxInfoServiceImpl.java
 * @Author      : Min Gi Ju
 * @Date        : 2022. 6. 3.
 * @Description : 거래정보 ServiceImpl
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2022. 6. 3.    Min Gi Ju        최초생성
 */
@Service
public class TrxInfoServiceImpl implements TrxInfoService {

	/** 거래정보 DAO */
	private final TrxInfoDAO trxInfoDAO;

	/**
	 * 거래정보 DAO
	 * 
	 * @param trxInfoDAO 거래정보 DAO
	 */
	public TrxInfoServiceImpl(TrxInfoDAO trxInfoDAO) {
		this.trxInfoDAO = trxInfoDAO;
	}

	/**
	 * 거래정보 조회
	 * 
	 * @MethodName : getTrxInfo
	 * @param trxcode 거래코드
	 * @return 거래정보
	 * @throws VerifierException
	 */
	@Override
	public TrxInfoVO getTrxInfo(String trxcode) throws VerifierException {
		TrxInfoVO trxInfo = null;

		try {
			trxInfo = trxInfoDAO.selectTrxInfo(trxcode);

			if (trxInfo == null) {
				throw new VerifierException(MipErrorEnum.SP_TRXCODE_NOT_FOUND, trxcode);
			}
		} catch (VerifierException e) {
			throw e;
		} catch (Exception e) {
			throw new VerifierException(MipErrorEnum.SP_DB_ERROR, trxcode, "trxInfo select");
		}

		return trxInfo;
	}

	/**
	 * 거래정보 등록
	 * 
	 * @MethodName : registTrxInfo
	 * @param trxInfo 거래정보
	 * @throws VerifierException
	 */
	@Override
	public void registTrxInfo(TrxInfoVO trxInfo) throws VerifierException {
		try {
			trxInfoDAO.insertTrxInfo(trxInfo);
		} catch (Exception e) {
			throw new VerifierException(MipErrorEnum.SP_DB_ERROR, trxInfo.getTrxcode(), "trxInfo insert");
		}
	}

	/**
	 * 거래정보 수정
	 * 
	 * @MethodName : modifyTrxInfo
	 * @param trxInfo 거래정보
	 * @throws VerifierException
	 */
	@Override
	public void modifyTrxInfo(TrxInfoVO trxInfo) throws VerifierException {
		try {
			trxInfoDAO.updateTrxInfo(trxInfo);
		} catch (Exception e) {
			throw new VerifierException(MipErrorEnum.SP_DB_ERROR, trxInfo.getTrxcode(), "trxInfo update");
		}
	}

	/**
	 * 거래정보 삭제
	 * 
	 * @MethodName : removeTrxInfo
	 * @param trxcode 거래코드
	 * @throws VerifierException
	 */
	@Override
	public void removeTrxInfo(String trxcode) throws VerifierException {
		try {
			trxInfoDAO.deleteTrxInfo(trxcode);
		} catch (Exception e) {
			throw new VerifierException(MipErrorEnum.SP_DB_ERROR, trxcode, "trxInfo delete");
		}
	}

}
