package com.vcode.ticket.brushVotesEventImpl;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.HttpCookie;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.vcode.http.client.VHttpResponse;
import com.vcode.http.client.methods.VHttpPost;
import com.vcode.http.client.parames.VParames;
import com.vcode.http.utils.VBrowser;
import com.vcode.http.utils.VHttpUtils;
import com.vcode.ticket.ui.HomePage;
import com.vcode.ticket.utils.PopList;

/**
 * 乘车人按钮事件
 * @author hh
 * @param <T>
 *
 */
@org.springframework.stereotype.Component
public class PassengerBtEvent<T> implements MouseListener {

	private JList list;

	@Autowired
	private HomePage<T> Page;
	
//	public PassengerBtEvent(HomePage t, JList List) {
//		super(t);
//		this.list = list;
//	}

	@Override
	public void mouseClicked(MouseEvent e) {
		//获取乘客列表
		DefaultListModel<Object> pupModel2 = Page.getPassengerDTOs();
		PopList.initPopup(e.getComponent(), list, pupModel2);
	}
   
	/**
	 * 预定界面中的token
	 */
	private String getRepeatSubmitToken(){
		String  REPEAT_SUBMIT_TOKEN="";
		VHttpPost post = new VHttpPost(
				"https://kyfw.12306.cn/otn/confirmPassenger/initDc");
		VParames parames = new VParames();
		parames.clear();
		parames.put("_json_att", "");
		post.setParames(parames);
		VHttpResponse res = VBrowser.execute(post);
		List<HttpCookie> cookies = res.getCookies();
		System.out.println(cookies);
		String body = VHttpUtils.outHtml(res.getBody());

		Pattern pattern = Pattern
				.compile("var globalRepeatSubmitToken = '[0-9 | a-z]{32}'");
		Pattern pattern2 = Pattern
				.compile("'key_check_isChange':'[0-9 | A-Z]{56}'");
		Matcher matcher = pattern.matcher(body);
		Matcher matcher2 = pattern2.matcher(body);
		while (matcher.find()) {
		   REPEAT_SUBMIT_TOKEN = matcher.group(0)
					.replace("var globalRepeatSubmitToken = '", "")
					.replace("'", "");
		}
		res.getEntity().disconnect();
		return REPEAT_SUBMIT_TOKEN;

	}
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}
