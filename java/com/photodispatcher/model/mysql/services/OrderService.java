package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.OrderTemp;
import com.photodispatcher.model.mysql.entities.SqlResult;

@RemoteDestination(id="orderService", source="orderService")
public interface OrderService {

	public SqlResult beginSync();
	public SqlResult addSyncItems(List<OrderTemp> items);

}
