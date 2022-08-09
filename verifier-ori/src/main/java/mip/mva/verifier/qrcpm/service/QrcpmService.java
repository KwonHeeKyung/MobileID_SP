package mip.mva.verifier.qrcpm.service;

import mip.mva.verifier.comm.exception.VerifierException;
import mip.mva.verifier.comm.vo.T520VO;
import mip.mva.verifier.comm.vo.TrxInfoVO;

/**
 * @Project     : 모바일 운전면허증 서비스 구축 사업
 * @PackageName : mip.mva.sp.qrcpm.service
 * @FileName    : QrcpmService.java
 * @Author      : Min Gi Ju
 * @Date        : 2022. 6. 3.
 * @Description : QR-CPM 인터페이스 검증 처리 Service
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2022. 6. 3.    Min Gi Ju        최초생성
 */
public interface QrcpmService {

	/**
	 * QR-CPM 시작
	 * 
	 * @MethodName : start
	 * @param t520 QR-CPM 정보
	 * @return QR-CPM 정보
	 * @throws VerifierException
	 */
	public TrxInfoVO start(T520VO t520) throws VerifierException;

}
