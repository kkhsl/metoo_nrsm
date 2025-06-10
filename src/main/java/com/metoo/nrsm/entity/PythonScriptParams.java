package com.metoo.nrsm.entity;


import lombok.Data;

@Data
public class PythonScriptParams {
    private String command;
    private String brand;
    private String ip;
    private String protocol;
    private int port;
    private String username;
    private String password;
    private String option;
}