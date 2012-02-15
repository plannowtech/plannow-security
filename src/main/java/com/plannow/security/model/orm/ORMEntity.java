package com.plannow.security.model.orm;

import java.util.Collection;
import java.util.LinkedHashMap;

public class ORMEntity
{
	final String className;
	final String classSimpleName;

	LinkedHashMap<String, ORMProperty> propertyNamesToProperties;

	public ORMEntity(String className, String classSimpleName)
	{
		super();
		this.className = className;
		this.classSimpleName = classSimpleName;

		propertyNamesToProperties = new LinkedHashMap<String, ORMProperty>();
	}

	public ORMEntity addProperty(ORMProperty property)
	{
		propertyNamesToProperties.put(property.getName(), property);
		property.entity = this;
		return this;
	}

	public ORMProperty getProperty(String propertyName)
	{
		return propertyNamesToProperties.get(propertyName);
	}

	public Collection<ORMProperty> getProperties()
	{
		return propertyNamesToProperties.values();
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
		return "ORMEntity [className=" + classSimpleName + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
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
		ORMEntity other = (ORMEntity) obj;
		if (className == null)
		{
			if (other.className != null)
				return false;
		}
		else if (!className.equals(other.className))
			return false;
		return true;
	}

}
