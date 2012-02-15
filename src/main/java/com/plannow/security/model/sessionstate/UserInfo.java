package com.plannow.security.model.sessionstate;

public class UserInfo
{
	private Long userId;

	private boolean impersonated;

	public UserInfo(Long userId)
	{
		this.userId = userId;
		impersonated = false;
	}

	public Long getUserId()
	{
		return userId;
	}

	public void setUserId(Long userId)
	{
		this.userId = userId;
	}

	public boolean isImpersonated()
	{
		return impersonated;
	}

	public void setImpersonated(boolean impersonated)
	{
		this.impersonated = impersonated;
	}
}
