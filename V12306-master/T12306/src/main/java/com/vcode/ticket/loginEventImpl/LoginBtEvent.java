package com.vcode.ticket.loginEventImpl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vcode.ticket.ApplicationContextFactory;
import com.vcode.ticket.methods.LoginMethods;
import com.vcode.ticket.ui.HomePage;
import com.vcode.ticket.ui.LoginPage;

/**
 * 登录界面--登录按钮事件
 * @author hh
 * @param <T>
 *
 */
@Component
public class LoginBtEvent<T> implements ActionListener {
	
	private Logger Log = Logger.getLogger(LoginBtEvent.class.getName());

	@Autowired
	private LoginPage<T> Page;
	@Autowired
	private LoginMethods<T> loginMethods;
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		HomePage<T> window = (HomePage<T>) ApplicationContextFactory.getBean(HomePage.class);
		window.printLog("登录成功,欢迎使用zcg自制代码抢票工具");
		window.printLog("双击车次即可提交订单哦,右击可将车次加入自动刷票的预选车次中,更多隐藏功能等你发现！");
		Page.frame.dispose();
		window.show(window);
		//loginMethods.CheckLogin();
		
		/*Page.frame.setVisible(false);
		HomePage<T> homePage = (HomePage<T>) ApplicationContextFactory.getBean(HomePage.class);
		homePage.show(homePage);*/
	}

}
