package cn.itrip.auth.service;

import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.common.MD5;
import cn.itrip.common.RedisAPI;
import cn.itrip.dao.user.ItripUserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("userService")
public class UserServiceImpl implements UserService {
    @Resource
    private RedisAPI redisAPI;
    @Resource
    private ItripUserMapper itripUserMapper;
    @Resource
    private SmsService smsService;
    @Resource
    private MailService mailService;
    public ItripUser findUserByUserCode(String userCode) throws  Exception{
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("userCode",userCode);
        List<ItripUser> users = itripUserMapper.getItripUserListByMap(param);
        if (users.size()>0){
            return users.get(0);
        }else{
            return null;
        }
    }

    public void itriptxCreateUser(ItripUser user) throws Exception{
        //1.添加用户信息
        itripUserMapper.insertItripUser(user);
        //2.生成激活码
        String code = MD5.getMd5(new Date().toString(),32);
        //3.发送邮件
        mailService.sendActivationMail(user.getUserCode(),code);
        //4..激活码存入redis
        redisAPI.set("activation:"+user.getUserCode(),30*30,code);
    }
    public void itriptxCreateUserByPhone(ItripUser user) throws Exception{
        //1.添加用户信息
        itripUserMapper.insertItripUser(user);
        //2.生成激活码
        int code = MD5.getRandomCode();
        //3.发送短信
        smsService.send(user.getUserCode(),"1",new String[]{String.valueOf(code),"5"});
        //4..激活码存入redis
        redisAPI.set("activation:"+user.getUserCode(),60,String.valueOf(code));
    }

    public boolean activate(String mail,String code )throws Exception{
        String value = redisAPI.get("activate:"+mail);
        if(value.equals(code)){
            Map<String,Object> param = new HashMap<String,Object>();
            List<ItripUser> users = itripUserMapper.getItripUserListByMap(param);
            if (users.size()>0){
                ItripUser user = users.get(0);
                user.setActivated(1);
                user.setUserType(0);
                user.setFlatID(user.getId());
                itripUserMapper.updateItripUser(user);
                return true;
            }
        }
        else
            return false;
        return false;
    }

    //短信验证
    public boolean validatePhone(String phoneNum,String code) throws  Exception{
        //比对验证码
        String key = "activation:"+phoneNum;
        String value = redisAPI.get(key);
        if(null != value && value.equals(code)){
            ItripUser user = this.findUserByUserCode(phoneNum);
            if (null != user){
                //2.更新用户激活状态
                user.setActivated(1);
                user.setFlatID(user.getId());
                user.setUserType(0);
                itripUserMapper.updateItripUser(user);
                return true;
            }
        }
        return false;
    }

    @Override
    public ItripUser login(String userCode, String userPassword) throws Exception {
        ItripUser user = this.findUserByUserCode(userCode);
        if(null!=user){
            if(user.getActivated()!=1){
                throw new Exception("用户未激活");
            }
            if(userPassword.equals(user.getUserPassword())){
                return user;
            }
        }
        return null;
    }
}

