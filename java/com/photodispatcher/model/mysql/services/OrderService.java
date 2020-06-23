package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.Order;
import com.photodispatcher.model.mysql.entities.OrderBook;
import com.photodispatcher.model.mysql.entities.OrderExtraInfo;
import com.photodispatcher.model.mysql.entities.OrderTemp;
import com.photodispatcher.model.mysql.entities.PrintGroup;
import com.photodispatcher.model.mysql.entities.PrintGroupReject;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.Source;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.SubOrder;

@RemoteDestination(id="orderService", source="orderService")
public interface OrderService {

	public SqlResult beginSync();
	public SqlResult addSyncItems(List<OrderTemp> items);
	public SelectResult<Source> sync();
	public SelectResult<Order> loadByState(int stateFrom, int stateTo);
	public SelectResult<OrderExtraInfo> loadExtraIfo(String id, String subId);
	public SelectResult<Order> loadOrder(String id);
	public DmlResult<Order> addManual(Order order);
	public SelectResult<Order> loadOrderBySrcCode(String code, String id);
	public SqlResult fillUpOrder(Order order);
	public SelectResult<OrderExtraInfo> loadExtraIfoByPG(String pgId);
	public SelectResult<Order> loadOrdersByIds(List<String> ids);
	public SelectResult<Order> loadOrderFull(String id);
	public SqlResult addReprintPGroups(List<PrintGroup> items);
	public SelectResult<Order> loadOrderVsChilds(String id);
	public SelectResult<SubOrder> loadSubOrderByPg(String pgId);
	public SelectResult<SubOrder> loadSubOrderByOrder(String orderId, String code);
	public SelectResult<SubOrder> loadSubOrdersOtk();
	public SelectResult<Order> loadOrder4Otk(String id, String sub_id);
	public SqlResult persistExtraInfo(OrderExtraInfo info);
	SqlResult cancelOrders(String[] ids, int state);
	SelectResult<Order> load4CleanFS(int source, int state, int days, int limit);
	SqlResult markCleanFS(String[] ids);
	SelectResult<PrintGroup> loadReprintsByPG(String pgId);
	SqlResult saveVsSuborders(Order order);
	SqlResult getLock(String key, String owner);
	SqlResult setState(Order order);
	SqlResult releaseLock(String key, String owner);
	SqlResult clearLocks();
	SqlResult setStateBatch(List<Order> orders);
	SelectResult<Order> loadDownloadErrs();
	SelectResult<Order> loadBuildErrs();
	SqlResult captureState(String orderId, int fromState, int toState, String owner);
	SqlResult cleanUpOrder(String id, int state);
	SqlResult setSuborderState(SubOrder suborder);
	SelectResult<PrintGroupReject> loadRejects4PG(String pgId);
	SelectResult<PrintGroupReject> loadRejects4Order(String pgId);
	SelectResult<OrderBook> loadOrderBooks(String orderId);
	SelectResult<OrderBook> loadSubOrderBooks(String orderId, String subId);
	SelectResult<OrderBook> loadPGBooks(String pgId);
	SelectResult<PrintGroup> loadCompoChilds(String compoID);
}
