package com.photodispatcher.model.mysql.services;


import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.TechLog;
import com.photodispatcher.model.mysql.entities.TechTimeline;

@Service("techService")
public class TechServiceImpl extends AbstractDAO implements TechService {

	@Override
	public DmlResult<TechLog> log(TechLog item){
		return runInsert(item);
	}
	
	@Override
	public SqlResult logByPg(TechLog item, int calc){
		//PROCEDURE techLogPg(IN pPgroup varchar(50), IN pSheet int, IN pTechPoint int, IN pDate datetime, IN pCalc int)
		String sql= "{CALL techLogPg(?,?,?,?,?)}";
		return runCall(sql, item.getPrint_group(), item.getSheet(), item.getSrc_id(), item.getLog_date(), calc);
	}

	@Override
	public SqlResult calcByPg(String pgId, int techPointId){
		//PROCEDURE techCalcPg(IN pPgroup varchar(50), IN pTechPoint int)
		String sql= "{CALL techCalcPg(?,?)}";
		return runCall(sql, pgId, techPointId);
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
		//TODO refactor & kill 
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

	@Override
	public SqlResult forwardMeterByTechPoint(int techPoint, String printgroup){
		//PROCEDURE lab_meter_forward_tp(IN ptechpoint int(7), IN pprintgroup varchar(50))
		String sql= "{CALL lab_meter_forward_tp(?, ?)}";
		return runCall(sql, techPoint, printgroup);
	}

	@Override
	public SelectResult<TechTimeline> loadTimeLine(int process){
		String sql="SELECT ? tech_process, os.id tech_type, os.name tech_type_name, os.book_part, bp.name book_part_name, tt.oper_time, tt.pass_time"+
					 " FROM order_state os"+
					   " INNER JOIN book_part bp ON bp.id = os.book_part"+
					   " LEFT OUTER JOIN tech_timeline tt ON tt.tech_process = ? AND tt.tech_type = os.id"+
					  " WHERE os.tech != 0"+
					  " ORDER BY os.id";
		return runSelect(TechTimeline.class, sql, process, process);
	}
	@Override
	public SqlResult persistTimeLineBatch(List<TechTimeline> items){
		return runInesrtUpdateBatch(items);
	}

}
