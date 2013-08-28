package com.github.diamond.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.github.diamond.web.model.User;
import com.github.diamond.web.service.UserService;

@Controller
public class SecurityController extends BaseController {
    @Autowired
    private UserService userService;

    @RequestMapping(value="/login",method = RequestMethod.POST)
    public String login(HttpServletRequest request, String userCode, String password) {
    	Object result = userService.login(userCode, password);
        if (result instanceof User) {
        	request.getSession().removeAttribute("message");
            request.getSession().setAttribute("sessionUser", result);
            return "redirect:/index";
        } else {
        	request.getSession().setAttribute("userCode", userCode);
        	request.getSession().setAttribute("message", result);
            return "redirect:/";
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/";
    }

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}
