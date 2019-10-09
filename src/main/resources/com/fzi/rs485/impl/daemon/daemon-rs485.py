#!/usr/bin/env python

import subprocess

def start_server(): 
	
	cmd = './socat tcp-l:54321,reuseaddr,fork file:/dev/ttyTool,nonblock,raw,waitlock=/var/run/tty' 
	try:
		result = subprocess.check_output(cmd, shell=True)
	except subprocess.CalledProcessError as err:
		file = open("result.txt", "w")
		file.write(str(err))
		file.close()

start_server()