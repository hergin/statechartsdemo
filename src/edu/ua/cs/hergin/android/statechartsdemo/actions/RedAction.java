package edu.ua.cs.hergin.android.statechartsdemo.actions;

import android.os.Handler;
import android.os.Message;
import edu.ua.cs.hergin.android.statecharts.Action;
import edu.ua.cs.hergin.android.statecharts.Metadata;
import edu.ua.cs.hergin.android.statecharts.Parameter;
import edu.ua.cs.hergin.android.statechartsdemo.TrafficLightsConst;

public class RedAction implements Action {

	private Handler handler;

	public RedAction(Handler h) {
		handler = h;
	}

	@Override
	public void execute(Metadata data, Parameter param) {
		Message message = handler.obtainMessage();
		message.what = TrafficLightsConst.RED;
		handler.sendMessage(message);
	}

}
