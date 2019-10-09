#!/usr/bin/env python

import atexit
import os
import signal
import subprocess
import sys

child_pid = None

def start_server(): 
	global child_pid
	cmd = ['./socat', 'tcp-l:54321,reuseaddr,fork', 'file:/dev/ttyTool,nonblock,raw,waitlock=/var/run/tty']
	proc = subprocess.Popen(cmd)
	child_pid = proc.pid
	print("started", child_pid)
	#os.killpg(os.getpgid(proc.pid), signal.SIGTERM)
	proc.communicate()

def kill_socat():
	print("In kill_socat")
	if child_pid:
		print("killing ", child_pid)
		os.kill(child_pid, signal.SIGKILL)

def signal_handler(sig, frame):
	kill_socat()




signal.signal(signal.SIGINT, signal_handler)
signal.signal(signal.SIGTERM, signal_handler)

start_server()

