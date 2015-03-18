package com.photodispatcher.model.mysql.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "package")
public class MailPackage extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="id")
    private int id;
    @Id
    @Column(name="source")
    private int source;
    @Column(name="id_name")
    private String id_name;
    @Column(name="client_id")
    private int client_id;
    @Column(name="state")
    private int state;
    @Column(name="state_date")
    private Date state_date;
    @Column(name="execution_date")
    private Date execution_date;
    @Column(name="delivery_id")
    private int delivery_id;
    @Column(name="delivery_name")
    private String delivery_name;
    @Column(name="src_state")
    private int src_state;
    @Column(name="src_state_name")
    private String src_state_name;
    @Column(name="mail_service")
    private int mail_service;
    @Column(name="orders_num")
    private int orders_num;
    
    //ref
    @Column(name="source_name", updatable=false, insertable=false)
    private String source_name;
    @Column(name="source_code", updatable=false, insertable=false)
    private String source_code;
    @Column(name="state_name", updatable=false, insertable=false)
    private String state_name;
    
    @Column(name="min_ord_state", updatable=false, insertable=false)
    private int min_ord_state;
    @Column(name="min_ord_state_name", updatable=false, insertable=false)
    private String min_ord_state_name;
    
    //childs
    @Transient
    private List<MailPackageProperty> properties;
    @Transient
    private List<Order> orders;
    @Transient
    private List<MailPackageBarcode> barcodes;
    @Transient
    private List<MailPackageMessage> messages;

    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public String getId_name() {
		return id_name;
	}
	public void setId_name(String id_name) {
		this.id_name = id_name;
	}
	public int getClient_id() {
		return client_id;
	}
	public void setClient_id(int client_id) {
		this.client_id = client_id;
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
	public Date getExecution_date() {
		return execution_date;
	}
	public void setExecution_date(Date execution_date) {
		this.execution_date = execution_date;
	}
	public int getDelivery_id() {
		return delivery_id;
	}
	public void setDelivery_id(int delivery_id) {
		this.delivery_id = delivery_id;
	}
	public String getDelivery_name() {
		return delivery_name;
	}
	public void setDelivery_name(String delivery_name) {
		this.delivery_name = delivery_name;
	}
	public int getSrc_state() {
		return src_state;
	}
	public void setSrc_state(int src_state) {
		this.src_state = src_state;
	}
	public String getSrc_state_name() {
		return src_state_name;
	}
	public void setSrc_state_name(String src_state_name) {
		this.src_state_name = src_state_name;
	}
	public int getMail_service() {
		return mail_service;
	}
	public void setMail_service(int mail_service) {
		this.mail_service = mail_service;
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
	public List<MailPackageProperty> getProperties() {
		return properties;
	}
	public void setProperties(List<MailPackageProperty> properties) {
		this.properties = properties;
	}
	public int getMin_ord_state() {
		return min_ord_state;
	}
	public void setMin_ord_state(int min_ord_state) {
		this.min_ord_state = min_ord_state;
	}
	public String getMin_ord_state_name() {
		return min_ord_state_name;
	}
	public void setMin_ord_state_name(String min_ord_state_name) {
		this.min_ord_state_name = min_ord_state_name;
	}
	public List<Order> getOrders() {
		return orders;
	}
	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}
	public int getOrders_num() {
		return orders_num;
	}
	public void setOrders_num(int orders_num) {
		this.orders_num = orders_num;
	}
	public List<MailPackageBarcode> getBarcodes() {
		return barcodes;
	}
	public void setBarcodes(List<MailPackageBarcode> barcodes) {
		this.barcodes = barcodes;
	}
	public List<MailPackageMessage> getMessages() {
		return messages;
	}
	public void setMessages(List<MailPackageMessage> messages) {
		this.messages = messages;
	}


}
