package com.dzm.chatroom;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import com.dzm.chatroom.netty.Server;

@SpringBootApplication
@ComponentScan("com.dzm.chatroom")
public class ApplicationRun {
    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(ApplicationRun.class).run(args);
        new Server(7788).run();
    }
}
