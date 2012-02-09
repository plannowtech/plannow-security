package security.services.impl;

import static security.utils.CriteriaUtils.ALIAS;
import static security.utils.CriteriaUtils.ALIAS_TRANSLATION;
import static security.utils.CriteriaUtils.EQUALS;
import static org.hibernate.criterion.Restrictions.and;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.ilike;
import static org.hibernate.criterion.Restrictions.or;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import security.entities.base.OrderedEntity;
import security.entities.base.impl.BaseOrderedRelation;
import security.model.orm.ORMProperty;
import security.services.GenericDAOService;
import security.utils.CollectionFactory;

public class GenericDAOServiceImpl implements GenericDAOService
{
	private final TypeCoercer typeCoercer;
	private final Session session;

	private static final String DOT_REG_EXP = "\\.";

	public GenericDAOServiceImpl(Session session, TypeCoercer typeCoercer)
	{
		super();
		this.typeCoercer = typeCoercer;
		this.session = session;
	}

	public Criteria crit(Class<?> entityClass)
	{
		return session.createCriteria(entityClass);
	}

	protected <T> Criteria findByCriteria(Class<T> entityClass, Criterion... criterion)
	{
		Criteria crit = crit(entityClass);
		for (Criterion c : criterion)
		{
			crit.add(c);
		}
		return crit;
	}

	protected <V> Criteria findByCriteria(Class<V> entityClass, String property, Object value,
			List<Order> ordering)
	{
		Criteria crit = EQUALS(property, value, crit(entityClass));

		for (Order o : ordering)
		{
			crit.addOrder(o);
		}

		return crit;
	}

	protected <V> Criteria findByCriteria(Class<V> entityClass, List<Order> ordering)
	{
		Criteria crit = crit(entityClass);

		for (Order o : ordering)
		{
			crit.addOrder(o);
		}

		return crit;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T findById(Class<T> entityClass, Serializable id)
	{
		return (T) session.load(entityClass, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T findById(String entityClass, Serializable id)
	{
		return (T) session.load(entityClass, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T find(Class<T> entityClass, String property, Object value)
	{
		return (T) EQUALS(property, value, crit(entityClass)).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T find(Class<T> entityClass, String p1, Object v1, String p2, Object v2)
	{
		return (T) EQUALS(p2, v2, EQUALS(p1, v1, crit(entityClass))).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T find(Class<T> entityClass, String p1, Object v1, String p2, Object v2, String p3,
			Object v3)
	{
		return (T) EQUALS(p3, v3, EQUALS(p2, v2, EQUALS(p1, v1, crit(entityClass)))).uniqueResult();
	}

	@Override
	public <T> Criteria findAllCrit(Class<T> entityClass)
	{
		return crit(entityClass);
	}

	public <T> Criteria findAllCrit(Criteria criteria, String property, Object value)
	{
		return EQUALS(property, value, criteria);
	}

	@Override
	public <T> Criteria findAllCrit(Class<T> entityClass, String property, Object value)
	{
		return EQUALS(property, value, crit(entityClass));
	}

	@Override
	public <T> Criteria findAllCrit(Class<T> entityClass, String property, Object value,
			String property2, Object value2)
	{
		return EQUALS(property2, value2, EQUALS(property, value, crit(entityClass)));
	}

	@Override
	public <T> Criteria findAllCrit(Class<T> entityClass, String property, Object value, Order order)
	{
		return EQUALS(property, value, crit(entityClass).addOrder(order));
	}

	@Override
	public <T> Criteria findAllCrit(Class<T> entityClass, Map<String, ?> propertyValues)
	{
		Criteria crit = crit(entityClass);
		for (String propertyName : propertyValues.keySet())
			crit = EQUALS(propertyName, propertyValues.get(propertyName), crit);

		return crit;
	}

	public <T, COLLECTION> COLLECTION findAll(Criteria criteria, Class<COLLECTION> collectionClass)
	{
		return typeCoercer.coerce(criteria, collectionClass);
	}

	public <T, COLLECTION> COLLECTION findAll(Class<T> entityClass,
			Class<COLLECTION> collectionClass)
	{
		return typeCoercer.coerce(findAllCrit(entityClass), collectionClass);
	}

	public <T, COLLECTION> COLLECTION findAll(Criteria criteria, Class<COLLECTION> collectionClass,
			String property, Object value)
	{
		return typeCoercer.coerce(EQUALS(property, value, criteria), collectionClass);
	}

	public <T, COLLECTION> COLLECTION findAll(Class<T> entityClass,
			Class<COLLECTION> collectionClass, String property, Object value)
	{
		return typeCoercer.coerce(findAllCrit(entityClass, property, value), collectionClass);
	}

	public <T, COLLECTION> COLLECTION findAll(Class<T> entityClass,
			Class<COLLECTION> collectionClass, String property, Object value, String property2,
			Object value2)
	{
		return typeCoercer.coerce(findAllCrit(entityClass, property, value, property2, value2),
				collectionClass);
	}

	public <T, COLLECTION> COLLECTION findAll(Class<T> entityClass,
			Class<COLLECTION> collectionClass, String property, Object value, Order order)
	{
		return typeCoercer
				.coerce(findAllCrit(entityClass, property, value, order), collectionClass);
	}

	public <T, COLLECTION> COLLECTION findAll(Class<T> entityClass,
			Class<COLLECTION> collectionClass, Map<String, ?> propertyValues)
	{
		return typeCoercer.coerce(findAllCrit(entityClass, propertyValues), collectionClass);
	}

	@Override
	public <V extends OrderedEntity> Criteria findAllOrderedCrit(Class<V> clazz, String order)
	{
		return findByCriteria(clazz, Arrays.asList(Order.asc("order.orderNo")));
	}

	@Override
	public <V extends OrderedEntity> Criteria findAllOrderedCrit(Class<V> clazz, String property,
			Object value, String order)
	{
		return findByCriteria(clazz, property, value, Arrays.asList(Order.asc(order)));
	}

	public <V extends OrderedEntity, COLLECTION> COLLECTION findAllOrderedAsc(Class<V> clazz,
			Class<COLLECTION> collectionClass, String order)
	{
		return typeCoercer.coerce(findByCriteria(clazz, Arrays.asList(Order.asc(order))),
				collectionClass);
	}

	public <V extends OrderedEntity, COLLECTION> COLLECTION findAllOrderedAsc(Class<V> clazz,
			Class<COLLECTION> collectionClass, String property, Object value, String order)
	{
		return typeCoercer.coerce(
				findByCriteria(clazz, property, value, Arrays.asList(Order.asc(order))),
				collectionClass);
	}

	@Override
	public Integer findMaxOrder(Class<? extends BaseOrderedRelation> object)
	{
		Integer max = (Integer) session.createCriteria(object)

		.setProjection(Projections.max("order.orderNo"))

		.uniqueResult();

		if (max != null)
			return max;

		return 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> List<U> list(Criteria c)
	{
		return c.list();
	}

	@Override
	public Criteria orAndLikeTextProperties(List<ORMProperty> filterProperties, String value,
			Criteria beginCriteria)
	{
		List<String> fullPropertyNames = CollectionFactory.newList();
		for (ORMProperty ormProperty : filterProperties)
			fullPropertyNames.add(ormProperty.getName());

		return orAndLikeTextProperties(filterProperties, fullPropertyNames, value, beginCriteria);
	}

	@Override
	public Criteria orAndLikeTextProperties(List<ORMProperty> filterProperties,
			List<String> fullPropertyNames, String value, Criteria beginCriteria)
	{
		if (value == null || filterProperties.size() == 0)
			return beginCriteria;

		// remove leading and trailing whitespace and replace multiple whitespace with one blank
		// space
		value = value.trim().replaceAll("\\s+", " ");

		Criterion orExpr = null;

		Map<String, String> firstPropertyNameToAlias = new LinkedHashMap<String, String>();
		for (String propertyName : fullPropertyNames)
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
						ALIAS(firstProperty, false, alias, beginCriteria);
						firstPropertyNameToAlias.put(firstProperty, alias);
					}
				}
				else
					firstPropertyNameToAlias.put(firstProperty, null);
			}
		}

		int i = 0;

		for (ORMProperty property : filterProperties)
		{
			String alias;

			String propertyName = fullPropertyNames.get(i++);
			String[] propertyNameComponents = propertyName.split(DOT_REG_EXP);
			boolean isAlias = false;
			if (propertyNameComponents.length > 1)
			{
				String firstProperty = propertyNameComponents[0];
				String firstPropertyAlias = firstPropertyNameToAlias.get(firstProperty);
				propertyName = propertyName.replaceFirst(firstProperty, firstPropertyAlias);
				isAlias = true;
			}

			alias = ALIAS_TRANSLATION(propertyName, isAlias, beginCriteria);

			Criterion ilike;

			// XXX: replace 1l with the current language
			ilike = and(ilike(alias + ".value", "%" + value + "%"), eq(alias + ".language.id", 1l));

			// initialize
			if (orExpr == null)
				orExpr = ilike;
			else
				// results in (or(or(Expr1,Expr2),Expr3)),..)
				orExpr = or(ilike, orExpr);
		}

		beginCriteria.add(orExpr);

		return beginCriteria;
	}

	@Override
	public void saveOrderedEntity(BaseOrderedRelation object)
	{
		security.entities.base.Order order = new security.entities.base.Order();

		order.setOrderNo((int) findMaxOrder(object.getClass()) + 1);
		object.setOrder(order);

		save(object);
	}

	@Override
	public void refresh(Object object)
	{
		session.refresh(object);
	}

	@Override
	public void save(Object object)
	{
		session.saveOrUpdate(object);
	}

	@Override
	public void delete(Object object)
	{
		session.delete(object);
	}

	@Override
	public void deleteAll(@SuppressWarnings("rawtypes") Collection collection)
	{
		for (Object o : collection)
			session.delete(o);
	}
}
