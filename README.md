# URcaps RS-485 daemon
Package to support relaying the RS-485 communication device in a UR robot to a remote PC. This feature is only supported on the e-series.

Basically, this starts [`socat`](https://linux.die.net/man/1/socat) to relay the tool communication device to the network socket on port 54321.

## Usage
Once the daemon is started, connect to the socket from a remote machine, also using `socat`:
```bash
# Setup your robot IP here:
export ROBOT_IP=192.168.56.101
# Setup the device name that should be available locally. Make sure that your user can write to that location.
export LOCAL_DEVICE_NAME=/tmp/ttyUR
socat socat pty,link=${LOCAL_DEVICE_NAME},raw,ignoreeof,waitslave tcp:${ROBOT_IP}:54321
```

After that you should be able to use the device under `${LOCAL_DEVICE_NAME}` just like any local RS-485 tty device.

## Use inside a ROS application
The [`ur_robot_driver`](http://wiki.ros.org/ur_robot_driver) has a [convenience script](https://github.com/UniversalRobots/Universal_Robots_ROS_Driver/blob/master/ur_robot_driver/scripts/tool_communication) wrapping the above `socat` call. When running the driver, the e-Series launchfiles provide a [flag](https://github.com/UniversalRobots/Universal_Robots_ROS_Driver/blob/master/ur_robot_driver/launch/ur10e_bringup.launch#L14) for automatically activating the tool communication. The launchfiles also allow setting up communication parameters on the robot directly.


