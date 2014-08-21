package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.Order;
import com.photodispatcher.model.mysql.entities.OrderExtraInfo;
import com.photodispatcher.model.mysql.entities.OrderTemp;
import com.photodispatcher.model.mysql.entities.PrintGroup;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.Source;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.StateLog;

@RemoteDestination(id="orderService", source="orderService")
public interface OrderService {

	public SqlResult beginSync();
	public SqlResult addSyncItems(List<OrderTemp> items);
	public SelectResult<Source> sync();
	public SelectResult<Order> loadByState(int stateFrom, int stateTo);
	public SelectResult<OrderExtraInfo> loadExtraIfo(String id, String subId);
	public SelectResult<Order> loadOrder(String id);
	public DmlResult<Order> addManual(Order order);
	public SqlResult cleanUpOrder(String id);
	public SqlResult cancelOrders(String[] ids);
	public SelectResult<Order> loadOrderBySrcCode(String code, String id);
	public SqlResult fillUpOrder(Order order);
	public SqlResult logState(StateLog item);
	public SelectResult<OrderExtraInfo> loadExtraIfoByPG(String pgId);
	public SelectResult<Order> loadOrdersByIds(List<String> ids);
	public SelectResult<Order> loadOrderFull(String id);
	public SqlResult addReprintPGroups(List<PrintGroup> items);
	public SelectResult<Order> loadOrderVsChilds(String id);

}
