package com.plannow.security.services;

/**
 * This service needs all of the symbols from PlannowSecuritySymbols.
 * 
 * @author dusko
 * 
 */
public interface MailService
{
	boolean sendMailTo(String address, String subject, String message);

	boolean sendHtmlMailTo(String address, String subject, String message);
}
