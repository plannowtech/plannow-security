package security.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.tapestry5.beaneditor.Validate;

import security.model.Constants;

@Entity
@Table(name = Constants.PLANNOW_SECURITY_DEFAULT_TABLE_PREFIX + "role_permissions")
public class RolePermission extends BaseEntityImpl
{
	private boolean locked;

	private Role role;
	private Permission permission;

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
	@JoinColumn(name = "role_id")
	public Role getRole()
	{
		return role;
	}

	public void setRole(Role role)
	{
		this.role = role;
	}

	@ManyToOne
	@Validate("required")
	@JoinColumn(name = "permission_id")
	public Permission getPermission()
	{
		return permission;
	}

	public void setPermission(Permission permission)
	{
		this.permission = permission;
	}
}
