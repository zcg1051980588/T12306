package com.vcode.ticket.methods;

import java.awt.Color;
import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.vcode.http.client.VHttpResponse;
import com.vcode.http.client.methods.VHttpGet;
import com.vcode.http.client.methods.VHttpPost;
import com.vcode.http.client.parames.VParames;
import com.vcode.http.utils.VBrowser;
import com.vcode.http.utils.VHttpUtils;
import com.vcode.ticket.ApplicationContextFactory;
import com.vcode.ticket.loginEventImpl.VerificationCodeEvent;
import com.vcode.ticket.ui.HomePage;
import com.vcode.ticket.ui.LoginPage;
import com.vcode.ticket.utils.ConfigUtils;

import sun.misc.BASE64Decoder;

/**
 * 登录界面的逻辑和方法
 * @author zcg
 * @param 2019/1/2
 *
 */
@org.springframework.stereotype.Component
public class LoginMethods<T> {
	
	@Autowired
	private LoginPage<T> Page;

	private static Logger Log = Logger.getLogger(LoginMethods.class.getName());
	
	private String newCode="";
	

	/**
	 * 登录界面，用于获取cookie
	 */
	public static void ticket_init(){
		VHttpGet get = new VHttpGet("https://kyfw.12306.cn/otn/login/init");
		VHttpResponse res = VBrowser.execute(get);	
		res.getEntity().disconnect();							
	}
	
	/**
	 * 获取登录界面验证码
	 * @return
	 */
	public static byte[] getLoginCode(){
		byte[] data = null;
		try{
			VHttpGet get = new VHttpGet("https://kyfw.12306.cn/passport/captcha/captcha-image64?login_site=E&module=login&rand=sjrand&"+Math.random()+"&callback=jQuery19102776869072627963"+Math.random()+"&_="+System.currentTimeMillis());
			VHttpResponse res = VBrowser.execute(get);	
			String body = VHttpUtils.outHtml(res.getBody());
			body = body.substring(body.indexOf("(")+1, body.lastIndexOf(")"));
			
			JSONObject json = new JSONObject(body);
			String imgBase = json.get("image").toString();
			//Log.info(imgBase);
			InputStream in = Base64ToInputStream(imgBase);
			//获取验证码
			data = VHttpUtils.InputStreamToByte(in);
		}catch(Exception e){
			try {
				InputStream in = VerificationCodeEvent.class.getResource("../image/loadError.png").openStream();
				data = VHttpUtils.InputStreamToByte(in);
			} catch (IOException e1) {
				Log.error("获取图片失败！");
				Log.error(e1.toString());
			}
			Log.error("获取验证码失败！");
		}
		return data;	
	}
	
	/**
	 * 将base64转换成输入流
	 * @param imgStr
	 * @return
	 */
	public static InputStream Base64ToInputStream(String imgStr) { // 对字节数组字符串进行Base64解码并生成图片
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			// Base64解码
			byte[] b = decoder.decodeBuffer(imgStr);
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {// 调整异常数据
					b[i] += 256;
				}
			}
 
		  InputStream InputStream = new ByteArrayInputStream(b);
	
			return InputStream;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
 
	}

	
	/**
	 * 表单校验
	 * @return
	 */
	private boolean IsChoiceCode(){
       		
		if (Page.userNameFidld.getText()==null || "".equals(Page.userNameFidld.getText())) {
			Page.msgLabel.setText("提示：请填写用户名");
			Page.msgLabel.setForeground(Color.red);
			return false;
		}
		if (Page.passwordField.getPassword()==null || Page.passwordField.getPassword().length==0) {
			Page.msgLabel.setText("提示：请填写密码");
			Page.msgLabel.setForeground(Color.red);
			return false;
		}
		JComponent p3 = (JComponent)Page.frame.getLayeredPane();
		if (p3.getComponents().length<=0) {
			Page.msgLabel.setText("提示：请选择验证码");
			Page.msgLabel.setForeground(Color.red);
			return false;
		}
		return true;
	}
	
	/**
	 * 判断是否登录成功
	 */
	public void CheckLogin(){
		
		if (!IsChoiceCode()) {
			return;
		}
		JComponent p3 = (JComponent)Page.frame.getLayeredPane();
		Component[] cons = p3.getComponents();
		for (int i=0;i<cons.length;i++) {
			if (cons[i] instanceof JLabel) {
				JLabel lb = (JLabel)cons[i];
				newCode += lb.getX()-64+(lb.getIcon().getIconWidth()/2) + "," ;
				newCode += lb.getY()-179+(lb.getIcon().getIconHeight()/2) + "";
				if (i<cons.length-1) {
					newCode += ",";
				}
			}
		}
		if(newCode!=null && newCode.length()>0) {
			newCode=newCode.substring(0, newCode.lastIndexOf(","));
		}
		Page.msgLabel.setText("当前验证码是："+newCode);
		Log.info("当前验证码是："+newCode);
		
		//校验验证码是否正确
		if(checkLoginCode(newCode)) {
			//进行登录
			login(newCode);
		}else {
			Page.msgLabel.setText("提示：验证码错误");
			Page.msgLabel.setForeground(Color.red);
			//res.getEntity().disconnect();
			Page.verificationCode.setIcon(new ImageIcon(getLoginCode()));
			clearCode();
		}
		
	}
	/**
	 * 登录账号
	 */
	@SuppressWarnings("unchecked")
	private void login(String code){
		VHttpPost post = new VHttpPost("https://kyfw.12306.cn/passport/web/login");
		VParames parames = new VParames();
		parames.put("username", Page.userNameFidld.getText());
		parames.put("password", new String(Page.passwordField.getPassword()));
		parames.put("appid", "excater");
		parames.put("answer", code);
		post.setParames(parames);		
		VHttpResponse res = VBrowser.execute(post);
		String body = VHttpUtils.outHtml(res.getBody());		//将网页内容转为文本
		try {
			JSONObject json = new JSONObject(body);
			String result_code = json.get("result_code").toString();
			//代表登录成功
			if("0".equals(result_code)) {
				//生成新的秘钥操作
				uamtk();
				Log.info(json.get("result_message"));
				//
				HomePage<T> window = (HomePage<T>) ApplicationContextFactory.getBean(HomePage.class);
				window.printLog("登录成功,欢迎使用zcg自制代码抢票工具");
				window.printLog("双击车次即可提交订单哦,右击可将车次加入自动刷票的预选车次中,更多隐藏功能等你发现！");
				Page.frame.dispose();
				window.show(window);
			}else {
				Page.msgLabel.setText("提示：验证码错误");
				Page.msgLabel.setForeground(Color.red);
				res.getEntity().disconnect();
			}
			//验证码错误
			if("5".equals(result_code)) {
				Page.msgLabel.setText("提示：验证码错误");
				Page.msgLabel.setForeground(Color.red);
				res.getEntity().disconnect();
				Page.verificationCode.setIcon(new ImageIcon(getLoginCode()));
				clearCode();
			}else {
				
			}
		
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		/*try {
			JSONObject json = new JSONObject(body);
			String result_code = json.get("result_code").toString();
		//开始提交登录信息
		VHttpPost post = new VHttpPost("https://kyfw.12306.cn/otn/login/loginAysnSuggest");
		VParames parames = new VParames();
		parames.put("loginUserDTO.user_name", Page.userNameFidld.getText());
		parames.put("randCode", newCode);
		parames.put("userDTO.password", new String(Page.passwordField.getPassword()));
		
		post.setParames(parames);
		
		VHttpResponse res = VBrowser.execute(post);
		String body = VHttpUtils.outHtml(res.getBody());			//将网页内容转为文本
		try {
			JSONObject json = new JSONObject(body);
			if ("true".equals(json.get("status").toString())) {
				JSONObject json2 = new JSONObject(json.get("data").toString());
				if (json2.length()>1 && "Y".equals(json2.get("loginCheck").toString())) {
					Page.msgLabel.setText("登录成功，正在跳转到主页");
				}else {
					System.out.println(json.get("messages"));
					System.exit(0);
				}
			}else {
				System.out.println(json.get("messages"));
				System.exit(0);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		res.getEntity().disconnect();
		
		//开始第二次登录
		post = new VHttpPost("https://kyfw.12306.cn/otn/login/userLogin");
		VParames parames2 = new VParames();
		parames2.put("_json_att", "");
		post.setParames(parames2);								//装配参数
		res = VBrowser.execute(post);								//提交登录
		if (res.getEntity().getStaus()==200){
			if (VHttpUtils.outHtml(res.getBody()).contains("欢迎您登录中国铁路客户服务中心网站")){		//验证是否登录成功
				Page.msgLabel.setText("登录成功");
				
				//处理记住密码
				boolean check = Page.rememberCheckBox.isSelected();
				String[] str = new String[]{Page.userNameFidld.getText(),new String(Page.passwordField.getPassword())};
				if (check){
					try {
						ConfigUtils.getInstace().rememberPass(str);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else {
					try {
						ConfigUtils.getInstace().removePass(str);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				HomePage<T> window = (HomePage<T>) ApplicationContextFactory.getBean(HomePage.class);
				window.printLog("登录成功,欢迎使用zcg自制代码抢票工具");
				window.printLog("双击车次即可提交订单哦,右击可将车次加入自动刷票的预选车次中,更多隐藏功能等你发现！");
				Page.frame.dispose();
				window.show(window);
			}else {
				Page.msgLabel.setText("提示：登录失败");
				Page.msgLabel.setForeground(Color.red);
			}
			res.getEntity().disconnect();
		}*/
	}
	
	private void uamtk() {
		VHttpPost post = new VHttpPost("https://kyfw.12306.cn/passport/web/auth/uamtk");
		VParames parames = new VParames();
		parames.put("appid", "excater");
		
		post.setParames(parames);		
		VHttpResponse res = VBrowser.execute(post);
		String body = VHttpUtils.outHtml(res.getBody());		//将网页内容转为文本
		JSONObject json = new JSONObject(body);
		String result_code = json.get("result_code").toString();
		String newapptk=json.getString("newapptk").toString();
		res.getEntity().disconnect();
		VHttpPost post1 = new VHttpPost("https://exservice.12306.cn/excater/uamauthclient");
		VParames parames1 = new VParames();
		parames1.put("tk", newapptk);
		
		post1.setParames(parames1);		
		VHttpResponse res1 = VBrowser.execute(post1);
		String body1 = VHttpUtils.outHtml(res1.getBody());		//将网页内容转为文本
		JSONObject json1 = new JSONObject(body1);
		Log.info(json1.get("username")+"===="+json1.get("result_message"));
		HomePage<T> window = (HomePage<T>) ApplicationContextFactory.getBean(HomePage.class);
		window.printLog("登录成功,姓名"+json1.get("username"));
		res1.getEntity().disconnect();
		
	}

	/**
	 * 校验验证码是否正确
	 * @return
	 */
	private boolean checkLoginCode(String newCode) {
		//开始提交登录信息
		VHttpGet get = new VHttpGet("https://kyfw.12306.cn/passport/captcha/captcha-check?callback=jQuery191028092085178782455_"+System.currentTimeMillis()+"&answer="+newCode+"&rand=sjrand&login_site=E&_="+System.currentTimeMillis());
		VHttpResponse res = VBrowser.execute(get);	
		//InputStream body = res.getBody();
		String body = VHttpUtils.outHtml(res.getBody());		//将网页内容转为文本
		body = body.substring(body.indexOf("(")+1, body.lastIndexOf(")"));
		JSONObject json = new JSONObject(body);
		String result_code = json.getString("result_code");
		//验证码校验成功
		if("4".equals(result_code)) {
			return true;
		}else {
			Log.info(json.getString("result_message"));
			return false;
		}

		
	}
	
	/**
	 * 
	 * 清空验证码
	 * 
	 */
	public void clearCode(){
		newCode = "";
		JComponent p3 = (JComponent)Page.frame.getLayeredPane();
		Component[] cons1 = p3.getComponents();
		for (Component con : cons1) {
			if (con instanceof JLabel) {
				p3.remove(con);
			}
		}
		Page.frame.repaint(); 
	}
}
