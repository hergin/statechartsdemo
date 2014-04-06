package edu.ua.cs.hergin.android.statechartsdemo;

import edu.ua.cs.hergin.android.statecharts.HierarchicalState;
import edu.ua.cs.hergin.android.statecharts.Metadata;
import edu.ua.cs.hergin.android.statecharts.PseudoState;
import edu.ua.cs.hergin.android.statecharts.State;
import edu.ua.cs.hergin.android.statecharts.Statechart;
import edu.ua.cs.hergin.android.statecharts.StatechartException;
import edu.ua.cs.hergin.android.statecharts.TimeoutEvent;
import edu.ua.cs.hergin.android.statecharts.Transition;
import edu.ua.cs.hergin.android.statechartsdemo.actions.AllOffAction;
import edu.ua.cs.hergin.android.statechartsdemo.actions.DeadAction;
import edu.ua.cs.hergin.android.statechartsdemo.actions.GreenAction;
import edu.ua.cs.hergin.android.statechartsdemo.actions.GreenSoonAction;
import edu.ua.cs.hergin.android.statechartsdemo.actions.RedAction;
import edu.ua.cs.hergin.android.statechartsdemo.actions.YellowAction;
import edu.ua.cs.hergin.android.statechartsdemo.events.CrosswalkEvent;
import edu.ua.cs.hergin.android.statechartsdemo.events.OnOffEvent;
import edu.ua.cs.hergin.android.statechartsdemo.events.PoliceEvent;
import edu.ua.cs.hergin.android.statechartsdemo.events.QuitEvent;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Statechart statechart;
	private Metadata metadata;
	private ImageView trafficLightsImage;

	// we are using handler else non-ui threads can't modify UI
	protected Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TrafficLightsConst.GREEN:
				trafficLightsImage.setImageResource(R.drawable.green);
				break;
			case TrafficLightsConst.RED:
				trafficLightsImage.setImageResource(R.drawable.red);
				break;

			case TrafficLightsConst.YELLOW:
				trafficLightsImage.setImageResource(R.drawable.yellow);
				break;

			case TrafficLightsConst.GREENSOON:
				trafficLightsImage.setImageResource(R.drawable.greensoon);
				break;

			case TrafficLightsConst.ALLOFF:
				trafficLightsImage.setImageResource(R.drawable.all_off);
				break;

			case TrafficLightsConst.DEAD:
				trafficLightsImage.setImageResource(R.drawable.all_off);
				((LinearLayout) findViewById(R.id.eventsLayout))
						.setVisibility(View.GONE);
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		trafficLightsImage = ((ImageView) findViewById(R.id.trafficLightsImage));

		if (statechart == null) {
			createStateChart();

			metadata = new Metadata();
			statechart.start(metadata);
		}
	}

	private void createStateChart() {
		// TODO auto generate this code (and action/event codes) from scxml
		try {
			// Statechart itself
			statechart = new Statechart("Traffic Lights");

			// States
			PseudoState startState = new PseudoState("start", statechart,
					PseudoState.pseudostate_start);

			State offState = new State("Off", statechart, null,
					new AllOffAction(handler), null);
			State deadState = new State("Dead", statechart, null,
					new DeadAction(handler), null);

			HierarchicalState onState = new HierarchicalState("On", statechart,
					null, null, null);
			PseudoState onStateStart = new PseudoState("On State Start",
					onState, PseudoState.pseudostate_start);
			PseudoState onStateHistory = new PseudoState("On State History",
					onState, PseudoState.pseudostate_deep_history);

			HierarchicalState normalState = new HierarchicalState("Normal",
					onState, null, null, null);
			PseudoState normalStateStart = new PseudoState(
					"Normal State Start", normalState,
					PseudoState.pseudostate_start);

			HierarchicalState redState = new HierarchicalState("Red",
					normalState, null, null, null);
			PseudoState redStateStart = new PseudoState("Red State Start",
					redState, PseudoState.pseudostate_start);

			State greenSoonState = new State("Green Soon", redState, null,
					new GreenSoonAction(handler), null);
			State greenState = new State("Green", normalState, null,
					new GreenAction(handler), null);
			State yellowState = new State("Yellow", normalState, null,
					new YellowAction(handler), null);
			State redWaitState = new State("Red Wait", redState, null,
					new RedAction(handler), null);

			HierarchicalState flashingState = new HierarchicalState("Flashing",
					onState, null, null, null);
			PseudoState flashingStateStart = new PseudoState(
					"Flashing State Start", flashingState,
					PseudoState.pseudostate_start);

			State yellowOnState = new State("Yellow On", flashingState, null,
					new YellowAction(handler), null);
			State yellowOffState = new State("Yellow Off", flashingState, null,
					new AllOffAction(handler), null);

			// Transitions
			new Transition(startState, onState);
			new Transition(onStateStart, onStateHistory);
			new Transition(onStateHistory, redState);
			// if a history is put, the transitions will be
			// (start->history->actual_start)

			new Transition(onState, deadState, new QuitEvent());
			new Transition(onState, offState, new OnOffEvent());
			new Transition(offState, onStateHistory, new OnOffEvent());

			new Transition(flashingStateStart, yellowOnState);
			new Transition(yellowOnState, yellowOffState, new TimeoutEvent(500));
			new Transition(yellowOffState, yellowOnState, new TimeoutEvent(500));

			new Transition(normalState, flashingState, new PoliceEvent());
			new Transition(flashingState, normalState, new PoliceEvent());

			new Transition(normalStateStart, redState);
			new Transition(redStateStart, redWaitState);
			new Transition(redWaitState, greenSoonState, new CrosswalkEvent());
			new Transition(redWaitState, greenSoonState, new TimeoutEvent(6000));
			new Transition(greenSoonState, greenState, new TimeoutEvent(2000));
			new Transition(greenState, yellowState, new TimeoutEvent(5000));
			new Transition(yellowState, redState, new TimeoutEvent(2000));

		} catch (StatechartException e) {
			Toast.makeText(this,
					"There's something wrong with the statechart!",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void crosswalk(View v) {
		statechart.dispatch(metadata, new CrosswalkEvent());
	}

	public void police(View v) {
		statechart.dispatch(metadata, new PoliceEvent());
	}

	public void onoff(View v) {
		statechart.dispatch(metadata, new OnOffEvent());
	}

	public void quit(View v) {
		statechart.dispatch(metadata, new QuitEvent());
	}

}
