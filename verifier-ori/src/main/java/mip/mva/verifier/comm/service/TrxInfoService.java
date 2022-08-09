package mip.mva.verifier.comm.service;

import mip.mva.verifier.comm.exception.VerifierException;
import mip.mva.verifier.comm.vo.TrxInfoVO;

/**
 * @Project     : 모바일 운전면허증 서비스 구축 사업
 * @PackageName : mip.mva.verifier.comm.service
 * @FileName    : TrxInfoService.java
 * @Author      : Min Gi Ju
 * @Date        : 2022. 6. 3.
 * @Description : 거래정보 Service
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2022. 6. 3.    Min Gi Ju        최초생성
 */
public interface TrxInfoService {

	/**
	 * 거래정보 조회
	 * 
	 * @MethodName : getTrxInfo
	 * @param trxcode 거래코드
	 * @return 거래정보
	 * @throws VerifierException
	 */
	public TrxInfoVO getTrxInfo(String trxcode) throws VerifierException;

	/**
	 * 거래정보 등록
	 * 
	 * @MethodName : registTrxInfo
	 * @param trxInfo 거래정보
	 * @throws VerifierException
	 */
	public void registTrxInfo(TrxInfoVO trxInfo) throws VerifierException;

	/**
	 * 거래정보 수정
	 * 
	 * @MethodName : modifyTrxInfo
	 * @param trxInfo 거래정보
	 * @throws VerifierException
	 */
	public void modifyTrxInfo(TrxInfoVO trxInfo) throws VerifierException;

	/**
	 * 거래정보 삭제
	 * 
	 * @MethodName : removeTrxInfo
	 * @param trxcode 거래코드
	 * @throws VerifierException
	 */
	public void removeTrxInfo(String trxcode) throws VerifierException;

}
