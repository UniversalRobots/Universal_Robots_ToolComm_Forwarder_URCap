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

def start_server(): 
	print "start server"
	# cmd = 'echo "before" > log.txt' 
	# os.system(cmd) 
	cmd = './socat tcp-l:54321,reuseaddr,fork file:/dev/ttyTool,nonblock,raw,waitlock=/var/run/tty' 
	os.system(cmd) 
	# cmd = 'echo "after" >> log.txt' 
	# os.system(cmd)
	print "end server"

start_server()
