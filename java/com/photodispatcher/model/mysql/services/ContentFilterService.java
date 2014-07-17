package com.photodispatcher.model.mysql.services;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.ContentFilter;
import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.SelectResult;

@RemoteDestination(id="contentFilterService", source="contentFilterService")
public interface ContentFilterService {
	
	public DmlResult insert(ContentFilter item);
	public DmlResult update(ContentFilter item);
	public SelectResult<ContentFilter> findeAll(boolean includeDefault);
	
}
