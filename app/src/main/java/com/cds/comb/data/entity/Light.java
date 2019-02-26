package com.cds.comb.data.entity;

/**
 * @Author: chengzj
 * @CreateDate: 2019/1/8 17:13
 * @Version: 3.0.0
 */
public class Light {
    private String mw;

    private String time;

    public Light() {
    }

    public Light(String mw, String time) {
        this.mw = mw;
        this.time = time;
    }

    public String getMw() {
        return mw;
    }

    public void setMw(String mw) {
        this.mw = mw;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
