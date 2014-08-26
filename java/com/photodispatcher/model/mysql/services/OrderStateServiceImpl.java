package com.photodispatcher.model.mysql.services;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.OrderState;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;
import com.photodispatcher.model.mysql.entities.StateLog;

@Service("orderStateService")
public class OrderStateServiceImpl extends AbstractDAO implements OrderStateService {

	@Override
	public SelectResult<OrderState> loadAll(){
		SelectResult<OrderState> result;
		String sql="SELECT * FROM phcconfig.order_state ORDER BY id";
		result=runSelect(OrderState.class, sql);
		return result;
	}
	
	@Override
	public SqlResult logState(StateLog item){
		return runInsert(item);
	}

	@Override
	public SqlResult logStateByPGroup(String pgId, int state, String comment){
		//PROCEDURE phcdata.logStateByPg(IN pPgId VARCHAR(50), IN pSate int, IN pMsg VARCHAR(250))
		String sql= "{CALL phcdata.logStateByPg(?,?,?)}";
		return runCall(sql, pgId, state, comment.substring(0, Math.max(250, comment.length())));
	}

	@Override
	public SelectResult<StateLog> loadStateLogs(Date from, boolean onlyErrors){
		String sql="SELECT sl.*, os.name state_name"+
					" FROM phcdata.state_log sl"+
					 " INNER JOIN phcconfig.order_state os ON sl.state = os.id"+
					" WHERE sl.state_date BETWEEN DATE(?) AND DATE_ADD(DATE(?), INTERVAL 1 DAY)";
		if (onlyErrors) sql+=" AND sl.state < 0";
		return runSelect(StateLog.class, sql, from, from);
	}

	/*
	@Override
	public SqlResult extraStateStart(String orderId, String subId, int state){
		//PROCEDURE phcdata.extraStateStart(IN pOrder VARCHAR(50), IN pSubOrder VARCHAR(50), IN pState INT)
		String sql= "{CALL phcdata.extraStateStart(?,?,?)}";
		return runCall(sql, orderId, subId, state);
	}

	@Override
	public SqlResult extraStateSet(String orderId, String subId, int state){
		//PROCEDURE phcdata.extraStateSet(IN pOrder VARCHAR(50), IN pSubOrder VARCHAR(50), IN pState INT)
		String sql= "{CALL phcdata.extraStateSet(?,?,?)}";
		return runCall(sql, orderId, subId, state);
	}

	@Override
	public SqlResult extraStateSetByPGroup(String pgId, int state){
		//PROCEDURE phcdata.extraStateSetByPGroup(IN pPrintGroup VARCHAR(50), IN pState INT)
		String sql= "{CALL phcdata.extraStateSetByPGroup(?,?)}";
		return runCall(sql, pgId, state);
	}
	*/

	@Override
	public SqlResult extraStateReset(String orderId, String subId, int state){
		//PROCEDURE phcdata.extraStateReset(IN pOrder VARCHAR(50), IN pSubOrder VARCHAR(50), IN pState int)
		String sql= "{CALL phcdata.extraStateReset(?,?,?)}";
		return runCall(sql, orderId, subId, state);
	}

	@Override
	public SqlResult extraStateProlong(String orderId, String subId, int state, String comment){
		//PROCEDURE phcdata.extraStateProlong(IN pOrder VARCHAR(50), IN pSubOrder VARCHAR(50), IN pState int, IN pComment VARCHAR(250))
		String sql= "{CALL phcdata.extraStateReset(?,?,?,?)}";
		return runCall(sql, orderId, subId, state, comment);
	}

	@Override
	public SqlResult printPost(String pgId, int lab){
		//PROCEDURE phcdata.printStateStart(IN pPgroupId VARCHAR(50), IN lab int)
		String sql= "{CALL phcdata.printStateStart(?,?)}";
		return runCall(sql, pgId, lab);
	}

	@Override
	public SqlResult printEndManual(String[] pgIds){
		SqlResult result= new SqlResult();
		//PROCEDURE phcdata.printStateEnd(IN pPgroupId VARCHAR(50))
		String sql= "{CALL phcdata.printStateEnd(?)}";
		for (String pgId:pgIds){
			result=runCall(sql, pgId);
			if(!result.isComplete()) return result;
		}
		return result;
	}

	@Override
	public SqlResult printCancel(String[] pgIds){
		SqlResult result= new SqlResult();
		//PROCEDURE phcdata.printStateCancel(IN pPgroupId VARCHAR(50))
		String sql= "{CALL phcdata.printStateCancel(?)}";
		for (String pgId:pgIds){
			result=runCall(sql, pgId);
			if(!result.isComplete()) return result;
		}
		return result;
	}

}
