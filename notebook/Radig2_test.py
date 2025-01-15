# Databricks notebook source
# MAGIC %md
# MAGIC Copyright (C) 2025 Ordnance Survey
# MAGIC
# MAGIC Licensed under the Open Government Licence v3.0 (the "License");
# MAGIC
# MAGIC you may not use this file except in compliance with the License.
# MAGIC
# MAGIC You may obtain a copy of the License at
# MAGIC
# MAGIC http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
# MAGIC
# MAGIC Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

# COMMAND ----------

# MAGIC %md
# MAGIC
# MAGIC ---
# MAGIC * A secondary key, also computed by RADIG, is added to each dataset for the purpose of partitioning
# MAGIC ---
# MAGIC
# MAGIC * Point-in-polygon spatial join between OS Open Data products `OS Open UPRN` and `OS OpenMap Local`.

# COMMAND ----------

# Databricks File System (DBFS) mount point - modify to specify your mount point
mnt_point = '/mnt/...'

# COMMAND ----------

import os
import time
from pyspark.sql import functions as F
from sedona.core.enums import GridType, IndexType
from sedona.core.spatialOperator import JoinQuery
from sedona.sql import st_constructors as sc

# COMMAND ----------

#/ initialise GeoSpark(Sedona)
from sedona.register import SedonaRegistrator
SedonaRegistrator.registerAll(spark)

# COMMAND ----------

# MAGIC %md
# MAGIC Supply your own data here.
# MAGIC
# MAGIC data_a.csv should contain a string column of 'id_a' for ID and a string column 'geom_a' for point geometry in WKT format
# MAGIC data_b.csv should contain a string column of 'id_b' for ID and a string column 'geom_b' for polygon geometry in WKT format

# COMMAND ----------

# Dataset A path
path_a = "data_a.csv"
# Dataset B path
path_b = "data_b.csv"

# COMMAND ----------

# Start time
start_time = time.time() 

# COMMAND ----------

# MAGIC %run ./Radig2

# COMMAND ----------

# MAGIC %run ./Radig2_Match

# COMMAND ----------


# COMMAND ----------

intPolicy = 0 # any intersection
maxDivLevel = 0 #2
minDivRes = 1.0 
eaThreshold = 0.35 #0.5
elThreshold = 0.35 #0.5
#key_res = 10000.0
key_res = 1000.0

# COMMAND ----------

# DataFrame A point
df_a = (spark
        .read
        .format("csv")
        .options(header=True, delimiter=';')
        .load(path_a)
        # Construct geometry from WKT representation
        # Add column geometry
        .withColumn('geometry', sc.ST_GeomFromWKT(F.col('geom_a')))
        .drop('geom_a')
        .withColumn("RADIG_MK_A", F.expr("compPointBNGRadigRes_UDF(geometry, True, 1.0)"))#match key
        .withColumn("RADIG_PK_A", F.expr(f"compPointBNGRadigRes_UDF(geometry, True, {key_res})"))#partition key
        .withColumnRenamed("geometry", "geom_a")
        .select("id_a", "geom_a", "RADIG_MK_A", F.explode("RADIG_PK_A").alias("RADIG_KEY_A")).drop("RADIG_PK_A")
        .select("id_a", "geom_a", "RADIG_KEY_A", F.explode("RADIG_MK_A").alias("RADIG_A")).drop("RADIG_MK_A")
)

# COMMAND ----------

# DataFrame B polygon
df_b = (spark
        .read
        .format("csv")
        .options(header=True, delimiter=';')
        .load(path_b)
        # Construct geometry from WKT representation
        # Add column geometry
        .withColumn('geometry', sc.ST_GeomFromWKT(F.col('geom_b')))
        # Drop column wkt
        .drop('geom_b')
        .withColumn("RADIG_MK_B", F.expr(f"compBNGRadigAdaptiveInt_UDF(geometry, True, {intPolicy}, {maxDivLevel}, {minDivRes}, {eaThreshold}, {elThreshold})"))# match key
        .withColumn("RADIG_PK_B", F.expr(f"compBNGRadigResInt_UDF(geometry,True, {key_res}, True, {intPolicy})")) # partition key
        .withColumnRenamed("geometry", "geom_b")
        .select("id_b", "geom_b", "RADIG_MK_B", F.explode("RADIG_PK_B").alias("RADIG_KEY_B")).drop("RADIG_PK_B")
        .select("id_b", "geom_b", "RADIG_KEY_B", F.explode("RADIG_MK_B").alias("RADIG_B")).drop("RADIG_MK_B")
)

# COMMAND ----------

# aggregate df_a by partition key and pack id, match_key and geometry into a struct
df_ag = df_a.groupBy("RADIG_KEY_A").agg(F.collect_list(F.struct(F.col("id_a"), F.col("RADIG_A"), F.col("geom_a"))).alias("data_a"))
# aggregate df_b by partition key and pack id, match_key and geometry into a struct
df_bg = df_b.groupBy("RADIG_KEY_B").agg(F.collect_list(F.struct(F.col("id_b"), F.col("RADIG_B"), F.col("geom_b"))).alias("data_b"))
# join the two aggregated data frames by the partition key
df_c = df_ag.join(df_bg, F.col("RADIG_KEY_A") == F.col("RADIG_KEY_B"), "inner").drop("RADIG_KEY_A", "RADIG_KEY_B").repartition(256)
# use synchronised trie traveral to match keys. If matched, further intersects test will be performed
df_d = df_c.withColumn("join_rlt_array", F.expr("RadigJoin_UDF(data_b, data_a, 'id_b', 'RADIG_B', 'geom_b', 'id_a', 'RADIG_A', 'geom_a')")).drop("data_a", "data_b")
# present the matching results (pair of ids)
df_e = df_d.withColumn("rlt", F.explode(F.col("join_rlt_array"))).select("rlt")
df_f = df_e.withColumn("left", F.col("rlt")[0]).withColumn("right", F.col("rlt")[1]).drop("rlt").distinct()

# COMMAND ----------

# End time
end_time = time.time()
# Processing time in seconds
processing_time = end_time - start_time
print(processing_time)

# COMMAND ----------

# now join back to the original dataframes to perform the final fitering with your prefered predicates