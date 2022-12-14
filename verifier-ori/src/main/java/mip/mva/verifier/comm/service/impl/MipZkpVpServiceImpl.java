package mip.mva.verifier.comm.service.impl;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ResourceUtils;

import com.google.gson.JsonSyntaxException;
import com.raonsecure.omnione.core.crypto.GDPCryptoHelperClient;
import com.raonsecure.omnione.core.data.did.v2.DIDs;
import com.raonsecure.omnione.core.data.iw.profile.CommonProfile;
import com.raonsecure.omnione.core.data.iw.profile.EncryptKeyTypeEnum;
import com.raonsecure.omnione.core.data.iw.profile.EncryptTypeEnum;
import com.raonsecure.omnione.core.data.iw.profile.Profile;
import com.raonsecure.omnione.core.data.iw.profile.result.VCVerifyProfileResult;
import com.raonsecure.omnione.core.data.rest.ResultJson;
import com.raonsecure.omnione.core.data.rest.ResultProfile;
import com.raonsecure.omnione.core.eoscommander.crypto.digest.Sha256;
import com.raonsecure.omnione.core.eoscommander.crypto.util.HexUtils;
import com.raonsecure.omnione.core.exception.IWException;
import com.raonsecure.omnione.core.key.IWKeyManagerInterface;
import com.raonsecure.omnione.core.key.IWKeyManagerInterface.OnUnLockListener;
import com.raonsecure.omnione.core.key.KeyManagerFactory;
import com.raonsecure.omnione.core.key.KeyManagerFactory.KeyManagerType;
import com.raonsecure.omnione.core.key.data.AESType;
import com.raonsecure.omnione.core.key.store.IWDIDFile;
import com.raonsecure.omnione.core.util.http.HttpException;
import com.raonsecure.omnione.core.zkp.ZkpConstants;
import com.raonsecure.omnione.core.zkp.data.CredentialDefinition;
import com.raonsecure.omnione.core.zkp.data.Proof;
import com.raonsecure.omnione.core.zkp.data.ProofRequest;
import com.raonsecure.omnione.core.zkp.data.proofrequest.AttributeInfo;
import com.raonsecure.omnione.core.zkp.data.proofrequest.PredicateInfo;
import com.raonsecure.omnione.core.zkp.data.schema.CredentialSchema;
import com.raonsecure.omnione.core.zkp.enums.PredicateType;
import com.raonsecure.omnione.core.zkp.revoc.data.dto.Identifiers;
import com.raonsecure.omnione.core.zkp.revoc.data.dto.ProofVerifyParam;
import com.raonsecure.omnione.core.zkp.util.BigIntegerUtil;
import com.raonsecure.omnione.sdk_server_core.api.ZKPApi;
import com.raonsecure.omnione.sdk_server_core.blockchain.common.BlockChainException;
import com.raonsecure.omnione.sdk_server_core.blockchain.common.ServerInfo;
import com.raonsecure.omnione.sdk_server_core.data.IWApiBaseData;
import com.raonsecure.omnione.sdk_server_core.data.VcResult;
import com.raonsecure.omnione.sdk_server_core.data.response.ResultCode;
import com.raonsecure.omnione.sdk_server_core.data.response.SDKResponse;
import com.raonsecure.omnione.sdk_verifier.VerifyApi;
import com.raonsecure.omnione.sdk_verifier.api.data.SpProfileParam;
import com.raonsecure.omnione.sdk_verifier.api.data.VcVerifyProfileParam;

import mip.mva.verifier.comm.enums.MipErrorEnum;
import mip.mva.verifier.comm.enums.TrxStsCodeEnum;
import mip.mva.verifier.comm.exception.VerifierException;
import mip.mva.verifier.comm.service.MipZkpVpService;
import mip.mva.verifier.comm.service.SvcService;
import mip.mva.verifier.comm.service.TrxInfoService;
import mip.mva.verifier.comm.util.Base64Util;
import mip.mva.verifier.comm.vo.TrxInfoSvcVO;
import mip.mva.verifier.comm.vo.TrxInfoVO;
import mip.mva.verifier.comm.vo.VP;
import mip.mva.verifier.config.ConfigBean;

/**
 * @Project     : ????????? ??????????????? ????????? ?????? ??????
 * @PackageName : mip.mva.verifier.comm.service.impl
 * @FileName    : MipZkpVpServiceImpl.java
 * @Author      : Min Gi Ju
 * @Date        : 2022. 6. 3.
 * @Description : 
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2022. 6. 3.    Min Gi Ju        ????????????
 */
@SuppressWarnings("unchecked")
@Service
public class MipZkpVpServiceImpl implements MipZkpVpService, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(MipZkpVpServiceImpl.class);

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
	/** API Basedata */
	private IWApiBaseData iWApiBaseData;

	/**
	 * ?????????
	 * 
	 * @param configBean ????????? ????????????
	 * @param svcService ????????? Service
	 * @param trxInfoService ???????????? Service
	 */
	public MipZkpVpServiceImpl(ConfigBean configBean, SvcService svcService, TrxInfoService trxInfoService) {
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
			LOGGER.debug("spKeyId : {}", configBean.getSpKeyId());
			LOGGER.debug("spDidPath : {}", configBean.getSpDidPath());

			blockChainServerInfo = new ServerInfo(configBean.getBlockchainServerDomain());

			File keyManagerFile = ResourceUtils.getFile(configBean.getKeymanagerPath());
			String keyManagerPath = keyManagerFile.getAbsolutePath();

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

			iWApiBaseData = new IWApiBaseData(blockChainServerInfo, keyManager, configBean.getSpKeyId(), configBean.getSpAccount());

			File didFile = ResourceUtils.getFile(configBean.getSpDidPath());

			didFilePath = didFile.getAbsolutePath();

			IWDIDFile iWDIDFile = new IWDIDFile(didFilePath);

			didDoc = iWDIDFile.getDataFromDIDsV2();
		} catch (Exception e) {
			LOGGER.error("[OMN] API Init Error - Check Log", e);
		}
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

		ResultProfile resultJson = new ResultProfile();

		String trxcode = trxInfoSvc.getTrxcode();
		String nonce = trxInfoSvc.getNonce();
		String spName = trxInfoSvc.getSpName();
		String serviceName = trxInfoSvc.getServiceName();
		String callBackUrl = trxInfoSvc.getCallBackUrl();
		
		// ?????? ??????????????? RSA ???????????? ?????? nonce ???????????? ????????? ???
		if (nonce == null) {
			byte[] tempNonce = null;

			try {
				tempNonce = new GDPCryptoHelperClient().generateNonce();
			} catch (IWException e) {
				e.printStackTrace();
			}

			nonce = Sha256.from(tempNonce).toString();
			
			LOGGER.debug("nonce : {}", nonce);
		}

		// profile value setting
		Profile profile = new Profile();
		
		profile.setPresentType(2); // 2 = ZKP VP
		profile.setEncryptType(EncryptTypeEnum.AES_256);
		profile.setKeyType(EncryptKeyTypeEnum.ALGORITHM_RSA);
		profile.setSpName(spName);
		profile.setName(serviceName);
		profile.setCallBackUrl(callBackUrl);
		profile.setNonce(nonce);
		profile.setType("VERIFY");
		
		ProofRequest proofRequest = this.createProofRequest(trxInfoSvc);
		
		profile.setProofRequest(proofRequest);
		
		// 3?????? param ?????? ????????? ????????? ????????? ??? ???????????? ??????
		SpProfileParam spProfileParam = new SpProfileParam(blockChainServerInfo, keyManager, configBean.getSpKeyId(), null, profile, didDoc.getId(),
				configBean.getSpAccount(), true);
		
		// ?????? ???????????? RSA ???/????????? ?????? ECIES ???/???????????? ???????????? ???????????? ?????? ??????
		if (profile.getKeyType() == EncryptKeyTypeEnum.ALGORITHM_RSA.getVal()) {
			try {
				spProfileParam.setEncPublicKey(keyManager.getPublicKey(configBean.getSpRsaKeyId()));
			} catch (IWException e) {
				e.printStackTrace();
			}
		}
		
		String zkpProfileJson = null;

		try {
			zkpProfileJson = VerifyApi.makeZkpProfile(spProfileParam);
		} catch (BlockChainException e) {
			e.printStackTrace();
		}

		LOGGER.debug("profile zkpProfileJson: {}", zkpProfileJson);

		resultJson.setProfileJson(zkpProfileJson);
		resultJson.setResult(true);

		CommonProfile commonProfile = ConfigBean.gson.fromJson(Base64Util.decode(resultJson.getProfileBase64()), CommonProfile.class);

		TrxInfoVO trxInfo = new TrxInfoVO();

		trxInfo.setTrxcode(trxcode);
		trxInfo.setTrxStsCode(TrxStsCodeEnum.PROFILE_REQ.getVal());
		trxInfo.setZkpNonce(commonProfile.getProfile().getProofRequest().getNonce().toString());

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

		Integer encryptType = vp.getEncryptType();
		Integer keyType = vp.getKeyType();
		String type = vp.getType();
		String data = vp.getData();
		String zkpNonce = vp.getZkpNonce();

		// VP ?????? Start
		VCVerifyProfileResult vCVerifyProfileResult = new VCVerifyProfileResult();

		vCVerifyProfileResult.setEncryptType(encryptType);
		vCVerifyProfileResult.setKeyType(keyType);
		vCVerifyProfileResult.setType(type);
		vCVerifyProfileResult.setData(data);
		vCVerifyProfileResult.setZkpNonce(zkpNonce);

		EncryptKeyTypeEnum keyTypeEnum = EncryptKeyTypeEnum.getEnum(vCVerifyProfileResult.getKeyType());

		if (keyTypeEnum == EncryptKeyTypeEnum.ALGORITHM_RSA) {
			try {
				AESType aESType = vCVerifyProfileResult.getEncryptType() == 1 ? AESType.AES128 : AESType.AES256;

				byte[] vpDataByte = keyManager.rsaDecrypt(configBean.getSpRsaKeyId(), HexUtils.toBytes(vCVerifyProfileResult.getData()), aESType);

				data = new String(vpDataByte, StandardCharsets.UTF_8);

				LOGGER.debug("data : {}", data);
			} catch (IWException e) {
				throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, trxcode, "decData");
			}
		}

		Proof proof = ConfigBean.gson.fromJson(data, Proof.class);
		TrxInfoSvcVO trxInfoSvc = svcService.getTrxInfoSvc(trxcode);
		BigInteger verifierNonce = new BigInteger(zkpNonce);

		List<ProofVerifyParam> proofVerifyParams = new LinkedList<>();

		for (Identifiers identifiers : proof.getIdentifiers()) {
			String schemaId = identifiers.getSchemaId();
			CredentialSchema credentialSchema = (CredentialSchema) new ZKPApi().getCredentialSchema(iWApiBaseData, schemaId).getResultData();
			String credDefId = identifiers.getCredDefId();
			CredentialDefinition credentialDefinition = (CredentialDefinition) new ZKPApi().getCredentialDefinition(iWApiBaseData, credDefId).getResultData();
			ProofVerifyParam proofVerifyParam = new ProofVerifyParam.Builder().setSchema(credentialSchema).setCredentialDefinition(credentialDefinition).build();

			proofVerifyParams.add(proofVerifyParam);
		}

		ProofRequest proofRequest = this.createProofRequest(trxInfoSvc);
		SDKResponse sDKResponse = new ZKPApi().verifyProof(iWApiBaseData, proof, proofRequest, proofVerifyParams, verifierNonce);

		ResultJson resultJson = new ResultJson();

		if (!sDKResponse.isSuccess()) {
			resultJson.setResult(false);
			resultJson.setErrorCode(sDKResponse.getResultCode());
			resultJson.setErrorMsg(sDKResponse.getResultMsg());
		} else {
			resultJson.setResult(true);
		}

		if (resultJson != null && resultJson.isResult()) {
			result = true;
		}

		String vpVerifyResult = result ? "Y" : "N";

		TrxInfoVO trxInfo = new TrxInfoVO();

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
		vcVerifyProfileResult.setZkpNonce(vp.getZkpNonce());

		// rsakeyId - ????????? ??????
		VcVerifyProfileParam vcVerifyParam = new VcVerifyProfileParam(blockChainServerInfo, keyManager, configBean.getSpKeyId(), configBean.getSpAccount(), vcVerifyProfileResult, didFilePath);

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
	 * ProofRequest ??????
	 * 
	 * @MethodName : createProofRequest
	 * @param trxInfoSvc ?????? & ???????????????
	 * @return ProofRequest
	 * @throws VerifierException
	 */
	private ProofRequest createProofRequest(TrxInfoSvcVO trxInfoSvc) throws VerifierException {
		String trxcode = trxInfoSvc.getTrxcode();
		
		// restrictionList ?????? Start
		List<Map<String, String>> restrictionList = new ArrayList<Map<String, String>>();
		Integer limit = 1000;
		SDKResponse sDKResponse = new ZKPApi().getCredentialList(iWApiBaseData, null, limit);
		List<Map<String, Object>> credentialList = (List<Map<String, Object>>) sDKResponse.getResultData();
		List<String> credentialDataList = new ArrayList<String>();
		
		try {
			String zkpSchemaName = configBean.getZkpSchemaName();
			
			for (Map<String, Object> credentail : credentialList) {
				sDKResponse = new ZKPApi().getSchemaList(iWApiBaseData, (String) credentail.get("schemaId"), 0);

				Map<String, Object> credentialSchema = (Map<String, Object>) sDKResponse.getResultData();
				
				if(!ObjectUtils.isEmpty(credentialSchema)) {
					if (credentialSchema.get("name").equals(zkpSchemaName)) {
						credentialDataList.add((String) credentail.get("id"));
					}
				}
			}
		} catch (Exception e) {
			throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, trxcode, "credential");
		}
		
		sDKResponse.setResultData(credentialDataList);
		sDKResponse.setResult(ResultCode.SUCCESS);

		List<String> creDefIdList = (List<String>) sDKResponse.getResultData();

		for (String creDefId : creDefIdList) {
			Map<String, String> restriction = new HashMap<String, String>();

			restriction.put("cred_def_id", creDefId);

			restrictionList.add(restriction);
		}
		// restrictionList ?????? End
		
		// attributes ?????? Start
		String attrListStr = trxInfoSvc.getAttrList();
		List<String> attrList = null;

		try {
			attrList = ConfigBean.gson.fromJson(attrListStr, ArrayList.class);
		} catch (JsonSyntaxException e) {
			throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, trxcode, "attrList");
		}

		Map<String, AttributeInfo> attributes = new HashMap<String, AttributeInfo>();
		
		try {
			if (!ObjectUtils.isEmpty(attrList)) {
				for (String attr : attrList) {
					AttributeInfo attributeInfo = new AttributeInfo();

					attributeInfo.setName(attr);

					for (Map<String, String> restriction : restrictionList) {
						attributeInfo.addRestriction(restriction);
					}

					attributes.put("attribute_referent_" + (attrList.indexOf(attr) + 1), attributeInfo);
				}
			}
		} catch (Exception e) {
			throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, trxcode, "attributes");
		}
		// attributes ?????? End
		
		// predicates ?????? Start
		String predListStr = trxInfoSvc.getPredList();
		List<String> predList = null;

		try {
			predList = ConfigBean.gson.fromJson(predListStr, ArrayList.class);
		} catch (JsonSyntaxException e) {
			throw new VerifierException(MipErrorEnum.SP_UNEXPECTED_MSG_FORMAT, trxcode, "predList");
		}

		Map<String, PredicateInfo> predicates = new HashMap<String, PredicateInfo>();

		int index = 1;

		try {
			if (!ObjectUtils.isEmpty(predList)) {
				for (Object obj : predList) {
					Map<String, Object> mapObj = (Map<String, Object>) obj;
					String key = mapObj.keySet().iterator().next();
					Map<String, Object> info = (Map<String, Object>) mapObj.get(key);
					String type = (String) info.get("type");
					Integer value = Integer.parseInt((String) info.get("value"));

					PredicateInfo predicateInfo = new PredicateInfo();

					predicateInfo.setPType(PredicateType.valueOf(type));
					predicateInfo.setName(key);

					if (key.equals("zkpbirth")) {
						LocalDateTime now = LocalDateTime.now();
						LocalDateTime targetDay = now.plusYears(value);

						int year = targetDay.getYear();
						int month = targetDay.getMonthValue();
						int day = targetDay.getDayOfMonth();

						String targetDayStr = String.format("%04d%02d%02d", year, month, day);
						int targetDayInt = Integer.parseInt(targetDayStr);

						predicateInfo.setPValue(targetDayInt);
					} else {
						predicateInfo.setPValue(value);
					}

					for (Map<String, String> restrictionValue : restrictionList) {
						predicateInfo.addRestriction(restrictionValue);
					}

					predicates.put("predicate_referent_" + index, predicateInfo);

					index++;
				}
			}
		} catch (Exception e) {
			throw new VerifierException(MipErrorEnum.UNKNOWN_ERROR, trxcode, "predicates");
		}
		// predicates ?????? End
		
		// proofRequest ?????? Start
		BigInteger nonce = new BigIntegerUtil().createRandomBigInteger(ZkpConstants.LARGE_NONCE);
		SDKResponse response = new ZKPApi().createProofRequest(null, attributes, predicates, nonce);
		ProofRequest proofRequest = (ProofRequest) response.getResultData();
		// proofRequest ?????? End

		return proofRequest;
	}

}
