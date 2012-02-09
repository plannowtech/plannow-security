package security.services.impl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.tapestry5.hibernate.HibernateGridDataSource;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.hibernate.criterion.Restrictions;

import security.entities.User;
import security.model.sessionstate.UserInfo;
import security.services.GenericDAOService;
import security.services.MailService;
import security.services.UserService;
import security.utils.Defense;

public class UserServiceImpl extends GenericServiceImpl<User, Long> implements UserService
{
	@Inject
	private ApplicationStateManager asm;

	@Inject
	private MailService mailService;

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

	public boolean userExists(String username)
	{
		return getSession().createCriteria(User.class).add(Restrictions.eq("username", username))
				.uniqueResult() != null;
	}

	@Inject
	private GenericDAOService genericDAOService;

	public boolean authenticate(String username, String password)
	{
		if (username == null || username.trim().length() == 0)
			return false;

		User user = genericDAOService.find(User.class, "email", username);

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
				/*
				 * If a user which is currently logged in fails to authenticate then log him out !
				 */
				logout();

				return false;
			}

			login(user);

			return true;
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return false;
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	void login(User user)
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
		User user = genericDAOService.find(User.class, "email", username);

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
		return getSession().createCriteria(User.class).add(Restrictions.eq("email", email))
				.uniqueResult() != null;
	}

	public boolean isEmailUnique(String email, User user)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isUsernameUnique(String username, User user)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void changeEmail(User user, String newMail)
	{
		// TODO Auto-generated method stub

	}

	public boolean isUserImpersonated()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean validateAccount(User user, String validationCode)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void changeIsValidEMail(User user, Boolean b)
	{
		// TODO Auto-generated method stub

	}

	public void logout()
	{
		asm.set(UserInfo.class, null);
	}

	public User getUser()
	{
		UserInfo userInfo = asm.getIfExists(UserInfo.class);
		if (userInfo == null)
			return null;

		return (User) genericDAOService.findById(User.class, userInfo.getUserId());
	}

	public User findUser(String username)
	{
		return find(User.class, "username", username);
	}

	public User findUserByEmail(String email)
	{
		return find(User.class, "email", email);
	}

	public User createNewUser(String email, String password, Class<?> page)
	{
		Defense.notNull(email, "email");

		if (emailExists(email))
			return null;

		User user = new User();
		user.seteMail(email);

		user.setPasswordSalt(generateSalt().getBytes());

		// save it's hash value
		user.setPasswordHash(getPasswordHash(String.valueOf(user.getPasswordSalt()), password));

		save(user);
		return user;
	}

	public User createNewUser(String email, Class<?> page)
	{
		Defense.notNull(email, "email");

		if (emailExists(email))
			return null;

		User user = new User();
		user.seteMail(email);

		user.setPasswordSalt(generateSalt().getBytes());

		// save it's hash value
		String password = generatePassword();
		user.setPasswordHash(getPasswordHash(String.valueOf(user.getPasswordSalt()), password));

		return user;
	}

	public User createEditUserPhone(Long entityId, Long roleId, String phone)
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

	public void editUser(User user, String password, Boolean valid)
	{
		Defense.notNull(user, "user");
		Defense.notNull(password, "password");

		user.setPasswordSalt(generateSalt().getBytes());
		user.setPasswordHash(getPasswordHash(String.valueOf(user.getPasswordSalt()), password));

		if (valid != null)
			user.setActive(valid);

		save(user);
	}

	public void deleteUser(Long userId)
	{
		// TODO Auto-generated method stub

	}

	public void impersonate(User user)
	{
		// TODO Auto-generated method stub

	}

	public Long getNumberOfSessionsForUser(User user)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public HibernateGridDataSource getUserSessionsGridDataSource(Date dateFrom, Date dateTo)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public User findUser(Long entityId)
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
		for (int idx = 0; idx < GENERATED_PASSWORD_LENGTH; idx++)
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
	public User createNewUser(Long entityId, List<Long> roleIds, String username, String email,
			String phone, boolean active, boolean valid)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void generateNewPassword(User user)
	{
		// generate password,
		String password = generatePassword();
		// save it's hash value
		user.setPasswordHash(getPasswordHash(String.valueOf(user.getPasswordSalt()), password));

		save(user); 

	}

	@Override
	public void resetPassword(User user, String newPassword)
	{
		Defense.notNull(user, "user");

		Defense.notNull(newPassword, "new password");

		// save it's hash value
		user.setPasswordHash(getPasswordHash(String.valueOf(user.getPasswordSalt()), newPassword));

		save(user);
	}
}
