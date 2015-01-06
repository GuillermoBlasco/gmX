# gmX

gmX is a library built on top of Spark GraphX that implements Belief Propagation algorithm.

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
    
You can find in `com.blackbox.gmx.impl.BeliefPropagationTest` class some examples where the graph
is built this way.

Then you can calibrate the cluster graph with the method `calibrated` and you'll get in return
a cluster graph with equal topology but the vertices shall be calibrated.

The `com.blackbox.gmx.model` package contains the model classes required to build a cluster graph:

* `Factor`

* `Variable`

Look at test to see some examples of usage of these classes.
    
# Execution

Ensure that `SPARK_HOME` is set to spark home. Execute in console:

    $SPARK_HOME/bin/spark-submit --class "com.blackbox.gmx.example.Sprinkler" --master local[1] target/gmx-core-1.0.0-SNAPSHOT.jar
    $SPARK_HOME/bin/spark-submit --class "com.blackbox.gmx.example.StudentTree" --master local[4] target/gmx-core-1.0.0-SNAPSHOT.jar
    $SPARK_HOME/bin/spark-submit --class "com.blackbox.gmx.example.SmallMarkov" --master local[4] target/gmx-core-1.0.0-SNAPSHOT.jar