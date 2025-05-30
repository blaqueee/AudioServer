package com.axelor.apps.audio.dto;

import javax.ws.rs.QueryParam;

public class SearchRequestDto {
    @QueryParam("model")
    private String model;

    @QueryParam("where")
    private String where;

    @QueryParam("orderBy")
    private String orderBy;

    @QueryParam("name")
    private String name;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }
}
