package com.photodispatcher.model.mysql.services;


import org.springframework.stereotype.Service;

import com.photodispatcher.mail.SendMailByGoogle;
import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.TechLog;

@Service("techService")
public class TechServiceImpl extends AbstractDAO implements TechService {

	@Override
	public DmlResult<TechLog> log(TechLog item){
		return runInsert(item);
	}
	
	@Override
	public SqlResult logByPg(TechLog item){
		//PROCEDURE techLogPg(IN pPgroup VARCHAR(50), IN pSheet INT, IN pTechPoint INT, IN pDate DATETIME)
		String sql= "{CALL techLogPg(?,?,?,?)}";
		return runCall(sql, item.getPrint_group(), item.getSheet(), item.getSrc_id(), item.getLog_date());
	}
	
	@Override
	public SelectResult<TechLog> loadBooks4Otk(String id, String sub_id){
		if(sub_id==null) sub_id="";
		//techlog books
		String sql="SELECT (tl.sheet DIV 100)*100 sheet, MIN(tl.log_date) log_date"+
					" FROM tech_point tp"+
					" INNER JOIN tech_log tl ON tl.src_id=tp.id"+
					" WHERE tp.tech_type=450 AND tl.sheet>99 AND tl.order_id=? AND tl.sub_id=?"+
					" GROUP BY (tl.sheet DIV 100)*100";
		return runSelect(TechLog.class, sql, id, sub_id);
	}
	
	@Override
	public SelectResult<TechLog> loadTechPulse(int techPointType){
		
		String sql = "SELECT tl.*  " +
				"FROM tech_log tl " +
				"INNER JOIN " +
				"(SELECT src_id, MAX(log_date) AS log_date FROM tech_log WHERE src_id IN (SELECT tp.id FROM tech_point tp WHERE tp.tech_type=?) " +
				"GROUP BY src_id) AS max USING (src_id, log_date);";
		
		return runSelect(TechLog.class, sql, techPointType);
		
	}
	
	@Override
	public boolean testMail() {
		
		return false;
		
	}

}
