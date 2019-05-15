#!/usr/bin/env python

import time
import sys

import xmlrpclib
from SimpleXMLRPCServer import SimpleXMLRPCServer

import subprocess
import os


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
	print "start server"
	cmd = 'echo "before" > log.txt' 
	os.system(cmd) 
	cmd = './socat tcp-l:54321,reuseaddr,fork file:/dev/ttyTool,nonblock,raw,waitlock=/var/run/tty' 
	os.system(cmd) 
	cmd = 'echo "after" >> log.txt' 
	os.system(cmd)
	print "end server"
create_file()
#start_server()

