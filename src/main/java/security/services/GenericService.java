package security.services;

import java.io.Serializable;
import java.util.List;

import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;

import security.entities.base.OrderedEntity;
import security.entities.base.impl.BaseOrderedRelation;
import security.grid.GridDataSource;
import security.model.orm.ORMProperty;

public interface GenericService<T, ID extends Serializable>
{
	T findById(ID id);

	<V> V findById(Class<V> clazz, ID id);

	<V> V find(Class<V> clazz, String property, Object value);

	<V> V find(Class<V> clazz, String p1, Object v1, String p2, Object v2);

	<V> V find(Class<V> clazz, String p1, Object v1, String p2, Object v2, String p3, Object v3);

	List<T> findAll();

	<U> List<U> findAll(Class<U> clazz);

	<U> List<U> findAll(Class<U> clazz, String property, Object value);

	<U> List<U> findAll(Class<U> clazz, String property, Object value, String property2,
			Object value2);

	<V> Criteria crit(Class<V> clazz);

	<U> List<U> findAll(Class<U> clazz, String property, Object value, Order order);

	<V> Long findCount(Class<V> clazz, String property, Object value);

	<U> GridDataSource<U> findAllGDS(Class<U> clazz, String property, Object value);

	<V extends OrderedEntity> List<V> findAllOrdered(Class<V> clazz, String order);

	<V extends OrderedEntity> List<V> findAllOrdered(Class<V> clazz, String property, Object value,
			String order);

	List<T> findByExample(T exampleInstance);

	List<T> findByExampleALike(T exampleInstance, String... excludeProps);

	void refresh(Object object);

	T makePersistent(T entity);

	void makeTransient(T entity);

	<U> List<U> list(Criteria c);

	Criteria orProperties(List<ORMProperty> filterProperties, Object value, Criteria beginCriteria);

	@CommitAfter
	void saveAndCommit(Object o);

	@CommitAfter
	void saveAndCommitOrderedEntity(BaseOrderedRelation object);

	@CommitAfter
	void delete(Object o);
}
