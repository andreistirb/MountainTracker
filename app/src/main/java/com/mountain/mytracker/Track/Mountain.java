package com.mountain.mytracker.Track;

/**
 * Created by anstirb on 17.08.2016.
 */
public class Mountain {

    private String name, description;
    private Integer id;

    public Mountain(){}

    public Mountain(Integer id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

}
