package com.photodispatcher.model.mysql.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "orders")
public class Order extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id")
    private String id;
    @Column(name="source")
    private int source;
    @Column(name="src_id")
    private String src_id;
    @Column(name="src_date")
    private Date src_date;
    @Column(name="data_ts")
    private String data_ts;
    @Column(name="state")
    private int state;
    @Column(name="state_date")
    private Date state_date;
    @Column(name="ftp_folder")
    private String ftp_folder;
    @Column(name="local_folder")
    private String local_folder;
    @Column(name="fotos_num")
    private int fotos_num;
    @Column(name="sync")
    private int sync;
    @Column(name="is_preload")
    private boolean is_preload;
    /*
    @Column(name="is_new")
    private boolean is_new;
    @Column(name="reload")
    private boolean reload;
    */
    
    //ref
    @Column(name="source_name", updatable=false, insertable=false)
    private String source_name;
    @Column(name="source_code", updatable=false, insertable=false)
    private String source_code;
    @Column(name="state_name", updatable=false, insertable=false)
    private String state_name;
    /*
    @Column(name="book_type", updatable=false, insertable=false)
    private int book_type;
    */
    
    //childs

    @Transient
    private OrderExtraInfo extraInfo;
    @Transient
    private List<SubOrder> suborders;
    @Transient
    private List<PrintGroup> printGroups;
    @Transient
    private List<TechLog> techLog;
    @Transient
    private List<StateLog> stateLog; 
    @Transient
    private List<OrderExtraState> extraState; 
    @Transient
    private List<OrderExtraStateProlong>  extraStateProlong;
    
    
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
	public Date getSrc_date() {
		return src_date;
	}
	public void setSrc_date(Date src_date) {
		this.src_date = src_date;
	}
	public String getData_ts() {
		return data_ts;
	}
	public void setData_ts(String data_ts) {
		this.data_ts = data_ts;
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
	public String getLocal_folder() {
		return local_folder;
	}
	public void setLocal_folder(String local_folder) {
		this.local_folder = local_folder;
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
	public boolean isIs_preload() {
		return is_preload;
	}
	public void setIs_preload(boolean is_preload) {
		this.is_preload = is_preload;
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
	public OrderExtraInfo getExtraInfo() {
		return extraInfo;
	}
	public void setExtraInfo(OrderExtraInfo extraInfo) {
		this.extraInfo = extraInfo;
	}
	public List<SubOrder> getSuborders() {
		return suborders;
	}
	public void setSuborders(List<SubOrder> suborders) {
		this.suborders = suborders;
	}
	public List<PrintGroup> getPrintGroups() {
		return printGroups;
	}
	public void setPrintGroups(List<PrintGroup> printGroups) {
		this.printGroups = printGroups;
	}
	public List<TechLog> getTechLog() {
		return techLog;
	}
	public void setTechLog(List<TechLog> techLog) {
		this.techLog = techLog;
	}
	public List<StateLog> getStateLog() {
		return stateLog;
	}
	public void setStateLog(List<StateLog> stateLog) {
		this.stateLog = stateLog;
	}
	public List<OrderExtraState> getExtraState() {
		return extraState;
	}
	public void setExtraState(List<OrderExtraState> extraState) {
		this.extraState = extraState;
	}
	public List<OrderExtraStateProlong> getExtraStateProlong() {
		return extraStateProlong;
	}
	public void setExtraStateProlong(List<OrderExtraStateProlong> extraStateProlong) {
		this.extraStateProlong = extraStateProlong;
	}
    

}
