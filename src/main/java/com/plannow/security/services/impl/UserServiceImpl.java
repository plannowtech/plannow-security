package com.plannow.security.services.impl;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Sha1Hash;
import org.apache.shiro.subject.Subject;
import org.apache.tapestry5.hibernate.HibernateSessionManager;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.tynamo.security.services.SecurityService;

import com.plannow.security.entities.BaseEntityImpl;
import com.plannow.security.entities.Permission;
import com.plannow.security.entities.Role;
import com.plannow.security.entities.RolePermission;
import com.plannow.security.entities.User2;
import com.plannow.security.entities.UserPermission;
import com.plannow.security.entities.UserRole;
import com.plannow.security.model.sessionstate.UserInfo;
import com.plannow.security.services.UserService;
import com.plannow.security.utils.CollectionFactory;

public class UserServiceImpl implements UserService
{
	@Inject
	private ApplicationStateManager asm;

	@Inject
	private Session session;

	@Inject
	private HibernateSessionManager hibernateSessionManager;

	@Inject
	private SecurityService securityService;

	@SuppressWarnings("unused")
	private static final int ITERATION_NUMBER = 1000;
	private static final int GENERATED_PASSWORD_LENGTH = 8;
	private static final int GENERATED_SALT_LENGTH = 8;

	public boolean authenticate(String email, String password)
	{
		if (email == null || email.trim().length() == 0)
			return false;

		User2 user = (User2) session.createCriteria(User2.class)
				.add(Restrictions.eq("email", email)).uniqueResult();

		if (user == null)
			return false;

		if (!user.isActive())
			return false;

		String passwordHash = user.getPasswordHash();

		String newhash = new Sha1Hash(password, user.getPasswordSalt()).toString();

		if (!newhash.equals(passwordHash))
		{
			logout();

			return false;
		}
		login(user);

		return true;
	}

	void login(User2 user)
	{
		asm.set(UserInfo.class, new UserInfo(user.getId()));
	}

	public boolean emailExists(String email)
	{
		return session.createCriteria(User2.class).add(Restrictions.eq("email", email))
				.uniqueResult() != null;
	}

	public void logout()
	{
		asm.set(UserInfo.class, null);
	}

	public User2 getUser()
	{
		UserInfo userInfo = asm.getIfExists(UserInfo.class);
		if (userInfo == null)
			return null;

		return (User2) session.createCriteria(User2.class)
				.add(Restrictions.eq("id", userInfo.getUserId())).uniqueResult();
	}

	public User2 findUserByEmail(String email)
	{
		return (User2) session.createCriteria(User2.class).add(Restrictions.eq("email", email))
				.uniqueResult();
	}

	@Override
	public User2 findUserByUsername(String email)
	{

		System.err.println(session.createCriteria(User2.class).list());
		return (User2) session.createCriteria(User2.class).add(Restrictions.eq("email", email))
				.uniqueResult();
	}

	@Override
	public String generatePassword()
	{
		return randomString(GENERATED_PASSWORD_LENGTH);
	}

	@Override
	public boolean checkPassword(User2 user, String password)
	{
		String passwordHash = user.getPasswordHash();

		String newhash = new Sha1Hash(password, user.getPasswordSalt()).toString();

		return newhash.equals(passwordHash);
	}

	/**
	 * Return random generated {@link String String}, 8 characters length by default.
	 * 
	 * @param length
	 */
	private String randomString(int length)
	{
		char[] chars =
		{ 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
				's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
				'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
				'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

		String randomStr = "";
		for (int idx = 0; idx < length; idx++)
			randomStr += chars[(int) (Math.random() * chars.length)];

		return randomStr;
	}

	@Override
	public User2 createNewUser(String username, String password, String email)
	{
		User2 user = new User2();
		user.setUsername(username);
		user.setPassword(password);
		user.setEmail(email);
		String validationCode = randomString(GENERATED_PASSWORD_LENGTH);
		user.setValidationCode(validationCode);
		user.setActive(true);
		session.save(user);
		return user;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Role> findRolesForUser(User2 user)
	{
		List<UserRole> userRolesFromUserInParameter = session.createCriteria(UserRole.class)
				.add(Restrictions.eq("user", user)).list();
		List<Role> checkedListSelectedRolesValues = CollectionFactory.newList();
		for (UserRole userRole : userRolesFromUserInParameter)
		{
			if (!checkedListSelectedRolesValues.contains(userRole.getRole()))
				checkedListSelectedRolesValues.add(userRole.getRole());
		}
		return checkedListSelectedRolesValues;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Role> findAllRoles()
	{
		return session.createCriteria(Role.class).list();
	}

	public Role findRoleByName(String roleName)
	{
		return (Role) session.createCriteria(Role.class).add(Restrictions.eq("name", roleName))
				.uniqueResult();
	}

	@Override
	public void saveOrUpdate(Object obj)
	{
		session.saveOrUpdate(obj);
		hibernateSessionManager.commit();
	}

	@Override
	public void delete(Object obj)
	{
		session.delete(obj);
		hibernateSessionManager.commit();
	}

	public UserRole findUserRoleByUserAndRole(User2 user, Role role)
	{
		return (UserRole) session.createCriteria(UserRole.class).add(Restrictions.eq("user", user))
				.add(Restrictions.eq("role", role)).uniqueResult();
	}

	@Override
	public boolean tynamoAuthenticate(String email, String password)
			throws UnknownAccountException, IncorrectCredentialsException, LockedAccountException,
			AuthenticationException
	{
		Subject currentUser = securityService.getSubject();
		UsernamePasswordToken token = new UsernamePasswordToken(email, password);
		try
		{
			currentUser.login(token);
		}
		catch (UnknownAccountException e)
		{
			throw e;
		}
		catch (IncorrectCredentialsException e)
		{
			throw e;
		}
		catch (LockedAccountException e)
		{
			throw e;
		}
		catch (AuthenticationException e)
		{
			throw e;
		}

		return authenticate(email, password);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<String> findRolesForUser(String email)
	{
		Set<String> rolesForUser = CollectionFactory.newSet();
		rolesForUser.addAll(session.createCriteria(UserRole.class).createAlias("user", "user")
				.add(Restrictions.eq("user.email", email)).createAlias("role", "role")
				.setProjection(Projections.distinct(Projections.property("role.name"))).list());
		return rolesForUser;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<String> findAllPermissionsForUser(String email)
	{
		Set<String> rolesForUser = findRolesForUser(email);

		Set<String> permissionsForUser = CollectionFactory.newSet();

		Criteria crit = session.createCriteria(RolePermission.class).createAlias("role", "r")
				.add(Restrictions.in("r.name", rolesForUser)).createAlias("permission", "p")
				.setProjection(Projections.distinct(Projections.property("p.name")));

		permissionsForUser.addAll(crit.list());

		Criteria crit2 = session.createCriteria(UserPermission.class).createAlias("user", "u")
				.add(Restrictions.eq("u.email", email)).createAlias("permission", "p")
				.setProjection(Projections.distinct(Projections.property("p.name")));

		permissionsForUser.addAll(crit2.list());
		return permissionsForUser;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<String> findNonRolePermissionsForUser(String username)
	{
		Set<String> rolesForUser = findRolesForUser(username);
		Set<String> permissionsForUser = CollectionFactory.newSet();

		Criteria crit = session.createCriteria(RolePermission.class).createAlias("role", "r")
				.add(Restrictions.in("r.name", rolesForUser)).createAlias("permission", "p")
				.setProjection(Projections.distinct(Projections.property("p.name")));

		permissionsForUser.addAll(crit.list());
		return CollectionUtils.subtract(findAllPermissionsToString(), permissionsForUser);
	}

	@Override
	public boolean userExistWithThatMail(String email, User2 user)
	{
		return (Long) session.createCriteria(User2.class).add(Restrictions.eq("email", email))
				.add(Restrictions.ne("id", user.getId())).setProjection(Projections.rowCount())
				.uniqueResult() > 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<String> findAllPermissionsToString()
	{
		return session.createCriteria(Permission.class)
				.setProjection(Projections.distinct(Projections.property("name"))).list();
	}

	@Override
	public Permission findPermissionByName(String name)
	{

		return (Permission) session.createCriteria(Permission.class)
				.add(Restrictions.eq("name", name)).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RolePermission> findRolePermissionForSelectedRole(Role selectedRole)
	{
		return session.createCriteria(RolePermission.class)
				.add(Restrictions.eq("role", selectedRole)).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Permission> findAllPermissions()
	{
		return session.createCriteria(Permission.class).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Permission> findNoPermissionsForSelectedRole(Role selectedRole)
	{
		List<Permission> permissionsForSelectedRole = CollectionFactory.newList();
		List<RolePermission> rolePermissionForSelectedRole = findRolePermissionForSelectedRole(selectedRole);
		for (RolePermission rolePermission : rolePermissionForSelectedRole)
		{
			permissionsForSelectedRole.add(rolePermission.getPermission());
		}

		return (List<Permission>) CollectionUtils.subtract(findAllPermissions(),
				permissionsForSelectedRole);
	}

	@Override
	public RolePermission findSpecificRolePermissionForSelectedRole(Role selectedRole,
			Permission permission)
	{
		return (RolePermission) session.createCriteria(RolePermission.class)
				.add(Restrictions.eq("role", selectedRole))
				.add(Restrictions.eq("permission", permission)).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserRole> findUserRoleByRole(Role role)
	{
		return session.createCriteria(UserRole.class).add(Restrictions.eq("role", role)).list();
	}

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	private UserInfo getUserInfo()
	{
		// in case this method is called before the servlet container has built the session
		if (asm == null)
			return null;

		// TODO Bojan
		// there is exception at start of app when saveSetting has been called
		try
		{
			if (!asm.exists(UserInfo.class))
				return null;
		}
		catch (Exception e)
		{
			return null;
		}

		return asm.get(UserInfo.class);
	}

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	private String generateSalt()
	{
		return randomString(GENERATED_SALT_LENGTH);
	}

	public static String byteToBase64(byte[] data)
	{
		return new String(new Base64().encode(data));
	}

	public static byte[] base64ToByte(String data)
	{
		return new Base64().decode(data.getBytes());
	}
}
