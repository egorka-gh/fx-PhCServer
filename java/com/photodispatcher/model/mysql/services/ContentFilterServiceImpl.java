package com.photodispatcher.model.mysql.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.ContentFilter;
import com.photodispatcher.model.mysql.entities.ContentFilterAlias;
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

	@Override
	public SelectResult<ContentFilterAlias> loadAliases(int filter, boolean editMode) {
		SelectResult<ContentFilterAlias> result;
		String sql="SELECT l.*, concat_ws(' ','id', l.alias) as alias_name, 1 as allow FROM phcconfig.content_filter_alias l WHERE l.filter = ?";
		/*
		sql='SELECT l.*, st.name src_type_name, bt.name book_type_name, ifnull(fa.alias,0) is_allow'+
				' FROM config.book_synonym l'+
				' INNER JOIN config.src_type st ON l.src_type = st.id'+
				' INNER JOIN config.book_type bt ON l.book_type = bt.id'+
				' LEFT OUTER JOIN config.content_filter_alias fa ON fa.filter= ? AND l.id=fa.alias'+
				' WHERE l.src_type = ?'+
				' ORDER BY l.synonym';
*/
		result=runSelect(ContentFilterAlias.class, sql, filter);
		return result;
	}

	@Override
	public DmlResult saveAliases(int filter, List<ContentFilterAlias> aliases) {
		String sql="DELETE FROM phcconfig.content_filter_alias l WHERE l.filter = ?";
		DmlResult result= runDML(sql, true, filter);
		if(result.isComplete()) result= runInsertBatch(aliases, null);
		return result;
	}

}
