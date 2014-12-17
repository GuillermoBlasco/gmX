CREATE
        // Variables
        ( a:Variable {name:"a", scope:2} ),
        ( b:Variable {name:"b", scope:2} ),
        ( c:Variable {name:"c", scope:2} ),
        ( d:Variable {name:"d", scope:2} ),
        ( e:Variable {name:"e", scope:2} ),

        // Factors
        ( phi1:Factor {
            name : "phi1",
            values : [
                "(a=0) -> 0.6",
                "(a=1) -> 0.4"
            ]
        } ),
        (phi1)-[:SCOPED]->(a),

        (phi2:Factor {
            name : "phi2",
            values : [
                "(b=0) -> 0.2",
                "(b=1) -> 0.8"
            ]
        } ),
        (phi2)-[:SCOPED]->(b),

        ( phi3:Factor {
            name : "phi3",
            values : [
                "(a=0,b=0) -> 0.1",
                "(a=1,b=0) -> 0.9",
                "(a=0,b=1) -> 0.7",
                "(a=1,b=1) -> 0.3"
            ]
        } ),
        (phi3)-[:SCOPED]->(a),
        (phi3)-[:SCOPED]->(b),

        ( phi4:Factor {
            name : "phi4",
            values : [
                "(c=0,b=0) -> 0.9",
                "(c=1,b=0) -> 0.1",
                "(c=0,b=1) -> 0.75",
                "(c=1,b=1) -> 0.25"
            ]
        } ),
        (phi4)-[:SCOPED]->(b),
        (phi4)-[:SCOPED]->(c),

        ( phi5:Factor {
            name : "phi5",
            values : [
                "(e=0,d=0) -> 0.5",
                "(e=1,d=0) -> 0.5",
                "(e=0,d=1) -> 0.9",
                "(e=1,d=1) -> 0.1"
            ]
        } ),
        (phi5)-[:SCOPED]->(e),
        (phi5)-[:SCOPED]->(d),

        ( phi6:Factor {
            name : "phi6",
            values : [
                "(e=0,b=0) -> 0.2",
                "(e=1,b=0) -> 0.8",
                "(e=0,b=1) -> 0.05",
                "(e=1,b=1) -> 0.95"
            ]
        } ),
        (phi6)-[:SCOPED]->(b),
        (phi6)-[:SCOPED]->(e),

        // CLUSTERS

        (c1:Cluster {name:"c1"}),
        (c1)-[:CONTAINS_FACTOR]->(phi1),
        (c1)-[:CONTAINS_VARIABLE]->(a),

        (c2:Cluster {name:"c2"}),
        (c2)-[:CONTAINS_FACTOR]->(phi2),
        (c2)-[:CONTAINS_VARIABLE]->(b),

        (c3:Cluster {name:"c3"}),
        (c3)-[:CONTAINS_FACTOR]->(phi3),
        (c3)-[:CONTAINS_VARIABLE]->(a),
        (c3)-[:CONTAINS_VARIABLE]->(b),

        (c4:Cluster {name:"c4"}),
        (c4)-[:CONTAINS_FACTOR]->(phi4),
        (c4)-[:CONTAINS_VARIABLE]->(b),
        (c4)-[:CONTAINS_VARIABLE]->(c),

        (c5:Cluster {name:"c5"}),
        (c5)-[:CONTAINS_FACTOR]->(phi5),
        (c5)-[:CONTAINS_VARIABLE]->(e),
        (c5)-[:CONTAINS_VARIABLE]->(d),

        (c6:Cluster {name:"c6"}),
        (c6)-[:CONTAINS_FACTOR]->(phi6),
        (c6)-[:CONTAINS_VARIABLE]->(b),
        (c6)-[:CONTAINS_VARIABLE]->(e),

        (c1)-[:SHARE_SCOPE]->(c3),
        (c1)<-[:SHARE_SCOPE]-(c3),
        (c2)-[:SHARE_SCOPE]->(c3),
        (c2)<-[:SHARE_SCOPE]-(c3),
        (c3)-[:SHARE_SCOPE]->(c4),
        (c3)<-[:SHARE_SCOPE]-(c4),
        (c4)-[:SHARE_SCOPE]->(c6),
        (c4)<-[:SHARE_SCOPE]-(c6),
        (c6)-[:SHARE_SCOPE]->(c5),
        (c6)<-[:SHARE_SCOPE]-(c5)


