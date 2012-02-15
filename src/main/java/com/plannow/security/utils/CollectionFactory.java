package com.plannow.security.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionFactory
{
	/**
	 * Constructs and returns a new generic {@link java.util.ArrayList} instance.
	 */
	public static <T> List<T> newList()
	{
		return new ArrayList<T>();
	}

	/**
	 * Constructs and returns a generic {@link HashMap} instance.
	 */
	public static <K, V> Map<K, V> newMap()
	{
		return new HashMap<K, V>();
	}

	/**
	 * Constructs and returns a generic {@link HashMap} instance.
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> newMap(Object... nameToValue)
	{
		Map<K, V> map = new HashMap<K, V>();

		if (nameToValue.length % 2 == 1)
			throw new IllegalArgumentException("nameToValue should be of size mod 2");

		for (int i = 0; i < nameToValue.length; i += 2)
			map.put((K) nameToValue[i], (V) nameToValue[i + 1]);

		return map;
	}

	/**
	 * Constructs and returns a generic {@link HashSet} instance.
	 */
	public static <K> Set<K> newSet()
	{
		return new HashSet<K>();
	}

	/**
	 * Constructs and returns a generic {@link LinkedHashMap} instance.
	 */
	public static <K, V> LinkedHashMap<K, V> newLinkedHashMap()
	{
		return new LinkedHashMap<K, V>();
	}

	/**
	 * Creates a new, fully modifiable list from an initial set of elements.
	 */
	public static <T, V extends T> List<T> newList(V... elements)
	{
		return new ArrayList<T>(Arrays.asList(elements));
	}
}
