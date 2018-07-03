package com.haojie.controller.portal;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.haojie.common.Const;
import com.haojie.common.ResponseCode;
import com.haojie.common.ServerResponse;
import com.haojie.pojo.User;
import com.haojie.service.IUserService;

@Controller
@RequestMapping("/user/")
public class UserController {
	@Autowired
	private IUserService iUserService;
	
	/**
	 * 用户登陆
	 * @param username
	 * @param password
	 * @param httpSession
	 * @return
	 */
	@RequestMapping(value="login.do",method=RequestMethod.GET)
	@ResponseBody
	public ServerResponse<User> login(String username,String password,HttpSession httpSession) {
		ServerResponse<User> response = iUserService.login(username, password);
		if (response.isSuccess()) {
			httpSession.setAttribute(Const.CURRENT_USER, response.getData());
		}
		return response;
	}
	
	/**
	 * 用户登出
	 * @param session
	 * @return
	 */
	@RequestMapping(value="logout.do",method=RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> logout(HttpSession session) {
		session.removeAttribute(Const.CURRENT_USER);
		return ServerResponse.createBySuccessMessage("退出成功");
	}
	
	/**
	 * 用户注册
	 * @param user
	 * @return
	 */
	@RequestMapping(value="register.do",method=RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> register(@RequestBody User user) {
		if (StringUtils.isBlank(user.getUsername()) && StringUtils.isBlank(user.getPassword()) && 
				StringUtils.isBlank(user.getEmail()) && StringUtils.isBlank(user.getPhone()) && 
				StringUtils.isBlank(user.getQuestion()) && StringUtils.isBlank(user.getAnswer())){
				return ServerResponse.createByErrorMessage("请填写完整信息");
			}
		return iUserService.register(user);
	}
	/**
	 * 检查用户名是否有效
	 * @param email
	 * @param username
	 * @return
	 */
	@RequestMapping(value="check_valid.do",method=RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> checkValid(String str,String type) {
		return iUserService.checkValid(str, type);
	}
	
	/**
	 * 获取登录用户信息
	 * @param session
	 * @return
	 */
	@RequestMapping(value="get_user_info.do",method=RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> getUserInfo(HttpSession session) {
		User user = (User)session.getAttribute(Const.CURRENT_USER);
		if (user!=null) {
			return ServerResponse.createBySuccess(user);
		}
		return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
	}
	
	/**
	 * 忘记密码,获取问题
	 * @param username
	 * @return
	 */
	@RequestMapping(value="forget_get_question.do",method=RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> forgetGetQuestion(String username) {
		return iUserService.forgetGetQuestion(username);
	}
	
	/**
	 * 提交问题答案,返回密码
	 * @param username
	 * @param question
	 * @param answer
	 * @return
	 */
	@RequestMapping(value="forget_check_answer.do",method=RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){
		return iUserService.forgetCheckAnswer(username, question, answer);
	}
	
	/**
	 * 忘记密码的重设密码
	 * @param username
	 * @param passwordNew
	 * @param forgetToken
	 * @return
	 */
	@RequestMapping(value="forget_reset_password.do",method=RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken) {
		return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
	}
	
	/**
	 * .登录中状态重置密码 
	 * @param passwordOld
	 * @param passwordNew
	 * @param session
	 * @return
	 */
	@RequestMapping(value="reset_password.do",method=RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,HttpSession session) {
		User user = (User)session.getAttribute(Const.CURRENT_USER);
		if (user == null) {
			return ServerResponse.createByErrorMessage("用户未登陆");
		}
		return iUserService.resetPassword(passwordOld, passwordNew, user);
	}
	
	/**
	 * 登录状态更新个人信息
	 * @param session
	 * @param user
	 * @return
	 */
	@RequestMapping(value="update_information.do",method=RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> updateInformation(HttpSession session,User user) {
		User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
		if (currentUser==null) {
			return ServerResponse.createByErrorMessage("用户未登陆");
		}
		ServerResponse<User> response = iUserService.updateInformation(user);
		if (response.isSuccess()) {
			response.getData().setUsername(currentUser.getUsername());
			session.setAttribute(Const.CURRENT_USER, response.getData());
		}
		return response;
	}
	
	/**
	 * 获取当前登录用户的详细信息，并强制登录
	 * @param session
	 * @return
	 */
	@RequestMapping(value="get_information.do",method=RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> getInformation(HttpSession session) {
		User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
		if (currentUser==null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录,无法获取当前用户信息,status=10,强制登录");
		}
		
		return iUserService.getInformation(currentUser.getId());
	}
}
