package com.helwigdev.helpdesk;

/**
 * Created by helwig on 10/13/2015.
 */
public interface RNInterface {
    //ReadNetwork Interface
    void processResult(String output, int taskID);
    void authErr(int type, int taskId, String message);
    void setCookie(String cookie);

}
