package com.github.diamond.web.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.github.diamond.utils.MD5;
import com.github.diamond.web.model.User;
import com.github.diamond.web.service.UserService;

@Controller
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@RequestMapping("/user/index")
	public void queryUsers(ModelMap modelMap) {
		List<User> users = userService.queryUsers();
		
		modelMap.addAttribute("users", users);
	}
	
	@RequestMapping("/user/new")
	public void newUser() {
	}
	
	@RequestMapping(value="/user/save", method={RequestMethod.POST})
	public String saveUser(User user, HttpSession session) {
		if(StringUtils.isBlank(user.getUserCode().trim()) && StringUtils.isBlank(user.getUserName().trim())) {
			session.setAttribute("message", "登录账号或者用户名不能为空");
			session.setAttribute("user", user);
			return "redirect:/user/new";
		} else {
			if(StringUtils.isNotBlank(user.getPassword())) {
				session.setAttribute("message", "新建用户成功");
				user.setPassword(MD5.getInstance().getMD5String(user.getPassword()));
			} else {
				session.setAttribute("message", "新建用户成功，用户默认密码为：000000");
				user.setPassword(MD5.getInstance().getMD5String("000000"));
			}
			userService.saveUser(user);
			return "redirect:/user/index";
		}
	}
	
	@RequestMapping("/user/delete")
	public String deleteUser(long id, HttpSession session) {
		userService.deleteUser(id);
		session.setAttribute("message", "用户删除成功");
		return "redirect:/user/index";
	}
}
