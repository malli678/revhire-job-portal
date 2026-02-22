package com.revhire.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Skill {


	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	public Long getId() {
		return id;
	}
	private String skillName;
	private String proficiency;
	
	@ManyToOne
	private User user;
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getSkillName() {
		return skillName;
	}
	
	public void setSkillName(String skillName) {
		this.skillName = skillName;
	}
	
	public String getProficiency() {
		return proficiency;
	}
	
	public void setProficiency(String proficiency) {
		this.proficiency = proficiency;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}


    // Getters & Setters
}