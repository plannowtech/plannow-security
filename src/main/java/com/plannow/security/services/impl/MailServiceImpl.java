package com.plannow.security.services.impl;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;

import com.plannow.security.model.PlannowSecuritySymbols;
import com.plannow.security.services.MailService;

public class MailServiceImpl implements MailService
{
	private static final String MAIL_CONTENT_CHARSET = "UTF-8";

	@Inject
	@Symbol(PlannowSecuritySymbols.MAIL_HOST)
	private String mailHost;

	@Inject
	@Symbol(PlannowSecuritySymbols.MAIL_PORT)
	private int mailPort;

	@Inject
	@Symbol(PlannowSecuritySymbols.MAIL_ADDRESS)
	private String mailAddress;

	@Inject
	@Symbol(PlannowSecuritySymbols.MAIL_USERNAME)
	private String mailUsername;

	@Inject
	@Symbol(PlannowSecuritySymbols.MAIL_PASSWORD)
	private String mailPassword;

	@Override
	public boolean sendMailTo(String address, String subject, String message)
	{

		Email email = new SimpleEmail();
		email.setHostName(mailHost);
		email.setSmtpPort(mailPort);
		email.setAuthenticator(new DefaultAuthenticator(mailUsername, mailPassword));
		email.setTLS(true);

		try
		{
			email.setFrom(mailAddress);

			email.setSubject(subject);
			email.setCharset(MAIL_CONTENT_CHARSET);
			email.setMsg(message);
			email.addTo(address);

			email.send();

			return true;
		}
		catch (EmailException e)
		{
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public boolean sendHtmlMailTo(String address, String subject, String message)
	{
		HtmlEmail email = new HtmlEmail();
		email.setHostName(mailHost);
		email.setSmtpPort(mailPort);
		email.setAuthenticator(new DefaultAuthenticator(mailUsername, mailPassword));
		email.setTLS(true);
		try
		{
			email.setFrom(mailAddress);

			email.setSubject(subject);

			email.setCharset(MAIL_CONTENT_CHARSET);

			email.setHtmlMsg(message);

			email.addTo(address);

			email.send();

			return true;

		}
		catch (EmailException e)
		{
			e.printStackTrace();

			return false;
		}

	}

}
