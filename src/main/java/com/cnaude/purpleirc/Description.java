/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.purpleirc;

/**
 *
 * @author cnaude
 */
public class Description {
    final String fullName;
    final String description;
    final String website;
    final String version;
    
    public Description(String fullName, String description, String website, String version) {
        this.fullName = fullName;
        this.description = description;
        this.website = website;
        this.version = version;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getWebsite() {
        return website;
    }
    
    public String getVersion() {
        return version;
    }
}
