package mip.mva.verifier.app2app.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import mip.mva.verifier.app2app.service.App2AppService;
import mip.mva.verifier.comm.exception.VerifierException;
import mip.mva.verifier.comm.service.DirectService;
import mip.mva.verifier.comm.util.Base64Util;
import mip.mva.verifier.comm.vo.M200VO;
import mip.mva.verifier.comm.vo.T530VO;
import mip.mva.verifier.config.ConfigBean;

/**
 * @Project     : 모바일 운전면허증 서비스 구축 사업
 * @PackageName : mip.mva.verifier.app2app.service.impl
 * @FileName    : App2AppService.java
 * @Author      : Min Gi Ju
 * @Date        : 2022. 6. 8.
 * @Description : App to App 인터페이스 검증 처리 ServiceImpl
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2022. 6. 8.    Min Gi Ju        최초생성
 */
@Service
public class App2AppServiceImpl implements App2AppService {

	private static final Logger LOGGER = LoggerFactory.getLogger(App2AppServiceImpl.class);

	/** Direct 검증 Service */
	private final DirectService directService;

	/**
	 * 생성자
	 * 
	 * @param directService Direct 검증 Service
	 */
	public App2AppServiceImpl(DirectService directService) {
		this.directService = directService;
	}

	/**
	 * App to App 시작
	 * 
	 * @MethodName : start
	 * @param mipApiData {"data":"base64로 인코딩된 푸시 정보"}
	 * @return {"result":true, "data":"base64로 인코딩된 M200 메시지"}
	 * @throws VerifierException
	 */
	@Override
	public T530VO start(T530VO t530) throws VerifierException {
		LOGGER.debug("t530 : {}", ConfigBean.gson.toJson(t530));

		String mode = t530.getMode();
		String svcCode = t530.getSvcCode();
		Boolean includeProfile = t530.getIncludeProfile();
		
		M200VO m200 = directService.getProfile(mode, svcCode, includeProfile);

		String data = Base64Util.encode(ConfigBean.gson.toJson(m200));

		t530.setM200Base64(data);

		return t530;
	}

}
