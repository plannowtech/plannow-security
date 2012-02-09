package security.model.orm;

public class ORMProperty
{
	ORMEntity entity;

	final String name;
	final String className;
	final String classSimpleName;

	public ORMProperty(String name, String className, String classSimpleName)
	{
		super();
		this.name = name;
		this.className = className;
		this.classSimpleName = classSimpleName;
	}

	public ORMEntity getEntity()
	{
		return entity;
	}

	public void setEntity(ORMEntity entity)
	{
		this.entity = entity;
	}

	public String getName()
	{
		return name;
	}

	public String getClassName()
	{
		return className;
	}

	public String getClassSimpleName()
	{
		return classSimpleName;
	}

	@Override
	public String toString()
	{
		return "ORMProperty [name=" + name + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		ORMProperty other = (ORMProperty) obj;
		if (entity == null)
		{
			if (other.entity != null)
				return false;
		}
		else if (!entity.equals(other.entity))
			return false;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}
}
