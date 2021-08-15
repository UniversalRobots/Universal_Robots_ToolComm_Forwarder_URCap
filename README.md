# URcaps RS-485 daemon
Package to support relaying the RS-485 communication device in a UR robot to a remote PC. This feature is only supported on the e-series.

Basically, this starts [`socat`](https://linux.die.net/man/1/socat) to relay the tool communication device to the network socket on port 54321.

## Installation
First, you need to put an `RS485 URCap` into the `programs` folder of your e-series robot.
You'll find the latest version [here](https://github.com/UniversalRobots/Universal_Robots_ToolComm_Forwarder_URCap/releases) in the *Releases* section.
Copying can be done either via `scp` or via using a USB stick.
Next, go through [these steps](doc/install_urcap.md) to install the URCap in Polyscope.


## Usage
Once the daemon is started in Polyscope, establish the connection from a remote machine, using `socat`:
```bash
# Setup your robot IP here:
export ROBOT_IP=192.168.56.101
# Setup the name for the device you wish to create. Make sure that your user can write to that location.
export LOCAL_DEVICE_NAME=/tmp/ttyUR
socat pty,link=${LOCAL_DEVICE_NAME},raw,ignoreeof,waitslave tcp:${ROBOT_IP}:54321
```

After that you should be able to use the device under `${LOCAL_DEVICE_NAME}` (in our case `/tmp/ttyUR`) just like any local RS-485 tty device.

## Use inside a ROS application
The [`ur_robot_driver`](http://wiki.ros.org/ur_robot_driver) has a [convenience script](https://github.com/UniversalRobots/Universal_Robots_ROS_Driver/blob/master/ur_robot_driver/scripts/tool_communication) wrapping the above `socat` call. When running the driver, the e-Series launchfiles provide a [flag](https://github.com/UniversalRobots/Universal_Robots_ROS_Driver/blob/master/ur_robot_driver/launch/ur10e_bringup.launch#L14) for automatically activating the tool communication. The launchfiles also allow setting up communication parameters on the robot directly.

## Acknowledgment
Developed in collaboration between:

[<img height="60" alt="Universal Robots A/S" src="doc/resources/ur_logo.jpg">](https://www.universal-robots.com/) &nbsp; and &nbsp;
[<img height="60" alt="FZI Research Center for Information Technology" src="doc/resources/fzi-logo_transparenz.png">](https://www.fzi.de).

<!--
    ROSIN acknowledgement from the ROSIN press kit
    @ https://github.com/rosin-project/press_kit
-->

<a href="http://rosin-project.eu">
  <img src="http://rosin-project.eu/wp-content/uploads/rosin_ack_logo_wide.png"
       alt="rosin_logo" height="60" >
</a>

Supported by ROSIN - ROS-Industrial Quality-Assured Robot Software Components.
More information: <a href="http://rosin-project.eu">rosin-project.eu</a>

<img src="http://rosin-project.eu/wp-content/uploads/rosin_eu_flag.jpg"
     alt="eu_flag" height="45" align="left" >

This project has received funding from the European Union’s Horizon 2020
research and innovation programme under grant agreement no. 732287.
