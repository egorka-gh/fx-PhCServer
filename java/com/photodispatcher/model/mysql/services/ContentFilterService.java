package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.granite.messaging.service.annotations.RemoteDestination;

import com.photodispatcher.model.mysql.entities.ContentFilter;
import com.photodispatcher.model.mysql.entities.ContentFilterAlias;
import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@RemoteDestination(id="contentFilterService", source="contentFilterService")
public interface ContentFilterService {
	
	public DmlResult<ContentFilter> insert(ContentFilter item);
	public DmlResult<ContentFilter> update(ContentFilter item);
	public SelectResult<ContentFilter> findeAll(boolean includeDefault);

	public SelectResult<ContentFilterAlias> loadAliases(int filter, boolean editMode);
	public SqlResult saveAliases(int filter, List<ContentFilterAlias> aliases);
	
}
