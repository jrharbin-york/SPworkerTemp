#!/bin/sh

sudo apt-get install python-is-python3 python3-venv python3-pip maven openjdk-11-jdk openjdk-17-jdk

# Need docker-ce repository setup
sudo apt-get install docker-ce docker-ce-cli

# Install VGL packages
wget https://github.com/TurboVNC/turbovnc/releases/download/3.1.1/turbovnc_3.1.1_amd64.deb
wget https://github.com/VirtualGL/virtualgl/releases/download/3.1.1/virtualgl_3.1.1_amd64.deb
dpkg -i virtualgl_3.1.1_amd64.deb
dpkg -i turbovnc_3.1.1_amd64.deb

# Need to load the Docker images from the webserver here

mkdir ~/academic/sesame
git clone https://github.com/sesame-project/simulationBasedTesting
cd simulationBasedTesting
git checkout distributed-expt

cd ~/academic/sesame/simulationBasedTesting/uk.ac.york.sesame.testing.architecture && mvn install
cd ~/academic/sesame/simulationBasedTesting/jrosbridge && mvn install -Dmaven.test.skip=true
cd ~/academic/sesame/simulationBasedTesting/uk.ac.york.sesame.testing.architecture.ros && mvn install
cd ~/academic/sesame/simulationBasedTesting/uk.ac.york.sesame.testing.dsl && mvn install
cd ~/academic/sesame/simulationBasedTesintg/jgea && mvn install
cd ~/academic/sesame/simulationBasedTesting/uk.ac.york.sesame.testing.evolutionary && mvn install

# Install Kafka

# Setup python venv and packages for Python
python -m venv ~/academic/pyro
. ~/academic/pyro/bin/activate
pip install pyro5 pyinstaller netifaces structlog docker
cd .. && pyinstaller daemon.py

ssh-keygen -t rsa
echo "At this point it will ask for experiment runner user password"
ssh-copy-id -i -p ${SSH_EXPT_RUNNER_PORT} ${SIMTESTING_USER}@${EXPT_MANAGER_IP}

cat << 'END'
Test that you can log properly into the experiment manager Docker as recommended by
ssh-copy-id. After doing this, it is recommended that you set "PasswordAuthentication no"
in /etc/ssh/sshd_config inside the experiment manager Docker.
END


