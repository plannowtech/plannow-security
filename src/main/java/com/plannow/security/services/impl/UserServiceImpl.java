package com.plannow.security.services.impl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.FlushModeType;

import org.apache.commons.codec.binary.Base64;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Sha1Hash;
import org.apache.shiro.subject.Subject;
import org.apache.tapestry5.hibernate.HibernateGridDataSource;
import org.apache.tapestry5.hibernate.HibernateSessionManager;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.tynamo.security.services.SecurityService;

import com.plannow.security.entities.BaseEntityImpl;
import com.plannow.security.entities.Role;
import com.plannow.security.entities.User2;
import com.plannow.security.entities.UserRole;
import com.plannow.security.model.sessionstate.UserInfo;
import com.plannow.security.services.MailService;
import com.plannow.security.services.UserService;
import com.plannow.security.utils.CollectionFactory;
import com.plannow.security.utils.Defense;

public class UserServiceImpl implements UserService
{
	@Inject
	private ApplicationStateManager asm;

	@Inject
	private MailService mailService;

	@Inject
	private Session session;

	private static final int ITERATION_NUMBER = 1000;
	private static final int GENERATED_PASSWORD_LENGTH = 8;
	private static final int GENERATED_SALT_LENGTH = 8;

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

	@Override
	public boolean userExists(String email)
	{
		return session.createCriteria(User2.class).add(Restrictions.eq("email", email))
				.uniqueResult() != null;
	}

	@Override
	public boolean userExistWithThatMail(String email, User2 user)
	{
		return (Long) session.createCriteria(User2.class).add(Restrictions.eq("email", email))
				.add(Restrictions.ne("id", user.getId())).setProjection(Projections.rowCount())
				.uniqueResult() > 0;
	}

	public boolean authenticate(String username, String password)
	{
		if (username == null || username.trim().length() == 0)
			return false;

		User2 user = (User2) session.createCriteria(User2.class)
				.add(Restrictions.eq("email", username)).uniqueResult();

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

	@Inject
	private SecurityService securityService;

	@Override
	public boolean tynamoAuthenticate(String username, String password)
			throws UnknownAccountException, IncorrectCredentialsException, LockedAccountException,
			AuthenticationException
	{
		Subject currentUser = securityService.getSubject();
		UsernamePasswordToken token = new UsernamePasswordToken(username, password);
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

		return authenticate(username, password);
	}

	void login(User2 user)
	{
		asm.set(UserInfo.class, new UserInfo(user.getId()));
	}

	/**
	 * From a password, a number of iterations and a salt, returns the corresponding digest
	 * 
	 * @param iterationNb
	 *            The number of iterations of the algorithm
	 * @param password
	 *            The password to encrypt
	 * @param salt
	 *            The salt
	 * @return The digested password
	 * @throws NoSuchAlgorithmException
	 *             If the algorithm doesn't exist
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] getHash(int iterationNb, String password, byte[] salt)
			throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		MessageDigest digest = MessageDigest.getInstance("SHA-1");
		digest.reset();
		digest.update(salt);
		byte[] input = digest.digest(password.getBytes("UTF-8"));
		for (int i = 0; i < iterationNb; i++)
		{
			digest.reset();
			input = digest.digest(input);
		}

		return input;
	}

	public boolean checkPassword(String username, String password)
	{
		User2 user = (User2) session.createCriteria(User2.class)
				.add(Restrictions.eq("email", username)).uniqueResult();

		if (user == null)
			return false;

		if (!user.isActive())
			return false;

		String passwordHash = user.getPasswordHash();
		String salt = String.valueOf(user.getPasswordSalt());

		try
		{ // Use Base 64 encoding
			byte[] bDigest = base64ToByte(passwordHash);
			byte[] bSalt = base64ToByte(salt);

			// Compute the new DIGEST

			byte[] proposedDigest = getHash(ITERATION_NUMBER, password, bSalt);

			if (!Arrays.equals(proposedDigest, bDigest))
			{
				return false;
			}

			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public boolean emailExists(String email)
	{
		return session.createCriteria(User2.class).add(Restrictions.eq("email", email))
				.uniqueResult() != null;
	}

	public boolean isEmailUnique(String email, User2 user)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isUsernameUnique(String username, User2 user)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void changeEmail(User2 user, String newMail)
	{
		// TODO Auto-generated method stub

	}

	public boolean isUserImpersonated()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean validateAccount(User2 user, String validationCode)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void changeIsValidEMail(User2 user, Boolean b)
	{
		// TODO Auto-generated method stub

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

	public User2 findUser(String username)
	{
		return (User2) session.createCriteria(User2.class)
				.add(Restrictions.eq("username", username)).uniqueResult();
	}

	public User2 findUserByEmail(String email)
	{
		return (User2) session.createCriteria(User2.class).add(Restrictions.eq("email", email))
				.uniqueResult();
	}

	public User2 createNewUser(String email, String password, Class<?> page)
	{
		Defense.notNull(email, "email");

		if (emailExists(email))
			return null;

		User2 user = new User2();
		user.setEmail(email);

		user.setPasswordSalt(generateSalt().getBytes());

		// save it's hash value
		user.setPassword(password);

		session.save(user);
		return user;
	}

	public User2 createNewUser(String email, Class<?> page)
	{
		Defense.notNull(email, "email");

		if (emailExists(email))
			return null;

		User2 user = new User2();
		user.setEmail(email);

		user.setPasswordSalt(generateSalt().getBytes());

		// save it's hash value
		String password = generatePassword();
		user.setPassword(password);

		return user;
	}

	public User2 createEditUserPhone(Long entityId, Long roleId, String phone)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void generateNewPassword(long userId)
	{
		// TODO Auto-generated method stub

	}

	public void editUser(long userId, String email, String phone, String username, String password,
			Boolean valid)
	{
		// TODO Auto-generated method stub

	}

	public void editUser(User2 user, String password, Boolean valid)
	{
		Defense.notNull(user, "user");
		Defense.notNull(password, "password");

		user.setPasswordSalt(generateSalt().getBytes());
		user.setPassword(password);

		if (valid != null)
			user.setActive(valid);

		session.save(user);
	}

	public void deleteUser(Long userId)
	{
		// TODO Auto-generated method stub

	}

	public void impersonate(User2 user)
	{
		// TODO Auto-generated method stub

	}

	public Long getNumberOfSessionsForUser(User2 user)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public HibernateGridDataSource getUserSessionsGridDataSource(Date dateFrom, Date dateTo)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public User2 findUser(Long entityId)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String generatePassword()
	{
		return randomString(GENERATED_PASSWORD_LENGTH);
	}

	private String generateSalt()
	{
		return randomString(GENERATED_SALT_LENGTH);
	}

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

	private static String getPasswordHash(String salt, String password)
	{
		try
		{
			byte[] hashedPassword = getHash(ITERATION_NUMBER, password, base64ToByte(salt));

			return byteToBase64(hashedPassword);

		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public static String byteToBase64(byte[] data)
	{
		return new String(new Base64().encode(data));
	}

	public static byte[] base64ToByte(String data)
	{
		return new Base64().decode(data.getBytes());
	}

	@Override
	public User2 createNewUser(Long entityId, List<Long> roleIds, String username, String email,
			String phone, boolean active, boolean valid)
	{
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public void generateNewPassword(User2 user)
	{
		// generate password,
		String password = generatePassword();
		// save it's hash value
		user.setPassword(password);

		session.save(user);

	}

	@Override
	public void resetPassword(User2 user, String newPassword)
	{
		Defense.notNull(user, "user");

		Defense.notNull(newPassword, "new password");

		// save it's hash value
		user.setPassword(newPassword);

		session.save(user);
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

	@Inject
	private HibernateSessionManager hibernateSessionManager;

	public UserRole findUserRoleByUserAndRole(User2 user, Role role)
	{
		return (UserRole) session.createCriteria(UserRole.class).add(Restrictions.eq("user", user))
				.add(Restrictions.eq("role", role)).uniqueResult();
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

	@Override
	public User2 findUserByUsername(String username)
	{
		return (User2) session.createCriteria(User2.class).add(Restrictions.eq("email", username))
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<String> findRolesForUser(String username)
	{
		Set<String> rolesForUser = CollectionFactory.newSet();
		rolesForUser.addAll(session.createCriteria(UserRole.class).createAlias("user", "user")
				.add(Restrictions.eq("user.email", username)).createAlias("role", "role")
				.setProjection(Projections.distinct(Projections.property("role.name"))).list());
		return rolesForUser;
	}

}
