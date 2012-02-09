package security.auth;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.crypto.hash.Sha1Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.SimpleByteSource;

import security.entities.User;
import security.services.UserService;

public class UserAuthorizingRealm extends AuthorizingRealm
{
	final UserService userService;

	public UserAuthorizingRealm(UserService userService)
	{
		super(new MemoryConstrainedCacheManager());
		this.userService = userService;
		setName("users_realm");
		setAuthenticationTokenClass(UsernamePasswordToken.class);
		setCredentialsMatcher(new HashedCredentialsMatcher(Sha1Hash.ALGORITHM_NAME));
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals)
	{
		if (principals == null)
			throw new AuthorizationException(
					"PrincipalCollection was null, which should not happen");

		if (principals.isEmpty())
			return null;

		if (principals.fromRealm(getName()).size() <= 0)
			return null;

		String username = (String) principals.fromRealm(getName()).iterator().next();
		if (username == null)
			return null;

		if (!userService.userExists(username))
			return null;

		// Collection<HealthQueueRole> healthQueueRoles = userService.findRolesForUser(username);
		// Set<String> roles = new HashSet<String>();
		// for (HealthQueueRole healthQueueRole : healthQueueRoles)
		// roles.add(healthQueueRole.name());

		return new SimpleAuthorizationInfo(null);
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
			throws AuthenticationException
	{
		UsernamePasswordToken upToken = (UsernamePasswordToken) token;
		String username = upToken.getUsername();

		// Null username is invalid
		if (username == null)
			throw new AccountException("Null usernames are not allowed by this realm.");

		// User user = userService.findByUsername(username);
		// if (user == null)
		// return null;

		// return new SimpleAuthenticationInfo(username, user.getEncodedPassword(),
		// new SimpleByteSource(user.getPasswordSalt()), getName());
		return null;
	}
}
