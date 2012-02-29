package com.plannow.security.services;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.tapestry5.corelib.components.PasswordField;
import org.apache.tapestry5.hibernate.HibernateSessionManager;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import com.plannow.security.entities.Permission;
import com.plannow.security.entities.Role;
import com.plannow.security.entities.User2;
import com.plannow.security.entities.UserRole;

public interface UserService
{
	/**
	 * Returns true and successfully logged in if there is an {@link User2 user} in database and his
	 * email and password match with parameters {@link String email}, {@link PasswordField password}
	 * and {@link User2 user} is active. Otherwise return false.
	 * 
	 * @param email
	 * @param password
	 * */
	boolean authenticate(String email, String password);

	/**
	 * Returns true if criteria return unique {@link User2 user} found by {@link String email}.
	 * Otherwise return false.
	 * 
	 * @param email
	 */
	boolean emailExists(String email);

	/**
	 * Log out logged {@link User2 user}.
	 */
	void logout();

	/**
	 * Returns logged {@link User2 user}.
	 */
	User2 getUser();

	/**
	 * Returns specific {@link User2 user} with param. {@link String email}.
	 * 
	 * @param email
	 */
	User2 findUserByEmail(String email);

	/**
	 * Returns specific {@link User2 user} with param. {@link String username}.
	 * 
	 * @param username
	 * @return
	 */
	User2 findUserByUsername(String email);

	/**
	 * Returns 8(by default) characters {@link String String}. The {@link String String} is
	 * generated random with another function.
	 */
	String generatePassword();

	/**
	 * Method create new {@link User2 user}. set attribute {@link String username},
	 * {@link PasswordField password} and {@link String email} and save the {@link User2 user} into
	 * database.
	 * 
	 * @param username
	 * @param password
	 * @param email
	 */
	@CommitAfter
	User2 createNewUser(String username, String password, String email);

	/**
	 * Returns a {@link List list} of {@link Role roles} for specific {@link User2 user} (
	 * {@link User2 User} in parameter).
	 * 
	 * @param user
	 */
	List<Role> findRolesForUser(User2 user);

	/**
	 * Returns a{@link List list} of all {@link Role roles} from Role {@link Role class}.
	 */
	List<Role> findAllRoles();

	/**
	 * Returns {@link Role role} with specific name ({@link String roleName}
	 * 
	 * @param roleName
	 */
	Role findRoleByName(String roleName);

	/**
	 * Save or update existing Object {@link Object obj}. Method is using
	 * {@link HibernateSessionManager hibernateSessionManager} for commit.
	 * 
	 * @param obj
	 */
	@CommitAfter
	void saveOrUpdate(Object obj);

	/**
	 * Delete existing Object {@link Object obj}. Method is using {@link HibernateSessionManager
	 * hibernateSessionManager} for commit.
	 * 
	 * @param obj
	 */
	@CommitAfter
	void delete(Object obj);

	/**
	 * Returns {@link UserRole userRole} with restrictions by {@link User2 user} and {@link Role
	 * role}.
	 * 
	 * @param user
	 * @param role
	 */
	UserRole findUserRoleByUserAndRole(User2 user, Role role);

	/**
	 * Method for security authenticate by {@link String email} and {@link PasswordField password}.
	 * Method is using when user try to login.
	 * 
	 * @param email
	 * @param password
	 */
	boolean tynamoAuthenticate(String username, String password) throws UnknownAccountException,
			IncorrectCredentialsException, LockedAccountException, AuthenticationException;

	/**
	 * Returns a {@link List list} of {@link Role roles} for specific {@link User2 user} which is
	 * found by {@link String email}.
	 * 
	 * @param email
	 */
	Set<String> findRolesForUser(String email);

	/**
	 * Includes permissions acquired via roles
	 * 
	 * @param email
	 * @return all permissions for the user
	 */
	Set<String> findAllPermissionsForUser(String email);

	/**
	 * Returns a {@link List list} of {@link String strings} which contains a sublist from
	 * findAllPermissions() for the {@link User2 user} which is found by {@link String email}.
	 * 
	 * @param email
	 */
	Collection<String> findNonRolePermissionsForUser(String email);

	/**
	 * Returns true if found {@link User2 user} with this {@link String email}.
	 * 
	 * @param email
	 * @param user
	 */
	boolean userExistWithThatMail(String email, User2 user);

	/**
	 * Returns a {@link Collection collection} of all {@link Permission permissions} with distinct
	 * projection by permission's {@link Permission name} property.
	 */
	Collection<String> findAllPermissions();

	/**
	 * Returns a {@link Permission permission} found by permission's {@link Permission name}.
	 * 
	 * @param name
	 */
	Permission findPermissionByName(String name);

	/**
	 * Return true if {@link User2 user}'s current password is matching with {@link PasswordField
	 * newPassword} (password in parameter).
	 * 
	 * @param email
	 * @param password
	 */
	boolean checkPassword(User2 user, String password);
}
