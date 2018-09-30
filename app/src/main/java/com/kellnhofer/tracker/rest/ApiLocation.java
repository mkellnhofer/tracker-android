package com.kellnhofer.tracker.rest;

import java.util.Date;
import java.util.List;

public class ApiLocation {
    public Long id;
    public Long changeTime;
    public String name;
    public Date time;
    public Double lat;
    public Double lng;
    public List<ApiPerson> persons;
}
