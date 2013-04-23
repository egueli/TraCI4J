TraCI4J
=======

This is TraCI4J, a Java library for high-level interaction with SUMO through
its TraCI interface.

Copyright (C) 2013 ApPeAL Group, Politecnico di Torino

Installation
------------

With Eclipse, import this package as a project in your workspace. It should
auto-build itself without problems

The library requires no external dependencies, apart from JUnit 4. It comes
with log4j and Xerces libraries built-in.

Usage
-----

Make your project depend on TraCI4J. In your code, declare a variable of the
SumoTraciConnection type. This is the main class that runs a SUMO instance,
establishes a connection with it and provides methods/objects to issue commands
and retrieve information.

Some basic usage examples can be found in the "examples" directory.

The JUnit tests in the test/ directory can also be a good source of example code.

For more information, see the Javadoc.

Need help?
----------
You can subscribe to the [traci4j-user mailing list](http://sourceforge.net/mailarchive/forum.php?forum_name=traci4j-user)
to get help and feedback.


License
-------

This program is released under the GNU GPL version 3. See the COPYING file
for the license.
