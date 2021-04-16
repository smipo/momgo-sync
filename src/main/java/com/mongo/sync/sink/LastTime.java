package com.mongo.sync.sink;

public class LastTime {

    public LastTime(){

    }
    public LastTime(int time,int inc){
        this.time = time;
        this.inc = inc;
    }

    private int time;

    private int inc;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getInc() {
        return inc;
    }

    public void setInc(int inc) {
        this.inc = inc;
    }
}
