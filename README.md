# gmX

gmX is a library built on top of Spark GraphX that implements Belief Propagation algorithm.

## License

Copyright (c) 2015, University of Barcelona
All rights reserved.

Redistribution and use in source and binary forms, with or without 
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, 
this list of conditions and the following disclaimer in the documentation 
and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors 
may be used to endorse or promote products derived from this software without 
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
THE POSSIBILITY OF SUCH DAMAGE.

## Requirements

* `scala 2.10.4`

* `maven > 3.0.4`

## Usage

### Integration as dependency

gmX can be included as dependency in your scala Spark project via maven:

    <dependency>
      <groupId>org.blackbox.gmx</groupId>
      <artifactId>gmx-core</artifactId>
      <version>1.0.0-SNAPSHOT</version>
    </dependency>
    
Since gmX does not have a remote public repository you have to install it first in your local 
repository by downloading the project and executing in the root of project's directory:

    mvn install
    
Or if your project does not support maven then put the packaged jar in the classpath of your app.
The jar can be obtained in `target/` directory after execute the maven goal `package`:

    mvn package
    
### Code usage

In order to use gmX you have to define a cluster graph. Here you have two chances. First passing
a set of factors that builds a Bethe cluster graph:

    ClusterGraph.apply(factors: Set[Factor], sc : SparkContext)
    
Second passing a cluster graph definition:

    ClusterGraph.apply(clusters: Map[Set[Variable], Set[Factor]], edges: Set[(Set[Variable], Set[Variable])], sc: SparkContext)
    
You can find in `edu.ub.guillermoblascojimenez.gmx.impl.BeliefPropagationTest` class some examples where the graph
is built this way.

Then you can calibrate the cluster graph with the method `calibrated` and you'll get in return
a cluster graph with equal topology but the vertices shall be calibrated.

The `edu.ub.guillermoblascojimenez.gmx.model` package contains the model classes required to build a cluster graph:

* `Factor`

* `Variable`

Look at test to see some examples of usage of these classes.
    
# Execution

Ensure that `SPARK_HOME` is set to spark home. Execute in console:

    $SPARK_HOME/bin/spark-submit --class "com.blackbox.gmx.example.Sprinkler" --master local[1] target/gmx-core-1.0.0-SNAPSHOT.jar
    $SPARK_HOME/bin/spark-submit --class "com.blackbox.gmx.example.StudentTree" --master local[4] target/gmx-core-1.0.0-SNAPSHOT.jar
    $SPARK_HOME/bin/spark-submit --class "com.blackbox.gmx.example.SmallMarkov" --master local[4] target/gmx-core-1.0.0-SNAPSHOT.jar