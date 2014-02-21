package com.liferay.portal.security.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.GoogleAuthUtil;
import com.liferay.portal.util.GoogleAuthWebKeys;
import com.liferay.portal.util.PortalUtil;

public class GoogleAuthAutoLogin implements AutoLogin {

	@Override
	public String[] handleException(HttpServletRequest request,
			HttpServletResponse response, Exception e)
			throws AutoLoginException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] login(HttpServletRequest request,
			HttpServletResponse response) throws AutoLoginException {
		
		// we need to set the credentials for the user to be logged in
		String[] credentials = null;
		
		try {
			long companyId = PortalUtil.getCompanyId(request);

			// if disabled don't return any credentials
			if (!GoogleAuthUtil.isEnabled(companyId)) {
				return credentials;
			}
			
			// get email adress from session
			HttpSession session = request.getSession();

			String emailAddress = (String) session.getAttribute(GoogleAuthWebKeys.GOOGLE_AUTH_EMAIL);
			
			// do nothing if no email set in session
			if (emailAddress == null || emailAddress.equals(StringPool.BLANK)) {
				return credentials;
			}
			
			User user = null;
			try {
				user = UserLocalServiceUtil.getUserByEmailAddress(companyId, emailAddress);
				
				credentials = new String[3];
				credentials[0] = String.valueOf(user.getUserId());
				credentials[1] = user.getPassword();
				credentials[2] = Boolean.FALSE.toString();
				
				// clear the key from session (user gets authenticated now)
				session.removeAttribute(GoogleAuthWebKeys.GOOGLE_AUTH_EMAIL);
				
			} catch (NoSuchUserException nsu) {
				_LOG.error("Could not fetch user by Email: " + emailAddress, nsu);
			}
			
			
		} catch (Exception ex) {
			_LOG.error(ex);
		}
		
		// return the given credentials (none if no user found or disabled)
		return credentials;
	}
	
	private static Log _LOG = LogFactoryUtil.getLog(GoogleAuthAutoLogin.class);

}
