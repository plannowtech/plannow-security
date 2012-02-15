package com.plannow.security.components;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;

import com.plannow.security.entities.Role;
import com.plannow.security.entities.User2;
import com.plannow.security.entities.UserRole;
import com.plannow.security.services.UserService;

public class AddRolesAndEditUser
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

	private List<Role> checkedListSelectedRolesValues;

	public void setCheckedListSelectedRolesValues(List<Role> checkedListSelectedRolesValues)
	{
		this.checkedListSelectedRolesValues = checkedListSelectedRolesValues;
	}

	public List<Role> getCheckedListSelectedRolesValues()
	{
		return userService.findRolesForUser(user);
	}

	public List<Role> getAllRoles()
	{
		return userService.findAllRoles();
	}

	public ValueEncoder<Role> getRolesEncoder()
	{
		return new ValueEncoder<Role>()
		{
			public Role toValue(String clientValue)
			{
				return userService.findRoleByName(clientValue);
			}

			public String toClient(Role value)
			{
				return value.getName();
			}
		};
	}

	@OnEvent(value = EventConstants.PREPARE, component = "rolesForm")
	void handleRolesFormPrepare(User2 user)
	{
		this.user = user;
	}

	@SuppressWarnings("unchecked")
	@OnEvent(value = EventConstants.SUCCESS, component = "rolesForm")
	void handleRolesFormSuccess()
	{
		List<Role> roles = userService.findRolesForUser(user);
		List<Role> deleteRoles = (List<Role>) CollectionUtils.subtract(roles,
				checkedListSelectedRolesValues);
		List<Role> addRoles = (List<Role>) CollectionUtils.subtract(checkedListSelectedRolesValues,
				roles);
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
