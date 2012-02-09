package security.services;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;

import security.entities.base.OrderedEntity;
import security.entities.base.impl.BaseOrderedRelation;
import security.model.orm.ORMProperty;

public interface GenericDAOService
{
	<T> T findById(Class<T> entityClass, Serializable id);

	<T> T findById(String entityClass, Serializable id);

	<T> T find(Class<T> entityClass, String property, Object value);

	<T> T find(Class<T> entityClass, String p1, Object v1, String p2, Object v2);

	<T> T find(Class<T> entityClass, String p1, Object v1, String p2, Object v2, String p3,
			Object v3);

	<T> Criteria findAllCrit(Class<T> entityClass);

	<T> Criteria findAllCrit(Class<T> entityClass, String property, Object value);

	<T> Criteria findAllCrit(Class<T> entityClass, String property, Object value, String property2,
			Object value2);

	<T> Criteria findAllCrit(Class<T> entityClass, String property, Object value, Order order);

	<T> Criteria findAllCrit(Class<T> entityClass, Map<String, ?> propertyValues);

	<V extends OrderedEntity> Criteria findAllOrderedCrit(Class<V> clazz, String order);

	<V extends OrderedEntity> Criteria findAllOrderedCrit(Class<V> clazz, String property,
			Object value, String order);

	<U> List<U> list(Criteria c);

	Criteria orAndLikeTextProperties(List<ORMProperty> filterProperties, String value,
			Criteria beginCriteria);

	Criteria orAndLikeTextProperties(List<ORMProperty> filterProperties,
			List<String> fullPropertyNames, String value, Criteria beginCriteria);

	Integer findMaxOrder(Class<? extends BaseOrderedRelation> object);

	void refresh(Object object);

	@CommitAfter
	void save(Object o);

	@CommitAfter
	void saveOrderedEntity(BaseOrderedRelation object);

	@CommitAfter
	void delete(Object o);

	@CommitAfter
	void deleteAll(@SuppressWarnings("rawtypes") Collection collection);
}
