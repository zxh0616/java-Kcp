package com.kcp.test.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description TODO
 * @Author: stem) Zjq
 * @Date: 11:04
 */

@RestController("/room")
public class Clent {

    //@Autowired
    //private KcpRttExampleClient kcpRttExampleClient;
    @PostMapping("/start")
    public void startStream() {

    }

    @PostMapping("/logout")
    public void logoutRoom(String roomID) {

    }


}
