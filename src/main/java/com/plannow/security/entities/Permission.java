package com.plannow.security.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.tapestry5.beaneditor.Validate;

import com.plannow.security.model.Constants;


@Entity
@Table(name = Constants.PLANNOW_SECURITY_DEFAULT_TABLE_PREFIX + "permissions")
public class Permission extends BaseEntityImpl
{
	private String name;

	private List<RolePermission> rolePermissions;
	private List<UserPermission> userPermissions;

	@Validate("required")
	@Column(name = "name", nullable = false)
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@OneToMany(mappedBy = "permission")
	public List<RolePermission> getRolePermissions()
	{
		return rolePermissions;
	}

	public void setRolePermissions(List<RolePermission> rolePermissions)
	{
		this.rolePermissions = rolePermissions;
	}

	@OneToMany(mappedBy = "permission")
	public List<UserPermission> getUserPermissions()
	{
		return userPermissions;
	}

	public void setUserPermissions(List<UserPermission> userPermissions)
	{
		this.userPermissions = userPermissions;
	}
}
