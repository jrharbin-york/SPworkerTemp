#!/bin/bash
eval "$(conda shell.bash hook)"
conda activate ros2_env
python3 ./nodes/turtlesim_publisher.py
