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
import com.photodispatcher.model.mysql.entities.DmlResult;
import com.photodispatcher.model.mysql.entities.PrintGroup;
import com.photodispatcher.model.mysql.entities.PrintGroupFile;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@Service("printGroupService")
public class PrintGroupServiceImpl extends AbstractDAO implements PrintGroupService {

	@Override
	public SelectResult<PrintGroup> loadById(String id){
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
					" WHERE pg.id=?";
		return runSelect(PrintGroup.class, sql, id);
	}

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
	public SelectResult<PrintGroup> loadInPrint(List<Integer> labIds){
		StringBuilder sb=new StringBuilder("");
		String sIn="";
		if(labIds!=null && !labIds.isEmpty()){
			sb.append(" AND pg.destination IN(");
			for(Integer id : labIds){
				sb.append(id).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");
			sIn=sb.toString();
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
				" INNER JOIN lab lab ON pg.destination = lab.id"+
				" WHERE pg.state IN ( 250, 255) AND o.state<450 "+ sIn+
				" ORDER BY pg.destination, pg.state_date";

		return runSelect(PrintGroup.class, sql);
	}

	
	@Override
	public SelectResult<PrintGroup> loadReady4Print(int limit, boolean onlyBook){
		String sql="SELECT pg.*, o.source source_id, o.ftp_folder order_folder, IFNULL(s.alias, pg.path) alias, os.name state_name, av.value paper_name"+
					 " FROM print_group pg"+
					   " INNER JOIN orders o ON pg.order_id = o.id"+
					   " INNER JOIN order_state os ON pg.state = os.id"+
					   " INNER JOIN attr_value av ON pg.paper = av.id"+
					   " LEFT OUTER JOIN suborders s ON s.order_id = pg.order_id AND s.sub_id = pg.sub_id"+
					  " WHERE pg.state = 200";
		if(onlyBook){
			sql+=" AND pg.book_type IN (1,2,3)";
		}
		sql+=" ORDER BY pg.state_date";
					//LIMIT 20";
		if(limit>0){
			sql=sql +" LIMIT "+Integer.toString(limit);
		}

		return runSelect(PrintGroup.class, sql);
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
	public SelectResult<PrintGroup> loadInPrintPost(List<Integer> labIds){
		StringBuilder sb=new StringBuilder("");
		String sIn="";
		if(labIds!=null && !labIds.isEmpty()){
			sb.append(" AND pg.destination IN(");
			for(Integer id : labIds){
				sb.append(id).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");
			sIn=sb.toString();
		}

		String sql="SELECT pg.*, l.name lab_name, av.value paper_name, os.name state_name"+
				" FROM print_group pg"+
					" INNER JOIN orders o ON pg.order_id = o.id"+
					" INNER JOIN order_state os ON pg.state = os.id"+
					" INNER JOIN lab l ON l.id = pg.destination"+
					" INNER JOIN attr_value av ON pg.paper = av.id"+
				" WHERE pg.state>=203 AND pg.state<=250 AND o.state<450 AND pg.book_type IN (1, 2, 3)"+ sIn+
				" ORDER BY pg.destination, pg.state_date";
		
		return runSelect(PrintGroup.class, sql);
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
						" SET pg.state = ?, pg.state_date = ?, pg.destination=?"+
						" WHERE pg.id = ? AND pg.state < ?";
		boolean updated=false;
		for(PrintGroup pg : printGroups){
			updated=false;
			try {
				connection=ConnectionFactory.getConnection();
				updated=OrmWriter.executeUpdate(connection, sqlUpdate, pg.getState(), pg.getState_date(), pg.getDestination(), pg.getId(), pg.getState())>0;
			} catch (SQLException e) {
				updated=false;
			}finally{
				SqlClosureElf.quietClose(connection);
			}
			if(!updated){
				pg.setState(-300);
				pg.setFiles(null);
			}else{
				//update lab meter
				LabServiceImpl ls= new LabServiceImpl();
				ls.forwardLabMeter(pg.getDestination(), pg.getState(),pg.getId());
				
				//reload print group
				String sql="SELECT pg.*, o.source source_id, o.ftp_folder order_folder, IFNULL(s.alias,pg.path) alias"+
							" FROM print_group pg"+
							" INNER JOIN orders o ON pg.order_id = o.id"+
							" LEFT OUTER JOIN suborders s ON pg.order_id = s.order_id AND pg.sub_id = s.sub_id"+
							" WHERE pg.id = ?";
				SelectResult<PrintGroup> selRes=runSelect(PrintGroup.class, sql, pg.getId());
				if(!selRes.isComplete() || selRes.getData()==null || selRes.getData().isEmpty()){
					pg.setState(-309);
					pg.setFiles(null);
				}else{
					pg=selRes.getData().get(0);
					if(loadFiles){
						SelectResult<PrintGroupFile> fRes=loadFiles(pg.getId());
						if(!fRes.isComplete()){
							pg.setState(-309);
							pg.setFiles(null);
						}else{
							pg.setFiles(fRes.getData());
						}
					}
				}
			}
			result.getData().add(pg);
		}
	
		return result;
	}

	@Override
	public DmlResult<PrintGroup> fillCaptured(String id){
		
		DmlResult<PrintGroup> res= new DmlResult<PrintGroup>(); //getObject(PrintGroup.class, id); 
		String sql="SELECT pg.*, o.source source_id, o.ftp_folder order_folder, IFNULL(s.alias,pg.path) alias"+
				" FROM print_group pg"+
				" INNER JOIN orders o ON pg.order_id = o.id"+
				" LEFT OUTER JOIN suborders s ON pg.order_id = s.order_id AND pg.sub_id = s.sub_id"+
				" WHERE pg.id = ?";
		SelectResult<PrintGroup> selRes=runSelect(PrintGroup.class, sql, id);
		
		if(!selRes.isComplete()){
			res.cloneError(selRes);
			return res;
		}
		if(selRes.getData()==null || selRes.getData().isEmpty()){
			res.setComplete(false);
			res.setErrMesage("Группа печати не найдена");
			return res;
		}
		
		res.setItem(selRes.getData().get(0));
		
		if(res.getItem().getState()!=203){
			//wrong state
			res.getItem().setState(-300);
			return res;
		}
		
		SelectResult<PrintGroupFile> fRes=loadFiles(id);
		if(!fRes.isComplete()){
			res.cloneError(fRes);
			res.getItem().setState(-309);
		}else{
			res.getItem().setFiles(fRes.getData());
			if (fRes.getData()==null || fRes.getData().isEmpty()){
				//empty file list
				res.getItem().setState(-300);
				//persists error
				String sqlUpdate="UPDATE print_group pg"+
									" SET pg.state = ?, pg.state_date = ?"+
									" WHERE pg.id = ?";
				SqlResult uRes=runDML(sqlUpdate,-300, new Date(), id);
				if(!uRes.isComplete()){
					res.cloneError(uRes);
				}
			}
		}
		return res;
	}

	@Override
	public SelectResult<PrintGroup> loadPrintPostByDev(List<Integer> devices, int loadPhoto){
		StringBuilder in= new StringBuilder("");
		for(Integer dev : devices){
			in.append(dev).append(",");
		}
		String devLst=in.toString();
		//load print groups
		//printLoad4PrintByDev(IN p_devlst TEXT, IN p_photo INT)
		String sql= "{CALL printLoad4PrintByDev(?,?)}";
		return runCallSelect(PrintGroup.class, sql, devLst, loadPhoto);
	}

}
