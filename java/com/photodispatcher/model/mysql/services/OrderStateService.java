package com.photodispatcher.model.mysql.services;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.OrderState;
import com.photodispatcher.model.mysql.entities.SelectResult;

@RemoteDestination(id="orderStateService", source="orderStateService")
public interface OrderStateService {

	public SelectResult<OrderState> loadAll();
}
