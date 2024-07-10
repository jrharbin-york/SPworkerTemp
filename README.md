# SPworkerTemp
Temporary repository to ease the worker daemon setup

## Installation instructions (internal)

## Depedencies

* This assumes a worker OS is Ubuntu 20.04
* Install Kafka 2.13-3.1.0 currently
* ROS components on worker too - all ROS components

```
sudo apt-get install python-is-python3 python3-venv python3-pip maven openjdk-11-jdk openjdk-17-jdk
docker run hello-world # - verify docker works as normal user

```

If Docker is not working as your user, check socket permissions and then try again
(may have to log out and in again, or reboot)

### Set up the SESAME code (distributed branch)

The SESAME code is needed to process and run the test runners:

```
mkdir ~/academic/sesame
git clone https://github.com/sesame-project/simulationBasedTesting
cd simulationBasedTesting
git checkout distributed-expt
```

Need to install the mvn arctifacts for the project from SESAME code:

```
cd ~/academic/sesame/simulationBasedTesting/uk.ac.york.sesame.testing.architecture
mvn install
cd ~/academic/sesame/simulationBasedTesting/jrosbridge
mvn install -Dmaven.test.skip=true
# mvn install -Dskiptests
# If it still runs tests, do Ctrl-C
cd ~/academic/sesame/simulationBasedTesting/uk.ac.york.sesame.testing.architecture.ros
mvn install
cd ~/academic/sesame/simulationBasedTesting/uk.ac.york.sesame.testing.dsl
mvn install
cd ~/academic/sesame/simulationBasedTesintg/jgea
mvn install
cd ~/academic/sesame/simulationBasedTesting/uk.ac.york.sesame.testing.evolutionary
mvn install
```

### Checkout the worker daemon from this repo

```
mkdir ~/academic/soprano
Checkout this repo for the daemon here
```

### Setup a virtualenv for needed Python packages and install daemon

```
python -m venv ~/academic/pyro
. ~/academic/pyro/bin/activate
pip install pyro5 pyinstaller netifaces structlog docker

cd ~/academic/soprano/REPO
pyinstaller daemon.py
```

### Setup an SSH key and copy to experiment manager
This is needed to synchronize the generated code with the experiment manager
```
ssh-keygen -t rsa
ssh-copy-id -i ~/.ssh/id_rsa jharbin@expt_manager_ip
```

### Before running experiment (PAL example)
The Pyro nameserver is now set up on the experiment runner, not on a worker.
It must be started first.

* Set up PAL from the containers
* Ensure PAL experiment script present at the start up location specified in the DSL model.
* Ensure Kafka/Zookeeper started
* Enter the virtualenv with the activate script

Start up daemon with expt runnner IP and port:
```
cd ~/academic/soprano/REPO
./dist/daemon/daemon --expt_runner_ip=192.168.1.28 --expt_runner_user=jharbin
```

NB. The daemon needs to be run under the dist directory from
pyinstaller; because otherwise e.g. if it is run by e.g. "python3 ./daemon.py", it will be
killed during the terminate script after a particular test!

* Ensure the IP of the experiment runner is set up in the model
