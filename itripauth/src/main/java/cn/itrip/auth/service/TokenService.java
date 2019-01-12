package cn.itrip.auth.service;

import cn.itrip.beans.pojo.ItripUser;

public interface TokenService {
    public String generateToken(String userAgent, ItripUser user)throws Exception;
    public void save(String token,ItripUser user)throws Exception;
}
