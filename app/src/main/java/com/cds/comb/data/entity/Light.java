package com.cds.comb.data.entity;

/**
 * @Author: chengzj
 * @CreateDate: 2019/1/8 17:13
 * @Version: 3.0.0
 */
public class Light {
    private String ir;

    private String irTime;

    private String red;

    private String redTime;

    public Light() {
    }

    public Light(String ir, String irTime, String red, String redTime) {
        this.ir = ir;
        this.irTime = irTime;
        this.red = red;
        this.redTime = redTime;
    }

    public String getIr() {
        return ir;
    }

    public void setIr(String ir) {
        this.ir = ir;
    }

    public String getIrTime() {
        return irTime;
    }

    public void setIrTime(String irTime) {
        this.irTime = irTime;
    }

    public String getRed() {
        return red;
    }

    public void setRed(String red) {
        this.red = red;
    }

    public String getRedTime() {
        return redTime;
    }

    public void setRedTime(String redTime) {
        this.redTime = redTime;
    }
}
