package com.plannow.security.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ValueEncoderSource;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;

import com.plannow.security.entities.Role;
import com.plannow.security.entities.User2;
import com.plannow.security.entities.UserRole;
import com.plannow.security.services.UserService;

public class PermissionsForUser
{
	@Parameter(required = true)
	@Property
	private User2 user;

	@Parameter(required = true)
	@Property
	private Zone zone;

	@Inject
	private UserService userService;

	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;

	@Property(read = false)
	private List<String> checkedListSelectedPermissionValues;

	public List<String> getCheckedListSelectedPermissionValues()
	{
		return new ArrayList(userService.findAllPermissionsForUser(user.getEmail()));
	}

	public List<String> getAllPermissions()
	{
		return (List<String>) userService.findNonRolePermissionsForUser(user.getEmail());
	}

	@Inject
	private ValueEncoderSource valueEncoderSource;

	public ValueEncoder<String> getPermissionEncoder()
	{
		return valueEncoderSource.getValueEncoder(String.class);
	}

	@OnEvent(value = EventConstants.PREPARE, component = "permissionsForm")
	void handleRolesFormPrepare(User2 user)
	{
		this.user = user;
	}

	@OnEvent(value = EventConstants.SUCCESS, component = "permissionsForm")
	void handlePermissionsFormSuccess()
	{
		List<Role> roles = userService.findRolesForUser(user);
		List<Role> deleteRoles = (List<Role>) CollectionUtils.subtract(roles,
				checkedListSelectedPermissionValues);
		List<Role> addRoles = (List<Role>) CollectionUtils.subtract(
				checkedListSelectedPermissionValues, roles);
		for (Role role : addRoles)
		{
			UserRole userRole = new UserRole();
			userRole.setLocked(false);
			userRole.setUser(user);
			userRole.setRole(role);
			userService.saveOrUpdate(userRole);
		}

		for (Role role : deleteRoles)
		{
			userService.delete(userService.findUserRoleByUserAndRole(user, role));
		}

		ajaxResponseRenderer.addRender(zone);
	}
}
