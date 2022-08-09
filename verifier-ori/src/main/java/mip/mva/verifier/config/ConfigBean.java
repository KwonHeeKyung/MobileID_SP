package mip.mva.verifier.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.raonsecure.omnione.sdk_server_core.OmniOption;

@Component
@ConfigurationProperties("app")
public class ConfigBean {

	public static final String TYPE = "mip";
	public static final String VERSION = "1.0.0";

	public static final String M120 = "120";
	public static final String M150 = "150";
	public static final String M200 = "200";
	public static final String M310 = "310";
	public static final String M320 = "320";
	public static final String M400 = "400";
	public static final String M900 = "900";

	public static final String START_QRCPM = "start_qrcpm";
	public static final String START_NONCPM = "start_noncpm";
	public static final String WAIT_JOIN = "wait_join";
	public static final String JOIN = "join";
	public static final String WAIT_VERIFY = "wait_verify";
	public static final String VERIFY_HOLDER = "verify_holder";
	public static final String VERIFY_VERIFIER = "verify_verifier";
	public static final String ACK = "ack";
	public static final String WAIT_PROFILE = "wait_profile";
	public static final String PROFILE = "profile";
	public static final String VP = "vp";
	public static final String FINISH = "finish";
	public static final String ERROR = "error";

	public static Gson gson = new Gson();

	private String svcFilePath;

	private String blockchainServerDomain;
	private String spServer;
	private String proxyServer;
	private Integer proxyConnTimeOut;
	private String pushServerDomain;
	private String pushMsCode;
	private String pushType;

	private String keymanagerPath;
	private String keymanagerPassword;
	private String spKeyId;
	private String spRsaKeyId;
	private String spAccount;
	private String spDidPath;
	private String zkpSchemaName;
	private String spBiImageUrl;
	private String spBiImageBase64;
	private Boolean includeProfile;
	private Boolean spCi;
	private Boolean issuerCheckVc;

	private Boolean sdkDetailLog;
	private Boolean sdkUseCache;

	private Boolean spApiUse;
	private String spApiJoin;
	private String spApiVerify;
	private String spApiProfile;
	private String spApiVp;
	private String spApiError;

	public String getSvcFilePath() {
		return svcFilePath;
	}

	public void setSvcFilePath(String svcFilePath) {
		this.svcFilePath = svcFilePath;
	}

	public String getBlockchainServerDomain() {
		return blockchainServerDomain;
	}

	public void setBlockchainServerDomain(String blockchainServerDomain) {
		this.blockchainServerDomain = blockchainServerDomain;
	}

	public String getSpServer() {
		return spServer;
	}

	public void setSpServer(String spServer) {
		this.spServer = spServer;
	}

	public String getProxyServer() {
		return proxyServer;
	}

	public void setProxyServer(String proxyServer) {
		this.proxyServer = proxyServer;
	}

	public Integer getProxyConnTimeOut() {
		return proxyConnTimeOut;
	}

	public void setProxyConnTimeOut(Integer proxyConnTimeOut) {
		this.proxyConnTimeOut = proxyConnTimeOut;
	}

	public String getPushServerDomain() {
		return pushServerDomain;
	}

	public void setPushServerDomain(String pushServerDomain) {
		this.pushServerDomain = pushServerDomain;
	}

	public String getPushMsCode() {
		return pushMsCode;
	}

	public void setPushMsCode(String pushMsCode) {
		this.pushMsCode = pushMsCode;
	}

	public String getPushType() {
		return pushType;
	}

	public void setPushType(String pushType) {
		this.pushType = pushType;
	}

	public String getKeymanagerPath() {
		return keymanagerPath;
	}

	public void setKeymanagerPath(String keymanagerPath) {
		this.keymanagerPath = keymanagerPath;
	}

	public String getKeymanagerPassword() {
		return keymanagerPassword;
	}

	public void setKeymanagerPassword(String keymanagerPassword) {
		this.keymanagerPassword = keymanagerPassword;
	}

	public String getSpKeyId() {
		return spKeyId;
	}

	public void setSpKeyId(String spKeyId) {
		this.spKeyId = spKeyId;
	}

	public String getSpRsaKeyId() {
		return spRsaKeyId;
	}

	public void setSpRsaKeyId(String spRsaKeyId) {
		this.spRsaKeyId = spRsaKeyId;
	}

	public String getSpAccount() {
		return spAccount;
	}

	public void setSpAccount(String spAccount) {
		this.spAccount = spAccount;
	}

	public String getSpDidPath() {
		return spDidPath;
	}

	public void setSpDidPath(String spDidPath) {
		this.spDidPath = spDidPath;
	}

	public String getZkpSchemaName() {
		return zkpSchemaName;
	}

	public void setZkpSchemaName(String zkpSchemaName) {
		this.zkpSchemaName = zkpSchemaName;
	}

	public String getSpBiImageUrl() {
		return spBiImageUrl;
	}

	public void setSpBiImageUrl(String spBiImageUrl) {
		this.spBiImageUrl = spBiImageUrl;
	}

	public String getSpBiImageBase64() {
		return spBiImageBase64;
	}

	public void setSpBiImageBase64(String spBiImageBase64) {
		this.spBiImageBase64 = spBiImageBase64;
	}

	public Boolean getIncludeProfile() {
		return includeProfile;
	}

	public void setIncludeProfile(Boolean includeProfile) {
		this.includeProfile = includeProfile;
	}

	public Boolean getSpCi() {
		return spCi;
	}

	public void setSpCi(Boolean spCi) {
		this.spCi = spCi;
	}

	public Boolean getIssuerCheckVc() {
		return issuerCheckVc;
	}

	public void setIssuerCheckVc(Boolean issuerCheckVc) {
		this.issuerCheckVc = issuerCheckVc;
	}

	public Boolean getSdkDetailLog() {
		return sdkDetailLog;
	}

	public void setSdkDetailLog(Boolean sdkDetailLog) {
		this.sdkDetailLog = sdkDetailLog;

		OmniOption.setSdkDetailLog(sdkDetailLog);
	}

	public Boolean getSdkUseCache() {
		return sdkUseCache;
	}

	public void setSdkUseCache(Boolean sdkUseCache) {
		this.sdkUseCache = sdkUseCache;

		OmniOption.setUseCache(sdkUseCache);
	}

	public Boolean getSpApiUse() {
		return spApiUse;
	}

	public void setSpApiUse(Boolean spApiUse) {
		this.spApiUse = spApiUse;
	}

	public String getSpApiJoin() {
		return spApiJoin;
	}

	public void setSpApiJoin(String spApiJoin) {
		this.spApiJoin = spApiJoin;
	}

	public String getSpApiVerify() {
		return spApiVerify;
	}

	public void setSpApiVerify(String spApiVerify) {
		this.spApiVerify = spApiVerify;
	}

	public String getSpApiProfile() {
		return spApiProfile;
	}

	public void setSpApiProfile(String spApiProfile) {
		this.spApiProfile = spApiProfile;
	}

	public String getSpApiVp() {
		return spApiVp;
	}

	public void setSpApiVp(String spApiVp) {
		this.spApiVp = spApiVp;
	}

	public String getSpApiError() {
		return spApiError;
	}

	public void setSpApiError(String spApiError) {
		this.spApiError = spApiError;
	}

}
