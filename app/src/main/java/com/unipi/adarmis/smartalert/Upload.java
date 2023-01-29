package com.unipi.adarmis.smartalert;

import java.util.Date;

public class Upload {
    private String mName;

    private Date date;
    private String mImageUrl;

    public Upload() {
        //empty constructor needed
    }

    public Upload(String name, Date date,String imageUrl) {
        mName = name;
        mImageUrl = imageUrl;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setDate(Date date) {
        this.date=date;
    }

    public Date getDate() {return date;}

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }
}
