// Databricks notebook source
// MAGIC %md
// MAGIC
// MAGIC Copyright (C) 2025 Ordnance Survey
// MAGIC
// MAGIC Licensed under the Open Government Licence v3.0 (the "License");
// MAGIC
// MAGIC you may not use this file except in compliance with the License.
// MAGIC
// MAGIC You may obtain a copy of the License at
// MAGIC
// MAGIC   http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
// MAGIC
// MAGIC Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
// MAGIC

// COMMAND ----------

// MAGIC %md Spatial Join of two dataframes using Radig Reference

// COMMAND ----------

import scala.jdk.CollectionConverters._
import scala.collection.mutable._
import scala.collection.JavaConverters._

import uk.osgb.algorithm.radig2.Radig_BNG
import uk.osgb.algorithm.radig2.Radig_DigitInterleaver
import uk.osgb.algorithm.radig2.RadigMatchTrie
import uk.osgb.algorithm.radig2.RadigObjRef
//
// import org.apache.spark.sql.functions._
// import org.apache.spark.sql._
// import org.apache.spark._
// import org.apache.spark.serializer.KryoSerializer
// //
// import org.apache.sedona.sql.utils._
// import org.apache.sedona.core.serde.SedonaKryoRegistrator
// import org.apache.sedona.core.enums._
// import org.apache.sedona.core.spatialOperator._
//
import org.locationtech.jts.geom._
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.geojson.GeoJsonReader

// COMMAND ----------

/* two way join. both col_a.contains(col_b) and col_b.contains(col_a) are tested. col_a.contains(col_b) is true if col_b.StartsWith(col_a) is true.
*  
*  This function uses Geometry as feature. There might be an issue that if two features have identical geometry
*/
//case class RadigMatchRlt(leftId:String, rightID:String)

def RadigJoin(col_a:WrappedArray[Row], col_b:WrappedArray[Row], id_a_nm:String, radig_a_nm:String, geom_a_nm:String, id_b_nm:String, radig_b_nm:String, geom_b_nm:String):Array[Array[String]]={
  val trieA:RadigMatchTrie = RadigMatchTrie.createRadigMatchTrie()
  for(row<-col_a){
    val id_a:String = row.getAs[String](id_a_nm)
    val radig_a:String = row.getAs[String](radig_a_nm)
    val geom_a:Geometry = row.getAs[Geometry](geom_a_nm)
    geom_a.setUserData(id_a)
    trieA.insertRadigRef(radig_a, geom_a)
  }
  val trieB = RadigMatchTrie.createRadigMatchTrie()
  for(row<-col_b){
    val id_b:String = row.getAs[String](id_b_nm)
    val radig_b:String = row.getAs[String](radig_b_nm)
    val geom_b:Geometry = row.getAs[Geometry](geom_b_nm)
    geom_b.setUserData(id_b)
    trieB.insertRadigRef(radig_b, geom_b)
  }
  val matchedPair = RadigMatchTrie.radigJoinJTSGeometry(trieA, trieB, false, false)
  // var newRowListBuffer:ListBuffer[knn_rlt] = new ListBuffer()
  // for(pair<=matchedPair){
  //   val row = new RadigMatchRlt(pair(0), pair(1))
  //   newRowListBuffer+=row
  // }
  // newRowListBuffer.toArray
  matchedPair
}
val RadigJoin_UDF=udf[Array[Array[String]], WrappedArray[Row], WrappedArray[Row], String, String, String, String, String, String](RadigJoin)
spark.udf.register("RadigJoin_UDF", RadigJoin_UDF)

// COMMAND ----------

/* two way join. both col_a.contains(col_b) and col_b.contains(col_a) are tested. col_a.contains(col_b) is true if col_b.StartsWith(col_a) is true.
*  This function uses java class RadigObjRef (with Comparable interface implemented) as feature type to address the issue of multiple features with identical geometry (a problem in Sedona spatial join)
*/
def RadigJoinObj(col_a:WrappedArray[Row], col_b:WrappedArray[Row], id_a_nm:String, radig_a_nm:String, geom_a_nm:String, id_b_nm:String, radig_b_nm:String, geom_b_nm:String):Array[Array[String]]={
  val trieA:RadigMatchTrie = RadigMatchTrie.createRadigMatchTrie()
  for(row<-col_a){
    val id_a:String = row.getAs[String](id_a_nm)
    val radig_a:String = row.getAs[String](radig_a_nm)
    val geom_a:Geometry = row.getAs[Geometry](geom_a_nm)
    geom_a.setUserData(id_a)
    trieA.insertRadigRef(radig_a, new RadigObjRef(id_a, geom_a))
  }
  val trieB = RadigMatchTrie.createRadigMatchTrie()
  for(row<-col_b){
    val id_b:String = row.getAs[String](id_b_nm)
    val radig_b:String = row.getAs[String](radig_b_nm)
    val geom_b:Geometry = row.getAs[Geometry](geom_b_nm)
    geom_b.setUserData(id_b)
    trieB.insertRadigRef(radig_b, new RadigObjRef(id_b, geom_b))
  }
  val matchedPair = RadigMatchTrie.radigJoinObjRef(trieA, trieB, false, false)
  // var newRowListBuffer:ListBuffer[knn_rlt] = new ListBuffer()
  // for(pair<=matchedPair){
  //   val row = new RadigMatchRlt(pair(0), pair(1))
  //   newRowListBuffer+=row
  // }
  // newRowListBuffer.toArray
  matchedPair
}
val RadigJoinObj_UDF=udf[Array[Array[String]], WrappedArray[Row], WrappedArray[Row], String, String, String, String, String, String](RadigJoinObj).asNondeterministic()
spark.udf.register("RadigJoinObj_UDF", RadigJoinObj_UDF)

// COMMAND ----------

/* one way join, for point to point or point in polygon test
*
*/