package com.plannow.security.grid;

import java.util.List;

public interface GridDataSource<T> extends org.apache.tapestry5.grid.GridDataSource
{
	List<T> getPreparedResults();
}
