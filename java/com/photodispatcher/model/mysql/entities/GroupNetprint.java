package com.photodispatcher.model.mysql.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "group_netprint")
public class GroupNetprint extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name="source")
    private int source;
    @Id
    @Column(name="group_id")
    private int groupId;
	@Id
    @Column(name="netprint_id")
    private String netprintId;
    @Column(name="created")
    private Date created;
    @Column(name="state")
    private int state;
    @Column(name="box_number")
    private int boxNumber;
    @Column(name="send")
    private boolean isSend;

    public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public String getNetprintId() {
		return netprintId;
	}
	public void setNetprintId(String netprintId) {
		this.netprintId = netprintId;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public int getBoxNumber() {
		return boxNumber;
	}
	public void setBoxNumber(int boxNumber) {
		this.boxNumber = boxNumber;
	}
	public Boolean getIsSend() {
		return isSend;
	}
	public void setIsSend(Boolean isSend) {
		this.isSend = isSend;
	}

}
