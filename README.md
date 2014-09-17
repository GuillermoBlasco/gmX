# Versions

* `scala 2.10.4`

* `sbt 0.13`

* `spark 1.1.0`

# Packaging

Execute:

    sbt package
    
# Execution

Ensure that `SPARK_HOME` is set to spark home. Execute in console:

    $SPARK_HOME/bin/spark-submit --class "com.blackbox.gmx.exampledata.Sprinkler" --master local[4] target/scala-2.10/gmx_2.10-1.0.jar