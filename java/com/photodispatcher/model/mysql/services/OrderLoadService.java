package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.Order;
import com.photodispatcher.model.mysql.entities.OrderFile;
import com.photodispatcher.model.mysql.entities.OrderLoad;
import com.photodispatcher.model.mysql.entities.OrderTemp;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.Source;
import com.photodispatcher.model.mysql.entities.SqlResult;

@RemoteDestination(id="orderLoadService", source="orderLoadService")
public interface OrderLoadService {

	SqlResult beginSync();
	SqlResult addSyncItems(List<OrderTemp> items);
	SelectResult<Source> sync();
	SelectResult<Order> loadByState(int stateFrom, int stateTo);
	SqlResult save(OrderLoad order);
	SelectResult<OrderLoad> loadById(String id);
	SelectResult<OrderLoad> merge(OrderLoad order);
	SqlResult saveFile(OrderFile file);
	SqlResult saveFiles(List<OrderFile> items);
}
