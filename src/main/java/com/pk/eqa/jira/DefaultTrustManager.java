package com.pk.eqa.jira;

import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

public class DefaultTrustManager implements X509TrustManager {

	public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

	public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return null;

	}

	public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
			throws java.security.cert.CertificateException {
		// TODO Auto-generated method stub

	}

	public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
			throws java.security.cert.CertificateException {
		// TODO Auto-generated method stub

	}

}
