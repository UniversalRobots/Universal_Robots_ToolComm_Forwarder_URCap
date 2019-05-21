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
	print "start server"
	# cmd = 'echo "before" > log.txt' 
	# os.system(cmd) 
	cmd = './socat tcp-l:54321,reuseaddr,fork file:/dev/ttyTool,nonblock,raw,waitlock=/var/run/tty' 
	os.system(cmd) 
	# cmd = 'echo "after" >> log.txt' 
	# os.system(cmd)
	print "end server"


def pop_up(): 
	HOST = "192.168.0.104"
	PORT = 30002
	s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	s.connect((HOST, PORT))
	s.send("set_digital_out(2,True)" + "\n")
	time.sleep(1)
	
	s.send("popup(\"Test rs daemon\", title=\"The Header\", blocking=True)" + "\n")
	time.sleep(1)
	s.send("set_digital_out(2,False)" + "\n")
	time.sleep(1)

	data = s.recv(1024)
	print "Good bye!"

def log_txt(): 
	print "log"
	
	HOST = "192.168.0.104"
	PORT = 30002
	s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	s.connect((HOST, PORT))
	s.send("set_digital_out(2,True)" + "\n")
	time.sleep(1)
	
	s.send("textmsg(\"Test rs daemon\")"+ "\n")
	time.sleep(1)
	s.send("set_digital_out(2,False)" + "\n")
	time.sleep(1)

	data = s.recv(1024)
	print "Good bye!"

# create_file()

start_server()

# pop_up()

# log_txt()
