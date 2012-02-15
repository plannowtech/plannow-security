package com.plannow.security.grid;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.grid.SortConstraint;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.impl.CriteriaImpl.Subcriteria;
import org.hibernate.loader.criteria.CriteriaJoinWalker;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.type.Type;

import com.plannow.security.entities.BaseEntityImpl;
import com.plannow.security.utils.CollectionFactory;


public class HibernateGridDataSource<T> implements GridDataSource<T>
{
	private final Session session;

	private final Class<T> entityType;

	private int startIndex;

	private List<T> preparedResults;

	private Criteria criteria;

	public HibernateGridDataSource(Session session, Class<T> entityType)
	{
		this(session, session.createCriteria(entityType), entityType);
	}

	public HibernateGridDataSource(Session session, Criteria criteria, Class<T> entityType)
	{
		this.session = session;
		this.criteria = criteria;
		this.entityType = entityType;
	}

	/**
	 * Returns the total number of rows for the configured entity type.
	 */
	public int getAvailableRows()
	{
		applyAdditionalConstraints(criteria);

		while (criteria instanceof Subcriteria)
			criteria = ((Subcriteria) criteria).getParent();

		CriteriaImpl criteriaImpl = (CriteriaImpl) criteria;
		SessionImplementor sessionImplementor = criteriaImpl.getSession();
		SessionFactoryImplementor factory = sessionImplementor.getFactory();
		CriteriaQueryTranslator translator = new CriteriaQueryTranslator(factory, criteriaImpl,
				criteriaImpl.getEntityOrClassName(), CriteriaQueryTranslator.ROOT_SQL_ALIAS);
		String[] implementors = factory.getImplementors(criteriaImpl.getEntityOrClassName());

		CriteriaJoinWalker walker = new CriteriaJoinWalker(
				(OuterJoinLoadable) factory.getEntityPersister(implementors[0]), translator,
				factory, criteriaImpl, criteriaImpl.getEntityOrClassName(),
				sessionImplementor.getLoadQueryInfluencers());

		String sql = walker.getSQLString();

		String idColumnAlias = sql.split(" ")[3].replace(",", "");
		String rowCountSQL = "select count(" + idColumnAlias + ") from (select distinct "
				+ idColumnAlias + " from ( " + sql + "))";

		SQLQuery query = session.createSQLQuery(rowCountSQL);

		Type[] types = translator.getQueryParameters().getPositionalParameterTypes();
		Object[] values = translator.getQueryParameters().getPositionalParameterValues();
		for (int i = 0; i < types.length; i++)
		{
			query.setParameter(i, values[i], types[i]);
		}
		Number rowCount = (Number) query.uniqueResult();

		return rowCount.intValue();
	}

	/**
	 * Prepares the results, performing a query (applying the sort results, and the provided start
	 * and end index). The results can later be obtained from {@link #getRowValue(int)} .
	 * 
	 * @param startIndex
	 *            index, from zero, of the first item to be retrieved
	 * @param endIndex
	 *            index, from zero, of the last item to be retrieved
	 * @param sortConstraints
	 *            zero or more constraints used to set the order of the returned values
	 */
	@SuppressWarnings("unchecked")
	public void prepare(int startIndex, int endIndex, List<SortConstraint> sortConstraints)
	{
		assert sortConstraints != null;

		criteria.setFirstResult(startIndex).setMaxResults(endIndex - startIndex + 1);

		for (SortConstraint constraint : sortConstraints)
		{
			String propertyName = constraint.getPropertyModel().getPropertyName();

			switch (constraint.getColumnSort())
			{

			case ASCENDING:

				criteria.addOrder(Order.asc(propertyName));
				break;

			case DESCENDING:
				criteria.addOrder(Order.desc(propertyName));
				break;

			default:
			}
		}

		applyAdditionalConstraints(criteria);

		this.startIndex = startIndex;

		CriteriaImpl criteriaImpl = (CriteriaImpl) criteria;
		SessionImplementor sessionImplementor = criteriaImpl.getSession();
		SessionFactoryImplementor factory = sessionImplementor.getFactory();
		CriteriaQueryTranslator translator = new CriteriaQueryTranslator(factory, criteriaImpl,
				criteriaImpl.getEntityOrClassName(), CriteriaQueryTranslator.ROOT_SQL_ALIAS);
		String[] implementors = factory.getImplementors(criteriaImpl.getEntityOrClassName());

		CriteriaJoinWalker walker = new CriteriaJoinWalker(
				(OuterJoinLoadable) factory.getEntityPersister(implementors[0]), translator,
				factory, criteriaImpl, criteriaImpl.getEntityOrClassName(),
				sessionImplementor.getLoadQueryInfluencers());

		String sql = walker.getSQLString().toLowerCase();

		String[] fromSplit = sql.split(" from ");
		String selectStmt = fromSplit[0];
		String fromStmt = fromSplit[1];
		String[] orderBy = sql.split("order by");
		String orderByStmt = null;
		if (orderBy.length > 1)
			orderByStmt = orderBy[1];

		String newSelectStmt = "SELECT DISTINCT this_.id";
		if (orderByStmt != null)
		{
			String[] selectColumns = selectStmt.replace("select", "").split(",");
			for (String column : selectColumns)
			{
				// remove the AS (alias)
				String columnName = column.split(" as ")[0].trim();
				if (orderByStmt.contains(columnName))
					newSelectStmt += "," + columnName.trim();
			}
		}

		String selectIdsSQL = newSelectStmt + " FROM " + fromStmt;

		SQLQuery query = session.createSQLQuery(selectIdsSQL);

		Type[] types = translator.getQueryParameters().getPositionalParameterTypes();
		Object[] values = translator.getQueryParameters().getPositionalParameterValues();
		for (int i = 0; i < types.length; i++)
		{
			query.setParameter(i, values[i], types[i]);
		}

		List<Long> ids = CollectionFactory.newList();
		for (Object number : query.list())
		{
			if (number instanceof Object[])
				ids.add(((Number) ((Object[]) number)[0]).longValue());
			else
				ids.add(((Number) number).longValue());
		}

		Map<Long, BaseEntityImpl> idToEntityMap = CollectionFactory.newMap();

		// the list is split in chunks of 1000 because the maximum allowed values in "IN" clauses is
		// 1000
		for (int i = 0; i < ids.size(); i += 1000)
		{
			Criteria crit = session.createCriteria(entityType);
			int toIndex = i + 1000 > ids.size() ? ids.size() : i + 1000;
			crit.add(Restrictions.in("id", ids.subList(i, toIndex)));

			List<BaseEntityImpl> notSortedResultSet = crit.list();
			for (BaseEntityImpl baseEntity : notSortedResultSet)
				idToEntityMap.put(baseEntity.getId(), baseEntity);
		}

		// sort crit.list() according to the id ordering of criteria

		preparedResults = CollectionFactory.newList();

		for (Long id : ids)
			preparedResults.add((T) idToEntityMap.get(id));
	}

	/**
	 * Invoked after the main criteria has been set up (firstResult, maxResults and any sort
	 * contraints). This gives subclasses a chance to apply additional constraints before the list
	 * of results is obtained from the criteria. This implementation does nothing and may be
	 * overridden.
	 */
	protected void applyAdditionalConstraints(Criteria crit)
	{
	}

	/**
	 * Returns a row value at the given index (which must be within the range defined by the call to
	 * {@link #prepare(int, int, java.util.List)} ).
	 * 
	 * @param index
	 *            of object
	 * @return object at that index
	 */
	public Object getRowValue(int index)
	{
		// empty grid
		if (preparedResults == null)
			return null;

		if ((index - startIndex) < preparedResults.size())
			return preparedResults.get(index);
		return null;
	}

	/**
	 * Returns the entity type, as provided via the constructor.
	 */
	public Class<T> getRowType()
	{
		return entityType;
	}

	@Override
	public List<T> getPreparedResults()
	{
		List<SortConstraint> empty = Collections.emptyList();
		prepare(0, getAvailableRows() - 1, empty);

		return preparedResults;
	}
}
