package mip.mva.verifier.comm.service.impl;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ResourceUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.raonsecure.omnione.core.crypto.GDPCryptoHelperClient;
import com.raonsecure.omnione.core.data.did.DIDAssertionType;
import com.raonsecure.omnione.core.data.did.v2.DIDs;
import com.raonsecure.omnione.core.data.iw.profile.CommonProfile;
import com.raonsecure.omnione.core.data.iw.profile.EncryptKeyTypeEnum;
import com.raonsecure.omnione.core.data.iw.profile.Profile;
import com.raonsecure.omnione.core.data.iw.profile.result.VCVerifyProfileResult;
import com.raonsecure.omnione.core.data.rest.ResultJson;
import com.raonsecure.omnione.core.data.rest.ResultProfile;
import com.raonsecure.omnione.core.data.rest.ResultVcStatus;
import com.raonsecure.omnione.core.eoscommander.crypto.digest.Sha256;
import com.raonsecure.omnione.core.eoscommander.crypto.util.HexUtils;
import com.raonsecure.omnione.core.exception.IWException;
import com.raonsecure.omnione.core.key.IWDIDManager;
import com.raonsecure.omnione.core.key.IWKeyManagerInterface;
import com.raonsecure.omnione.core.key.IWKeyManagerInterface.OnUnLockListener;
import com.raonsecure.omnione.core.key.KeyManagerFactory;
import com.raonsecure.omnione.core.key.KeyManagerFactory.KeyManagerType;
import com.raonsecure.omnione.core.key.data.AESType;
import com.raonsecure.omnione.core.key.store.IWDIDFile;
import com.raonsecure.omnione.core.util.http.HttpException;
import com.raonsecure.omnione.sdk_server_core.api.EosDataApi;
import com.raonsecure.omnione.sdk_server_core.blockchain.common.BlockChainException;
import com.raonsecure.omnione.sdk_server_core.blockchain.common.ServerInfo;
import com.raonsecure.omnione.sdk_server_core.blockchain.common.StateDBResultDatas;
import com.raonsecure.omnione.sdk_server_core.blockchain.convert.VcStatusTbl;
import com.raonsecure.omnione.sdk_server_core.blockchain.convert.VcStatusTbl.VCStatusEnum;
import com.raonsecure.omnione.sdk_server_core.data.VcResult;
import com.raonsecure.omnione.sdk_verifier.VerifyApi;
import com.raonsecure.omnione.sdk_verifier.api.data.SpProfileParam;
import com.raonsecure.omnione.sdk_verifier.api.data.VcVerifyProfileParam;

import mip.mva.verifier.comm.enums.MipErrorEnum;
import mip.mva.verifier.comm.enums.TrxStsCodeEnum;
import mip.mva.verifier.comm.enums.VcStatusEnum;
import mip.mva.verifier.comm.exception.VerifierException;
import mip.mva.verifier.comm.service.MipDidVpService;
import mip.mva.verifier.comm.service.SvcService;
import mip.mva.verifier.comm.service.TrxInfoService;
import mip.mva.verifier.comm.util.Base64Util;
import mip.mva.verifier.comm.vo.SvcVO;
import mip.mva.verifier.comm.vo.TrxInfoSvcVO;
import mip.mva.verifier.comm.vo.TrxInfoVO;
import mip.mva.verifier.comm.vo.VP;
import mip.mva.verifier.config.ConfigBean;

/**
 * @Project     : ????????? ??????????????? ????????? ?????? ??????
 * @PackageName : mip.mva.verifier.comm.service.impl
 * @FileName    : MipDidVpServiceImpl.java
 * @Author      : Min Gi Ju
 * @Date        : 2022. 5. 31.
 * @Description : VP ?????? ServiceImpl
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2022. 5. 31.    Min Gi Ju        ????????????
 */
@SuppressWarnings("unchecked")
@Service
public class MipDidVpServiceImpl implements MipDidVpService, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(MipDidVpServiceImpl.class);

	/** ????????? ???????????? */
	private final ConfigBean configBean;
	/** ????????? Service */
	private final SvcService svcService;
	/** ???????????? Service */
	private final TrxInfoService trxInfoService;

	/** ???????????? ???????????? */
	private ServerInfo blockChainServerInfo;
	/** ???????????? */
	private IWKeyManagerInterface keyManager;
	/** DID ?????? ?????? */
	private String didFilePath;
	/** DID Document */
	private DIDs didDoc;

	/**
	 * ?????????
	 * 
	 * @param configBean ????????? ????????????
	 * @param svcService ????????? Service
	 * @param trxInfoService ???????????? Service
	 */
	public MipDidVpServiceImpl(ConfigBean configBean, SvcService svcService, TrxInfoService trxInfoService) {
		this.configBean = configBean;
		this.svcService = svcService;
		this.trxInfoService = trxInfoService;
	}

	/**
	 * ?????? ??????
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			LOGGER.debug("blockchainServerDomain : {}", configBean.getBlockchainServerDomain());
			LOGGER.debug("keyManagerPath : {}", configBean.getKeymanagerPath());
			LOGGER.debug("spDidPath : {}", configBean.getSpDidPath());

			blockChainServerInfo = new ServerInfo(configBean.getBlockchainServerDomain());

			File keyManagerFile = ResourceUtils.getFile(configBean.getKeymanagerPath());
			String keyManagerPath = keyManagerFile.getAbsolutePath();

			//keymanager, spAcount ????????? - ????????? ??? ??????
			StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		    
		    encryptor.setAlgorithm("PBEWITHMD5ANDTRIPLEDES");
		    encryptor.setPassword("test_password");
		    
		    String decryption = encryptor.decrypt(configBean.getKeymanagerPassword());
		    configBean.setKeymanagerPassword(decryption);
		    
		    decryption = encryptor.decrypt(configBean.getSpAccount());
		    configBean.setSpAccount(decryption);
		    
			keyManager = KeyManagerFactory.getKeyManager(KeyManagerType.DEFAULT, keyManagerPath, configBean.getKeymanagerPassword().toCharArray());

			keyManager.unLock(configBean.getKeymanagerPassword().toCharArray(), new OnUnLockListener() {
				@Override
				public void onSuccess() {
					LOGGER.debug("[OMN] API keyManager onSuccess");
				}

				@Override
				public void onFail(int errCode) {
					LOGGER.error("[OMN] API keyManager onFail", errCode);
				}

				@Override
				public void onCancel() {
					LOGGER.error("[OMN] API keyManager onCancel");
				}
			});

			File didFile = ResourceUtils.getFile(configBean.getSpDidPath());

			didFilePath = didFile.getAbsolutePath();

			IWDIDFile iWDIDFile = new IWDIDFile(didFilePath);

			didDoc = iWDIDFile.getDataFromDIDsV2();
		} catch (Exception e) {
			LOGGER.error("[OMN] API Init Error - Check Log", e);
		}
	}

	/**
	 * DID Assertion ??????
	 * 
	 * @MethodName : makeDIDAssertion
	 * @param nonce Nonce
	 * @return DID Assertion
	 * @throws VerifierException
	 */
	@Override
	public String makeDIDAssertion(String nonce) throws VerifierException {
		String didAssertion = "";

		try {
			IWDIDManager didManager = new IWDIDManager(didFilePath);

			didAssertion = didManager.makeDIDAssertion2(DIDAssertionType.DEFAULT, configBean.getSpKeyId(), HexUtils.toBytes(nonce), null, keyManager);
		} catch (IWException e) {
			throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, null, e.getErrorMsg());
		}

		return didAssertion;
	}

	/**
	 * Profile ??????
	 * 
	 * @MethodName : getProfile
	 * @param trxInfoSvc ?????? & ???????????????
	 * @return Base64??? ???????????? Profile
	 * @throws VerifierException
	 */
	@Override
	public String getProfile(TrxInfoSvcVO trxInfoSvc) throws VerifierException {
		LOGGER.debug("trxInfoSvc : {}", ConfigBean.gson.toJson(trxInfoSvc));

		String trxcode = trxInfoSvc.getTrxcode();
		String trxStsCode = trxInfoSvc.getTrxStsCode();
		String vpVerifyResult = trxInfoSvc.getVpVerifyResult();

		// profile ????????? ??????(0001) ???????????? ?????? ??????.
		// 0001 ????????? ?????? ?????? ?????? profile ????????? ?????????, verify ??? ????????? ??? ??????.
		if (!TrxStsCodeEnum.SERCIVE_REQ.getVal().equals(trxStsCode)) {
			throw new VerifierException(MipErrorEnum.SP_MSG_SEQ_ERROR, trxcode, "stsCode != 0001");
		}

		// ?????? verify ??? trx
		if ("Y".equals(vpVerifyResult)) {
			throw new VerifierException(MipErrorEnum.SP_MSG_SEQ_ERROR, trxcode, "verifyResult == Y");
		}

		String svcCode = trxInfoSvc.getSvcCode();
		String branchName = trxInfoSvc.getBranchName();

		// ????????? ??????
		SvcVO svc = svcService.getSvc(svcCode);

		LOGGER.debug("svc : {}", ConfigBean.gson.toJson(svc));

		String name = (branchName == null) ? (String) svc.getServiceName() : branchName;

		// Profile ?????? Start
		Profile profile = new Profile();

//		profile.setCallBackUrl(svc.getCallBackUrl());
		profile.setEncryptType(svc.getEncryptType());
		profile.setPresentType(svc.getPresentType());
		profile.setSpName(svc.getSpName() + "-" + name);
		profile.setName(svc.getServiceName());
		profile.setKeyType(svc.getKeyType());

		String authType = svc.getAuthType();

		if (!ObjectUtils.isEmpty(authType)) {
			try {
				profile.setAuthType(ConfigBean.gson.fromJson(authType.toLowerCase(), ArrayList.class));
			} catch (JsonSyntaxException e) {
				throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, trxcode, "authType");
			}
		}

		byte[] tempNonce = null;

		try {
			tempNonce = new GDPCryptoHelperClient().generateNonce();
		} catch (IWException e) {
			throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, trxcode, e.getErrorMsg());
		}

		String nonce = Sha256.from(tempNonce).toString();

		profile.setNonce(nonce);
		
		SpProfileParam spProfileParam = new SpProfileParam(blockChainServerInfo, keyManager, configBean.getSpKeyId(), svcCode, profile, didDoc.getId(), configBean.getSpAccount());

		if (profile.getKeyType() == EncryptKeyTypeEnum.ALGORITHM_RSA.getVal()) {
			try {
				spProfileParam.setEncPublicKey(keyManager.getPublicKey(configBean.getSpRsaKeyId()));
			} catch (IWException e) {
				throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, trxcode, e.getErrorMsg());
			}
		}

		String spProfileJson = null;

		try {
			spProfileJson = VerifyApi.makeSpProfile(spProfileParam);
		} catch (BlockChainException e) {
			throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, trxcode, e.getErrorMsg());
		} catch (HttpException e) {
			throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, trxcode, e.getErrorMsg());
		}
		// Profile ?????? End

		ResultProfile resultJson = new ResultProfile();

		resultJson.setResult(true);
		resultJson.setProfileJson(spProfileJson);

		CommonProfile commonProfile = ConfigBean.gson.fromJson(Base64Util.decode(resultJson.getProfileBase64()), CommonProfile.class);

		TrxInfoVO trxInfo = new TrxInfoVO();

		trxInfo.setTrxcode(trxcode);
		trxInfo.setTrxStsCode(TrxStsCodeEnum.PROFILE_REQ.getVal());
		trxInfo.setNonce(commonProfile.getProfile().getNonce());

		trxInfoService.modifyTrxInfo(trxInfo);

		return resultJson.getProfileBase64();
	}

	/**
	 * VP ??????
	 * 
	 * @MethodName : verifyVP
	 * @param trxcode ????????????
	 * @param vp VP ??????
	 * @return ?????? ??????
	 * @throws VerifierException
	 */
	@Override
	public Boolean verifyVP(String trxcode, VP vp) throws VerifierException {
		LOGGER.debug("trxcode : {}, vp : {}", trxcode, ConfigBean.gson.toJson(vp));

		Boolean result = false;

		TrxInfoVO trxInfo = new TrxInfoVO();

		trxInfo.setTrxcode(trxcode);
		trxInfo.setTrxStsCode(TrxStsCodeEnum.VERIFY_REQ.getVal());

		trxInfoService.modifyTrxInfo(trxInfo);

		Integer encryptType = vp.getEncryptType();
		Integer keyType = vp.getKeyType();
		String type = vp.getType();
		String data = vp.getData();
		List<String> authType = vp.getAuthType();
		String did = vp.getDid();
		String nonce = vp.getNonce();

		// VP ?????? Start
		VCVerifyProfileResult vCVerifyProfileResult = new VCVerifyProfileResult();

		vCVerifyProfileResult.setEncryptType(encryptType);
		vCVerifyProfileResult.setKeyType(keyType);
		vCVerifyProfileResult.setType(type);
		vCVerifyProfileResult.setData(data);

		vCVerifyProfileResult.setAuthType(authType);
		vCVerifyProfileResult.setDid(did);
		vCVerifyProfileResult.setNonce(nonce);
		
		ResultJson resultJson = this.verify(vCVerifyProfileResult, trxcode);

		if (resultJson == null || !resultJson.isResult()) {
			return result;
		}
		// VP ?????? End

		// VP ?????? ?????? Start
		Map<String, Object> vpDataMap = null;

		EncryptKeyTypeEnum keyTypeEnum = EncryptKeyTypeEnum.getEnum(vCVerifyProfileResult.getKeyType());

		if (keyTypeEnum == EncryptKeyTypeEnum.ALGORITHM_RSA) {
			try {
				AESType aESType = vCVerifyProfileResult.getEncryptType() == 1 ? AESType.AES128 : AESType.AES256;

				byte[] vpDataByte = keyManager.rsaDecrypt(configBean.getSpRsaKeyId(), HexUtils.toBytes(vCVerifyProfileResult.getData()), aESType);

				data = new String(vpDataByte, StandardCharsets.UTF_8);

				LOGGER.debug("data : {}", data);
			} catch (IWException e) {
				throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, trxcode, e.getErrorMsg());
			}
		}

		try {
			vpDataMap = ConfigBean.gson.fromJson(data, HashMap.class);
		} catch (JsonSyntaxException e) {
			throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, trxcode, "data");
		}

		List<Map<String, Object>> verifiableCredentialList = (List<Map<String, Object>>) vpDataMap.get("verifiableCredential");

		if (ObjectUtils.isEmpty(verifiableCredentialList)) {
			throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, trxcode, "vp");
		}

		Map<String, Object> verifiableCredential = verifiableCredentialList.get(0);

		String vcId = (String) verifiableCredential.get("id");

		ResultVcStatus resultVcStatus = null;

		try {
			resultVcStatus = this.getVCStatus(vcId);
		} catch (BlockChainException e) {
			throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, trxcode, e.getErrorMsg());
		}

		String vcStatus = resultVcStatus.getVcStatus();

		if (vcStatus.equalsIgnoreCase(VcStatusEnum.ACTIVE.getVal())) { // ????????? ??????
			result = true;
		} else if (vcStatus.equalsIgnoreCase(VcStatusEnum.NEED_RENEW.getVal())) { // ???????????? ??????
			String memo = resultVcStatus.getMemo();

			if (memo.equals("????????????")) {
				result = true;
			} else {
				throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, trxcode, "???????????? ?????? : " + vcStatus + "(" + memo + ")");
			}
		} else {
			throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, trxcode, "???????????? ?????? : " + vcStatus);
		}
		// VP ?????? ?????? End

		String vpVerifyResult = result ? "Y" : "N";

		trxInfo.setTrxcode(trxcode);
		trxInfo.setTrxStsCode(TrxStsCodeEnum.VERIFY_COM.getVal());
		trxInfo.setVpVerifyResult(vpVerifyResult);

		trxInfoService.modifyTrxInfo(trxInfo);

		return result;
	}

	/**
	 * VP ?????????
	 * 
	 * @MethodName : reVerifyVP
	 * @param svcCode ???????????????
	 * @param vp VP ??????
	 * @return ?????? ??????
	 * @throws VerifierException
	 */
	@Override
	public Boolean reVerifyVP(String svcCode, VP vp) throws VerifierException {
		VCVerifyProfileResult vcVerifyProfileResult = new VCVerifyProfileResult();

		vcVerifyProfileResult.setEncryptType(vp.getEncryptType());
		vcVerifyProfileResult.setKeyType(vp.getKeyType());
		vcVerifyProfileResult.setType(vp.getType());
		vcVerifyProfileResult.setData(vp.getData());
		vcVerifyProfileResult.setAuthType(vp.getAuthType());
		vcVerifyProfileResult.setDid(vp.getDid());
		vcVerifyProfileResult.setNonce(vp.getNonce());

		// rsakeyId - ????????? ??????
		VcVerifyProfileParam vcVerifyParam = new VcVerifyProfileParam(blockChainServerInfo, keyManager, configBean.getSpKeyId(),
				configBean.getSpAccount(), vcVerifyProfileResult, didFilePath);

		// vp ?????? ?????? ????????? ?????? rsakeyId??? ??????
		EncryptKeyTypeEnum keyTypeEnum = EncryptKeyTypeEnum.getEnum(vcVerifyProfileResult.getKeyType());

		if (keyTypeEnum == EncryptKeyTypeEnum.ALGORITHM_RSA) {
			vcVerifyParam.setEncryptKeyId(configBean.getSpRsaKeyId());
		}

		// ????????? ?????? ??????
		vcVerifyParam.setServiceCode(svcCode);
		// Issuer ?????? ?????? ??????
		vcVerifyParam.setCheckVC(configBean.getIssuerCheckVc());
		// setCheckVC(true) ??? ?????? ??????
		vcVerifyParam.setIssuerProofVerifyCheck(configBean.getIssuerCheckVc());

		VcResult vcResult = null;

		try {
			vcResult = VerifyApi.checkUserProof(vcVerifyParam, false);
		} catch (BlockChainException e) {
			throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, null, e.getErrorMsg());
		} catch (HttpException e) {
			throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, null, e.getErrorMsg());
		}

		if (vcResult.getStatus().equals("1")) {
			LOGGER.debug("VP timezone : {}", vcVerifyProfileResult.getTimezone());

			// vp ??? ?????? ??????
			if (vcResult.getReqVp().getProofs() != null) {// ?????? ????????? ??????
				LOGGER.debug("VP ???????????? : {}", vcResult.getReqVp().getProofs().get(0).getCreated());
			} else {// ?????? ????????? ??????
				LOGGER.debug("VP ???????????? : {}", vcResult.getReqVp().getProof().getCreated());
			}
		}

		return vcResult.getStatus().equals("1");
	}
	
	/**
	 * VP data ??????
	 * 
	 * @MethodName : getVPData
	 * @param vp VP
	 * @throws SpException
	 */
	@Override
	public String getVPData(VP vp) throws VerifierException {
		String vpData = vp.getData();
		
		EncryptKeyTypeEnum keyTypeEnum = EncryptKeyTypeEnum.getEnum(vp.getKeyType());

		if (keyTypeEnum == EncryptKeyTypeEnum.ALGORITHM_RSA) {
			try {
				AESType aESType = vp.getEncryptType() == 1 ? AESType.AES128 : AESType.AES256;

				byte[] vpDataByte = keyManager.rsaDecrypt(configBean.getSpRsaKeyId(), HexUtils.toBytes(vp.getData()), aESType);

				vpData = new String(vpDataByte, StandardCharsets.UTF_8);

				LOGGER.debug("vpData : {}", vpData);
			} catch (IWException e) {
				throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, null, e.getErrorMsg());
			}
		}
		
		return vpData;
	}
	
	/**
	 * CI ??????
	 * 
	 * @MethodName : getCI
	 * @param vp VP
	 * @throws SpException
	 */
	@Override
	public String getCI(VP vp) throws VerifierException {
		String ci = null;
		
		try {
			String vpData = this.getVPData(vp);
			
			JsonObject vpDataObj = JsonParser.parseString(vpData).getAsJsonObject();
			JsonObject proofObj = vpDataObj.get("proof").getAsJsonObject();
			
			String nonce = proofObj.getAsJsonObject().get("nonce").getAsString();
			
			int ciLength = 88;
			
			if (!ObjectUtils.isEmpty(nonce) && nonce.length() > ciLength * 2) {
				String ciHex = nonce.substring(nonce.length() - ciLength * 2);
				
				ci = new String(HexUtils.toBytes(ciHex));
			}
		} catch(Exception e) {
			LOGGER.error(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT.getMsg());
		}
		
		return ci;
	}
	
	/**
	 * VC ?????? ??????
	 * 
	 * @MethodName : getVCStatus
	 * @param vcId VCID
	 * @return VC ??????
	 * @throws BlockChainException
	 */
	private ResultVcStatus getVCStatus(String vcId) throws BlockChainException {
		ResultVcStatus resultVcStatus = new ResultVcStatus();

		resultVcStatus.setVcId(vcId);

		EosDataApi eosDataApi = new EosDataApi();

		StateDBResultDatas<VcStatusTbl> stateDBResultDatas = eosDataApi.getVCStatus(blockChainServerInfo, vcId);

		if (!stateDBResultDatas.getDataList().isEmpty()) {
			VcStatusTbl vcStatusTbl = stateDBResultDatas.getDataList().get(0);

			resultVcStatus.setVcStatus(vcStatusTbl.getStatusCodeEnum().toString());
			resultVcStatus.setMemo(vcStatusTbl.getMemo().toString());
		} else {
			resultVcStatus.setVcStatus(VCStatusEnum.NOT_EXIST.toString());
		}

		resultVcStatus.setResult(true);

		return resultVcStatus;
	}

	/**
	 * ??????
	 * 
	 * @MethodName : verify
	 * @param vCVerifyProfileResult ?????? ????????????
	 * @param trxcode ????????????
	 * @return ?????? ??????
	 * @throws VerifierException
	 */
	private ResultJson verify(VCVerifyProfileResult vCVerifyProfileResult, String trxcode) throws VerifierException {
		VcResult vcResult = null;

		TrxInfoVO trxInfo = trxInfoService.getTrxInfo(trxcode);

		String svcCode = trxInfo.getSvcCode();

		VcVerifyProfileParam vcVerifyProfileParam = new VcVerifyProfileParam(blockChainServerInfo, keyManager, configBean.getSpKeyId(),
				configBean.getSpAccount(), vCVerifyProfileResult, didFilePath);

		vcVerifyProfileParam.setServiceCode(svcCode);
		vcVerifyProfileParam.setCheckVCExpirationDate(true);
		vcVerifyProfileParam.setIssuerProofVerifyCheck(true);

		EncryptKeyTypeEnum keyTypeEnum = EncryptKeyTypeEnum.getEnum(vCVerifyProfileResult.getKeyType());

		if (keyTypeEnum == EncryptKeyTypeEnum.ALGORITHM_RSA)
			vcVerifyProfileParam.setEncryptKeyId(configBean.getSpRsaKeyId());
	
		try {
			vcResult = VerifyApi.verify2(vcVerifyProfileParam, false);
		} catch (BlockChainException e) {
			throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, trxcode, e.getErrorMsg());
		} catch (HttpException e) {
			throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, trxcode, e.getErrorMsg());
		}

		ResultJson resultJson = new ResultJson();

		if (vCVerifyProfileResult.getAuthType() != null) {
			String signKeyId = vcResult.getSignKeyId().toString();

			for (String auth : vCVerifyProfileResult.getAuthType()) {
				if (!StringUtils.containsIgnoreCase(signKeyId, auth)) {
					resultJson.setResult(vcResult.getStatus().equals("0"));

					return resultJson;
				}
			}
		}

		resultJson.setResult(vcResult.getStatus().equals("1"));

		return resultJson;
	}

}
