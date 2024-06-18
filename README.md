# SPworkerTemp
Temporary repository to ease the worker daemon setup

## Installation instructions (internal)

## Depedencies

* This assumes a worker OS is Ubuntu 20.04
* Install Kafka 2.13-3.1.0 currently
* ROS components on worker too - all ROS components

```
apt-get install python-is-python3 python3-venv python3-pip maven openjdk-11-jdk
docker run hello-world # - verify docker works
```
If Docker is not working as your user, verify it works.


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
mvn install -DskipTests
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

### Setup a virtualenv for needed Python packages

```
python -m venv ~/academic/pyro
. ~/academic/pyro/bin/activate
pip install pyro5 pyinstaller netifaces

cd ~/academic/soprano/REPO
pyinstaller daemon.py
```

### Setup an SSH key and copy to experiment manager
```
ssh-keygen -t rsa
ssh-copy-id -i ~/.ssh/id_rsa jharbin@expt_manager_ip
```

### Befor running experiment (PAL example)
The Pyro nameserver is now set up on the experiment runner, not on a worker

* Set up PAL from the containers
* Ensure pal experiment script present at the start up location specified in the model!
* Ensure kafka started
* Enter the virtualenv with the activate script

Start up daemon with expt runnner IP and port:
```
cd ~/academic/soprano/REPO
./dist/daemon/daemon --expt_runner_ip=192.168.1.28 --expt_runner_user=jharbin
```

NB. The daemon needs to be run under the dist directory from
pyinstaller; because otherwise e.g. if it is run by Python, it will be
killed during the terminate script.
