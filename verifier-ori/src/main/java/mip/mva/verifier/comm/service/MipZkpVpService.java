package mip.mva.verifier.comm.service;

import mip.mva.verifier.comm.exception.VerifierException;
import mip.mva.verifier.comm.vo.TrxInfoSvcVO;
import mip.mva.verifier.comm.vo.VP;

/**
 * @Project     : 모바일 운전면허증 서비스 구축 사업
 * @PackageName : mip.mva.verifier.comm.service
 * @FileName    : MipZkpVpService.java
 * @Author      : Min Gi Ju
 * @Date        : 2022. 6. 3.
 * @Description : 영지식 VP 검증 Service
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2022. 6. 3.    Min Gi Ju        최초생성
 */
public interface MipZkpVpService {

	/**
	 * Profile 요청
	 * 
	 * @MethodName : getProfile
	 * @param trxInfoSvc 거래 & 서비스정보
	 * @return Base64로 인코딩된 Profile
	 * @throws VerifierException
	 */
	public String getProfile(TrxInfoSvcVO trxInfoSvc) throws VerifierException;

	/**
	 * VP 검증
	 * 
	 * @MethodName : verifyVP
	 * @param trxcode 거래코드
	 * @param vp VP 정보
	 * @return 검증 결과
	 * @throws VerifierException
	 */
	public Boolean verifyVP(String trxcode, VP vp) throws VerifierException;

	/**
	 * VP 재검증
	 * 
	 * @MethodName : reVerifyVP
	 * @param svcCode 서비스코드
	 * @param vp VP 정보
	 * @return 검증 결과
	 * @throws VerifierException
	 */
	public Boolean reVerifyVP(String svcCode, VP vp) throws VerifierException;

}
