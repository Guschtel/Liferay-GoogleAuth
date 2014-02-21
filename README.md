Liferay-GoogleAuth
==================

Liferay-GoogleAuth is an Liferay ext plugin enabling Google OAuth 2 login via Googles Services.

It extends Liferay by adding several classes as well as an AutoLogin for the login through Googles Services.

To enable the Google login you need to enable it in your Portal properties file like in the plugins portal-ext.properties.

You need to set at least the following properties and enable the plugin (default):

googleAuth.auth.appId=1234567890.apps.googleusercontent.com  
googleAuth.auth.secret=SomeSecretHere  
googleAuth.auth.redirectUrl=http://example.com/c/login/googleAuth  


These properties can be obtained in the google project console at:
https://cloud.google.com/console/project


Create a new project and enable the required APIs.
In the consent screen you can specify what values (name, logo, etc.) will be presented to the user in the permission screen.


This extension was developed for Liferay 6.2 CE by 

	Dr. Christoph Hermann IT-Unternehmensberatung
	http://drhermann.de
	Email: info@drhermann.de


It was completely rewritten from scratch using eclipse and the Liferay IDE.	
The idea is based on the Rotterdam CS Google+ Authentication plugin which can be found at:
	http://www.rotterdam-cs.com/-/google-authentication
	
