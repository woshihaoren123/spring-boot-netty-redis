package com.dzm.chatroom.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dzm.chatroom.jedis.JedisDaoImpl;

@Controller
public class UserController {

	@Autowired
	private JedisDaoImpl jedis;

	@GetMapping("index")
	public String index(String id) {
		if(jedis.exists(id)){
			return "index";
		}
		return "login";
	}

	@GetMapping("login")
	public String login() {
		return "login";
	}
	
	@ResponseBody
	@GetMapping("dologin")
	public String dologin(String username, String passwd) {
		String userId = UUID.randomUUID().toString();
		jedis.set(userId, username);
		return userId;
	}
}
