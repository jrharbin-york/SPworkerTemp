
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.connector.file.sink.FileSink;

import org.apache.flink.configuration.Configuration;

import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import uk.ac.york.sesame.testing.architecture.metrics.flink.PyroMetricFlinkSink;
import uk.ac.york.sesame.testing.architecture.metrics.flink.TriggeredControlProducer;
import uk.ac.york.sesame.testing.architecture.fuzzingoperations.*;
import uk.ac.york.sesame.testing.architecture.ros.ROSSimulator;
import uk.ac.york.sesame.testing.architecture.config.ConnectionProperties;
import uk.ac.york.sesame.testing.architecture.data.*;

import uk.ac.york.sesame.testing.architecture.simulator.SimCore;
import uk.ac.york.sesame.testing.evolutionary.utilities.TestRunnerUtils;
import uk.ac.york.sesame.testing.architecture.simulator.SubscriptionFailure;

import metrics.custom.*;
import metrics.fixed.*;

import net.razorvine.pyro.*;

public class Test_001_06_10_2024_04_35_31_TestRunner_paramtest {
	public static void main(String[] args) {
	
		// Ensure we get the worker lookup name from the supplied arg, or exit
		String PYRO_WORKER_LOOKUP_NAME = "SOPRANOWorkerDaemon_192_168_1_238";
		final String TESTNAME = "Test_001_06_10_2024_04_35_31";
		
		final boolean QUIT_MIDDLEWARE_ON_TOPIC_SUBSCRIPTION_FAILURE = true;
		
		// JRH: needed to increase the number of buffers used
		Configuration cfg = new Configuration();
		int defaultLocalParallelism = 1;
		cfg.setString("taskmanager.network.numberOfBuffers", "4096");
		StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment(defaultLocalParallelism, cfg);
		
		
		Properties properties = new Properties();
		properties.setProperty("bootstrap.servers", "localhost:9092");
		properties.setProperty("group.id", "test");
		
			// TODO: finish TriggeredControlProducer producer here
			DataStream<ControlMessage> controlStream = env
					.addSource(new TriggeredControlProducer(11.0));
					
		env.setParallelism(1);
	    env.setMaxParallelism(1);
		
		DataStream<EventMessage> streamOrig = env
			.addSource(new FlinkKafkaConsumer<EventMessage>("IN", new EventMessageSchema(), properties)).returns(EventMessage.class);
			
			DataStream<EventMessage> stream = streamOrig;
			
		
		DataStream<EventMessage> streamOut = env
				.addSource(new FlinkKafkaConsumer<EventMessage>("OUT", new EventMessageSchema(), properties))
				.returns(EventMessage.class);
		
		// Kafka Sink to OUT
		FlinkKafkaProducer<EventMessage> myProducer = new FlinkKafkaProducer<EventMessage>("OUT", // target topic
				new EventMessageSchema(), properties);
							
        // TODO: this needs to be set up in the DSL and encoded here by the generator
        String PYRO_NS_HOSTNAME = "192.168.1.238";
        int PYRO_NS_PORT = 9523;
        PyroMetricFlinkSink pyroMetricFlinkSink = new PyroMetricFlinkSink(TESTNAME, PYRO_NS_HOSTNAME, PYRO_NS_PORT, PYRO_WORKER_LOOKUP_NAME);
		
		ROSSimulator rosSim = new ROSSimulator();
		ConnectionProperties cp = new ConnectionProperties();
		HashMap<String, Object> propsMap = new HashMap<String, Object>();
		propsMap.put(ConnectionProperties.HOSTNAME, "0.0.0.0");
		propsMap.put(ConnectionProperties.PORT, 9090);
		cp.setProperties(propsMap);
		
		// JRH: moved the simulation launcher outside of the thread to the main code
		HashMap<String, String> params = new HashMap<String,String>();
		params.put("launchPath", "/home/jharbin/academic/soprano/turtlesim-example/docker/run_turtlesim.sh");
		params.put("testID", TESTNAME);
		
		// Preprocessing before simulation starts
		System.out.println("Preprocessing Phase");
		
		SimCore.getInstance().setSimulatorInterface(rosSim);
		System.out.println("Simulator Starts");
		//rosSim.run(params);
		// TODO: this delay needs to be configurable
		waitForSeconds(10);
		
		System.out.println("Simulator run period completed - connecting to topics");
		rosSim.connect(cp);
		
		FuzzingOperation fuzzOp0 = new DistortVelocity_0_Test_001_06_10_2024_04_35_31_paramtest(); // TODO: add the timing/condition in a comment here; 
		fuzzOp0.preprocessing();
				
		Thread subscriber_thread__turtle1_cmd_velIN = new Thread() {
			public void run() {
				try {
					System.out.println("Subscriber _turtle1_cmd_velIN Starts");
					rosSim.consumeFromTopic("/turtle1/cmd_vel", "geometry_msgs/msg/Twist", true, "IN");
				} catch (SubscriptionFailure e) {
					if (QUIT_MIDDLEWARE_ON_TOPIC_SUBSCRIPTION_FAILURE) {
						System.out.println("Subscription to _turtle1_cmd_velIN Failed - Exiting Middleware");
						System.exit(1);
					} else {
						System.out.println("Subscription to _turtle1_cmd_velIN Failed - Continuing");
					}
				}
			}
		};
		
		subscriber_thread__turtle1_cmd_velIN.start();
		Thread subscriber_thread__turtle1_pose = new Thread() {
			public void run() {
				try {
					System.out.println("Subscriber _turtle1_pose Starts");
					rosSim.consumeFromTopic("/turtle1/pose", "turtlesim/msg/Pose", true, "IN");
				} catch (SubscriptionFailure e) {
					if (QUIT_MIDDLEWARE_ON_TOPIC_SUBSCRIPTION_FAILURE) {
						System.out.println("Subscription to _turtle1_pose Failed - Exiting Middleware");
						System.exit(1);
					} else {
						System.out.println("Subscription to _turtle1_pose Failed - Continuing");
					}
				}
			}
		};
		subscriber_thread__turtle1_pose.start();

		Thread time_subscriber = new Thread() {
			public void run() {
				System.out.println("updateTime starting");
				try {
					rosSim.updateTime();
				} catch (SubscriptionFailure e) {
					if (QUIT_MIDDLEWARE_ON_TOPIC_SUBSCRIPTION_FAILURE) {
						System.out.println("Subscription to time failed - Exiting Middleware");
						System.exit(1);
					} else {
						System.out.println("Subscription to time failed - Continuing");
					}
				}
			}
		};
		time_subscriber.start();
		
		Thread from_out_to_sim = new Thread() {
			public void run() {
				System.out.println("From out to sim starts");
				while (true) {
					SimCore.getInstance().processDeferredActions();
					
					ConsumerRecords<Long, EventMessage> cr = DataStreamManager.getInstance().consume("OUT");
					for (ConsumerRecord<Long, EventMessage> record : cr) {
						String inTopic = record.value().getTopic().toString();
						String outTopic = rosSim.translateTopicNameForOutput(inTopic);
						System.out.println("OUT TO SIM: inTopic = " + inTopic + " -> " + outTopic);
						rosSim.publishToTopic(outTopic, record.value().getType(), record.value().getValue().toString());
					}
				}
			}
		}; 
		
		from_out_to_sim.start();
		
		// Generate keyed streams
		KeyedStream<EventMessage, String> topicKeyedIn = stream.keyBy(value -> value.getTopic());
		KeyedStream<EventMessage, String> topicKeyedOut = streamOut.keyBy(value -> value.getTopic());
		KeyedStream<EventMessage, String> testKeyedIn = stream.keyBy(value -> "Test_001_06_10_2024_04_35_31");
		KeyedStream<EventMessage, String> testKeyedOut = streamOut.keyBy(value -> "Test_001_06_10_2024_04_35_31");;
		
		
		ConnectedStreams<EventMessage,ControlMessage> eventsAndControlIn = testKeyedIn.connect(controlStream).keyBy(e -> TESTNAME,m -> TESTNAME);
		ConnectedStreams<EventMessage,ControlMessage> eventsAndControlOut = testKeyedOut.connect(controlStream).keyBy(e -> TESTNAME,m -> TESTNAME);
		
		List<Double> fuzzOpTimes = new ArrayList<Double>(); 
		
				fuzzOpTimes.add(Double.valueOf(0.024267351226172185));
		
		
			
				DataStream<Double> fuzzingOperationTimesresStream = eventsAndControlIn.process(new fuzzingOperationTimesMetric(fuzzOpTimes));
			
				DataStream<Double> distanceToPointresStream = eventsAndControlIn.process(new distanceToPoint3DMetric());
		
		
		SimCore simcore = SimCore.getInstance();
		simcore.setTestName(TESTNAME);
		
 
			// Generate a message stream for all metrics
			DataStream<MetricMessage> metricMsgdistanceToPoint = distanceToPointresStream.map(d -> new MetricMessage("Test_001_06_10_2024_04_35_31", "distanceToPoint", d));
 
			// Generate a message stream for all metrics
			DataStream<MetricMessage> metricMsgfuzzingOperationTimes = fuzzingOperationTimesresStream.map(d -> new MetricMessage("Test_001_06_10_2024_04_35_31", "fuzzingOperationTimes", d));
		
		
		DataStream<MetricMessage> allMetrics = 		
		metricMsgdistanceToPoint
		
		.union(metricMsgfuzzingOperationTimes)
	
;
		
		
		DataStream<MetricMessage> campaignMetrics =
		metricMsgfuzzingOperationTimes
		
		.union(metricMsgdistanceToPoint)
	
;
		
		campaignMetrics.addSink(pyroMetricFlinkSink);
	
		
		ConnectedStreams<EventMessage,MetricMessage> streamFO0 = stream.connect(allMetrics).keyBy(e -> TESTNAME,m -> TESTNAME);
		
		   		DataStream<EventMessage> streamFO1 = streamFO0.process(fuzzOp0);
			streamFO1.addSink(myProducer);
		
		try {
			//env.execute();
			env.executeAsync();
			
			waitForEndTrigger();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void waitForEndTrigger() {
		// Need to receive the clock from the metric
		// Wait 1 second each time, check if we have everything to exit or not
		// Or use another thread that calls exit?
			
		//; TODO: other execution end triggers need to be handled here other than fixed time
		
		boolean done = false;
		
		double wall_seconds_time = 0.0;
		while (!done) {
			waitForSeconds(1);
			wall_seconds_time += 1.0;
			double time = SimCore.getInstance().getTime();
			if (time > 5000) {
				done = true;
			}
		}
		
		// TODO: need to trigger the control message here
		System.out.println("----------------- TESTRUNNER TERMINATING ---------------------");
		System.exit(0);
	}
	
	public static void waitForSeconds(long timeDelaySeconds) {
		long endTimeMillis = System.currentTimeMillis() + timeDelaySeconds * 1000;
		while (System.currentTimeMillis() < endTimeMillis) {
			try {
				Thread.sleep(endTimeMillis - System.currentTimeMillis());
			} catch (InterruptedException e) {
				
			}
		}
	}
}

