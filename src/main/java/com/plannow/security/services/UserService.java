package com.plannow.security.services;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.tapestry5.hibernate.HibernateGridDataSource;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import com.plannow.security.entities.Role;
import com.plannow.security.entities.User2;
import com.plannow.security.entities.UserRole;

public interface UserService
{
	boolean userExists(String email);

	boolean authenticate(String username, String password);

	boolean checkPassword(String username, String password);

	boolean emailExists(String email);

	boolean isEmailUnique(String email, User2 user);

	boolean isUsernameUnique(String username, User2 user);

	void changeEmail(User2 user, String newMail);

	/**
	 * This will change user session language . It is used when user is not authenticated but we
	 * want to translate his UI
	 * 
	 * @param language
	 */
	boolean isUserImpersonated();

	@CommitAfter
	boolean validateAccount(User2 user, String validationCode);

	@CommitAfter
	void changeIsValidEMail(User2 user, Boolean b);

	void logout();

	User2 getUser();

	User2 findUser(String username);

	User2 findUserByEmail(String email);

	User2 findUserByUsername(String username);
	
	String generatePassword();

	@CommitAfter
	User2 createNewUser(Long entityId, List<Long> roleIds, String username, String email,
			String phone, boolean active, boolean valid);

	@CommitAfter
	User2 createNewUser(String email, String password, Class<?> page);

	@CommitAfter
	User2 createNewUser(String email, Class<?> page);

	@CommitAfter
	User2 createEditUserPhone(Long entityId, Long roleId, String phone);

	@CommitAfter
	User2 createNewUser(String username, String password, String email);

	@CommitAfter
	void editUser(User2 user, String password, Boolean valid);

	@CommitAfter
	void generateNewPassword(long userId);

	@CommitAfter
	void generateNewPassword(User2 user);

	@CommitAfter
	void editUser(long userId, String email, String phone, String username, String password,
			Boolean valid);

	@CommitAfter
	void deleteUser(Long userId);

	void impersonate(User2 user);

	Long getNumberOfSessionsForUser(User2 user);

	HibernateGridDataSource getUserSessionsGridDataSource(final Date dateFrom, final Date dateTo);

	User2 findUser(Long entityId);

	@CommitAfter
	void resetPassword(User2 user, String newPassword);

	List<Role> findRolesForUser(User2 user);

	List<Role> findAllRoles();

	Role findRoleByName(String roleName);

	@CommitAfter
	void saveOrUpdate(Object obj);

	@CommitAfter
	void delete(Object obj);

	UserRole findUserRoleByUserAndRole(User2 user, Role role);

	boolean tynamoAuthenticate(String username, String password) throws UnknownAccountException,
			IncorrectCredentialsException, LockedAccountException, AuthenticationException;
	
	Set<String> findRolesForUser(String username);

	boolean userExistWithThatMail(String email, User2 user);
}
