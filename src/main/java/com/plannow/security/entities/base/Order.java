package com.plannow.security.entities.base;

import javax.persistence.Column;

import org.apache.tapestry5.beaneditor.Validate;

public class Order
{
	private Integer orderNo;

	@Validate("required")
	@Column(name = "order_no", nullable = false)
	public Integer getOrderNo()
	{
		return orderNo;
	}

	public void setOrderNo(Integer orderNo)
	{
		this.orderNo = orderNo;
	}
}
