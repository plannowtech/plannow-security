package security.model.sessionstate;

public class UserInfo
{
	private Long userId;

	private Long selectedLanguageId;

	private boolean impersonated;

	public UserInfo(Long userId)
	{
		this.userId = userId;
		impersonated = false;
	}

	public UserInfo(Long userId, Long selectedLanguageId)
	{
		this.userId = userId;
		this.selectedLanguageId = selectedLanguageId;
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

	public Long getSelectedLanguageId()
	{
		return selectedLanguageId;
	}

	public void setSelectedLanguageId(Long selectedLanguageId)
	{
		this.selectedLanguageId = selectedLanguageId;
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
