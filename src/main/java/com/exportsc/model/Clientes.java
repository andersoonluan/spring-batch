/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.exportsc.model;

/**
 *
 * @author Anderson_Rodrigues2
 */
public class Clientes {
    
    private Integer id;
    private String name;
    private String description;

    
    //Getters
    public Integer getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    
    
    
}
