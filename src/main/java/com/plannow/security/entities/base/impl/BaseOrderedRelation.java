package com.plannow.security.entities.base.impl;

import javax.persistence.Embedded;
import javax.persistence.Transient;

import org.apache.tapestry5.beaneditor.NonVisual;

import com.plannow.security.entities.BaseEntityImpl;
import com.plannow.security.entities.base.Order;
import com.plannow.security.entities.base.OrderedEntity;


public abstract class BaseOrderedRelation extends BaseEntityImpl implements OrderedEntity
{
	private Order order;

	@Embedded
	public Order getOrder()
	{
		return order;
	}

	public void setOrder(Order order)
	{
		this.order = order;
	}

	@Transient
	@NonVisual
	public Integer getOrderNo()
	{
		return order.getOrderNo();
	}

	@Transient
	public void setOrderNo(Integer orderNo)
	{
		this.order.setOrderNo(orderNo);
	}
}
