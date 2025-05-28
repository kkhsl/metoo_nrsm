package com.metoo.nrsm.core.service;

import java.util.List;
import java.util.Map;

public interface INtpService {

    boolean open(Boolean instance);
    boolean openNtp(Boolean instance);
    boolean saveTime(String instance);
    boolean saveNtp(List<String> instance);
    boolean restart() throws Exception;
    boolean env() throws Exception;
    boolean status() throws Exception;



    Map<String, List<String>> select();

}
