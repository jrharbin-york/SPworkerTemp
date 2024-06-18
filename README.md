# SPworkerTemp
Temporary repository to ease the worker daemon setup
Worker OS; Ubuntu 20.04

Install Kafka 2.13-3.1.0 currently

ROS components on worker too - all ROS components

apt-get install python-is-python3 python3-venv python3-pip maven openjdk-11-jdk

docker run hello-world - verify docker works

mkdir ~/academic/soprano

Install the daemon here

mkdir ~/academic/sesame
git clone https://github.com/sesame-project/simulationBasedTesting
cd simulationBasedTesting
git checkout distributed-expt

python -m venv ~/academic/pyro
. ~/academic/pyro/bin/activate
pip install pyro5 pyinstaller netifaces

cd ~/academic/soprano/worker_daemon
pyinstaller daemon.py

ssh-keygen -t rsa
ssh-copy-id -i ~/.ssh/id_rsa jharbin@<expt_manager_ip>

Need to install the mvn arctifacts for the project from ~/academic/sesame:

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

The nameserver is set on the experiment runner

Set up PAL from the containers
Ensure pal script present at the start up location specified in the model!

Ensure kafka started

Enter the virtualenv:
Start up daemon with expt runnner IP and port:

./dist/daemon/daemon --expt_runner_ip=192.168.1.28 --expt_runner_user=jharbin
