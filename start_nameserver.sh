#!/bin/bash

if [[ $# -eq 0 ]] ; then
	echo "Please supply the current IP for this node"
	exit 1
fi

IP=$1
pyro5-ns -n ${IP} -p 9523 --bcport 9524
