package com.fariza.mpezexpenses.Model;

public class ModelMembers {

    String name, role, uid;

    public ModelMembers() {

    }

    public ModelMembers(String name, String role, String uid) {
        this.name = name;
        this.role = role;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
