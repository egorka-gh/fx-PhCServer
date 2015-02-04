package com.photodispatcher.model.mysql.services;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sansorm.SqlClosureElf;
import org.sansorm.internal.OrmWriter;
import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.ConnectionFactory;
import com.photodispatcher.model.mysql.entities.PrintGroup;
import com.photodispatcher.model.mysql.entities.PrintGroupFile;
import com.photodispatcher.model.mysql.entities.SelectResult;

@Service("printGroupService")
public class PrintGroupServiceImpl extends AbstractDAO implements PrintGroupService {
	
	@Override
	public SelectResult<PrintGroup> loadByState(int stateFrom, int stateTo){
		String sql="SELECT pg.*, o.source source_id, s.name source_name, o.ftp_folder order_folder, os.name state_name,"+
						" p.value paper_name, fr.value frame_name, cr.value correction_name, cu.value cutting_name,"+
						" lab.name lab_name, bt.name book_type_name, bp.name book_part_name"+
					" FROM print_group pg"+
						" INNER JOIN orders o ON pg.order_id = o.id"+
						" INNER JOIN sources s ON o.source = s.id"+
						" INNER JOIN order_state os ON pg.state = os.id"+
						" INNER JOIN attr_value p ON pg.paper = p.id"+
						" INNER JOIN attr_value fr ON pg.frame = fr.id"+
						" INNER JOIN attr_value cr ON pg.correction = cr.id"+
						" INNER JOIN attr_value cu ON pg.cutting = cu.id"+
						" INNER JOIN book_type bt ON pg.book_type = bt.id"+
						" INNER JOIN book_part bp ON pg.book_part = bp.id"+
						" LEFT OUTER JOIN lab lab ON pg.destination = lab.id";
		String  where="";
		if(stateFrom!=-1){
			where+=" pg.state>=?";
		}
		if(stateTo!=-1){
			if(where.length()>0) where+=" AND";
			where+=" pg.state<?";
		}
		if(where.length()>0) where=" WHERE"+where;
		sql+=where;
		sql+=" ORDER BY pg.state_date";

		if(stateFrom==-1 && stateTo==-1){
			return runSelect(PrintGroup.class,sql);
		}else if(stateFrom!=-1 && stateTo==-1){
			return runSelect(PrintGroup.class,sql,stateFrom);
		}else if(stateFrom==-1 && stateTo!=-1){
			return runSelect(PrintGroup.class,sql,stateTo);
		}
		return runSelect(PrintGroup.class, sql, stateFrom, stateTo);
	}

	@Override
	public SelectResult<PrintGroup> loadByOrderState(int stateFrom, int stateTo){
		String sql="SELECT pg.*, o.source source_id, s.name source_name, o.ftp_folder order_folder, os.name state_name,"+
						" p.value paper_name, fr.value frame_name, cr.value correction_name, cu.value cutting_name,"+
						" lab.name lab_name, bt.name book_type_name, bp.name book_part_name"+
					" FROM print_group pg"+
						" INNER JOIN orders o ON pg.order_id = o.id"+
						" INNER JOIN sources s ON o.source = s.id"+
						" INNER JOIN order_state os ON pg.state = os.id"+
						" INNER JOIN attr_value p ON pg.paper = p.id"+
						" INNER JOIN attr_value fr ON pg.frame = fr.id"+
						" INNER JOIN attr_value cr ON pg.correction = cr.id"+
						" INNER JOIN attr_value cu ON pg.cutting = cu.id"+
						" INNER JOIN book_type bt ON pg.book_type = bt.id"+
						" INNER JOIN book_part bp ON pg.book_part = bp.id"+
						" LEFT OUTER JOIN lab lab ON pg.destination = lab.id";
		String  where="";
		if(stateFrom!=-1){
			where+=" o.state>=?";
		}
		if(stateTo!=-1){
			if(where.length()>0) where+=" AND";
			where+=" o.state<?";
		}
		if(where.length()>0) where=" WHERE"+where;
		sql+=where;
		sql+=" ORDER BY pg.state_date";

		if(stateFrom==-1 && stateTo==-1){
			return runSelect(PrintGroup.class,sql);
		}else if(stateFrom!=-1 && stateTo==-1){
			return runSelect(PrintGroup.class,sql,stateFrom);
		}else if(stateFrom==-1 && stateTo!=-1){
			return runSelect(PrintGroup.class,sql,stateTo);
		}
		return runSelect(PrintGroup.class, sql, stateFrom, stateTo);
	}

	@Override
	public SelectResult<PrintGroup> loadInPrint(int lab){
		
		String sql="SELECT pg.*, "+
				" COUNT(DISTINCT tl.sheet) prints_done"+
				" FROM print_group pg"+
					" INNER JOIN orders o ON pg.order_id = o.id"+
					" INNER JOIN sources s ON o.source = s.id"+
					" INNER JOIN order_state os ON pg.state = os.id"+
					" LEFT OUTER JOIN tech_log tl ON pg.id = tl.print_group AND tl.sheet!=0"+
					" LEFT OUTER JOIN tech_point tp ON tl.src_id=tp.id AND tp.tech_type=300"+
				" WHERE pg.state=250 AND o.state<450 AND (?=0 OR pg.destination=?) AND pg.book_type !=0"+
				" GROUP BY pg.id"+
				" ORDER BY pg.destination, pg.state_date";
		
		return runSelect(PrintGroup.class, sql, lab, lab);
		
	}

	@Override
	public SelectResult<PrintGroup> loadPrinted(Date after){
		String  where="";
		if(after != null){
			where+=" AND o.state_date>=?";
		}

		String sql="SELECT pg.*, o.source source_id, s.name source_name, o.ftp_folder order_folder, os.name state_name,"+
						" p.value paper_name, fr.value frame_name, cr.value correction_name, cu.value cutting_name,"+
						" lab.name lab_name, bt.name book_type_name, bp.name book_part_name"+
					" FROM print_group pg"+
						" INNER JOIN orders o ON pg.order_id = o.id"+
						" INNER JOIN sources s ON o.source = s.id"+
						" INNER JOIN order_state os ON pg.state = os.id"+
						" INNER JOIN attr_value p ON pg.paper = p.id"+
						" INNER JOIN attr_value fr ON pg.frame = fr.id"+
						" INNER JOIN attr_value cr ON pg.correction = cr.id"+
						" INNER JOIN attr_value cu ON pg.cutting = cu.id"+
						" INNER JOIN book_type bt ON pg.book_type = bt.id"+
						" INNER JOIN book_part bp ON pg.book_part = bp.id"+
						" LEFT OUTER JOIN lab lab ON pg.destination = lab.id"+
					" WHERE o.state>=?" + where +
					" ORDER BY pg.state_date";

		if(after != null){
			return runSelect(PrintGroup.class,sql, 300, after );
		}
		return runSelect(PrintGroup.class, sql, 300);
	}

	@Override
	public SelectResult<PrintGroup> loadPrintPost(List<String> ids){
		SelectResult<PrintGroup> result= new SelectResult<PrintGroup>();
		result.setData(new ArrayList<PrintGroup>());
		SelectResult<PrintGroup> subResult;
		List<String> inList= new ArrayList<String>();
		StringBuilder in= new StringBuilder("");
		for(String id : ids){
			if(in.length()>200){
				inList.add(in.toString());
				in= new StringBuilder("");
			}
			if(in.length()>0) in.append(",");
			in.append("'"); in.append(id); in.append("'");
		}
		if(in.length()>0) inList.add(in.toString());
		//load print groups
		String sql="SELECT pg.id, pg.state FROM print_group pg WHERE pg.id";
		for (String where :inList){
			subResult=runSelect(PrintGroup.class,sql+" IN ("+where+")");
			if(subResult.isComplete()){
				result.getData().addAll(subResult.getData());
			}else{
				result.cloneError(subResult);
				return result;
			}
		}
		//load print files
		//sql="SELECT pgf.* FROM print_group_file pgf WHERE pgf.print_group = ?";
		for (PrintGroup pg : result.getData()){
			SelectResult<PrintGroupFile> fRes=loadFiles(pg.getId());
			if(!fRes.isComplete()){
				result.cloneError(fRes);
				return result;
			}
			pg.setFiles(fRes.getData());
		}

		return result;
	}
	
	private SelectResult<PrintGroupFile> loadFiles(String pgId){
		//load print files
		String sql="SELECT pgf.* FROM print_group_file pgf WHERE pgf.print_group = ?";
		return runSelect(PrintGroupFile.class,sql, pgId);
	}

	@Override
	public SelectResult<PrintGroup> capturePrintState(List<PrintGroup> printGroups, boolean loadFiles){
		SelectResult<PrintGroup> result= new SelectResult<PrintGroup>();
		result.setData(new ArrayList<PrintGroup>());
		
		Connection connection = null;
		String sqlUpdate="UPDATE print_group pg"+
						" SET pg.state = ?, pg.state_date = ?"+
						" WHERE pg.id = ? AND pg.state < ?";
		boolean updated=false;
		for(PrintGroup pg : printGroups){
			updated=false;
			try {
				connection=ConnectionFactory.getConnection();
				updated=OrmWriter.executeUpdate(connection, sqlUpdate, pg.getState(), pg.getState_date(), pg.getId(), pg.getState())>0;
			} catch (SQLException e) {
				updated=false;
			}finally{
				SqlClosureElf.quietClose(connection);
			}
			if(!updated){
				pg.setState(-300);
				pg.setFiles(null);
			}else if(loadFiles){
				SelectResult<PrintGroupFile> fRes=loadFiles(pg.getId());
				if(!fRes.isComplete()){
					pg.setState(-309);
					pg.setFiles(null);
				}else{
					pg.setFiles(fRes.getData());
				}
			}
			result.getData().add(pg);
		}
	
		return result;
	}

}
