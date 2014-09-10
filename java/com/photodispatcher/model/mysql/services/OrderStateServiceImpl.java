package com.photodispatcher.model.mysql.services;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.FieldValue;
import com.photodispatcher.model.mysql.entities.OrderExtraState;
import com.photodispatcher.model.mysql.entities.OrderState;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.StateLog;

@Service("orderStateService")
public class OrderStateServiceImpl extends AbstractDAO implements OrderStateService {

	@Override
	public SelectResult<OrderState> loadAll(){
		SelectResult<OrderState> result;
		String sql="SELECT * FROM order_state ORDER BY id";
		result=runSelect(OrderState.class, sql);
		return result;
	}
	
	@Override
	public SqlResult logState(StateLog item){
		return runInsert(item);
	}

	@Override
	public SqlResult logStateByPGroup(String pgId, int state, String comment){
		//PROCEDURE logStateByPg(IN pPgId VARCHAR(50), IN pSate int, IN pMsg VARCHAR(250))
		String sql= "{CALL logStateByPg(?,?,?)}";
		return runCall(sql, pgId, state, comment.substring(0, Math.min(250, comment.length())));
	}

	@Override
	public SelectResult<StateLog> loadStateLogs(Date from, boolean onlyErrors){
		String sql="SELECT sl.*, os.name state_name"+
					" FROM state_log sl"+
					 " INNER JOIN order_state os ON sl.state = os.id"+
					" WHERE sl.state_date BETWEEN DATE(?) AND DATE_ADD(DATE(?), INTERVAL 1 DAY)";
		if (onlyErrors) sql+=" AND sl.state < 0";
		return runSelect(StateLog.class, sql, from, from);
	}

	@Override
	public SqlResult extraStateStart(String orderId, String subId, int state, Date date){
		//PROCEDURE extraStateStart(IN pOrder varchar(50), IN pSubOrder varchar(50), IN pState int, IN pDate datetime)
		String sql= "{CALL extraStateStart(?,?,?,?)}";
		return runCall(sql, orderId, subId, state, date);
	}

	@Override
	public SqlResult extraStateSet(String orderId, String subId, int state, Date date){
		//PROCEDURE extraStateSet(IN pOrder varchar(50), IN pSubOrder varchar(50), IN pState int, IN pDate datetime)
		String sql= "{CALL extraStateSet(?,?,?,?)}";
		return runCall(sql, orderId, subId, state, date);
	}

	/*

	@Override
	public SqlResult extraStateSetByPGroup(String pgId, int state){
		//PROCEDURE extraStateSetByPGroup(IN pPrintGroup VARCHAR(50), IN pState INT)
		String sql= "{CALL extraStateSetByPGroup(?,?)}";
		return runCall(sql, pgId, state);
	}
	*/

	@Override
	public SqlResult extraStateReset(String orderId, String subId, int state){
		//PROCEDURE extraStateReset(IN pOrder VARCHAR(50), IN pSubOrder VARCHAR(50), IN pState int)
		String sql= "{CALL extraStateReset(?,?,?)}";
		return runCall(sql, orderId, subId, state);
	}

	@Override
	public SqlResult extraStateProlong(String orderId, String subId, int state, String comment){
		//PROCEDURE extraStateProlong(IN pOrder VARCHAR(50), IN pSubOrder VARCHAR(50), IN pState int, IN pComment VARCHAR(250))
		String sql= "{CALL extraStateReset(?,?,?,?)}";
		return runCall(sql, orderId, subId, state, comment);
	}

	@Override
	public SqlResult printPost(String pgId, int lab){
		//PROCEDURE printStateStart(IN pPgroupId VARCHAR(50), IN lab int)
		String sql= "{CALL printStateStart(?,?)}";
		return runCall(sql, pgId, lab);
	}

	@Override
	public SqlResult printEndManual(String[] pgIds){
		SqlResult result= new SqlResult();
		//PROCEDURE printStateEnd(IN pPgroupId VARCHAR(50))
		String sql= "{CALL printStateEnd(?)}";
		for (String pgId:pgIds){
			result=runCall(sql, pgId);
			if(!result.isComplete()) return result;
		}
		return result;
	}

	@Override
	public SqlResult printCancel(String[] pgIds){
		SqlResult result= new SqlResult();
		//PROCEDURE printStateCancel(IN pPgroupId VARCHAR(50))
		String sql= "{CALL printStateCancel(?)}";
		for (String pgId:pgIds){
			result=runCall(sql, pgId);
			if(!result.isComplete()) return result;
		}
		return result;
	}

	@Override
	public SqlResult printGroupMarkInPrint(String pgId){
		//PROCEDURE printMarkInPrint(IN pPgroupId VARCHAR(50))
		String sql= "{CALL printMarkInPrint(?)}";
		return runCall(sql, pgId);
	}

	@Override
	public SelectResult<OrderExtraState> loadMonitorEState(int startState, int techState, int endState){
		String sql="SELECT es.id, es.sub_id, es.state, es.state_date, os.name state_name,"+ 
						" (CASE WHEN es2.id IS NULL THEN 1000 WHEN es2.state_date IS NULL THEN 900 ELSE es2.state END) state2,"+ 
						" es2.start_date start_date2, es2.state_date state_date2, os2.name state_name2"+
					" FROM order_extra_state es"+
					  " INNER JOIN order_state os ON os.id = es.state"+
					  " LEFT OUTER JOIN order_extra_state es2 ON es.id = es2.id AND es2.state = ?"+
					  " LEFT OUTER JOIN order_state os2 ON os2.id = es2.state"+
					  " LEFT OUTER JOIN order_extra_state es3 ON es.id = es3.id AND es3.state = ?"+
					" WHERE es.state = ? AND es3.state_date IS NULL"+
					" ORDER BY (CASE WHEN es2.id IS NULL THEN 1000 WHEN es2.state_date IS NULL THEN 900 ELSE es2.state END), es.state_date";
		return runSelect(OrderExtraState.class, sql, endState, techState, startState);
	}

	
	@Override
	public SqlResult extraStateStartMonitor(String orderId, String subId, int stateStart, int stateStop){
		int resultCode=0;
		SqlResult result= new SqlResult();
		String sql="SELECT IFNULL(MAX(IF(state = ?, 1, IF(state_date IS NOT NULL, 2, 1))), 0) AS value"+
					" FROM order_extra_state"+
					" WHERE id = ? AND sub_id = ? AND state IN (?, ?)";
		SelectResult<FieldValue> subres=runSelect(FieldValue.class, sql, stateStop, orderId, subId, stateStart, stateStop);
		if(!subres.isComplete()){
			result.cloneError(subres);
			return result;
		}
		if(subres.getData()!= null && !subres.getData().isEmpty()) resultCode=subres.getData().get(0).getValue();
		if(resultCode>0){
			result.setResultCode(resultCode);
			return result;
		}
		Date date= new Date();
		//add extra states
		if(stateStop!=0){
			//stop previous
			result=extraStateSet(orderId, subId, stateStop, date);
			if(!result.isComplete()) return result;
		}
		//start new
		result= extraStateStart(orderId, subId, stateStart, date);
		return result;
	}

	@Override
	public SqlResult extraStateStartOTK(String orderId, String subId, int stateStart){
		int resultCode=0;
		SqlResult result= new SqlResult();
		String sql="SELECT IFNULL(MAX(IF(state_date IS NOT NULL, 2, 1)), 0) AS value"+
					" FROM order_extra_state"+
					" WHERE id = ? AND sub_id = ? AND state = ?)";
		SelectResult<FieldValue> subres=runSelect(FieldValue.class, sql, orderId, subId, stateStart);
		if(!subres.isComplete()){
			result.cloneError(subres);
			return result;
		}
		if(subres.getData()!= null && !subres.getData().isEmpty()) resultCode=subres.getData().get(0).getValue();
		if(resultCode>0){
			result.setResultCode(resultCode);
			return result;
		}
		Date date= new Date();
		//start 
		result= extraStateStart(orderId, subId, stateStart, date);
		return result;
	}

}
