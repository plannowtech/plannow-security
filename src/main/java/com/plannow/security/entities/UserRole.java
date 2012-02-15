package com.plannow.security.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.tapestry5.beaneditor.Validate;

import com.plannow.security.model.Constants;


@Entity
@Table(name = Constants.PLANNOW_SECURITY_DEFAULT_TABLE_PREFIX + "user_roles")
public class UserRole extends BaseEntityImpl
{
	private boolean locked;

	private User2 user;
	private Role role;

	@Column(name = "is_locked", nullable = false)
	public boolean isLocked()
	{
		return locked;
	}

	public void setLocked(boolean locked)
	{
		this.locked = locked;
	}

	@ManyToOne
	@Validate("required")
	@JoinColumn(name = "user_id", nullable = false)
	public User2 getUser()
	{
		return user;
	}

	public void setUser(User2 user)
	{
		this.user = user;
	}

	@ManyToOne
	@Validate("required")
	@JoinColumn(name = "role_id")
	public Role getRole()
	{
		return role;
	}

	public void setRole(Role role)
	{
		this.role = role;
	}

}
