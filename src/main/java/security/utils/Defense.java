package security.utils;

public final class Defense
{
	private Defense()
	{
	}

	/**
	 * Checks that a method parameter value is not null, and returns it.
	 * 
	 * @param <T>
	 *            the value type
	 * @param value
	 *            the value (which is checked to ensure non-nullness)
	 * @param parameterName
	 *            the name of the parameter, used for exception messages
	 * @return the value
	 * @throws IllegalArgumentException
	 *             if the value is null
	 */
	public static <T> T notNull(T value, String parameterName)
	{
		if (value == null)
			throw new IllegalArgumentException(parameterName + " must not be null");

		return value;
	}
}
