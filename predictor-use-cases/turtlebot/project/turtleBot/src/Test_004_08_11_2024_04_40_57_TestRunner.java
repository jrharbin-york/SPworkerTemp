
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Properties;
import org.apache.flink.configuration.Configuration;

import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import uk.ac.york.sesame.testing.architecture.metrics.flink.PyroMetricFlinkSink;
import uk.ac.york.sesame.testing.architecture.metrics.flink.TriggeredControlProducer;
import uk.ac.york.sesame.testing.architecture.fuzzingoperations.*;
import uk.ac.york.sesame.testing.architecture.ros.ROSSimulator;
import uk.ac.york.sesame.testing.architecture.config.ConnectionProperties;
import uk.ac.york.sesame.testing.architecture.data.*;

import uk.ac.york.sesame.testing.architecture.simulator.SimCore;
import uk.ac.york.sesame.testing.architecture.simulator.SubscriptionFailure;

import metrics.custom.*;
import metrics.fixed.*;

public class Test_004_08_11_2024_04_40_57_TestRunner {
	public static void main(String[] args) {
		// Ensure we get the worker lookup name from the supplied arg, or exit
		String PYRO_WORKER_LOOKUP_NAME = "SOPRANOWorkerDaemon_192_168_1_238";
	
		final String TESTNAME = "Test_004_08_11_2024_04_40_57";
		
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
					.addSource(new TriggeredControlProducer(99.0));
			
					
		
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
		params.put("launchPath", "/home/jharbin/academic/soprano/turtlebot/docker/run_turtlebot.sh");
		params.put("testID", TESTNAME);
		
		// Preprocessing before simulation starts
		System.out.println("Preprocessing Phase");
		
		FuzzingOperation fuzzOp0 = new DistortVelocity_0_Test_004_08_11_2024_04_40_57(); // TODO: add the timing/condition in a comment here; 
		fuzzOp0.preprocessing();
				
		//System.out.println("Simulator Starts");
		rosSim.run(params);
		// TODO: this delay needs to be configurable
		//waitForSeconds(10);
		
		System.out.println("Simulator run period completed - connecting to topics");
		rosSim.connect(cp);
				
		Thread subscriber_thread__cmd_vel_nav = new Thread() {
			public void run() {
				try {
					System.out.println("Subscriber _cmd_vel_nav Starts");
					rosSim.consumeFromTopic("/cmd_vel_nav", "geometry_msgs/msg/Twist", true, "IN");
				} catch (SubscriptionFailure e) {
					if (QUIT_MIDDLEWARE_ON_TOPIC_SUBSCRIPTION_FAILURE) {
						System.out.println("Subscription to _cmd_vel_nav Failed - Exiting Middleware");
						System.exit(1);
					} else {
						System.out.println("Subscription to _cmd_vel_nav Failed - Continuing");
					}
				}
			}
		};
		subscriber_thread__cmd_vel_nav.start();
		Thread subscriber_thread__odom = new Thread() {
			public void run() {
				try {
					System.out.println("Subscriber _odom Starts");
					rosSim.consumeFromTopic("/odom", "nav_msgs/msg/Odometry", true, "IN");
				} catch (SubscriptionFailure e) {
					if (QUIT_MIDDLEWARE_ON_TOPIC_SUBSCRIPTION_FAILURE) {
						System.out.println("Subscription to _odom Failed - Exiting Middleware");
						System.exit(1);
					} else {
						System.out.println("Subscription to _odom Failed - Continuing");
					}
				}
			}
		};
		subscriber_thread__odom.start();

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
		KeyedStream<EventMessage, String> testKeyedIn = stream.keyBy(value -> "Test_004_08_11_2024_04_40_57");
		KeyedStream<EventMessage, String> testKeyedOut = streamOut.keyBy(value -> "Test_004_08_11_2024_04_40_57");;
		
		
		ConnectedStreams<EventMessage,ControlMessage> eventsAndControlIn = testKeyedIn.connect(controlStream).keyBy(e -> TESTNAME,m -> TESTNAME);
		ConnectedStreams<EventMessage,ControlMessage> eventsAndControlOut = testKeyedOut.connect(controlStream).keyBy(e -> TESTNAME,m -> TESTNAME);
		
		List<Double> fuzzOpTimes = new ArrayList<Double>(); 
		
				fuzzOpTimes.add(Double.valueOf(9.60858571543838));
		
		
			
				DataStream<Double> fuzzingOperationTimesresStream = eventsAndControlIn.process(new fuzzingOperationTimesMetric(fuzzOpTimes));
			
				DataStream<Double> distanceToPoint3DresStream = eventsAndControlIn.process(new distanceToPoint3DMetric());
		
		
		SimCore simcore = SimCore.getInstance();
		simcore.setTestName(TESTNAME);
		
 
			// Generate a message stream for all metrics
			DataStream<MetricMessage> metricMsgdistanceToPoint3D = distanceToPoint3DresStream.map(d -> new MetricMessage("Test_004_08_11_2024_04_40_57", "distanceToPoint3D", d));
 
			// Generate a message stream for all metrics
			DataStream<MetricMessage> metricMsgfuzzingOperationTimes = fuzzingOperationTimesresStream.map(d -> new MetricMessage("Test_004_08_11_2024_04_40_57", "fuzzingOperationTimes", d));
		
		
		DataStream<MetricMessage> allMetrics = 		
		metricMsgdistanceToPoint3D
		
		.union(metricMsgfuzzingOperationTimes)
	
;
		
		
		DataStream<MetricMessage> campaignMetrics =
		metricMsgfuzzingOperationTimes
		
		.union(metricMsgdistanceToPoint3D)
	
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
			if (time > 10000) {
				done = true;
			}
			
			if (wall_seconds_time > (10.0 * 100)) {
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

