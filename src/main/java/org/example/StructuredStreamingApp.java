package org.example;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.concurrent.TimeoutException;


public class StructuredStreamingApp {
    public static void main(String[] args) throws TimeoutException, StreamingQueryException {
        SparkSession ss = SparkSession.builder()
                .appName("StructuredStreamingApp")
                .getOrCreate();

        StructType schema = new StructType(new StructField[]{
                DataTypes.createStructField("id", DataTypes.LongType, true),
                DataTypes.createStructField("nom", DataTypes.StringType, true),
                DataTypes.createStructField("prenom", DataTypes.StringType, true),
                DataTypes.createStructField("note", DataTypes.DoubleType, true)
        });

        Dataset<Row> dfInput=ss.readStream().option("header",true).schema(schema).csv("hdfs://namenode:8020/input");
        Dataset<Row> dfOutput=dfInput.where("note>=15");
        StreamingQuery query= dfOutput.writeStream().format("console").outputMode("append").start();
        query.awaitTermination();


    }
}
