package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.Order;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.TechReject;

@RemoteDestination(id="techRejecService", source="techRejecService")
public interface TechRejecService {

	SqlResult create(TechReject reject);
	SelectResult<TechReject> captureState(List<TechReject> rejects);
	DmlResult<TechReject> updateReject(TechReject item);
	SelectResult<TechReject> loadByOrder(String orderId, int state);
	SelectResult<Order> loadReprintWaiteAsOrder();
	SqlResult updateRejectBatch(List<TechReject> items);
	SqlResult cancelReject(String itemId);
	SelectResult<TechReject> loadByState(int stateFrom, int stateTo);
}
