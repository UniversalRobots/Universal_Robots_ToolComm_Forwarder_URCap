#!/usr/bin/env python

import time
import sys

import xmlrpclib
from SimpleXMLRPCServer import SimpleXMLRPCServer

import subprocess
import os
import socket
import logging


title = ""

# server:                ./socat tcp-l:54321,reuseaddr,fork file:/dev/ttyTool,nonblock,raw,waitlock=/var/run/tty

def create_file():

	info = "Hey start server"
	file = open("in_file.txt", "w")
	file.write(info)
	file.close()
	cmd = 'wc -l my_text_file.txt > out_file.txt'
	os.system(cmd)


def start_server(): 

	cmd = 'echo "before" > log.txt' 
	os.system(cmd) 
	cmd = './socat tcp-l:54321,reuseaddr,fork file:/dev/ttyTool,nonblock,raw,waitlock=/var/run/tty' 
	result = os.system(cmd) 
	# result = "result"
	cmd = 'echo "after" >> log.txt' 
	os.system(cmd)
	cmd = 'echo result >> log.txt' 
	os.system(cmd)
	#file = open("result.txt", "w")
	#file.write(result)
	#file.close()

	cmd = 'wc -l my_text_file.txt > out_file.txt'
	os.system(cmd)

start_server()
#create_file()


