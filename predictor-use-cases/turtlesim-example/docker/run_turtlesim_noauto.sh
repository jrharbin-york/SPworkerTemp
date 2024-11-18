#!/bin/bash
xhost +local:docker
# Need port forwarding for Pyro nameserver
docker run -v /var/run/docker.sock:/var/run/docker.sock -it --net=host -e DISPLAY=$DISPLAY turtlesim /bin/bash
