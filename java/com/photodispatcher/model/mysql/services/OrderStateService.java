package com.photodispatcher.model.mysql.services;

import java.util.Date;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.OrderBook;
import com.photodispatcher.model.mysql.entities.OrderExtraState;
import com.photodispatcher.model.mysql.entities.OrderState;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SpyData;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.StateLog;

@RemoteDestination(id="orderStateService", source="orderStateService")
public interface OrderStateService {

	public SelectResult<OrderState> loadAll();

	public SqlResult logState(StateLog item);
	public SqlResult logStateByPGroup(String pgId, int state, String comment);
	public SelectResult<StateLog> loadStateLogs(Date from, boolean onlyErrors);

	public SqlResult extraStateStart(String orderId, String subId, int state, Date date);
	public SqlResult extraStateSet(String orderId, String subId, int state, Date date);
	public SqlResult extraStateReset(String orderId, String subId, int state);
	public SqlResult extraStateProlong(String orderId, String subId, int state, String comment);
	//public SqlResult extraStateSetByPGroup(String pgId, int state);

	SqlResult printPost(String pgId, int lab);
	SqlResult printEndManual(String[] pgIds);
	SqlResult printCancel(String[] pgIds);
	SqlResult printGroupMarkInPrint(String pgId);
	
	SelectResult<OrderExtraState> loadMonitorEState(int techState, int waitState);
	SqlResult extraStateStartMonitor(String orderId, String subId, int state);

	SelectResult<SpyData> loadSpyData(Date pDate, int pFromState, int pToState, int pBookPart);

	SqlResult extraStateFix(String orderId, int state, Date date);

	SqlResult getStateByPGroups(String orderId);

	SqlResult extraStateStartOTK(String orderId, String subId, int stateStart);
	SqlResult extraStateSetOTK(String orderId, String subId, Date date);
	SqlResult extraStateSetOTKbyPG(String pgId, Date date);

	SqlResult setEntireBookState(OrderBook book);

	SqlResult setBookState(OrderBook book, boolean resetReject);

	SqlResult extraStateStop(String orderId, String subId, int state, Date date);

	SelectResult<SpyData> loadSpyRejects(int pFromState, int pToState);

}
