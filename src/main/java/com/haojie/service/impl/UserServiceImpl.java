package com.haojie.service.impl;

import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DaoSupport;
import org.springframework.stereotype.Service;

import com.haojie.common.Const;
import com.haojie.common.ServerResponse;
import com.haojie.common.TokenCache;
import com.haojie.dao.UserMapper;
import com.haojie.pojo.User;
import com.haojie.service.IUserService;
import com.haojie.utils.MD5Util;

@Service("iUserService")
public class UserServiceImpl implements IUserService {
	@Autowired
	private UserMapper userMapper;

	@Override
	public ServerResponse<User> login(String username, String password) {
		int count = userMapper.checkUsername(username);
		if(count==0){
			return ServerResponse.createByErrorMessage("用户名不正确");
		}
		String md5Password = MD5Util.MD5EncodeUtf8(password);
		User user = userMapper.selectLogin(username, md5Password);
		if(user==null){
			return ServerResponse.createByErrorMessage("密码错误");
		}
		user.setPassword(StringUtils.EMPTY);
		return ServerResponse.createBySuccess("用户登陆成功", user);
	}

	@Override
	public ServerResponse<String> register(User user) {
		
		ServerResponse validResponse = this.checkValid(user.getUsername(),Const.USERNAME );
		if (!validResponse.isSuccess()) {
			return validResponse;
		}
		
		validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
		if (!validResponse.isSuccess()) {
			return validResponse;
		}
		
		user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
		userMapper.insert(user);
		return ServerResponse.createBySuccessMessage("校验成功");
	}

	@Override
	public ServerResponse<String> checkValid(String str, String type) {
		if (StringUtils.isNotBlank(type)) {
			if (Const.USERNAME.equals(type)) {
				int count = userMapper.checkUsername(str);
				if(count>0){
					return ServerResponse.createByErrorMessage("用户名已存在");
				}
			}
			if (Const.EMAIL.equals(type)) {
				int checkEmail = userMapper.checkEmail(str);
				if(checkEmail>0){
					return ServerResponse.createByErrorMessage("邮箱已存在");
				}
			}
		} else {
			return ServerResponse.createByErrorMessage("参数有误");
		}
		return ServerResponse.createBySuccessMessage("校验成功");
	}

	@Override
	public ServerResponse<String> forgetGetQuestion(String username) {
		
		ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        
		String question = userMapper.selectQuestionByUsername(username);
		if (question != null) {
			return ServerResponse.createBySuccess(question);
		}
		return ServerResponse.createBySuccessMessage("该用户未设置找回密码问题");
	}

	@Override
	public ServerResponse<String> forgetCheckAnswer(String username,String question, String answer) {
		int count = userMapper.checkAnswer(username, question, answer);
		if(count>0){
			//说明问题及问题答案是这个用户的,并且是正确的
			String forgetToken = UUID.randomUUID().toString();
			TokenCache.setKey(TokenCache.TOKEN_PERFIX+username, forgetToken);
			return ServerResponse.createBySuccess(forgetToken);
		}
		return ServerResponse.createBySuccessMessage("问题的答案错误");
	}

	@Override
	public ServerResponse<String> forgetResetPassword(String username,
			String passwordNew, String forgetToken) {
		if (StringUtils.isBlank(forgetToken)) {
			return ServerResponse.createByErrorMessage("参数错误，token需要传递");
		}
		ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
		if (validResponse.isSuccess()) {
			return ServerResponse.createByErrorMessage("用户名不存在");
		}
		String token = TokenCache.getKey(TokenCache.TOKEN_PERFIX+username);
		if (StringUtils.isBlank(token)) {
			return ServerResponse.createByErrorMessage("token无效或已过期");
		}
		if (StringUtils.equals(forgetToken, token)) {
			String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
			int rowCount = userMapper.updatePasswordByUsername(username, passwordNew);
			if (rowCount>0) {
				return ServerResponse.createBySuccessMessage("修改密码成功");
			}
		} else {
			return ServerResponse.createByErrorMessage("token错误,请重新获取重置密码的token");
		}
		return ServerResponse.createByErrorMessage("修改密码失败");
	}

	@Override
	public ServerResponse<String> resetPassword(String passwordOld,
			String passwordNew, User user) {
		//防止横向越权,要校验一下这个用户的旧密码,一定要指定是这个用户;
		int count = userMapper.checkPassword(user.getId(), MD5Util.MD5EncodeUtf8(passwordOld));
		if (count==0) {
			return ServerResponse.createByErrorMessage("旧密码错误");
		} 
		user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
		int rowCount = userMapper.updateByPrimaryKeySelective(user);
		if (rowCount>0) {
			return ServerResponse.createBySuccessMessage("密码更新成功");
		}
		return ServerResponse.createByErrorMessage("密码更新失败");
	}

	@Override
	public ServerResponse<User> updateInformation(User user) {
		int count = userMapper.checkEmail(user.getId(), user.getEmail());
		if (count==0) {
			return ServerResponse.createByErrorMessage("邮箱已存在");
		}
		//用户名是不能改的
		User updateUser = new User();
		updateUser.setId(user.getId());
		updateUser.setEmail(user.getEmail());
		updateUser.setPhone(user.getPhone());
		updateUser.setQuestion(user.getQuestion());
		updateUser.setAnswer(user.getAnswer());
		
		int rowCount = userMapper.updateByPrimaryKeySelective(updateUser);
		if (rowCount>0) {
			return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
		}
		return ServerResponse.createBySuccessMessage("更新个人信息失败");
	}

	@Override
	public ServerResponse<User> getInformation(Integer id) {
		User user = userMapper.selectByPrimaryKey(id);
		if (user==null) {
			return ServerResponse.createBySuccessMessage("用户不存在");
		}
		user.setPassword(StringUtils.EMPTY);
		return ServerResponse.createBySuccess(user);
	}

}
