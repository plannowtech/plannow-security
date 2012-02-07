package security.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.tapestry5.beaneditor.Validate;

import security.model.Constants;

@Entity
@Table(name = Constants.PLANNOW_SECURITY_DEFAULT_TABLE_PREFIX + "roles")
public class Role extends BaseEntityImpl
{
	private String name;
	
	private List<UserRole> userRoles;
	private List<RolePermission> rolePermissions;

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

	@OneToMany(mappedBy = "role")
	public List<UserRole> getUserRoles()
	{
		return userRoles;
	}

	public void setUserRoles(List<UserRole> userRoles)
	{
		this.userRoles = userRoles;
	}

	@OneToMany(mappedBy = "role")
	public List<RolePermission> getRolePermissions()
	{
		return rolePermissions;
	}

	public void setRolePermissions(List<RolePermission> rolePermissions)
	{
		this.rolePermissions = rolePermissions;
	}
}
