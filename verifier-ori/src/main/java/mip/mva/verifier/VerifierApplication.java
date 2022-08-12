package mip.mva.verifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ResourceUtils;

import com.google.gson.reflect.TypeToken;

import mip.mva.verifier.comm.dao.SvcDAO;
import mip.mva.verifier.comm.util.SpringUtil;
import mip.mva.verifier.comm.vo.SvcVO;
import mip.mva.verifier.config.ConfigBean;

/**
 * @Project     : 모바일 운전면허증 서비스 구축 사업
 * @PackageName : mip.mva.verifier
 * @FileName    : VerifierApplication.java
 * @Author      : Min Gi Ju
 * @Date        : 2022. 6. 8.
 * @Description : Spring Boot Initializer
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2022. 6. 8.    Min Gi Ju        최초생성
 */
@SpringBootApplication
public class VerifierApplication extends SpringBootServletInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(VerifierApplication.class);

	/**
	 * Spring boot 실행
	 * 
	 * @MethodName : main
	 * @param args
	 */
	public static void main(String[] args) {
		
		ConfigurableApplicationContext ctx = SpringApplication.run(VerifierApplication.class, args);
	
		Scanner sc = new Scanner(System.in);
		LOGGER.info("Please enter a password:");
		String openPwd = sc.nextLine();
		if(!openPwd.equals("1234567890") ) {
			LOGGER.info("The password is incorrect. Exit the program.");
			ctx.close();
		}
		
		try {
			ConfigBean configBean = (ConfigBean) SpringUtil.getBean(ConfigBean.class);
			SvcDAO svcDAO = (SvcDAO) SpringUtil.getBean(SvcDAO.class);

			BufferedReader br = null;

			try {
				File svcFile = ResourceUtils.getFile(configBean.getSvcFilePath());

				br = new BufferedReader(new InputStreamReader(new FileInputStream(svcFile), StandardCharsets.UTF_8));
			} catch (Exception e) {
				LOGGER.error("Service file load error!");
			}

			Type listType = new TypeToken<ArrayList<SvcVO>>() {
			}.getType();

			List<SvcVO> svcList = ConfigBean.gson.fromJson(br, listType);

			for (SvcVO svc : svcList) {
				svcDAO.insertSvc(svc);
			}
			
			LOGGER.debug("::::::::::::::::::::::::::::::::::::::::::::::::::");
			LOGGER.debug("::::::::::::Mobile ID SP Server Start!::::::::::::");
			LOGGER.debug("::::::::::::::::::::::::::::::::::::::::::::::::::");
			
			
		} catch (Exception e) {
			LOGGER.error("Service regist error!");
		}
	}

	/**
	 * Spring boot Configure
	 * 
	 * @MethodName : configure
	 * @param builder SpringApplicationBuilder
	 * @return SpringApplicationBuilder
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(VerifierApplication.class);
	}

}
