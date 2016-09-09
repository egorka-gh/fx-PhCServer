package com.photodispatcher.model.mysql.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "orders_load")
public class OrderLoad extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id")
    private String id;
    @Column(name="source")
    private int source;
    @Column(name="src_id")
    private String src_id;
    @Column(name="src_state")
    private int src_state;
    @Column(name="state")
    private int state;
    @Column(name="state_date")
    private Date state_date;
    @Column(name="ftp_folder")
    private String ftp_folder;
    @Column(name="fotos_num")
    private int fotos_num;
    @Column(name="sync")
    private int sync;
    @Column(name="clean_fs")
    private boolean clean_fs;
    @Column(name="resume_load")
    private boolean resume_load;
    
    //ref
    @Column(name="source_name", updatable=false, insertable=false)
    private String source_name;
    @Column(name="source_code", updatable=false, insertable=false)
    private String source_code;
    @Column(name="state_name", updatable=false, insertable=false)
    private String state_name;

    @Column(name="tag", updatable=false, insertable=false)
    private String tag;
    
    //childs
    @Transient
    private List<OrderFile> files;
    @Transient
    private List<StateLog> stateLog;
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public String getSrc_id() {
		return src_id;
	}
	public void setSrc_id(String src_id) {
		this.src_id = src_id;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public Date getState_date() {
		return state_date;
	}
	public void setState_date(Date state_date) {
		this.state_date = state_date;
	}
	public String getFtp_folder() {
		return ftp_folder;
	}
	public void setFtp_folder(String ftp_folder) {
		this.ftp_folder = ftp_folder;
	}
	public int getFotos_num() {
		return fotos_num;
	}
	public void setFotos_num(int fotos_num) {
		this.fotos_num = fotos_num;
	}
	public int getSync() {
		return sync;
	}
	public void setSync(int sync) {
		this.sync = sync;
	}
	public boolean isClean_fs() {
		return clean_fs;
	}
	public void setClean_fs(boolean clean_fs) {
		this.clean_fs = clean_fs;
	}
	public boolean isResume_load() {
		return resume_load;
	}
	public void setResume_load(boolean resume_load) {
		this.resume_load = resume_load;
	}
	public String getSource_name() {
		return source_name;
	}
	public void setSource_name(String source_name) {
		this.source_name = source_name;
	}
	public String getSource_code() {
		return source_code;
	}
	public void setSource_code(String source_code) {
		this.source_code = source_code;
	}
	public String getState_name() {
		return state_name;
	}
	public void setState_name(String state_name) {
		this.state_name = state_name;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public List<OrderFile> getFiles() {
		return files;
	}
	public void setFiles(List<OrderFile> files) {
		this.files = files;
	}
	public List<StateLog> getStateLog() {
		return stateLog;
	}
	public void setStateLog(List<StateLog> stateLog) {
		this.stateLog = stateLog;
	}
	public int getSrc_state() {
		return src_state;
	}
	public void setSrc_state(int src_state) {
		this.src_state = src_state;
	} 

}
