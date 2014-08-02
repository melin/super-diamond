package com.github.diamond.web.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.diamond.utils.MD5;
import com.github.diamond.utils.PageUtil;
import com.github.diamond.utils.SessionHolder;
import com.github.diamond.web.model.User;
import com.github.diamond.web.service.UserService;

@Controller
public class UserController extends BaseController {
	
	@Autowired
	private UserService userService;
	
	private static final int LIMIT = 10;
	
	@RequestMapping("/user/index")
	public void queryUsers(ModelMap modelMap, @RequestParam(defaultValue="1") int page) {
		List<User> users = userService.queryUsers(PageUtil.getOffset(page, LIMIT), LIMIT);
		modelMap.addAttribute("users", users);
		
		long recordCount = userService.queryUserCount();
		modelMap.addAttribute("totalPages", PageUtil.pageCount(recordCount, LIMIT));
		modelMap.addAttribute("currentPage", page);
	}
	
	@RequestMapping("/user/new")
	public void newUser() {
	}
	
	@RequestMapping(value="/user/save", method={RequestMethod.POST})
	public String saveUser(User user, String repassword, HttpSession session) {
		user.setPassword(MD5.getInstance().getMD5String(user.getPassword()));
		userService.saveUser(user);
		return "redirect:/user/index";
	}
	
	@RequestMapping("/user/delete")
	public String deleteUser(long id, HttpSession session) {
		userService.deleteUser(id);
		session.setAttribute("message", "用户删除成功");
		return "redirect:/user/index";
	}
	
	@RequestMapping("/user/password")
	public void password() {
	}
	
	@RequestMapping("/user/updatePassword")
	public String updatePassword(String password, String newpassword, HttpSession session) {
		String oldPassword = MD5.getInstance().getMD5String(password);
		User user = (User) SessionHolder.getSession().getAttribute("sessionUser");
		
		if(!oldPassword.equals(user.getPassword()))
			session.setAttribute("message", "原密码不正确");
		else {
			userService.updatePassword(user.getId(), MD5.getInstance().getMD5String(newpassword));
			session.setAttribute("message", "密码修改成功");
		}
		return "redirect:/user/password";
	}
}
