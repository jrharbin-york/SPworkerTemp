#!/bin/sh
cp ~/.Xauthority .
docker build -t cuda_v6_vgl .
