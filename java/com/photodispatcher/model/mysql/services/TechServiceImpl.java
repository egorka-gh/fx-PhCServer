package com.photodispatcher.model.mysql.services;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.TechLog;

@Service("techService")
public class TechServiceImpl extends AbstractDAO implements TechService {

	@Override
	public SqlResult logByPg(TechLog item){
		//PROCEDURE phcdata.techLogPg(IN pPgroup VARCHAR(50), IN pSheet INT, IN pTechPoint INT, IN pDate DATETIME)
		String sql= "{CALL phcdata.techLogPg(?,?,?,?)}";
		return runCall(sql, item.getPrint_group(), item.getSheet(), item.getSrc_id(), item.getLog_date());
	}
}
