package com.plannow.security.components;

import java.util.List;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;

import com.plannow.security.entities.Permission;
import com.plannow.security.entities.Role;
import com.plannow.security.entities.RolePermission;
import com.plannow.security.entities.UserRole;
import com.plannow.security.model.Constants;
import com.plannow.security.services.UserService;
import com.plannow.security.utils.CollectionFactory;

public class PermissionsForRoles
{
	@Property
	private Role role;

	@Persist
	@Property
	private Role selectedRole;

	@Property
	private Permission selectedRoleHasPermission;

	@SuppressWarnings("unused")
	@Property
	private Permission selectedRoleHasNoPermission;

	@Property(read = false)
	private int indexNo;

	@Property
	private String name;

	@Inject
	private UserService userService;

	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;

	@Inject
	private Messages messages;

	@InjectComponent
	private Zone permissionsForRolesZone, createRoleZone, editRoleZone,
			lockUnlockRolePermissionZone;

	@SuppressWarnings("unused")
	@Inject
	@Path(Constants.RIGHT_ARROW_ICON_PATH)
	@Property
	private Asset addPermissonIcon;

	@SuppressWarnings("unused")
	@Inject
	@Path(Constants.LEFT_ARROW_ICON_PATH)
	@Property
	private Asset removePermissonIcon;

	@SuppressWarnings("unused")
	@Inject
	@Path(Constants.EDIT_ICON_PATH)
	@Property
	private Asset editRoleIcon;

	@Inject
	@Path(Constants.LOCK_ICON_PATH)
	@Property
	private Asset lockIcon;

	@Inject
	@Path(Constants.UNLOCK_ICON_PATH)
	@Property
	private Asset unlockIcon;

	@SuppressWarnings("unused")
	@Inject
	@Path(Constants.REMOVE_ROLE_ICON_PATH)
	@Property
	private Asset removeRoleIcon;

	void setupRender()
	{
		if (selectedRole == null && getRoles().size() > 0)
			selectedRole = getRoles().get(0);
	}

	public List<Role> getRoles()
	{
		return userService.findAllRoles();
	}

	public List<Permission> getSelectedRoleHasPermissions()
	{
		List<Permission> selectedRoleHasPermissions = CollectionFactory.newList();
		List<RolePermission> rolePermissionsForSelectedRole = userService
				.findRolePermissionForSelectedRole(selectedRole);
		for (RolePermission rolePermission : rolePermissionsForSelectedRole)
		{
			selectedRoleHasPermissions.add(rolePermission.getPermission());
		}
		return selectedRoleHasPermissions;
	}

	public List<Permission> getSelectedRoleHasNoPermissions()
	{
		return userService.findNoPermissionsForSelectedRole(selectedRole);
	}

	public int getIndexNo()
	{
		return indexNo + 1;
	}

	@OnEvent("permissionsForRoles")
	void handlePermissionsForRoles(Role role)
	{
		this.selectedRole = role;
		ajaxResponseRenderer.addRender(permissionsForRolesZone);
	}

	@OnEvent("removePermissionForSelectedRole")
	void handleRemovePermissionForSelectedRole(Role selectedRole,
			Permission selectedRoleHasPermission)
	{
		userService.delete(userService.findSpecificRolePermissionForSelectedRole(selectedRole,
				selectedRoleHasPermission));
		ajaxResponseRenderer.addRender(permissionsForRolesZone);
	}

	@OnEvent("addPermissionForSelectedRole")
	void handleAddPermissionForSelectedRole(Role selectedRole,
			Permission selectedRoleHasNoPermission)
	{
		RolePermission rolePermission = new RolePermission();
		rolePermission.setLocked(false);
		rolePermission.setRole(selectedRole);
		rolePermission.setPermission(selectedRoleHasNoPermission);
		userService.saveOrUpdate(rolePermission);
		ajaxResponseRenderer.addRender(permissionsForRolesZone);
	}

	public String getRoleClass()
	{
		return role.getId().equals(selectedRole.getId()) ? "selected" : "";
	}

	@OnEvent(value = EventConstants.FAILURE, component = "createRoleForm")
	void handleCreateRoleFormFailure()
	{
		ajaxResponseRenderer.addRender(createRoleZone);
	}

	@OnEvent(value = EventConstants.SUCCESS, component = "createRoleForm")
	void handleCreateRoleFormSuccess()
	{
		Role role = new Role();
		role.setName(name);
		userService.saveOrUpdate(role);
		ajaxResponseRenderer.addRender(permissionsForRolesZone);
	}

	@OnEvent("linkDialog")
	void handleEditUser(Role role)
	{
		this.role = role;
	}

	private List<UserRole> userRolesForRole()
	{
		return userService.findUserRoleByRole(role);
	}

	private List<RolePermission> rolePermissionForRole()
	{
		return userService.findRolePermissionForSelectedRole(role);
	}

	public String getWarningMessage()
	{
		String connection1 = null;
		String connection2 = null;

		if (userRolesForRole().size() > 0)
			connection1 = messages.get("exist_relation_with_user");
		if (rolePermissionForRole().size() > 0)
			connection2 = messages.get("exist_relation_with_permission");

		String warningMessage = null;
		if (connection1 != null || connection2 != null)
			warningMessage = messages.get("all_connections_will_be_lost_if_remove_this_role");

		String questionForContinue = messages.get("do_you_want_to_remove_this_role");

		String returnString = null;
		if (warningMessage == null)
			returnString = String.format("%s", questionForContinue);

		if (connection1 == null && connection2 != null)
		{
			returnString = String.format("%s <br/> %s <br/> %s", connection2, warningMessage,
					questionForContinue);
		}

		if (connection1 != null && connection2 == null)
		{
			returnString = String.format("%s <br/> %s <br/> %s", connection1, warningMessage,
					questionForContinue);
		}

		if (connection1 != null && connection2 != null)
			returnString = String.format("%s <br/> %s <br/> %s <br/> %s", connection1, connection2,
					warningMessage, questionForContinue);

		return returnString;
	}

	@OnEvent("removeRole")
	void handleRemoveRole(Role role)
	{
		List<UserRole> userRoles = userService.findUserRoleByRole(role);
		for (UserRole userRole : userRoles)
		{
			userService.delete(userRole);
		}

		List<RolePermission> rolePermissions = userService.findRolePermissionForSelectedRole(role);
		for (RolePermission rolePermission : rolePermissions)
		{
			userService.delete(rolePermission);
		}

		userService.delete(role);

		ajaxResponseRenderer.addRender(permissionsForRolesZone);
	}

	@OnEvent(value = EventConstants.FAILURE, component = "editRoleForm")
	void handleEditRoleFormFailure()
	{
		ajaxResponseRenderer.addRender(editRoleZone);
	}

	@OnEvent(value = EventConstants.PREPARE, component = "editRoleForm")
	void handleEditRoleFormPrepare(Role role)
	{
		this.role = role;
	}

	@OnEvent(value = EventConstants.SUCCESS, component = "editRoleForm")
	void handleEditRoleFormSuccess()
	{
		userService.saveOrUpdate(role);
		ajaxResponseRenderer.addRender(permissionsForRolesZone);
	}

	@OnEvent("lockUnlockRolePermission")
	void handleLockUnlockRolePermission(Permission selectedRoleHasPermission, Role selectedRole)
	{
		this.selectedRoleHasPermission = selectedRoleHasPermission;
		this.selectedRole = selectedRole;

		RolePermission rolePermission = userService.findSpecificRolePermissionForSelectedRole(
				selectedRole, selectedRoleHasPermission);

		rolePermission.setLocked(!rolePermission.isLocked());

		userService.saveOrUpdate(rolePermission);
		ajaxResponseRenderer.addRender(lockUnlockRolePermissionZone);
	}

	public String getZoneId()
	{
		return selectedRoleHasPermission.getId() + "zone";
	}

	public Asset getLockedValue(Permission permission)
	{
		RolePermission rolePermission = userService.findSpecificRolePermissionForSelectedRole(
				selectedRole, permission);
		return rolePermission.isLocked() ? lockIcon : unlockIcon;
	}

}
