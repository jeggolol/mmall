package com.haojie.dao;

import java.util.StringTokenizer;

import org.apache.ibatis.annotations.Param;

import com.haojie.pojo.User;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);
    
    int checkUsername(String username);
    
    int checkPhone(String phone);
    
    int checkEmail(String email);
    
    User selectLogin(@Param("username")String userName,@Param("password")String passWord);
    
    String selectQuestionByUsername(String username);
   
    int checkAnswer(@Param("username")String username,@Param("question")String question,@Param("answer")String answer);
    
    int updatePasswordByUsername(@Param("username")String username,@Param("passwordNew")String passwordNewString);
    
    int checkPassword(@Param("id")Integer id,@Param("passwordOld")String passwordOld);
    
    int checkEmail(@Param("id")Integer id,@Param("email")String email);
}