package com.photodispatcher.model.mysql.services;

import org.springframework.stereotype.Service;


import com.photodispatcher.model.mysql.entities.AttrType;
import com.photodispatcher.model.mysql.entities.FieldValue;
import com.photodispatcher.model.mysql.entities.SelectResult;

@Service("dictionaryService")
public class DictionaryServiceImpl extends AbstractDAO implements DictionaryService {

	@Override
	public SelectResult<AttrType> getPrintAttrs(){
		SelectResult<AttrType> result;
		String sql="SELECT at.* FROM phcconfig.attr_type at WHERE at.persist=1 AND at.attr_fml=1 AND at.list=1";
		result=runSelect(AttrType.class, sql);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getFieldValueList(int fieldId, boolean includeNone){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, value label FROM phcconfig.attr_value av WHERE av.attr_tp IN (0,?)";
		if(includeNone) sql="SELECT 0 value, ' ' label UNION "+sql;
		result=runSelect(FieldValue.class, sql, fieldId);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getBookTypeValueList(boolean includeDefault){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, name label FROM phcconfig.book_type";
		if(!includeDefault) sql+=" WHERE id!=0";
		result=runSelect(FieldValue.class, sql);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getSrcTypeValueList(int locType, boolean includeDefault){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, name label FROM phcconfig.src_type WHERE loc_type = ?";
		if(includeDefault) sql+=" OR id = 0";
		result=runSelect(FieldValue.class, sql, locType);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getWeekDaysValueList(boolean includeDefault){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, name label FROM phcconfig.week_days ORDER BY id";
		if(includeDefault) sql="SELECT 0 value, ' ' label UNION "+sql;
		result=runSelect(FieldValue.class, sql);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getTechPointValueList(boolean includeDefault){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, name label FROM phcconfig.tech_point ORDER BY id";
		if(includeDefault) sql="SELECT null value, ' ' label UNION "+sql;
		result=runSelect(FieldValue.class, sql);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getTechLayerValueList(boolean includeDefault){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, name label FROM phcconfig.layer ORDER BY id";
		//if(includeDefault) sql="SELECT null value, ' ' label UNION "+sql;
		result=runSelect(FieldValue.class, sql);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getLayerGroupValueList(boolean includeDefault){
		SelectResult<FieldValue> result;
		String sql="SELECT id value, name label FROM phcconfig.layer_group WHERE id!=2 ORDER BY id";
		if(includeDefault) sql="SELECT null value, ' ' label UNION "+sql;
		result=runSelect(FieldValue.class, sql);
		return result;
	}

	@Override
	public SelectResult<FieldValue> getRollValueList(boolean includeDefault){
		SelectResult<FieldValue> result;
		String sql="SELECT width value, width label FROM phcconfig.roll ORDER BY width";
		if(includeDefault) sql="SELECT null value, ' ' label UNION "+sql;
		result=runSelect(FieldValue.class, sql);
		return result;
	}

}
