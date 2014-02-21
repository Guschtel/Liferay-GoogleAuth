package com.liferay.portal.util;

import javax.servlet.http.HttpSession;

import com.liferay.portal.googleauth.GoogleAuthImpl;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.googleauth.GoogleAuth;
import com.liferay.portal.model.User;

/**
 * Utility class to access the methods in {@link GoogleAuth}
 * @author guschtel
 */
public class GoogleAuthUtil {
	
	public static boolean isEnabled(long companyId) throws SystemException {
		return getGoogleAuth().isEnabled(companyId);
	}

	public static String getAccessToken(long companyId, String redirect,
			String authCode) throws SystemException {
		return getGoogleAuth().getAccessToken(companyId, redirect, authCode);
	}

	public static User createOrUpdateUser(HttpSession session, long companyId,
			String accessToken) throws SystemException, PortalException {
		return getGoogleAuth().createOrUpdateUser(session, companyId, accessToken);
	}

	public static void setGoogleCredentials(HttpSession session, long companyId,
			User user) throws SystemException {
		getGoogleAuth().setGoogleCredentials(session, companyId, user);
	}
	
	public static String getAuthUrl(long companyId) throws SystemException {
		return getGoogleAuth().getAuthUrl(companyId);
	}
	
	public static String getAppId(long companyId) throws SystemException {
		return getGoogleAuth().getAppId(companyId);
	}
	
	public static String getRedirectUrl(long companyId) throws SystemException {
		return getGoogleAuth().getRedirectUrl(companyId);
	}
	
	public static String getScope(long companyId, boolean encodeUrl) throws SystemException {
		return getGoogleAuth().getScope(companyId, encodeUrl);
	}
	
	public static String getSecret(long companyId) throws SystemException {
		return getGoogleAuth().getSecret(companyId);
	}
	
	/**
	 * Method used to access the Google Auth Util 
	 * See {@link GoogleAuth} for a list of methods
	 * 
	 * @return {@link GoogleAuth}
	 */
	public static GoogleAuth getGoogleAuth() {
		if (_googleAuth == null) {
			_googleAuth = new GoogleAuthImpl();
		}
		return _googleAuth;
	}
	
	private static GoogleAuth _googleAuth = null;

	
}
