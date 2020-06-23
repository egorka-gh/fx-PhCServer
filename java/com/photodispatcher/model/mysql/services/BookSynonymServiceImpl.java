package com.photodispatcher.model.mysql.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.photodispatcher.model.mysql.entities.BookPgAltPaper;
import com.photodispatcher.model.mysql.entities.BookPgTemplate;
import com.photodispatcher.model.mysql.entities.BookSynonym;
import com.photodispatcher.model.mysql.entities.BookSynonymCompo;
import com.photodispatcher.model.mysql.entities.BookSynonymGlue;
import com.photodispatcher.model.mysql.entities.GlueCommand;
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
		if (!result.isComplete()) return result;
	
		//load childs
		for (BookSynonym item : result.getData()){
			//load compo
			SelectResult<BookSynonymCompo> compos=loadCompo(item.getId());
			if(!compos.isComplete()){
				result.cloneError(compos);
				break;
			}
			item.setCompos(compos.getData());
			
			//templates
			SelectResult<BookPgTemplate> childs=loadTemplates(item.getId());
			if(!childs.isComplete()){
				result.cloneError(childs);
				break;
			}
			item.setTemplates(childs.getData());
			
			for (BookPgTemplate pgt : item.getTemplates()){
				pgt.setCompo_type(item.getCompo_type());
			}

			//load glues
			SelectResult<BookSynonymGlue> glues=loadGlue(item.getId());
			if(glues.isComplete()){
				item.setGlueCommands(glues.getData());
			}else{
				result.cloneError(glues);
				break;
			}
		}
		return result;
	}

	@Override
	public SelectResult<BookSynonym> loadAll(int src_type, int contentFilter){
		SelectResult<BookSynonym> result;
		String sql;
		if(contentFilter==0){
			sql="SELECT l.*, st.name src_type_name, bt.name book_type_name, 1 is_allow, bst.name synonym_type_name, op.name order_program_name, ct.name compo_type_name"+
				" FROM book_synonym l"+
				" INNER JOIN src_type st ON l.src_type = st.id " +
				" INNER JOIN book_type bt ON l.book_type = bt.id"+
				" INNER JOIN book_synonym_type bst ON l.synonym_type = bst.id"+
				" INNER JOIN order_program op ON l.order_program=op.id"+
				" INNER JOIN compo_type ct ON l.compo_type = ct.id"+
				" WHERE l.src_type = ?"+
				" ORDER BY l.synonym";
			result=runSelect(BookSynonym.class, sql, src_type);
		}else{
			sql="SELECT l.*, st.name src_type_name, bt.name book_type_name, ifnull(fa.alias,0) is_allow, op.name order_program_name, ct.name compo_type_name"+
				" FROM book_synonym l" +
				" INNER JOIN src_type st ON l.src_type = st.id"+
				" INNER JOIN book_type bt ON l.book_type = bt.id"+
				" INNER JOIN order_program op ON l.order_program=op.id"+
				" INNER JOIN compo_type ct ON l.compo_type = ct.id"+
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
		String sql="SELECT pg.*, p.value paper_name, fr.value frame_name, cr.value correction_name, cu.value cutting_name, la.value laminat_name, bp.name book_part_name, st1.name lab_type_name"+
					" FROM book_pg_template pg"+
					" INNER JOIN attr_value p ON pg.paper = p.id"+
					" INNER JOIN attr_value fr ON pg.frame = fr.id"+
					" INNER JOIN attr_value cr ON pg.correction = cr.id"+
					" INNER JOIN attr_value cu ON pg.cutting = cu.id"+
					" INNER JOIN attr_value la ON pg.laminat = la.id"+
					" INNER JOIN book_part bp ON pg.book_part = bp.id"+
					" INNER JOIN src_type st1 ON st1.id=pg.lab_type "+
					" WHERE pg.book=?";
		result=runSelect(BookPgTemplate.class, sql, book);
		if(result.isComplete()){
			for (BookPgTemplate item : result.getData()){
				SelectResult<BookPgAltPaper> subRes=loadAltPaper(item.getId());
				if(subRes.isComplete()){
					item.setAltPaper(subRes.getData());
				}else{
					result.cloneError(subRes);
					break;
				}
			}
		}
		return result;
	}

	@Override
	public SelectResult<BookSynonymCompo> loadCompo(int book){
		String sql="SELECT bsc.*, bs.synonym AS child_alias" +
					 " FROM book_synonym_compo bsc"+
					   " INNER JOIN book_synonym bs ON bsc.child = bs.id"+
					 " WHERE bsc.parent = ?";
		return runSelect(BookSynonymCompo.class, sql, book);
	}

	private SelectResult<BookPgAltPaper> loadAltPaper(int template){
		String sql="SELECT ap.*, av.value paper_name, l.name interlayer_name"+
					 " FROM book_pg_alt_paper ap"+
					   " INNER JOIN attr_value av ON ap.paper = av.id"+
					   " LEFT OUTER JOIN layerset l ON ap.interlayer = l.id"+
					 " WHERE ap.template = ? ORDER BY ap.sh_from";
		return runSelect(BookPgAltPaper.class, sql, template);
	}

	private SelectResult<BookSynonymGlue> loadGlue(int book){
		String sql="SELECT bg.*, av.value paper_name, l.name interlayer_name, gc.cmd glue_cmd_name"+
					 " FROM book_synonym_glue bg"+
					   " INNER JOIN attr_value av ON av.id = bg.paper"+
					   " INNER JOIN layerset l ON bg.interlayer = l.id"+
					   " INNER JOIN glue_cmd gc ON bg.glue_cmd = gc.id"+
					  " WHERE bg.book_synonym = ?";
		return runSelect(BookSynonymGlue.class, sql, book);
	}

	@Override
	public SelectResult<BookSynonymGlue> loadBookGlueEdit(int book){
		String sql="SELECT bg.id, bg.add_layers, tp.book book_synonym, tp.paper, l.id interlayer, IFNULL(bg.glue_cmd,0) glue_cmd, gc.cmd glue_cmd_name, l.name interlayer_name, av.value paper_name"+
				  " FROM (SELECT bpt.book, bpt.paper"+
				          " FROM book_pg_template bpt"+
				          " WHERE bpt.book = ? AND bpt.book_part IN (2, 5)"+
				        " UNION"+
				        " SELECT DISTINCT bpt.book, bpap.paper"+
				         " FROM book_pg_template bpt"+
				           " INNER JOIN book_pg_alt_paper bpap ON bpt.id = bpap.template"+
				          " WHERE bpt.book = ? AND bpt.book_part IN (2, 5)) tp"+
				  " INNER JOIN layerset l ON l.subset_type=1"+
				  " INNER JOIN attr_value av ON av.id=tp.paper"+
				  " LEFT OUTER JOIN book_synonym_glue bg ON bg.book_synonym=tp.book AND bg.paper= tp.paper AND bg.interlayer=l.id"+
				  " LEFT OUTER JOIN glue_cmd gc ON gc.id=bg.glue_cmd"+
				  " ORDER BY av.value, l.name";
		return runSelect(BookSynonymGlue.class, sql, book, book);
	}

	@Override
	public SqlResult persistBookGlue(List<BookSynonymGlue> items){
		List<BookSynonymGlue> insertList=new ArrayList<BookSynonymGlue>();
		List<BookSynonymGlue> updateList=new ArrayList<BookSynonymGlue>();
		List<BookSynonymGlue> delList=new ArrayList<BookSynonymGlue>();
		
		for(BookSynonymGlue item : items){
			if(item.getGlue_cmd()!=0){
				if(item.getId()==0){
					insertList.add(item);
				}else{
					updateList.add(item);
				}
			}else{
				if(item.getId()!=0) delList.add(item);
			}
		}

		SqlResult dmlResult=new SqlResult();
		if(!delList.isEmpty()){
			dmlResult=runDeleteBatch(delList);
			if(!dmlResult.isComplete()){
				return dmlResult;
			}
		}

		if(!insertList.isEmpty()){
			dmlResult=runInsertBatch(insertList);
			if(!dmlResult.isComplete()){
				return dmlResult;
			}
		}
		if(!updateList.isEmpty()){
			dmlResult=runUpdateBatch(updateList);
		}
		return dmlResult;
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
		List<BookPgTemplate> deleteChildList=new ArrayList<BookPgTemplate>();
		List<BookPgAltPaper> insertAltPaperList=new ArrayList<BookPgAltPaper>();
		List<BookPgAltPaper> updateAltPaperList=new ArrayList<BookPgAltPaper>();
		List<BookPgAltPaper> delAltPaperList=new ArrayList<BookPgAltPaper>();
		List<BookSynonymCompo> delCompo=new ArrayList<BookSynonymCompo>();
		List<BookSynonymCompo> addCompo=new ArrayList<BookSynonymCompo>();
		List<BookSynonymCompo> updateCompo=new ArrayList<BookSynonymCompo>();

		for(BookSynonym item : items){
			if(item.getPersistState()==0){
				insertList.add(item);
			}else if(item.getPersistState()==-1){
				updateList.add(item);
			}
			//compo's
			if(item.getCompos()!=null){
				for (BookSynonymCompo c : item.getCompos()){
					if(c.isDeleted()){
						delCompo.add(c);
					}else if(c.getPersistState()==0 ){
						addCompo.add(c);
					}else{
						updateCompo.add(c);
					}
				}
			}
			//childs
			if(item.getPersistState()!=0 && item.getTemplates()!=null){
				for(BookPgTemplate child : item.getTemplates()){
					if(child.getPersistState()==0 && child.getPaper()!=0){
						insertChildList.add(child);
					}else if(child.getPersistState()==-1){
						if(child.getPaper()==0){
							deleteChildList.add(child);
						}else{
							updateChildList.add(child);
							if(child.getAltPaper()!=null){
								//alt papers
								for(BookPgAltPaper ap : child.getAltPaper()){
									//to delete
									if((ap.getSh_from()==0 && ap.getSh_to()==0) || (ap.getPaper()==0 && ap.getInterlayer()==0)){
										delAltPaperList.add(ap);
									}else{
										//to save
										if(ap.getPersistState()==0){
											insertAltPaperList.add(ap);
										}else{
											updateAltPaperList.add(ap);
										}
									}
								}
							}
						}
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
		if(result.isComplete() && !deleteChildList.isEmpty()){
			result=runDeleteBatch(deleteChildList);
		}
		if(result.isComplete() && !insertAltPaperList.isEmpty()){
			result=runInsertBatch(insertAltPaperList);
		}
		if(result.isComplete() && !updateAltPaperList.isEmpty()){
			result=runUpdateBatch(updateAltPaperList);
		}
		if(result.isComplete() && !delAltPaperList.isEmpty()){
			result=runDeleteBatch(delAltPaperList);
		}
		if(!result.isComplete()) return result;

		//compo's
		result=runDeleteBatch(delCompo);
		if(!result.isComplete()) return result;
		result=runInsertBatch(addCompo);
		if(!result.isComplete()) return result;
		result=runUpdateBatch(updateCompo);
		if(!result.isComplete()) return result;
		//clear wrong compo's
		String sql="DELETE FROM book_synonym_compo"+
					" WHERE NOT EXISTS (SELECT 1 FROM book_synonym bs WHERE bs.id = book_synonym_compo.parent AND bs.compo_type = 2)"+
					 " OR NOT EXISTS (SELECT 1 FROM book_synonym bs WHERE bs.id = book_synonym_compo.child AND bs.compo_type = 1)";
		return runDML(sql) ;
	}

	@Override
	public SelectResult<GlueCommand> loadGlueCommandAll(){
		String sql="SELECT * FROM glue_cmd gc WHERE id!=0 ORDER BY gc.cmd";
		return runSelect(GlueCommand.class, sql);
	}

	@Override
	public SelectResult<GlueCommand> persistGlueCommandBatch(List<GlueCommand> items){
		SqlResult dmlResult=new SqlResult();
		SelectResult<GlueCommand> result= new SelectResult<GlueCommand>();
		List<GlueCommand> insertList=new ArrayList<GlueCommand>();
		List<GlueCommand> updateList=new ArrayList<GlueCommand>();
		List<GlueCommand> delList=new ArrayList<GlueCommand>();
		for(GlueCommand item : items){
			if(item.getPersistState()==0 || item.getPersistState()==-1){
				if(item.getCmd()!=null && !item.getCmd().isEmpty()){
					if(item.getPersistState()==0 ){
						insertList.add(item);
					}else{
						updateList.add(item);
					}
				}else{
					if(item.getId()!=0) delList.add(item);
				}
			}
		}
		
		if(!delList.isEmpty()){
			dmlResult=runDeleteBatch(delList);
			if(!dmlResult.isComplete()){
				result.cloneError(dmlResult);
				return result;
			}
		}
		if(!insertList.isEmpty()){
			dmlResult=runInsertBatch(insertList);
			if(!dmlResult.isComplete()){
				result.cloneError(dmlResult);
				return result;
			}
		}
		if(!updateList.isEmpty()){
			dmlResult=runUpdateBatch(updateList);
			if(!dmlResult.isComplete()){
				result.cloneError(dmlResult);
				return result;
			}
		}
		
		return loadGlueCommandAll();
	}

}
