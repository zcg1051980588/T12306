package com.vcode.ticket.brushVotesEventImpl;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultListModel;

import org.springframework.beans.factory.annotation.Autowired;

import com.vcode.ticket.ui.HomePage;

/**
 * 乘车人列表事件
 * @author hh
 * @param <T>
 *
 */
public class PassengerListEvent<T> implements MouseListener {

	@Autowired
	private HomePage<T> Page;

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() >= 2) {
			DefaultListModel<Object> model_Passenger = new DefaultListModel<Object>();
			if (Page.passengerList.getSelectedIndex() > -1) {
				for (int i = 0; i < Page.passengerList.getModel().getSize(); i++) {
					if (i != Page.passengerList.getSelectedIndex()) {
						model_Passenger.addElement(Page.passengerList.getModel().getElementAt(i));
					}
				}
				Page.passengerList.setModel(model_Passenger);
			}
		}else {
			System.out.println("点击乘车人");
		}
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
