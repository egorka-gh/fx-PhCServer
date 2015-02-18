package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.BookPgTemplate;
import com.photodispatcher.model.mysql.entities.BookSynonym;
import com.photodispatcher.model.mysql.entities.SelectResult;
import com.photodispatcher.model.mysql.entities.SqlResult;

@Service("bookSynonymService")
public class BookSynonymServiceImpl extends AbstractDAO implements BookSynonymService {
	
	@Override
	public SelectResult<BookSynonym> loadFull(){
		SelectResult<BookSynonym> result;
		//exclude deleted (synonym_type=-1)
		String sql="SELECT l.* FROM book_synonym l " +
				" WHERE l.synonym_type!=-1";
		result=runSelect(BookSynonym.class, sql);
		if (result.isComplete()){
			//load childs
			for (BookSynonym item : result.getData()){
				sql="SELECT t.*"+
					" FROM book_pg_template t"+
					" WHERE t.book=?";
				SelectResult<BookPgTemplate> childs=runSelect(BookPgTemplate.class, sql, item.getId());
				if(childs.isComplete()){
					item.setTemplates(childs.getData());
				}else{
					result.cloneError(childs);
					break;
				}
			}
		}
		return result;
	}

	@Override
	public SelectResult<BookSynonym> loadAll(int src_type, int contentFilter){
		SelectResult<BookSynonym> result;
		String sql;
		if(contentFilter==0){
			sql="SELECT l.*, st.name src_type_name, bt.name book_type_name, 1 is_allow, bst.name synonym_type_name"+
				" FROM book_synonym l"+
				" INNER JOIN src_type st ON l.src_type = st.id " +
				" INNER JOIN book_type bt ON l.book_type = bt.id"+
				" INNER JOIN book_synonym_type bst ON l.synonym_type = bst.id"+
				" WHERE l.src_type = ?"+
				" ORDER BY l.synonym";
			result=runSelect(BookSynonym.class, sql, src_type);
		}else{
			sql="SELECT l.*, st.name src_type_name, bt.name book_type_name, ifnull(fa.alias,0) is_allow"+
				" FROM book_synonym l" +
				" INNER JOIN src_type st ON l.src_type = st.id"+
				" INNER JOIN book_type bt ON l.book_type = bt.id"+
				" LEFT OUTER JOIN content_filter_alias fa ON fa.filter= ? AND l.id=fa.alias"+
				" WHERE l.src_type = ?"+
				" ORDER BY l.synonym";
			result=runSelect(BookSynonym.class, sql, contentFilter, src_type);
		}
		return result;
	}

	@Override
	public SelectResult<BookPgTemplate> loadTemplates(int book){
		SelectResult<BookPgTemplate> result;
		String sql="SELECT pg.*, p.value paper_name, fr.value frame_name, cr.value correction_name, cu.value cutting_name, bp.name book_part_name, st1.name lab_type_name"+
					" FROM book_pg_template pg"+
					" INNER JOIN attr_value p ON pg.paper = p.id"+
					" INNER JOIN attr_value fr ON pg.frame = fr.id"+
					" INNER JOIN attr_value cr ON pg.correction = cr.id"+
					" INNER JOIN attr_value cu ON pg.cutting = cu.id"+
					" INNER JOIN book_part bp ON pg.book_part = bp.id"+
					" INNER JOIN src_type st1 ON st1.id=pg.lab_type "+
					" WHERE pg.book=?";
		result=runSelect(BookPgTemplate.class, sql, book);
		return result;
	}

	@Override
	public SqlResult clone(int pId){
		SqlResult result= new SqlResult();
		//PROCEDURE phcconfig.bookSynonymClone(IN pId int)
		String sql= "{CALL bookSynonymClone(?)}";
		SelectResult<BookSynonym> subResult=runCallSelect(BookSynonym.class, sql, pId);
		if(!subResult.isComplete()){
			result.cloneError(subResult);
		}else{
			if(subResult.getData()!=null && !subResult.getData().isEmpty()){
				result.setResultCode(subResult.getData().get(0).getId());
			}
		}
		return result;
	}

	@Override
	public SqlResult persistBatch(List<BookSynonym> items){
		SqlResult result=new SqlResult();
		List<BookSynonym> insertList=new ArrayList<BookSynonym>();
		List<BookSynonym> updateList=new ArrayList<BookSynonym>();
		List<BookPgTemplate> insertChildList=new ArrayList<BookPgTemplate>();
		List<BookPgTemplate> updateChildList=new ArrayList<BookPgTemplate>();

		for(BookSynonym item : items){
			if(item.getPersistState()==0){
				insertList.add(item);
			}else if(item.getPersistState()==-1){
				updateList.add(item);
			}
			//childs
			if(item.getPersistState()!=0 && item.getTemplates()!=null){
				for(BookPgTemplate child : item.getTemplates()){
					if(child.getPersistState()==0){
						insertChildList.add(child);
					}else if(child.getPersistState()==-1){
						updateChildList.add(child);
					}
				}
			}
		}
		if(!insertList.isEmpty()){
			result=runInsertBatch(insertList);
		}
		if(result.isComplete() && !updateList.isEmpty()){
			result=runUpdateBatch(updateList);
		}
		if(result.isComplete() && !insertChildList.isEmpty()){
			result=runInsertBatch(insertChildList);
		}
		if(result.isComplete() && !updateChildList.isEmpty()){
			result=runUpdateBatch(updateChildList);
		}
		return result;
	}

}
