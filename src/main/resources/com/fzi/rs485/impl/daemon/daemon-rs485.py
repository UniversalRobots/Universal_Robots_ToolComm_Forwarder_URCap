#!/usr/bin/env python

import atexit
import os
import signal
import subprocess
import sys
import thread

import xmlrpclib
from SimpleXMLRPCServer import SimpleXMLRPCServer
from time import sleep

child_pid = None
SOCAT_PORT = 54321
quit = False
server = SimpleXMLRPCServer(("127.0.0.1", 40404))

def start_socat(): 
	global child_pid
	cmd = ['./socat', 'tcp-l:' + str(SOCAT_PORT) + ',reuseaddr,fork', 'file:/dev/ttyTool,nonblock,raw,waitlock=/var/run/tty']
	proc = subprocess.Popen(cmd)
	child_pid = proc.pid
	print("started", child_pid)
	#os.killpg(os.getpgid(proc.pid), signal.SIGTERM)
	#proc.communicate()

def kill_socat():
	print("In kill_socat")
	if child_pid:
		print("killing ", child_pid)
		os.kill(child_pid, signal.SIGKILL)

def signal_handler(sig, frame):
	global kill

	kill_socat()
	print "Setting quit flag."
	thread.start_new_thread(server.shutdown, ())

def set_port(port):
	global SOCAT_PORT
	kill_socat()
	sleep(2)
	print str(port)
	SOCAT_PORT = int(str(port))
	start_socat()


signal.signal(signal.SIGINT, signal_handler)
signal.signal(signal.SIGTERM, signal_handler)

server.register_function(set_port, "set_port")

start_socat()
server.serve_forever()

