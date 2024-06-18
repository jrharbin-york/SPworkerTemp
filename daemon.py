from netifaces import interfaces, ifaddresses, AF_INET
from enum import IntEnum
import subprocess
import Pyro5.api
import threading
import time
import uuid
import sys
import argparse
import os

# Circus: https://circus.readthedocs.io/en/latest/for-devs/library/#library
# Circus library can test e.g. the process status

# Worker daemon should also support STOP method... to e.g. terminate a test early
# Pre-test

# Build/run with pyinstaller
VERSION_ID = 0.31

def get_en_ip():
    for ifaceName in interfaces():
        if "en" in ifaceName:
            addresses = [i['addr'] for i in ifaddresses(ifaceName).setdefault(AF_INET, [{'addr':'No IP addr'}] )]
            for a in addresses:
                return a

def get_en_ip_as_underscores():
    a = get_en_ip()
    return a.replace(".", "_")

parser = argparse.ArgumentParser(description="SOPRANO Worker Node Daemon")

parser.add_argument("--worker_ip",
                    dest="worker_ip",
                    help="the IP address to use for this node (rather than auto-detection)",
                    default = get_en_ip())

parser.add_argument("--worker_port",
                    dest="worker_port",
                    help="the port to use for the worker node",
                    default = 9600)

parser.add_argument("--expt_runner_ip",
                    dest="expt_runner_ip",
                    help="the IP address for the experiment runner",
                    required = True)

parser.add_argument("--expt_runner_user",
                    dest="expt_runner_user",
                    help="The user name for the experiment runner",
                    required = True)

parser.add_argument("--nameserver_port",
                    dest="nameserver_port",
                    help="the port to use for the worker nameserver",
                    default = 9523)

parser.add_argument("--debug_polling",
                    dest="debug_polling",
                    help="Debug the polling on the worker node",
                    default = False)

parser.add_argument("--debug_metric_updates",
                    dest="debug_metric_updates",
                    help="Debug the metric updates on the worker node",
                    default = False)

args = parser.parse_args()

node_name = args.worker_ip.replace(".", "_")
pyro_daemon_full_name = "SOPRANOWorkerDaemon_" + node_name
print("SOPRANO Worker Daemon node name: ", pyro_daemon_full_name)

# TODO: rename 'job' to 'test' throughout

# TODO: this needs to be supplied in the experiment config
#RUN_PATH = "/home/jharbin/eclipse-workspace/PALTesting"
REMOTE_CODE_DIRECTORY = "/samba/shared-soprano-code/"
REMOTE_CODE_PATH = args.expt_runner_user + "@" + args.expt_runner_ip + ":" + REMOTE_CODE_DIRECTORY

print("REMOTE_CODE_PATH: " + str(REMOTE_CODE_PATH))

LOCAL_RUN_PATH = os.getcwd() + "/soprano_code/"

LOCAL_SCRIPT_PATH = LOCAL_RUN_PATH + "/scripts"

# Resync command is in the local script directory
RESYNC_CMD = "./scripts/rsync_via_ssh.sh"
COMPILE_CMD = LOCAL_SCRIPT_PATH + "/compile.sh"
EXPT_RUNNER_CMD = LOCAL_SCRIPT_PATH + "/execute.sh"
TERMINATE_CMD = LOCAL_SCRIPT_PATH + "/terminate.sh"
CLEANUP_CMD = LOCAL_SCRIPT_PATH + "/cleanup.sh"

class DepCheckCodes(IntEnum):
    DEPS_OK = 0
    MISSING_DEP = 1

class TestStatusCodes(IntEnum):
    RUNNING = 0
    PENDING = 1
    COMPLETED = 2
    FAILED = 3

class MetricRegisterCode(IntEnum):
    OK = 0
    FAILED_INVALID_TEST = 1

# TODO: experiment is also a test campaign
class ExptConfig:
    def __init__(self,expt_name, dependency_spec):
        # TODO: how to get the class ID included here
        # TODO: how to setup the metrics
        self.expt_name = expt_name
        self.unique_run_id = str(uuid.uuid4())
        self.dependency_spec = dependency_spec

    def preinit(self):
        print("Preinitialising experiment: " + str(self))
        # TODO: Need to ensure all the dependencies are ready here!
        # e.g. docker, java/maven packages - from dependency_spec

    def get_unique_run_id(self):
        return self.unique_run_id

class TestRunJob:
    def __init__(self,test_id):
        # TODO: how to get the class ID included here
        self.test_id = test_id
        self.unique_run_id = str(uuid.uuid4())
        self.metric_values = {}

    def compile(self):
        print("Performing compilation for " + self.test_id)
        script_output = subprocess.call([COMPILE_CMD, LOCAL_RUN_PATH])
        print("Resync output:", script_output)
        return script_output
        
    def resync(self):
        script_output = subprocess.call([RESYNC_CMD, REMOTE_CODE_PATH, LOCAL_RUN_PATH])
        print("Resync output:", script_output)
        return script_output
        
    def prepare(self):
        # TODO: do the resync of the directory here
        resync_output = self.resync()
        if (resync_output == 0):
            return self.compile()
        else:
            return resync_output

    def execute(self):
        #Need to find named class file and generate the relevant run command!
        # The classname for the testrunner can be pro
        classname = self.test_id + "_TestRunner"
        print("Executing job for", self.test_id, ": classname is", classname)
        script_output = subprocess.call([EXPT_RUNNER_CMD, classname, LOCAL_RUN_PATH, pyro_daemon_full_name])
        return script_output

    def terminate(self):
        script_output = subprocess.call([TERMINATE_CMD])
        print("Terminate output:", script_output)
        return script_output
    
    def handle(self):
        # Ensure the metrics are cleared before starting
        self.metric_values = {}
        self.prepare()
        self.execute()
        self.terminate()

    def get_unique_run_id(self):
        return self.unique_run_id

    def get_test_id(self):
        return self.test_id

    def update_metric_for_test(self, metric_name, metric_value, timestamp):
        self.metric_values[metric_name] = metric_value
        if args.debug_metric_updates:
            print(str(self) + " - update_metric " + metric_name + "=" + str(metric_value) + " (timestamp " + str(timestamp) + ")")
            return int(MetricRegisterCode.OK)
    
    def get_all_metrics_for_test(self):
        return self.metric_values

daemon = Pyro5.api.Daemon(host=args.worker_ip, port=args.worker_port)
    
class WorkManager:
    def __init__(self, name):
        self.pending_jobs = []
        self.watcher = threading.Thread(target=self.watch_job_queue, name="WorkManagerWatcher", daemon=True)
        self.current_expt = {}
        self.active_test = None

        # This maps the job unqiue run ID to a hash of info
        self.job_run_info = {}

        # The ID of the currently running test. We assume each node is only running one test simultaneously
        self.current_test_id = ""

        self.watcher.start()

        # Need to ensure we cannot start an experiment before another is completed!

    def set_job_status(self, job, status):
        urun_id = job.get_unique_run_id()
        self.job_run_info[urun_id]['status'] = status
        
    def watch_job_queue(self):
        while True:
            print("Polling incoming work queue - " + str(len(self.pending_jobs)) + " jobs registered")
            while len(self.pending_jobs) > 0:
                job = self.pending_jobs.pop()
                self.active_test = job
                # Job is now running here
                self.current_test_id = job.get_test_id()
                self.set_job_status(job, TestStatusCodes.RUNNING)
                # Need to check nothing else is running now!
                job.handle()
		# block for a moment before testing again
                self.set_job_status(job, TestStatusCodes.COMPLETED)
                self.active_test = None
            time.sleep(1)

    def get_current_test_id(self):
        return self.current_test_id

    def status_of_job(self, urun_id):
        # Need to verify if the process is still running!
        if urun_id in self.job_run_info:
            return self.job_run_info[urun_id]['status']
        else:
            return None
        
    def submit_test(self, j):
        self.pending_jobs.append(j)
        urun_id = j.get_unique_run_id()
        self.job_run_info[urun_id] = { 'status' : TestStatusCodes.PENDING }
        print("Job Manager: added job "+ str(j) + "- queue length is now " + str(len(self.pending_jobs)))

    def submit_experiment(self, expt):
        self.current_expt = expt
        # TODO: check if an experiment is currently running
        # TODO: check the dependencies
        print("Submitting experiment" + exptconfig)

    def active_test_id_matches(self, given_test_id):
        if self.active_test is None:
            return False
        else:
            return (self.active_test.get_test_id() == given_test_id)

    def update_metric(self, update_test_id, metric_name, metric_value, timestamp):
        # Verify that the test ID matches here
        jobmanager_test_id = self.get_current_test_id()
        if self.active_test_id_matches(update_test_id):
            return self.active_test.update_metric_for_test(metric_name, metric_value, timestamp)
        else:
            print(str(self) + "METRIC rejected due to invalid test id - should be", jobmanager_test_id, " - received", update_test_id)
            return int(MetricRegisterCode.FAILED_INVALID_TEST)
        
    def get_all_metrics(self, target_test_id):
        # TODO: Receive the pending metrics for this
        if self.active_test_id_matches(target_test_id):
            return self.active_test.get_all_metrics_for_test()
        else:
            return {}

jobmanager = WorkManager("SOPRANO")

@Pyro5.api.expose
# Instance mode single is needed, otherwise every Pyro call creates a unique object!
@Pyro5.server.behavior(instance_mode="single")
class SopranoWorkerDaemon(object):
    def __init__(self):
        # TODO: how to get the class ID included here
        self.metric_values = {}
    
    def get_version_id(self):
        return VERSION_ID

    def init_experiment(self, expt_name_dsl, dependencies):
        expt= ExptConfig(expt_name_dsl, dependencies)
        urun_id = exptj.get_unique_run_id()
        print("Checking the configuration for experiment campaign name in DSL - " + str(expt_name_dsl))
        depstatus = jobmanager.check_dependencies(expt)
        if (depstatus == DepCheckCodes.DEPS_OK):
              print("Dependencies OK... submitting experiment to expt manager")
              submit_experiment(expt)
        else:
              print("Dependency error")
              # TODO: details about the missing dependency

    def submit_test(self, test_id):
        # Need to verify preconditions - e.g. are the classes/etc availble in the filesystem?
        testj = TestRunJob(test_id)
        urun_id = testj.get_unique_run_id()
        print("Submitted job to run test ID " + str(test_id))
        jobmanager.submit_test(testj)
        return urun_id

    def poll_for_status(self, urun_id, test_id):
        status = jobmanager.status_of_job(urun_id)
        if args.debug_polling:
            print("poll_for_status for test ID ", test_id, + str(urun_id) + " = " + str(status))
        return int(status)

    def update_metric(self, update_test_id, metric_name, metric_value, timestamp):
        return jobmanager.update_metric(update_test_id, metric_name, metric_value, timestamp)
    
    def get_all_metrics(self, target_test_id):
        return jobmanager.get_all_metrics(target_test_id)
    
wd_uri = daemon.register(SopranoWorkerDaemon)
ns = Pyro5.core.locate_ns(host = args.expt_runner_ip, port = args.nameserver_port)
print("SOPRANO Worker Daemon Ready: Object uri =", wd_uri)       # print the uri so we can use it in the client later

ns.register(pyro_daemon_full_name, wd_uri)

print("Worker daemon registered with nameserver")

daemon.requestLoop()                    # start the event loop of the server to wait for calls
