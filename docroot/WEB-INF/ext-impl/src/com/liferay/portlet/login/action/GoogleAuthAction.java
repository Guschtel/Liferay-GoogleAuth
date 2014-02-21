package com.liferay.portlet.login.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.struts.PortletAction;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.GoogleAuthUtil;
import com.liferay.portal.util.GoogleAuthWebKeys;

/**
 * See this blog entry on how to create struts actions from within hook plugins
 * http://www.liferay.com/de/web/mika.koivisto/blog/-/blogs/7132115
 * 
 * This class gets called after we passed the Google Authentication It's called
 * by the respective struts-action /login/googleAuth It basically just sends a
 * redirect to /login/login_redirect after setting the access token
 * 
 * @author guschtel
 * 
 */
public class GoogleAuthAction extends PortletAction {

	@Override
	public ActionForward strutsExecute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		if (_log.isDebugEnabled()) {
			_log.debug("GoogleAuthAction entered");
		}

		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		HttpSession session = request.getSession();
		long companyId = themeDisplay.getCompanyId();

		// read the parameters given back by google
		// store the token in the session for accessing it later

		// default redirect if none given
		String redirect = ParamUtil.getString(request, "redirect", "/");

		// get the code parameter returned by gogle
		String authCode = ParamUtil.getString(request, "code");

		// generate Google token from request
		String accessToken = GoogleAuthUtil.getAccessToken(companyId, redirect, authCode);

		if (Validator.isNotNull(accessToken)) {
			// set the access token in the session for further use
			session.setAttribute(GoogleAuthWebKeys.GOOGLE_AUTH_ACCESS_TOKEN, accessToken);

			// use the access token to fetch google details like email, etc. and
			// create a user account with it
			User user = GoogleAuthUtil.createOrUpdateUser(session, companyId, accessToken);

			// set google id and email for use in AutoLogin
			GoogleAuthUtil.setGoogleCredentials(session, companyId, user);

		} else {
			// failed to get access token, return root page
			redirect = "/";
		}

		if (_log.isDebugEnabled()) {
			_log.debug("returning redirect: " + redirect);
		}
		
		// after action send a redirect
		response.sendRedirect(redirect);
		return null;
	}

	private static Log _log = LogFactoryUtil.getLog(GoogleAuthAction.class);

}
