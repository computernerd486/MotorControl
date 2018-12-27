# ThunderBorg Motor Control (Java Port)

This is a port of the open source ThunderBorg Motor control board interface,
(https://github.com/piborg/monster-self-drive/blob/master/ThunderBorg.py)
to the Java 8+


#### Usage:
+ Create an instance of the interface: `ThunderBorg tb = new ThunderBorg();`
+ Set the motor/speed via `tb.setMotor(Motor m, float spd)`
    + There is an public Enum. `ThunderBorg.Motor.*` which is used to specify which motor set to change the speed of
    + Speed is from `-1.0 to 1.0`, `-1.0` being 100% reverse, `1.0` being 100% forward 


This uses Pi4J to handle I2C communication and data writing:
(http://pi4j.com/)

PiBorg / ThunderBorg / MonsterBorg is registered trade name of Freeburn Robotics Ltd
(https://www.piborg.org/)