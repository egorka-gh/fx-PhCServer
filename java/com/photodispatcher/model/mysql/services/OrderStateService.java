package com.photodispatcher.model.mysql.services;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.OrderState;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@RemoteDestination(id="orderStateService", source="orderStateService")
public interface OrderStateService {

	public SelectResult<OrderState> loadAll();

	public SqlResult extraStateStart(String orderId, String subId, int techPoint);
	public SqlResult extraStateSet(String orderId, String subId, int techPoint);
	public SqlResult extraStateReset(String orderId, String subId, int state);
	public SqlResult extraStateProlong(String orderId, String subId, int state, String comment);
}
