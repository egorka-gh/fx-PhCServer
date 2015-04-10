package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.DeliveryTypeDictionary;
import com.photodispatcher.model.mysql.entities.MailPackage;
import com.photodispatcher.model.mysql.entities.Order;
import com.photodispatcher.model.mysql.entities.RackSpace;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@RemoteDestination(id="mailPackageService", source="mailPackageService")
public interface MailPackageService {

	SqlResult persist(MailPackage item);
	SelectResult<MailPackage> loadReady4Mail();
	SelectResult<Order> loadChildOrders(int source, int id);
	SelectResult<MailPackage> load(int source, int id);
	SelectResult<MailPackage> loadByClient(int source, int client);
	SqlResult persistsDeliveryTypeBatch(List<DeliveryTypeDictionary> items);
	SelectResult<DeliveryTypeDictionary> loadDeliveryTypeDictionar4Edit(int source);
	SelectResult<DeliveryTypeDictionary> loadDeliveryTypeDictionary();
	SqlResult join(int source, int targetId, List<Integer> joinIds);
	SqlResult startState(MailPackage item);
	SqlResult stopPreviousExtraStates(MailPackage item);
	SqlResult getStateByOrders(int source, int id);
	SelectResult<MailPackage> loadByState(int state);
	SqlResult getStateByPackages(int source, List<Integer> packageIds);
	SelectResult<RackSpace> loadRackSpaces(MailPackage mailPackage);
	SelectResult<RackSpace> getRackSpaces(String orderId, int techPoint);
	SqlResult setRackSpace(String orderId, int space);
	SqlResult resetRackSpace(String orderId);
	
}
