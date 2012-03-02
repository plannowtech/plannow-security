package com.plannow.security.components;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ValueEncoderSource;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.util.AbstractSelectModel;

import com.plannow.security.entities.Role;
import com.plannow.security.entities.User2;
import com.plannow.security.entities.UserRole;
import com.plannow.security.services.UserService;
import com.plannow.security.utils.CollectionFactory;

public class RolesForUser
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

	@Inject
	private ValueEncoderSource valueEncoderSource;

	private List<Role> checkedListSelectedRolesValues;

	public void setCheckedListSelectedRolesValues(List<Role> checkedListSelectedRolesValues)
	{
		this.checkedListSelectedRolesValues = checkedListSelectedRolesValues;
	}

	public List<Role> getCheckedListSelectedRolesValues()
	{
		return userService.findRolesForUser(user);
	}

	public SelectModel getAllRoles()
	{ 
		return new AbstractSelectModel()
		{
			@Override
			public List<OptionModel> getOptions()
			{
				List<OptionModel> options = CollectionFactory.newList();
				List<Role> roles = userService.findAllRoles();
				for (Role r : roles)
				{
					options.add(new OptionModelImpl(r.getName(), r));
				}
				return options;
			}

			@Override
			public List<OptionGroupModel> getOptionGroups()
			{
				return null;
			}
		};
	}

	public ValueEncoder<Role> getRolesEncoder()
	{
		return valueEncoderSource.getValueEncoder(Role.class);
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
