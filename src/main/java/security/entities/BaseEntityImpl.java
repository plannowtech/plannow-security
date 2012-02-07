package security.entities;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.annotations.Proxy;

import security.entities.BaseEntityImpl;
import security.entities.base.BaseEntity;

@Proxy(lazy = false)
@MappedSuperclass
public class BaseEntityImpl implements BaseEntity<Long>
{
	@NonVisual
	public static final String PROP_ID = "id";

	protected Long id;
	protected UUID uuid;

	public BaseEntityImpl()
	{
		uuid = UUID.randomUUID();
	}

	@Transient
	public UUID getUuid()
	{
		return uuid;
	}

	@NonVisual
	@Transient
	public Long getEntityId()
	{
		return getId();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id", nullable = false)
	@NonVisual
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return new StringBuilder(getClass().getSimpleName()).append(":").append(getId()).toString();
	}

	/**
	 * Prints complete information by calling all public getters on the entity.
	 */
	public String print()
	{

		final String EQUALS = "=";
		final String DELIMITER = ", ";
		final String ENTITY_FORMAT = "(id=%d)";

		StringBuffer sb = new StringBuffer(toString() + " {");

		PropertyDescriptor[] properties = PropertyUtils.getPropertyDescriptors(this);
		PropertyDescriptor property = null;
		int i = 0;

		List<String> skipValues = Arrays.asList("class", "uuid", "id");
		while (i < properties.length)
		{
			property = properties[i];
			if (skipValues.contains(property.getName()))
			{
				i++;
				continue;
			}

			sb.append(property.getName());
			sb.append(EQUALS);

			try
			{
				Object value = PropertyUtils.getProperty(this, property.getName());
				if (value instanceof BaseEntityImpl)
				{
					BaseEntityImpl entityValue = (BaseEntityImpl) value;
					String objectValueString = String.format(ENTITY_FORMAT, entityValue.getId());
					sb.append(objectValueString);
				}
				else
				{
					sb.append(value);
				}
			}
			catch (IllegalAccessException e)
			{
				// do nothing
			}
			catch (InvocationTargetException e)
			{
				// do nothing
			}
			catch (NoSuchMethodException e)
			{
				// do nothing
			}

			i++;
			if (i < properties.length)
			{
				sb.append(DELIMITER);
			}
		}

		sb.append("}");

		return sb.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseEntityImpl other = (BaseEntityImpl) obj;
		if (id == null)
		{
			if (other.id != null)
				return false;
		}
		else if (!id.equals(other.id))
			return false;
		return true;
	}
}
