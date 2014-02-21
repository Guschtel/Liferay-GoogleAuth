package com.liferay.portal.kernel.googleauth;

import javax.servlet.http.HttpSession;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;

/**
 * Interface defining the methods in our GoogleAuthUtil
 * 
 * https://developers.google.com/accounts/docs/OAuth2?hl=de
 * OAuth 2.0 is a relatively simple protocol. To begin, you register your
 * application with Google. Then your client application requests an access
 * token from the Google Authorization Server, extracts a token from the
 * response, and sends the token to the Google API that you want to access.
 * 
 * 
 * @author guschtel
 * 
 */
public interface GoogleAuth {
	
	/**
	 * Check whether the google Auth is enabled
	 * @param companyId
	 * @return
	 */
	public boolean isEnabled(long companyId) throws SystemException;

	/**
	 * create an access token from the auth code using the google API
	 * @param companyId
	 * @param redirect
	 * @param authCode
	 * @return
	 */
	public String getAccessToken(long companyId, String redirect, String authCode) throws SystemException;
	
	
	/**
	 * Create or update a {@link User} using the data provided by google
	 * @param session
	 * @param companyId
	 * @param accessToken
	 * @return
	 * @throws PortalException 
	 */
	public User createOrUpdateUser(HttpSession session, long companyId, String accessToken) throws SystemException, PortalException;
	
	/**
	 * Set the credentials required for GoogleAutoLogin in the session
	 * @param session
	 * @param companyId
	 * @param user
	 */
	public void setGoogleCredentials(HttpSession session, long companyId, User user) throws SystemException;
	
	/**
	 * Get the Google Auth URL from properties
	 * @param companyId
	 * @return URL
	 */
	public String getAuthUrl(long companyId) throws SystemException;

	/**
	 * Return the app id given in the props file necessary for google auth
	 * @param companyId
	 * @return
	 * @throws SystemException
	 */
	public String getAppId(long companyId) throws SystemException;

	/**
	 * Returns the (internal) redirect URL, i.e. our struts action handling the google auth process
	 * @param companyId
	 * @return
	 * @throws SystemException
	 */
	public String getRedirectUrl(long companyId) throws SystemException;

	/**
	 * Returns the scope for the Google auth
	 * @param companyId
	 * @return
	 * @throws SystemException
	 */
	public String getScope(long companyId, boolean encodeUrl) throws SystemException;

	/**
	 * Returns the secret for this google app
	 * @param companyId
	 * @return
	 */
	public String getSecret(long companyId) throws SystemException;
}
