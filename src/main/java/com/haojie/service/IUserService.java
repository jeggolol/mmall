package com.haojie.service;

import javax.servlet.http.HttpSession;

import com.haojie.common.ServerResponse;
import com.haojie.pojo.User;

public interface IUserService {
	ServerResponse<User> login(String userName,String passWord);
	ServerResponse<String> register(User user);
	ServerResponse<String> checkValid(String email,String username);
	ServerResponse<String> forgetGetQuestion(String username);
	ServerResponse<String> forgetCheckAnswer(String username,String question,String answer);
	ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken);
	ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user);
	ServerResponse<User> updateInformation(User user);
	ServerResponse<User> getInformation(Integer id);
}
