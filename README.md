What is TraCI4J
---

TraCI4J is a Java library for interfacing [SUMO](http://sumo.sourceforge.net) with a Java program to control and/or watch a traffic simulation via SUMO's [TraCI](http://sumo-sim.org/userdoc/TraCI.html) interface.

It was initially developed by members at [ApPeAL (Applied Pervasive Architectures Lab)](http://www.dauin.polito.it/it/la_ricerca/gruppi_di_ricerca/appeal_applied_pervasive_architectures_lab) in [Politecnico di Torino](http://www.polito.it).

What can TraCI4J do
---

The library can act as a complete front-end for a SUMO instance. The simulation can be started, stopped and advanced step by step.

The SUMO instance can be run by the library itself or can be already running. Since the TraCI communication is done via TCP, the existing SUMO instance can be in the same machine or in another host.

While the simulation is running, many informations can be retrieved, both static (e.g. the road network topology) and dynamic (e.g. position and speed of vehicles). A set of TraCI4J classes match the corresponding [TraCI objects](http://www.polito.it), each with methods that allow for value reading and state changing.


Development status
---

The library is currently in *alpha* development stage. Some of the TraCI features are available as TraCI4J classes and work as expected (although more testing is needed), while some others still need to be written.


How to get it
---

You can download TraCI4J right here with the tools provided via GitHub.

There are no binaries: the library is available as source code in an [Eclipse](http://www.eclipse.org) project. Import it in your workspace and Eclipse will try to automatically build it.

The library can also be built outside Eclipse via Ant. The default target in the build.xml file will try to compile all code to .class files.

How to get feedback
---

You can use the [mailing list](https://lists.sourceforge.net/lists/listinfo/traci4j-user tracu4j-user) to ask for information to the authors and/or other users.

Bug reports and feature requests can be posted in the Issues tab.

How to use it
---

First of all, you need:

* A working SUMO installation (0.23.0 or higher)
* Be familiar with SUMO, i.e. know its basic principles, how to set up the input files, how to run it...
* A SUMO file set (a config file, a net description file and a routes file at least)
* A Java SE 1.7 virtual machine

You can find some usage examples [in this directory](examples/it/polito/appeal/traci/examples).

Disclaimer
---

TraCI4J is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

TraCI4J is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
[GNU General Public License](http://www.gnu.org/licenses/#GPL) for more details.
