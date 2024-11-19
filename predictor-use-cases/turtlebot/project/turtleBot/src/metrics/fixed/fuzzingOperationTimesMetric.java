package metrics.fixed;

import java.util.ArrayList;
import java.util.List;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

import uk.ac.york.sesame.testing.architecture.data.ControlMessage;
import uk.ac.york.sesame.testing.architecture.data.EventMessage;
import uk.ac.york.sesame.testing.architecture.metrics.Metric;
import uk.ac.york.sesame.testing.architecture.simulator.SimCore;

public class fuzzingOperationTimesMetric extends Metric {

	private static final long serialVersionUID = 1L;
	private List<Double> attackTimes = new ArrayList<Double>();
	   
	public fuzzingOperationTimesMetric(List<Double> attackTimes) {
		this.attackTimes = attackTimes;
	}
	
    public void open(Configuration parameters) throws Exception {
    	
    }
      
    public void processElement1(EventMessage msg, Context ctx, Collector<Double> out) throws Exception {

    }
    
	public void processElement2(ControlMessage controlMsg, CoProcessFunction<EventMessage, ControlMessage, Double>.Context ctx, Collector<Double> out) throws Exception {
		// Transmit this on the finalisation
		SimCore.getInstance().finaliseFuzzingTimes();
    	double total = 0.0;
    	for (Double d : attackTimes) {
    		total += d;
    	}
    	
    	total = total + SimCore.getInstance().getTotalFuzzingSeconds();
    	out.collect(total);		
		
	}
}