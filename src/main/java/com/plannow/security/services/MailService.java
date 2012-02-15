package com.plannow.security.services;

public interface MailService
{
	boolean sendMailTo(String address, String subject, String message);

	boolean sendHtmlMailTo(String address, String subject, String message);
}
