#!/bin/sh

export SSH_EXPT_RUNNER_PORT=39222
export SIMTESTING_USER=simtesting
export EXPT_MANAGER_IP=144.32.50.77

# Need to use official Docker packages - add Docker's official GPG key:
sudo apt-get update
sudo apt-get install ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

# Add the Docker repository to Apt sources:
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update

# Required packages for SBT worker
sudo apt-get install python-is-python3 python3-venv python3-pip maven openjdk-11-jdk openjdk-17-jdk

# Docker packages
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Install VGL packages
wget https://github.com/TurboVNC/turbovnc/releases/download/3.1.1/turbovnc_3.1.1_amd64.deb
wget https://github.com/VirtualGL/virtualgl/releases/download/3.1.1/virtualgl_3.1.1_amd64.deb
sudo dpkg -i virtualgl_3.1.1_amd64.deb
sudo dpkg -i turbovnc_3.1.1_amd64.deb

# Need to load the Docker images from the webserver here

mkdir ~/academic/sesame && cd ~/academic/sesame
git clone https://github.com/sesame-project/simulationBasedTesting
cd simulationBasedTesting
git checkout distributed-expt

cd ~/academic/sesame/simulationBasedTesting/uk.ac.york.sesame.testing.architecture && mvn install
cd ~/academic/sesame/simulationBasedTesting/jrosbridge && mvn install -Dmaven.test.skip=true
cd ~/academic/sesame/simulationBasedTesting/uk.ac.york.sesame.testing.architecture.ros && mvn install
cd ~/academic/sesame/simulationBasedTesting/uk.ac.york.sesame.testing.dsl && mvn install
cd ~/academic/sesame/simulationBasedTesting/jgea && mvn install
cd ~/academic/sesame/simulationBasedTesting/uk.ac.york.sesame.testing.evolutionary && mvn install

# Setup python venv and packages for Python
python -m venv ~/academic/pyro
. ~/academic/pyro/bin/activate
pip install pyro5 pyinstaller netifaces structlog docker
cd .. && pyinstaller daemon.py

# Kafka
mkdir -p ~/source && cd ~/source && wget https://downloads.apache.org/kafka/3.7.1/kafka_2.13-3.7.1.tgz
tar -xvzf kafka_2.13-3.7.1.tgz
# Setup Kafka script here
cat << END_START_KAFKA > $HOME/start_zookeeper_kafka.sh
#!/bin/sh

KAFKA_BASE=$HOME/source/kafka/kafka_2.13-3.1.0
cd $KAFKA_BASE
echo "Starting Zookeeper"
xterm -hold -e /bin/bash -l -c "./bin/zookeeper-server-start.sh ./config/zookeeper.properties" &
sleep 10

echo "Starting Kafka"
xterm -hold -e /bin/bash -l -c "./bin/kafka-server-start.sh ./config/server.properties" &
END_START_KAFKA

chmod 700 $HOME/start_zookeeper_kafka.sh

# Now doing VGL setup
echo "Please follow the below instructions for VGL setup"
sudo /opt/VirtualGL/bin/vglserver_config


echo "Now installing the key on the experiment runner... please check below and make sure the experiment runner is ready to receive it, or Ctrl-C now"
echo "SSH_EXPT_RUNNER_PORT: ${SSH_EXPT_RUNNER_PORT}"
echo "SIMTESTNG_USER: ${SIMTESTING_USER}"
echo "EXPT_MANAGER_IP: ${EXPT_MANAGER_IP}"
read -r line

ssh-keygen -t rsa
echo "At this point it will ask for experiment runner user password"
ssh-copy-id -i -p ${SSH_EXPT_RUNNER_PORT} ${SIMTESTING_USER}@${EXPT_MANAGER_IP}

cat << 'END'
Test that you can log properly into the experiment manager Docker as recommended by
ssh-copy-id. After doing this, it is recommended that you set "PasswordAuthentication no"
in /etc/ssh/sshd_config inside the experiment manager Docker.
END
