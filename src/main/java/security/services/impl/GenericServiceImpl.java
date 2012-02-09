package security.services.impl;

import static org.hibernate.criterion.Restrictions.ilike;
import static org.hibernate.criterion.Restrictions.or;
import static security.utils.CriteriaUtils.ALIAS;
import static security.utils.CriteriaUtils.EQUALS;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import security.entities.base.OrderedEntity;
import security.entities.base.impl.BaseOrderedRelation;
import security.grid.GridDataSource;
import security.grid.HibernateGridDataSource;
import security.model.orm.ORMProperty;
import security.services.GenericDAOService;
import security.services.GenericService;

public class GenericServiceImpl<T, ID extends Serializable> implements GenericService<T, ID>
{
	@Inject
	private Session session;

	private Class<T> persistentClass;

	@SuppressWarnings("unchecked")
	public GenericServiceImpl()
	{
		this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
	}

	public Session getSession()
	{
		return session;
	}

	public Class<T> getPersistentClass()
	{
		return persistentClass;
	}

	@SuppressWarnings("unchecked")
	public T findById(ID id)
	{
		return (T) getSession().load(getPersistentClass(), id);
	}

	@SuppressWarnings("unchecked")
	public <V> V findById(Class<V> clazz, ID id)
	{
		return (V) getSession().load(clazz, id);
	}

	@SuppressWarnings("unchecked")
	public <V> V find(Class<V> clazz, String property, Object value)
	{
		return (V) EQUALS(property, value, crit(clazz)).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public <V> V find(Class<V> clazz, String p1, Object v1, String p2, Object v2)
	{
		return (V) EQUALS(p2, v2, EQUALS(p1, v1, crit(clazz))).uniqueResult();
	}

	public <V> Criteria crit(Class<V> clazz)
	{
		return session.createCriteria(clazz);
	}

	@SuppressWarnings("unchecked")
	public <V> V find(Class<V> clazz, String p1, Object v1, String p2, Object v2, String p3,
			Object v3)
	{
		return (V) EQUALS(p3, v3, EQUALS(p2, v2, EQUALS(p1, v1, crit(clazz)))).uniqueResult();
	}

	public List<T> findAll()
	{
		return findByCriteria();
	}

	public List<T> findByExample(T exampleInstance)
	{
		return findByCriteria(Example.create(exampleInstance));
	}

	public List<T> findByExampleALike(T exampleInstance, String... excludeProps)
	{
		Example example = Example.create(exampleInstance);
		example.enableLike(MatchMode.ANYWHERE);
		for (String prop : excludeProps)
			Example.create(example).excludeProperty(prop);
		return findByCriteria(example);
	}

	public T makePersistent(T entity)
	{
		getSession().saveOrUpdate(entity);
		return entity;
	}

	public void makeTransient(T entity)
	{
		getSession().delete(entity);
	}

	public void flush()
	{
		getSession().flush();
	}

	@SuppressWarnings("unchecked")
	protected <V> List<V> findByCriteria(Criteria criteria, Criterion... criterion)
	{
		for (Criterion c : criterion)
		{
			criteria.add(c);
		}

		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	protected <V> List<V> findByCriteria(Class<V> clazz, Criterion... criterion)
	{
		Criteria crit = crit(clazz);
		for (Criterion c : criterion)
		{
			crit.add(c);
		}
		return crit.list();
	}

	protected List<T> findByCriteria(Criterion... criterion)
	{
		return findByCriteria(getPersistentClass());
	}

	@SuppressWarnings("unchecked")
	protected List<T> findByCriteria(List<Order> ordering, Criterion... criterion)
	{
		Criteria crit = crit(getPersistentClass());

		for (Criterion c : criterion)
		{
			crit.add(c);
		}

		for (Order o : ordering)
		{
			crit.addOrder(o);
		}

		return crit.list();
	}

	@SuppressWarnings("unchecked")
	protected <V> List<V> findByCriteria(Class<V> clazz, List<Order> ordering)
	{
		Criteria crit = crit(clazz);

		for (Order o : ordering)
		{
			crit.addOrder(o);
		}

		return crit.list();
	}

	@SuppressWarnings("unchecked")
	protected <V> List<V> findByCriteria(Class<V> clazz, String property, Object value,
			List<Order> ordering)
	{
		Criteria crit = EQUALS(property, value, crit(clazz));

		for (Order o : ordering)
		{
			crit.addOrder(o);
		}

		return crit.list();
	}

	protected static String composite(String... strings)
	{
		StringBuffer buffer = new StringBuffer(strings[0]);
		for (int i = 1; i < strings.length; i++)
			buffer.append("." + strings[i]);
		return buffer.toString();
	}

	@SuppressWarnings("unchecked")
	public <V> List<V> findAll(Class<V> clazz)
	{
		return crit(clazz).list();
	}

	@SuppressWarnings("unchecked")
	public <U> List<U> findAll(Class<U> clazz, String property, Object value)
	{
		return EQUALS(property, value, crit(clazz)).list();
	}

	@SuppressWarnings("unchecked")
	public <U> List<U> findAll(Class<U> clazz, String property, Object value, String property2,
			Object value2)
	{
		return EQUALS(property2, value2, EQUALS(property, value, crit(clazz))).list();
	}

	public <V> Long findCount(Class<V> clazz, String property, Object value)
	{
		return (Long) EQUALS(property, value, crit(clazz)).setProjection(Projections.rowCount())
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public <U> List<U> findAll(Class<U> clazz, String property, Object value, Order order)
	{
		return EQUALS(property, value, crit(clazz).addOrder(order)).list();
	}

	public <U> GridDataSource<U> findAllGDS(final Class<U> clazz, final String property,
			final Object value)
	{
		return new HibernateGridDataSource<U>(getSession(), clazz)
		{
			@Override
			protected void applyAdditionalConstraints(Criteria crit)
			{
				EQUALS(property, value, crit);
			}

		};
	}

	@SuppressWarnings("unchecked")
	protected <V> List<V> findAll(Class<V> clazz, Order... ordering)
	{
		Criteria crit = crit(clazz);

		for (Order o : ordering)
		{
			crit.addOrder(o);
		}

		return crit.list();
	}

	@SuppressWarnings("unchecked")
	protected <U> List<U> getList(Criteria crit)
	{
		return (List<U>) crit.list();
	}

	public void save(Object o)
	{
		getSession().saveOrUpdate(o);
	}

	public void merge(Object o)
	{
		getSession().merge(o);
	}

	public void delete(Object o)
	{
		getSession().delete(o);
	}

	public void saveAndCommit(Object o)
	{
		save(o);
	}

	public void refresh(Object object)
	{
		getSession().refresh(object);
	}

	public <U> List<U> list(Criteria c)
	{
		return getList(c);
	}

	@Inject
	private GenericDAOService genericDAOService;

	public void saveAndCommitOrderedEntity(BaseOrderedRelation object)
	{
		security.entities.base.Order order = new security.entities.base.Order();

		order.setOrderNo((int) genericDAOService.findMaxOrder(object.getClass()) + 1);
		object.setOrder(order);

		save(object);
	}

	public Criteria orProperties(List<ORMProperty> filterProperties, Object value,
			Criteria beginCriteria)
	{
		if (value == null)
			return beginCriteria;

		Criterion orExpr = null;
		for (ORMProperty property : filterProperties)
		{
			String alias = ALIAS(property.getName(), false, null, beginCriteria);

			Criterion ilike = ilike(alias, "%" + property + "%");

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

	public <V extends OrderedEntity> List<V> findAllOrdered(Class<V> clazz, String order)
	{
		return findByCriteria(clazz, Arrays.asList(Order.asc(order)));
	}

	public <V extends OrderedEntity> List<V> findAllOrdered(Class<V> clazz, String property,
			Object value, String order)
	{
		return findByCriteria(clazz, property, value, Arrays.asList(Order.asc("order.orderNo")));
	}
}
