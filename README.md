# Versions

* `scala 2.10.4`

* `maven > 3.0.4`

* `spark 1.1.2-SNAPSHOT` from [https://github.com/GuillermoBlasco/spark](https://github.com/GuillermoBlasco/spark)

# Packaging

Execute:

    mvn package
    
# Execution

Ensure that `SPARK_HOME` is set to spark home. Execute in console:

    $SPARK_HOME/bin/spark-submit --class "com.blackbox.gmx.example.Sprinkler" --master local[1] target/gmx-core-1.0.0-SNAPSHOT.jar
    $SPARK_HOME/bin/spark-submit --class "com.blackbox.gmx.example.Student" --master local[4] target/gmx-core-1.0.0-SNAPSHOT.jar
    $SPARK_HOME/bin/spark-submit --class "com.blackbox.gmx.example.SmallMarkov" --master local[4] target/gmx-core-1.0.0-SNAPSHOT.jar