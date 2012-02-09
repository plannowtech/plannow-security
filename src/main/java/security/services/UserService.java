package security.services;

import java.util.Date;
import java.util.List;

import org.apache.tapestry5.hibernate.HibernateGridDataSource;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;

import security.entities.User;

public interface UserService
{
	boolean userExists(String username);

	boolean authenticate(String username, String password);

	boolean checkPassword(String username, String password);

	boolean emailExists(String email);

	boolean isEmailUnique(String email, User user);

	boolean isUsernameUnique(String username, User user);

	void changeEmail(User user, String newMail);

	/**
	 * This will change user session language . It is used when user is not authenticated but we
	 * want to translate his UI
	 * 
	 * @param language
	 */
	boolean isUserImpersonated();

	@CommitAfter
	boolean validateAccount(User user, String validationCode);

	@CommitAfter
	void changeIsValidEMail(User user, Boolean b);

	void logout();

	User getUser();

	User findUser(String username);

	User findUserByEmail(String email);

	String generatePassword();

	@CommitAfter
	User createNewUser(Long entityId, List<Long> roleIds, String username, String email,
			String phone, boolean active, boolean valid);

	@CommitAfter
	User createNewUser(String email, String password, Class<?> page);

	@CommitAfter
	User createNewUser(String email, Class<?> page);

	@CommitAfter
	User createEditUserPhone(Long entityId, Long roleId, String phone);

	@CommitAfter
	void editUser(User user, String password, Boolean valid);

	@CommitAfter
	void generateNewPassword(long userId);

	@CommitAfter
	void generateNewPassword(User user);

	@CommitAfter
	void editUser(long userId, String email, String phone, String username, String password,
			Boolean valid);

	@CommitAfter
	void deleteUser(Long userId);

	void impersonate(User user);

	Long getNumberOfSessionsForUser(User user);

	HibernateGridDataSource getUserSessionsGridDataSource(final Date dateFrom, final Date dateTo);

	User findUser(Long entityId);

	@CommitAfter
	void resetPassword(User user, String newPassword);
}
