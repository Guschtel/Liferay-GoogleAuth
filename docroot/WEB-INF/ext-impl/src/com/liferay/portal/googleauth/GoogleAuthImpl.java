package com.liferay.portal.googleauth;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.googleauth.GoogleAuth;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.Http.Body;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.URLCodec;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.GoogleAuthPropsKeys;
import com.liferay.portal.util.GoogleAuthUtil;
import com.liferay.portal.util.GoogleAuthWebKeys;

/**
 * Class implementing the methods listed in the interface class at {@link GoogleAuth}
 * 
 * @author guschtel
 *
 */
public class GoogleAuthImpl implements GoogleAuth {

	@Override
	public boolean isEnabled(long companyId) throws SystemException {
		return PrefsPropsUtil.getBoolean(companyId, GoogleAuthPropsKeys.GOOGLE_AUTH_ENABLED, true);
	}

	/**
	 * using the provided code obtain an access token to be able to make further calls to googles APIs
	 * @throws SystemException 
	 */
	@Override
	public String getAccessToken(long companyId, String redirect,
			String authCode) throws SystemException {
		// using the code obtain an access token from google
		String accessTokenUrl = PrefsPropsUtil.getString(companyId, GoogleAuthPropsKeys.GOOGLE_AUTH_TOKEN_URL, "https://accounts.google.com/o/oauth2/token");
		
		// build the required HTTP post parameters
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("client_id=");
		queryBuilder.append(getAppId(companyId));
		queryBuilder.append("&client_secret=");
		queryBuilder.append(getSecret(companyId));
		queryBuilder.append("&code=");
		queryBuilder.append(URLCodec.encodeURL(authCode, StringPool.UTF8, true));
		queryBuilder.append("&grant_type=authorization_code");
		queryBuilder.append("&redirect_uri=");
		queryBuilder.append(HttpUtil.encodeURL(GoogleAuthUtil.getRedirectUrl(companyId)));
		
		Body body = new Body(queryBuilder.toString(), ContentTypes.APPLICATION_X_WWW_FORM_URLENCODED, StringPool.UTF8);

		Http.Options options = new Http.Options();
		options.setBody(body);
		options.setLocation(accessTokenUrl);
		options.setPost(true);

		try {
			// get response
			String content = HttpUtil.URLtoString(options);
			if (_LOG.isDebugEnabled()) {
				_LOG.debug("content=" + content);
			}

			// build json object from response string
			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(content);

			// extrakt token value from response json object
			return jsonObject.getString("access_token");
		} catch (Exception ex) {
			throw new SystemException("Unable to retrieve Google access token", ex);
		}
	}

	@Override
	public User createOrUpdateUser(HttpSession session, long companyId,
			String accessToken) throws SystemException, PortalException {
		
		// create or update new user given the data from google
		
		// fetch details using the access token
		JSONObject graphData = getGoogleGraphObject(companyId, "", accessToken, "");
		
		// fetch email
		String emailAddress = graphData.getString("email");
		
		// check if user exists, if not create it
		User user = null;
		try {
			user = UserLocalServiceUtil.getUserByEmailAddress(companyId, emailAddress);
			// update, since user already exists
			user = updateUser(user, graphData);
		} catch (NoSuchUserException nsu) {
			// create since no such user could be found
			user = addUser(companyId, session, graphData);
		}
		
		// return user
		return user;
	}
	
	
	private User addUser(long companyId, HttpSession session,
			JSONObject graphData) throws SystemException, PortalException {
		
		if (_LOG.isDebugEnabled()) {
			_LOG.debug("Creating new user");
		}
		
		String emailAddress = graphData.getString("email");
		String firstName = graphData.getString("given_name");
		String lastName = graphData.getString("family_name");
		String pictureUrl = graphData.getString("picture");
		boolean male = Validator.equals(graphData.getString("gender"), "male");

		long creatorUserId = 0;
		boolean autoPassword = true;
		String password1 = StringPool.BLANK;
		String password2 = StringPool.BLANK;
		boolean autoScreenName = true;
		String screenName = StringPool.BLANK;
		long facebookId = 0;
		String openId = StringPool.BLANK;
		Locale locale = LocaleUtil.getDefault();
		String middleName = StringPool.BLANK;
		int prefixId = 0;
		int suffixId = 0;
		int birthdayMonth = Calendar.JANUARY;
		int birthdayDay = 1;
		int birthdayYear = 1970;
		String jobTitle = StringPool.BLANK;
		long[] groupIds = null;
		long[] organizationIds = null;
		long[] roleIds = null;
		long[] userGroupIds = null;
		boolean sendEmail = true;

		ServiceContext serviceContext = new ServiceContext();

		User user = UserLocalServiceUtil.addUser(creatorUserId, companyId,
				autoPassword, password1, password2, autoScreenName, screenName,
				emailAddress, facebookId, openId, locale, firstName,
				middleName, lastName, prefixId, suffixId, male, birthdayMonth,
				birthdayDay, birthdayYear, jobTitle, groupIds, organizationIds,
				roleIds, userGroupIds, sendEmail, serviceContext);

		UserLocalServiceUtil.updateLastLogin(user.getUserId(), user.getLoginIP());

		UserLocalServiceUtil.updatePasswordReset(user.getUserId(), true);

		UserLocalServiceUtil.updateEmailAddressVerified(user.getUserId(), true);

		user.setAgreedToTermsOfUse(true);
		user.setReminderQueryQuestion("No question specified");
		user.setReminderQueryAnswer(new BigInteger(130, new SecureRandom()).toString(32));
		user.setPasswordReset(false);
		UserLocalServiceUtil.updateUser(user);
		
		// TODO add image from google account
		
		return user;
	}

	private User updateUser(User user, JSONObject graphData) {
		// TODO update User object
		
		// return the user object (necessary for further login processing)
		return user;
	}

	private JSONObject getGoogleGraphObject(long companyId, String apiPath, String accessToken, String fields) {

		try {
			String url = HttpUtil.addParameter(getGoogleGraphURL(companyId).concat(apiPath), "access_token", accessToken);

			// build an http request
			Http.Options options = new Http.Options();
			options.setLocation(url);

			// get the content as string from the request
			String content = HttpUtil.URLtoString(options);

			// return the created JSONObject from the content
			return JSONFactoryUtil.createJSONObject(content);

		} catch (Exception e) {
			if (_LOG.isWarnEnabled()) {
				_LOG.warn(e, e);
			}
		}

		return null;
	}

	/**
	 * Return the Google Auth URL where to fetch Graph data from
	 * @param companyId
	 * @return
	 * @throws SystemException
	 */
	private String getGoogleGraphURL(long companyId) throws SystemException {
		return PrefsPropsUtil.getString(companyId, GoogleAuthPropsKeys.GOOGLE_GRAPH_URL, "https://www.googleapis.com/oauth2/v1/userinfo");
	}

	/**
	 * Set the credentials needed in the session for the Google Auth AutoLogin
	 */
	@Override
	public void setGoogleCredentials(HttpSession session, long companyId,
			User user) throws SystemException {
		if (user != null) {
			session.setAttribute(GoogleAuthWebKeys.GOOGLE_AUTH_EMAIL, user.getEmailAddress());
		}
	}
	
	@Override
	public String getAuthUrl(long companyId) throws SystemException {
		return PrefsPropsUtil.getString(companyId, GoogleAuthPropsKeys.GOOGLE_AUTH_URL, "https://accounts.google.com/o/oauth2/auth");
	}
	
	@Override
	public String getAppId(long companyId) throws SystemException {
		return PrefsPropsUtil.getString(companyId, GoogleAuthPropsKeys.GOOGLE_AUTH_APP_ID, "");
	}
	
	@Override
	public String getRedirectUrl(long companyId) throws SystemException {
		return PrefsPropsUtil.getString(companyId, GoogleAuthPropsKeys.GOOGLE_AUTH_REDIRECT_URL, "http://localhost:8080/c/login/googleAuth");
	}
	
	/**
	 * Get scope stored as comma-separated array and return as google auth compatible string
	 */
	@Override
	public String getScope(long companyId, boolean encodeUrl) throws SystemException {
		if (encodeUrl) {
			String[] stringArray = PrefsPropsUtil.getStringArray(companyId, GoogleAuthPropsKeys.GOOGLE_AUTH_SCOPE, StringPool.COMMA);
			if (stringArray != null) {
				try {
					StringBuilder str = new StringBuilder();
					
						str.append(URLCodec.encodeURL(stringArray[0], StringPool.UTF8, true));
						str.append("+");
						str.append(URLCodec.encodeURL(stringArray[1], StringPool.UTF8, true));
					return str.toString();
				} catch (Exception e) {
					_LOG.error(e.getMessage());
				}
			}
		} else {
			PrefsPropsUtil.getString(companyId, GoogleAuthPropsKeys.GOOGLE_AUTH_SCOPE, "https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email");
		}
		return "https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email";
	}
	
	@Override
	public String getSecret(long companyId) throws SystemException {
		return PrefsPropsUtil.getString(companyId, GoogleAuthPropsKeys.GOOGLE_AUTH_SECCRET, "");
	}
	
	private static Log _LOG = LogFactoryUtil.getLog(GoogleAuthImpl.class);
}
