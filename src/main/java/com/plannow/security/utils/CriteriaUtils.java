package com.plannow.security.utils;

import static org.hibernate.criterion.Restrictions.ilike;
import static org.hibernate.criterion.Restrictions.or;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class CriteriaUtils
{
	private static final String DOT_REG_EXP = "\\.";

	/**
	 * Build alias for expressions like property1.property2.property3 = value. The join type is LEFT
	 * JOIN.
	 * 
	 * @param propertyName
	 *            The name of the property for applying equals to
	 * @param isFilterPropertyAlias
	 *            Is the first property in propertyName an alias
	 * @param propAlias
	 *            The alias that will be created for the entire association path for the property
	 * @param startCriteria
	 *            starting criteria on which to build on
	 * @return the final alias
	 */
	public static String ALIAS(String propertyName, boolean isFilterPropertyAlias,
			String propAlias, Criteria startCriteria)
	{
		String[] split = propertyName.split(DOT_REG_EXP);

		// create aliases one by one for each 'dot property' expression
		String assocPath = isFilterPropertyAlias ? split[0] + "." : "";

		for (int i = isFilterPropertyAlias ? 1 : 0; i < split.length - 1; i++)
		{
			final String prop = split[i];

			String alias = prop + "split";
			startCriteria.createAlias(assocPath + prop, alias, Criteria.LEFT_JOIN);

			assocPath = alias + ".";
		}

		assocPath += split[split.length - 1];

		if (propAlias != null)
			startCriteria.createAlias(assocPath, propAlias, Criteria.LEFT_JOIN);

		return assocPath;
	}

	public static String ALIAS_TRANSLATION(String propertyName, Criteria startCriteria)
	{
		return ALIAS_TRANSLATION(propertyName, false, startCriteria);
	}

	public static String ALIAS_TRANSLATION(String propertyName, boolean isFilterPropertyAlias,
			Criteria startCriteria)
	{
		// alias for translation
		String alias = propertyName + "translation";
		ALIAS(propertyName, isFilterPropertyAlias, alias, startCriteria);

		// alias for translation.translationValue
		final String tvAlias = "tv" + propertyName;
		startCriteria.createAlias(alias + ".translationValues", tvAlias, Criteria.LEFT_JOIN);

		return tvAlias;
	}

	public static Criteria EQUALS(String propertyName, boolean isFilterPropertyAlias,
			String propAlias, Object value, Criteria startCriteria)
	{
		String alias = ALIAS(propertyName, isFilterPropertyAlias, propAlias, startCriteria);

		/**
		 * Must make the alias before checking for null value because the alias may be used in
		 * another criteria expression
		 */
		if (value == null)
			return startCriteria;

		return startCriteria.add(Restrictions.eq(alias, value));
	}

	public static Criteria EQUALS(String propertyName, Object property, Criteria c)
	{
		return EQUALS(propertyName, false, null, property, c);
	}

	public static Criteria EQUALS(String propertyName, boolean isFilterPropertyAlias,
			Object property, Criteria c)
	{
		return EQUALS(propertyName, isFilterPropertyAlias, null, property, c);
	}

	public static Criteria GE(String propertyName, boolean isFilterPropertyAlias, String propAlias,
			Object value, Criteria startCriteria)
	{
		String alias = ALIAS(propertyName, isFilterPropertyAlias, propAlias, startCriteria);

		/**
		 * Must make the alias before checking for null value because the alias may be used in
		 * another criteria expression
		 */
		if (value == null)
			return startCriteria;

		return startCriteria.add(Restrictions.ge(alias, value));
	}

	public static Criteria GE(String propertyName, Object property, Criteria c)
	{
		return GE(propertyName, false, null, property, c);
	}

	public static Criteria GE(String propertyName, boolean isFilterPropertyAlias, Object property,
			Criteria c)
	{
		return GE(propertyName, isFilterPropertyAlias, null, property, c);
	}

	public static Criteria LE(String propertyName, boolean isFilterPropertyAlias, String propAlias,
			Object value, Criteria startCriteria)
	{
		String alias = ALIAS(propertyName, isFilterPropertyAlias, propAlias, startCriteria);

		/**
		 * Must make the alias before checking for null value because the alias may be used in
		 * another criteria expression
		 */
		if (value == null)
			return startCriteria;

		return startCriteria.add(Restrictions.le(alias, value));
	}

	public static Criteria LE(String propertyName, Object property, Criteria c)
	{
		return GE(propertyName, false, null, property, c);
	}

	public static Criteria LE(String propertyName, boolean isFilterPropertyAlias, Object property,
			Criteria c)
	{
		return GE(propertyName, isFilterPropertyAlias, null, property, c);
	}

	public static Criteria OR_LIKE(List<String> propertyNames, Object property, Criteria c)
	{
		if (propertyNames == null)
			return c;

		if (propertyNames.size() == 1)
			return EQUALS(propertyNames.get(0), property, c);

		Criterion orExpr = null;
		Map<String, String> firstPropertyNameToAlias = new LinkedHashMap<String, String>();
		for (String propertyName : propertyNames)
		{
			String[] propertyNameComponents = propertyName.split(DOT_REG_EXP);
			if (propertyNameComponents.length > 1)
			{
				String firstProperty = propertyNameComponents[0];
				if (firstPropertyNameToAlias.containsKey(firstProperty))
				{
					// check this in order to make the alias for the second propertyName with the
					// same firstProperty and
					// not for the third, fourth etc.
					if (firstPropertyNameToAlias.get(firstProperty) == null)
					{
						String alias = firstProperty + "_alias";
						ALIAS(firstProperty, false, alias, c);
						firstPropertyNameToAlias.put(firstProperty, alias);
					}
				}
				else
					firstPropertyNameToAlias.put(firstProperty, null);
			}
		}

		for (String propertyName : propertyNames)
		{
			String[] propertyNameComponents = propertyName.split(DOT_REG_EXP);
			String alias;
			if (propertyNameComponents.length > 1)
			{
				String firstProperty = propertyNameComponents[0];
				String firstPropertyAlias = firstPropertyNameToAlias.get(firstProperty);
				propertyName = propertyName.replaceFirst(firstProperty, firstPropertyAlias);
				alias = ALIAS(propertyName, true, null, c);
			}
			else
				alias = ALIAS(propertyName, false, null, c);

			// initialize
			if (orExpr == null)
				orExpr = ilike(alias, "%" + property + "%");
			else
				// results in (or(or(Expr1,Expr2),Expr3)),..)
				orExpr = or(ilike(alias, "%" + property + "%"), orExpr);
		}

		c.add(orExpr);
		return c;
	}

	public static Criteria EQUALS_TRANSLATION(String propertyName, Object value,
			Criteria startCriteria)
	{
		if (value == null)
			return startCriteria;

		String tvAlias = ALIAS_TRANSLATION(propertyName, startCriteria);

		return startCriteria.add(Restrictions.eq(tvAlias + ".value", value)).add(
				Restrictions.eq(tvAlias + ".language.id", 1l));
	}

	public static Criteria copy(Criteria criteria)
	{
		try
		{
			ByteArrayOutputStream baostream = new ByteArrayOutputStream();
			ObjectOutputStream oostream = new ObjectOutputStream(baostream);
			oostream.writeObject(criteria);
			oostream.flush();
			oostream.close();
			ByteArrayInputStream baistream = new ByteArrayInputStream(baostream.toByteArray());
			ObjectInputStream oistream = new ObjectInputStream(baistream);
			Criteria copy = (Criteria) oistream.readObject();
			oistream.close();
			return copy;
		}
		catch (Throwable t)
		{
			throw new HibernateException(t);
		}
	}
}
