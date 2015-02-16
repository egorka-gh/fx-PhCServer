package com.photodispatcher.model.mysql.services;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.MailPackage;
import com.photodispatcher.model.mysql.entities.Order;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@RemoteDestination(id="mailPackageService", source="mailPackageService")
public interface MailPackageService {

	SqlResult persist(MailPackage item);
	SelectResult<MailPackage> loadReady4Mail();
	SelectResult<Order> loadChildOrders(int id);

}
