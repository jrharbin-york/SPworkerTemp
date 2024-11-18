#!/bin/bash
xhost +local:docker
docker run -v /var/run/docker.sock:/var/run/docker.sock -it --net=host -e DISPLAY=$DISPLAY turtlebot3 /bin/bash
