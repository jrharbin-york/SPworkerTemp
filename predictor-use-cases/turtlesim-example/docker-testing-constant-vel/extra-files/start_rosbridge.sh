#!/bin/bash
eval "$(conda shell.bash hook)"
conda activate ros2_env
ros2 launch rosbridge_server rosbridge_websocket_launch.xml
