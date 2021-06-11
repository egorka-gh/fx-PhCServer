package com.photodispatcher.model.mysql.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "lab")
public class Lab extends AbstractEntity {
	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;
    @Column(name="src_type")
    private int src_type;
    @Column(name="name")
    private String name;
    @Column(name="hot")
    private String hot;
    @Column(name="hot_nfs")
    private String hot_nfs;
    @Column(name="queue_limit")
    private int queue_limit;
    @Column(name="is_active")
    private boolean is_active;
    @Column(name="is_managed")
    private boolean is_managed;
    
    @Column(name="soft_speed")
    private float soft_speed;

    @Column(name="post_delay")
    private float post_delay;

    
    @Column(name="pusher_enabled")
    private boolean pusher_enabled;
    @Column(name="url")
    private String url;
    
  //db drived
    @Column(name="src_type_name", updatable=false, insertable=false)
    private String src_type_name;

    @Transient
    private boolean isSelected;

    //childs
    @Transient
    private List<LabDevice> devices;
    @Transient
    private List<LabProfile> profiles;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSrc_type() {
		return src_type;
	}

	public void setSrc_type(int src_type) {
		this.src_type = src_type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHot() {
		return hot;
	}

	public void setHot(String hot) {
		this.hot = hot;
	}

	public String getHot_nfs() {
		return hot_nfs;
	}

	public void setHot_nfs(String hot_nfs) {
		this.hot_nfs = hot_nfs;
	}

	public int getQueue_limit() {
		return queue_limit;
	}

	public void setQueue_limit(int queue_limit) {
		this.queue_limit = queue_limit;
	}

	public boolean isIs_active() {
		return is_active;
	}

	public void setIs_active(boolean is_active) {
		this.is_active = is_active;
	}

	public boolean isIs_managed() {
		return is_managed;
	}

	public void setIs_managed(boolean is_managed) {
		this.is_managed = is_managed;
	}

	public String getSrc_type_name() {
		return src_type_name;
	}

	public void setSrc_type_name(String src_type_name) {
		this.src_type_name = src_type_name;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public List<LabDevice> getDevices() {
		return devices;
	}

	public void setDevices(List<LabDevice> devices) {
		this.devices = devices;
	}

	public List<LabProfile> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<LabProfile> profiles) {
		this.profiles = profiles;
	}

	public float getSoft_speed() {
		return soft_speed;
	}

	public void setSoft_speed(float soft_speed) {
		this.soft_speed = soft_speed;
	}

	public float getPost_delay() {
		return post_delay;
	}

	public void setPost_delay(float post_delay) {
		this.post_delay = post_delay;
	}

	public boolean getPusher_enabled() {
		return pusher_enabled;
	}

	public void setPusher_enabled(boolean pusher_enabled) {
		this.pusher_enabled = pusher_enabled;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
