package mip.mva.verifier.comm.exception;

import mip.mva.verifier.comm.enums.MipErrorEnum;
import mip.mva.verifier.comm.enums.ProxyErrorEnum;

/**
 * @Project     : 모바일 운전면허증 서비스 구축 사업
 * @PackageName : mip.mva.verifier.comm.exception
 * @FileName    : VerifierException.java
 * @Author      : Min Gi Ju
 * @Date        : 2022. 6. 8.
 * @Description : Verifier Exception
 * ==================================================
 * DATE            AUTHOR           NOTE
 * ==================================================
 * 2022. 6. 8.    Min Gi Ju        최초생성
 */
public class VerifierException extends Exception {

	private static final long serialVersionUID = -5755027831540928495L;

	/** 오류코드 */
	private Integer errcode;
	/** 오류메세지 */
	private String errmsg;
	/** 거래코드 */
	private String trxcode;

	/**
	 * 생성자
	 * 
	 * @param errcode 오류코드
	 * @param errmsg 오류메세지
	 */
	public VerifierException(Integer errcode, String errmsg) {
		super();

		this.errcode = errcode;
		this.errmsg = errmsg;
	}

	/**
	 * 생성자
	 * 
	 * @param errcode 오류코드
	 * @param errmsg 오류메세지
	 * @param trxcode 거래코드
	 */
	public VerifierException(Integer errcode, String errmsg, String trxcode) {
		super();

		this.errcode = errcode;
		this.errmsg = errmsg;
		this.trxcode = trxcode;
	}

	/**
	 * 생성자
	 * 
	 * @param mipErrorEnum MIP 오류정보
	 */
	public VerifierException(MipErrorEnum mipErrorEnum) {
		super();

		this.errcode = mipErrorEnum.getCode();
		this.errmsg = mipErrorEnum.getMsg();
	}

	/**
	 * 생성자
	 * 
	 * @param mipErrorEnum MIP 오류정보
	 * @param trxcode 거래코드
	 */
	public VerifierException(MipErrorEnum mipErrorEnum, String trxcode) {
		super();

		this.errcode = mipErrorEnum.getCode();
		this.errmsg = mipErrorEnum.getMsg();
		this.trxcode = trxcode;
	}

	/**
	 * 생성자
	 * 
	 * @param mipErrorEnum MIP 오류정보
	 * @param trxcode 거래코드
	 * @param reason 오류사유
	 */
	public VerifierException(MipErrorEnum mipErrorEnum, String trxcode, String reason) {
		super();

		this.errcode = mipErrorEnum.getCode();
		this.errmsg = mipErrorEnum.getMsg() + " : " + reason;
		this.trxcode = trxcode;
	}

	/**
	 * 생성자
	 * 
	 * @param proxyErrorEnum Proxy 오류정보
	 */
	public VerifierException(ProxyErrorEnum proxyErrorEnum) {
		super();

		this.errcode = proxyErrorEnum.getCode();
		this.errmsg = proxyErrorEnum.getMsg();
	}

	/**
	 * 생성자
	 * 
	 * @param proxyErrorEnum Proxy 오류정보
	 * @param trxcode 거래코드
	 */
	public VerifierException(ProxyErrorEnum proxyErrorEnum, String trxcode) {
		super();

		this.errcode = proxyErrorEnum.getCode();
		this.errmsg = proxyErrorEnum.getMsg();
		this.trxcode = trxcode;
	}

	/**
	 * 생성자
	 * 
	 * @param proxyErrorEnum Proxy 오류정보
	 * @param trxcode 거래코드
	 * @param reason 오류사유
	 */
	public VerifierException(ProxyErrorEnum proxyErrorEnum, String trxcode, String reason) {
		super();

		this.errcode = proxyErrorEnum.getCode();
		this.errmsg = proxyErrorEnum.getMsg() + " : " + reason;
		this.trxcode = trxcode;
	}

	public String getTrxcode() {
		return trxcode;
	}

	public void setTrxcode(String trxcode) {
		this.trxcode = trxcode;
	}

	public Integer getErrcode() {
		return errcode;
	}

	public void setErrcode(Integer errcode) {
		this.errcode = errcode;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

}
