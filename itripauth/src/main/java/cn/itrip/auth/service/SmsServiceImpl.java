package cn.itrip.auth.service;

import com.cloopen.rest.sdk.CCPRestSmsSDK;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service("smsService")
public class SmsServiceImpl implements SmsService {
    public void send(String to,String templateId,String[] datas) throws Exception{
        CCPRestSmsSDK sdk = new CCPRestSmsSDK();
        sdk.init("app.cloopen.com","8883");
        sdk.setAccount("8aaf070867e885ce016821d40ca900f2","15af4888bb1d4b0caeb897f6cc18f53d");
        sdk.setAppId("8aaf070867e885ce016821d40ccd00f3");
        HashMap result = sdk.sendTemplateSMS(to,templateId,datas);
        if ("000000".equals(result.get("statusCode"))){
            System.out.println("短信发送成功");
        }else{
            throw new Exception(result.get("statusCode").toString() +"："+result.get("statusMsg").toString());
        }
    }
}
