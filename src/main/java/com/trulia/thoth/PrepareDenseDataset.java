//package com.trulia.thoth;
//
//import com.trulia.thoth.pojo.QueryPojo;
//import com.trulia.thoth.pojo.QuerySamplingDetails;
//import net.sf.javaml.core.Dataset;
//import net.sf.javaml.core.DefaultDataset;
//import net.sf.javaml.core.DenseInstance;
//import net.sf.javaml.tools.data.FileHandler;
//import org.codehaus.jackson.map.ObjectMapper;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Random;
//
///**
// * Created by pmhatre on 7/23/14.
// */
//public class PrepareDenseDataset {
//
//  static ObjectMapper mapper = new ObjectMapper();
//  static HashMap<String, Integer> attributeIndex = new HashMap<String, Integer>();
//  static int attributeCount = 0;
//  static final int slowQueryThreshold = 50;
//
//  public static void main(String[] args) throws IOException {
//
//    //    BufferedReader br = new BufferedReader(new FileReader("/Users/pmhatre/thoth-data/2014_07_23_merged"));
//    BufferedReader br = new BufferedReader(new FileReader("/Users/pmhatre/thoth-data/2014_07_23_merged_full"));
//
//    // hostname_s, pool_s, source_s, params_s, qtime_i, hits_i, bitmask_s
//    //bw.write("");
//    Dataset dataset = new DefaultDataset();
//    ArrayList<Integer> qtimes = new ArrayList<Integer>();
//    int slowQueries = 0, fastQueries = 0;
//
//    // Training and test datasets
//    Dataset train = new DefaultDataset();
//    Dataset test = new DefaultDataset();
//
//    String line = new String();
//    int emptyCount = 0;
//    while ( (line=br.readLine()) != null ) {
//
//      //DenseInstance instance = createInstance(line);
//      String[] splitLine = line.split("\t");
//      if (splitLine.length != 7) continue;
//      DenseInstance instance = createInstance(getQueryPojoFromSplittedLine(splitLine));
//
//      if(instance == null)
//        continue;
//
//      dataset.add(instance);
//
//      // Separate into training and test
//      Random random = new Random();
//      int next = random.nextInt(100);
//      if(next >= 70) {
//        test.add(instance);
//      }
//      else {
//        train.add(instance);
//      }
//    }
//
//    //    System.out.println("Empty count: " + emptyCount);
//    //
//    //    Collections.sort(qtimes);
//    //    int size = qtimes.size();
//    //    System.out.println("Query time stats: ");
//    //    System.out.println("min: " + qtimes.get(0) + " max: " + qtimes.get(size - 1) + " median: " + qtimes.get( size/2 ));
//    //
//    //    System.out.print("#slow: " + slowQueries + " #fast: " + fastQueries);
//
//    // Optional normalization step
//    //    NormalizeMidrange nmr = new NormalizeMidrange(0.5, 1);
//    //    nmr.filter(dataset);
//    //    nmr.filter(train);
//    //    nmr.filter(test);
//
//    // FileHandler.exportDataset(dataset, new File("/Users/pmhatre/thoth-data/preparedDatasetNormalized"));
//    //Train and test datasets
//    FileHandler.exportDataset(train,
//        new File("/Users/pmhatre/thoth-data/exp/latest/sept/dense_train"));
//    System.out.println("Training set size: " + train.size());
//    System.out.println("Classindex: " + train.classIndex(null));
//    FileHandler.exportDataset(test,
//        new File("/Users/pmhatre/thoth-data/exp/latest/sept/dense_test"));
//    System.out.println("Test set size: " + test.size());
//    System.out.println("Classindex: " + test.classIndex(null));
//
//    for(String key: attributeIndex.keySet()) {
//      System.out.println(key + ": " + attributeIndex.get(key));
//    }
//  }
//
//  private static QueryPojo getQueryPojoFromSplittedLine(String[] fields){
//    QueryPojo queryPojo = new QueryPojo();
//    queryPojo.setParams(fields[3]);
//    if (!fields[4].isEmpty()) queryPojo.setQtime(fields[4]);
//    if (!fields[5].isEmpty()) queryPojo.setHits(fields[5]);
//    queryPojo.setBitmask(fields[6]);
//    return queryPojo;
//  }
//
//  public static DenseInstance createInstance(QueryPojo queryPojo) throws IOException {
//    DenseInstance instance = new DenseInstance(10);
//
//    QuerySamplingDetails querySamplingDetails = mapper.readValue(queryPojo.getParams(), QuerySamplingDetails.class);
//    QuerySamplingDetails.Details details = querySamplingDetails.getDetails();
//    int start = details.getStart();
//    addDoubleField(instance, "start", start);
//
//    String query = details.getQuery();
//    if(query != null) {
//      //        System.out.println("valid query");
//      query = query.replace("(", "");
//      query = query.replace(")", "");
//      query = query.replace("\"", "");
//      query = query.replace("+", "");
//      String[] queryFields = query.split("AND|OR");
//      //        for(String queryField: queryFields) {
//      //          String[] pair = queryField.split(":");
//      //          if(pair.length  == 2) {
//      //            String fieldName = pair[0];
//      //            addStringField(instance, "field:" + fieldName);
//      //          }
//      //          else {
//      //            System.out.println("Invalid field key value pair");
//      //          }
//      //        }
//
//      // Number of fields as a separate field
//      addDoubleField(instance, "fieldCount", queryFields.length);
//      //        System.out.println("fieldCount: " + queryFields.length);
//    }
//
//    //      if(details.isSlowpool()) {
//    //        addDoubleField(instance, "slowpool", 1);
//    //      }
//    //      else {
//    //        addDoubleField(instance, "slowpool", 0);
//    //      }
//
//    // 5. qtime_i
//    // ---------------- add isNumber check -----------------
//    if(queryPojo.getQtime() == null) {
//      //      emptyCount++;
//    }
//    else {
//      int qtime = Integer.parseInt(queryPojo.getQtime());
//      //addDoubleField(instance, "qtime", qtime);
//      //      qtimes.add(qtime);
//      //      instance.setClassValue(new Double(qtime)); // for regression
//
//      // --------- for classification --------------
//      if(qtime < slowQueryThreshold) {
//        //          fastQueries++;
//        instance.setClassValue(new Double(0));
//      }
//      else {
//        //          slowQueries++;
//        instance.setClassValue(new Double(1));
//      }
//    }
//
//
//    // 6. hits_i
//    if(queryPojo.getHits() == null) {
//      //      emptyCount++;
//    }
//    else {
//      int hits = Integer.parseInt(queryPojo.getHits());
//      addDoubleField(instance, "hits", hits);
//    }
//
//    // 7. bitmask_s
//    addBitmaskBooleanFields(instance, queryPojo.getBitmask());
//
//    return instance;
//  }
//
//
//  private static DenseInstance createInstance(String line) throws IOException {
//    DenseInstance instance = new DenseInstance(10);
//    String[] fields = line.split("\t");
//    if(fields.length != 7) {
//      return null;
//    }
//
//    // These fields are not available before query execution
//    //    // 1. hostname_s
//    //    String hostName = fields[0];
//    //    addStringField(instance, "hostname_" + hostName);
//    //
//    //    // 2. pool_s
//    //    String pool = fields[1];
//    //    addStringField(instance, "pool_" + pool);
//    //
//    //    // 3. source_s
//    //    String source = fields[2];
//    //    addStringField(instance, "source_" + source);
//
//    // 4. param_s
//
//    String params = fields[3];
//    //      System.out.println(params);
//
//    QuerySamplingDetails querySamplingDetails = mapper.readValue(params, QuerySamplingDetails.class);
//    QuerySamplingDetails.Details details = querySamplingDetails.getDetails();
//
//    int start = details.getStart();
//    //      if(start != 0) {
//    //        System.out.println("start: " + start);
//    //      }
//
//    addDoubleField(instance, "start", start);
//
//    String query = details.getQuery();
//    if(query != null) {
//      //        System.out.println("valid query");
//      query = query.replace("(", "");
//      query = query.replace(")", "");
//      query = query.replace("\"", "");
//      query = query.replace("+", "");
//      String[] queryFields = query.split("AND|OR");
//      //        for(String queryField: queryFields) {
//      //          String[] pair = queryField.split(":");
//      //          if(pair.length  == 2) {
//      //            String fieldName = pair[0];
//      //            addStringField(instance, "field:" + fieldName);
//      //          }
//      //          else {
//      //            System.out.println("Invalid field key value pair");
//      //          }
//      //        }
//
//      // Number of fields as a separate field
//      addDoubleField(instance, "fieldCount", queryFields.length);
//      //        System.out.println("fieldCount: " + queryFields.length);
//    }
//
//    //      if(details.isSlowpool()) {
//    //        addDoubleField(instance, "slowpool", 1);
//    //      }
//    //      else {
//    //        addDoubleField(instance, "slowpool", 0);
//    //      }
//
//    // 5. qtime_i
//    // ---------------- add isNumber check -----------------
//    if(fields[4].isEmpty()) {
//      //      emptyCount++;
//    }
//    else {
//      int qtime = Integer.parseInt(fields[4]);
//      //addDoubleField(instance, "qtime", qtime);
//      //      qtimes.add(qtime);
//      //      instance.setClassValue(new Double(qtime)); // for regression
//
//      // --------- for classification --------------
//      if(qtime < slowQueryThreshold) {
//        //          fastQueries++;
//        instance.setClassValue(new Double(0));
//      }
//      else {
//        //          slowQueries++;
//        instance.setClassValue(new Double(1));
//      }
//    }
//
//
//    // 6. hits_i
//    if(fields[5].isEmpty()) {
//      //      emptyCount++;
//    }
//    else {
//      int hits = Integer.parseInt(fields[5]);
//      addDoubleField(instance, "hits", hits);
//    }
//
//    // 7. bitmask_s
//    addBitmaskBooleanFields(instance, "0000000");
//
//    return instance;
//  }
//
//  private static void addBitmaskBooleanFields(DenseInstance instance, String bitmask) {
//    if(bitmask.length() != 7) {
//      //System.out.println("Invalid bitmask: " + bitmask);
//      return;
//    }
//
//    addDoubleField(instance, "containsRangeQuery", Integer.parseInt(String.valueOf(bitmask.charAt(0))));
//    addDoubleField(instance, "isFacetSearch", Integer.parseInt(String.valueOf(bitmask.charAt(1))));
//    addDoubleField(instance, "isPropertyLookup", Integer.parseInt(String.valueOf(bitmask.charAt(2))));
//    addDoubleField(instance, "isPropertyHashLookup", Integer.parseInt(String.valueOf(bitmask.charAt(3))));
//    addDoubleField(instance, "isCollapsingSearch", Integer.parseInt(String.valueOf(bitmask.charAt(4))));
//    addDoubleField(instance, "isGeospatialSearch", Integer.parseInt(String.valueOf(bitmask.charAt(5))));
//    addDoubleField(instance, "containsOpenHomes", Integer.parseInt(String.valueOf(bitmask.charAt(6))));
//  }
//
//  private static void addDoubleField(DenseInstance instance, String attributeName, int attributeValue) {
//    if(attributeIndex.containsKey(attributeName)) {
//      int index = attributeIndex.get(attributeName);
//      instance.put(index, (double) attributeValue);
//    }
//    else {
//      int index = attributeCount;
//      attributeIndex.put(attributeName, attributeCount++);
//      instance.put(index, (double) attributeValue);
//    }
//  }
//
//  private static void addStringField(DenseInstance instance, String attribute) {
//    if(attributeIndex.containsKey(attribute)) {
//      int index = attributeIndex.get(attribute);
//      instance.put(index, (double) 1);
//    }
//    else {
//      int index = attributeCount;
//      attributeIndex.put(attribute, attributeCount++);
//      instance.put(index, (double) 1);
//    }
//  }
//}