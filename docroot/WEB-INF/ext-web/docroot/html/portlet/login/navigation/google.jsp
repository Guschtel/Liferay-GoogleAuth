<%--
/**
 * Copyright (c) 2014 Dr. Christoph Hermann IT-Unternehmensberatung. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ include file="/html/portlet/login/init.jsp" %>

<%@ page import="com.liferay.portal.util.GoogleAuthUtil" %>
<%@ page import="com.liferay.portal.kernel.util.ParamUtil" %>
<%@ page import="com.liferay.portal.kernel.util.HttpUtil" %>
<%@ page import="com.liferay.portal.kernel.portlet.LiferayWindowState" %>

<%
String strutsAction = ParamUtil.getString(request, "struts_action");

boolean showGoogleConnectIcon = false;

if (!strutsAction.startsWith("/login/Google_connect") && GoogleAuthUtil.isEnabled(company.getCompanyId())) {
	showGoogleConnectIcon = true;
}
%>

<c:if test="<%= showGoogleConnectIcon %>">
	<portlet:renderURL var="loginRedirectURL" windowState="<%= LiferayWindowState.POP_UP.toString() %>">
		<portlet:param name="struts_action" value="/login/login_redirect" />
	</portlet:renderURL>

	<%
	// url google redirects us after successful login
	String googleAuthRedirectUrl = GoogleAuthUtil.getRedirectUrl(themeDisplay.getCompanyId());
	// will return to something like http://localhost:8080/c/login/googleAuth?code=XYZ where XYZ is the code required for further processing
	
	String googleAuthUrl = GoogleAuthUtil.getAuthUrl(themeDisplay.getCompanyId());
	
	//  build google auth URL supplying additional parameters
	googleAuthUrl = HttpUtil.addParameter(googleAuthUrl, "client_id", GoogleAuthUtil.getAppId(themeDisplay.getCompanyId()));
	// googleAuthUrl = HttpUtil.addParameter(googleAuthUrl, "redirect_uri", HttpUtil.encodeURL(googleAuthRedirectUrl));
	googleAuthUrl = HttpUtil.addParameter(googleAuthUrl, "redirect_uri", googleAuthRedirectUrl);
	googleAuthUrl = HttpUtil.addParameter(googleAuthUrl, "response_type", "code");
	// googleAuthUrl = HttpUtil.addParameter(googleAuthUrl, "scope", GoogleAuthUtil.getScope(themeDisplay.getCompanyId(), true));
	googleAuthUrl = HttpUtil.addParameter(googleAuthUrl, "scope", GoogleAuthUtil.getScope(themeDisplay.getCompanyId(), false));

	// String taglibOpenGoogleConnectLoginWindow = "javascript:var googleConnectLoginWindow = window.open('" + googleAuthUrl.toString() + "', 'Google', 'align=center,directories=no,height=560,location=no,menubar=no,resizable=yes,scrollbars=yes,status=no,toolbar=no,width=1000'); void(''); googleConnectLoginWindow.focus();";
	// no popup
	String taglibOpenGoogleConnectLoginWindow = googleAuthUrl.toString();
	%>

	<!-- TODO I18N google.login -->
	<liferay-ui:icon
		image="../social_bookmarks/google-icon"
		message="google.login"
		url="<%= taglibOpenGoogleConnectLoginWindow %>"
	/>
</c:if>