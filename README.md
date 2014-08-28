CppFBP
===

C++ Implementation of Flow-Based Programming (FBP)

**Compile/link errors right now - working on them....**


General
---

In computer programming, flow-based programming (FBP) is a programming paradigm that defines applications as networks of "black box" processes, which exchange data across predefined connections by message passing, where the connections are specified externally to the processes. These black box processes can be reconnected endlessly to form different applications without having to be changed internally. FBP is thus naturally component-oriented.

FBP is a particular form of dataflow programming based on bounded buffers, information packets with defined lifetimes, named ports, and separate definition of connections.

One interesting aspect of this implementation is that it supports the scripting language `Lua`, so large parts of your networks can be written in a scripting language if desired.

Web sites for FBP: 
* http://www.jpaulmorrison.com/fbp/
* https://github.com/flowbased/flowbased.org/wiki

Prerequisites
---

Install Visual C++Express

Download `Boost`

Download `Lua`

Build FBP Project
---

Create empty `cppfbp` directory in your local GitHub directory

Do `git clone https://github.com/jpaulm/cppfbp`

Now go into Visual C++, and `Open/Project/Solution` `CppFBP.sln` (in the just cloned directory)

There will be a "solution" line, followed by a number of "projects" - two of which are `CppFBPCore` and `CppFBPComponents`.

- Right click on `CppFBPCore`, click on `Properties`
- Go to `Configuration Properties`/`C/C++`/`Additional Include Libraries`; add location of your `Boost` _include_ library (without `Headers`)
 
For all other subprojects,

- Go to `Configuration Properties`/`Linker`/`Additional Library Directories`
- Add location of your `Boost` `stage\lib` directory
- Add `../../Debug/CppFBPCore.lib`
- Add `../../Debug/CppFBPComponents.lib`

If you are interested in the Lua interface,

- Right click on `CppFBPComponents`, click on `Properties`
- Go to `Configuration Properties`/`C/C++`/`Additional Include Libraries`; add location of your `Lua` _include_ file 
- Go to `Configuration Properties`/`Linker`/`Input`/`Additional Dependencies`; add location of your `Lua` _lib_ file


Right click on `CppFBPCore` and do a `Build`

Right click on `CppFBPComponents` and do a `Build`

Right click on the "solution" line, and do `Build Solution`

If you only get warnings, you can proceed


Testing "TimingTest1" (console application)
---

Right click on `TimingTest1` in Solution Explorer; Debug/Start new instance



