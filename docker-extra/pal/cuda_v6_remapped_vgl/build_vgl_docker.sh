#!/bin/sh
cp ~/.Xauthority .
docker build -t cuda_v6_remapped_vgl .
