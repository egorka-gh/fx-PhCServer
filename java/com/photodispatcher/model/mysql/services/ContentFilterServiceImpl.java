package com.photodispatcher.model.mysql.services;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.ContentFilter;
import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.SelectResult;

@Service("contentFilterService")
public class ContentFilterServiceImpl extends AbstractDAO implements ContentFilterService{

	@Override
	public SelectResult<ContentFilter> findeAll(boolean includeDefault){
		SelectResult<ContentFilter> result;
		String sql="SELECT l.* FROM phcconfig.content_filter l";
		if(!includeDefault) sql+=" WHERE l.id != 0";
		result=runSelect(ContentFilter.class, sql);
		return result;
	}

	@Override
	public DmlResult insert(ContentFilter item) {
		DmlResult result= runInsert(item, null);
		if(result.isComplete()) result.setLastId(item.getId());
		return result;
	}

	@Override
	public DmlResult update(ContentFilter item) {
		DmlResult result= runUpdate(item, null);
		return result;
	}

}
