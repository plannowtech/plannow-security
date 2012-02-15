package com.plannow.security.entities;

import java.util.List;

import javax.persistence.Access;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha1Hash;
import org.apache.shiro.util.ByteSource;
import org.apache.tapestry5.beaneditor.Validate;
import org.hibernate.annotations.Proxy;

import com.plannow.security.model.Constants;

@Entity
@Table(name = Constants.PLANNOW_SECURITY_DEFAULT_TABLE_PREFIX + "users")
@Proxy(lazy = false)
public class User2 extends BaseEntityImpl
{
	private String username;
	@Access(value = javax.persistence.AccessType.FIELD)
	@Column(name = "password_hash", nullable = false)
	private String passwordHash;
	private String email;
	private boolean active;
	private byte[] passwordSalt;
	private String validationCode;

	private List<UserRole> userRoles;
	private List<UserPermission> userPermissions;

	@Validate("required")
	@Column(name = "username", nullable = false, unique = true)
	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	@Validate("required")
	@Transient
	public String getPassword()
	{
		return "";
	}

	public void setPassword(String password)
	{
		if (password != null && !password.equals(passwordHash) && !"".equals(password))
		{
			ByteSource saltSource = new SecureRandomNumberGenerator().nextBytes();
			this.passwordSalt = saltSource.getBytes();
			this.passwordHash = new Sha1Hash(password, saltSource).toString();
		}
	}

	public String getPasswordHash()
	{
		return passwordHash;
	}

	@Column(name = "e_mail", nullable = false, unique = true)
	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	@Column(name = "is_active", nullable = false)
	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	@Lob
	@Column(name = "password_salt")
	public byte[] getPasswordSalt()
	{
		return passwordSalt;
	}

	public void setPasswordSalt(byte[] passwordSalt)
	{
		this.passwordSalt = passwordSalt;
	}

	@Column(name = "validation_code", nullable = false)
	public String getValidationCode()
	{
		return validationCode;
	}

	public void setValidationCode(String validationCode)
	{
		this.validationCode = validationCode;
	}

	@OneToMany(mappedBy = "user")
	public List<UserRole> getUserRoles()
	{
		return userRoles;
	}

	public void setUserRoles(List<UserRole> userRoles)
	{
		this.userRoles = userRoles;
	}

	@OneToMany(mappedBy = "user")
	public List<UserPermission> getUserPermissions()
	{
		return userPermissions;
	}

	public void setUserPermissions(List<UserPermission> userPermissions)
	{
		this.userPermissions = userPermissions;
	}
}
