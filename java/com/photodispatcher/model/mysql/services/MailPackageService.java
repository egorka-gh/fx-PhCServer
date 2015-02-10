package com.photodispatcher.model.mysql.services;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.MailPackage;
import com.photodispatcher.model.mysql.entities.SqlResult;

@RemoteDestination(id="mailPackageService", source="mailPackageService")
public interface MailPackageService {

	SqlResult persist(MailPackage item);

}
