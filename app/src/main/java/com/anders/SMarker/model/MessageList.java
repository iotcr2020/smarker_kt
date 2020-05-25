package com.anders.SMarker.model;

public class MessageList {

    public String send_receive_chk;
    public String message_user_nm;
    public String message_co_idx;
    public String message_ut_nm;
    public String message_gubn;
    public String message_content;
    public String reg_dt;
    public String read_chk;
    public String action_content;

    public String getAction_content() { return action_content;}

    public void setAction_content(String val){
        this.action_content= val;
    }

    public String getSend_receive_chk() {
        return send_receive_chk;
    }

    public String getRead_chk() {
        return read_chk;
    }

    public void setRead_chk(String read_chk) {
        this.read_chk = read_chk;
    }

    public String getSned_receive_chk() {
        return send_receive_chk;
    }

    public void setSend_receive_chk(String send_receive_chk) {
        this.send_receive_chk = send_receive_chk;
    }

    public String getMessage_co_idx() {
        return message_co_idx;
    }

    public void setMessage_co_idx(String message_co_idx) {
        this.message_co_idx = message_co_idx;
    }

    public String getMessage_user_nm() {
        return message_user_nm;
    }

    public void setMessage_user_nm(String message_user_nm) {
        this.message_user_nm = message_user_nm;
    }

    public String getMessage_ut_nm() {
        return message_ut_nm;
    }

    public void setMessage_ut_nm(String message_ut_nm) {
        this.message_ut_nm = message_ut_nm;
    }

    public String getMessage_gubn() {
        return message_gubn;
    }

    public void setMessage_gubn(String message_gubn) {
        this.message_gubn = message_gubn;
    }

    public String getMessage_content() {
        return message_content;
    }

    public void setMessage_content(String message_content) {
        this.message_content = message_content;
    }

    public String getReg_dt() {
        return reg_dt;
    }

    public void setReg_dt(String reg_dt) {
        this.reg_dt = reg_dt;
    }
}
