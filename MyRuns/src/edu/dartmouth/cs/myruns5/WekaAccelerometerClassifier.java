package edu.dartmouth.cs.myruns5;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.classifiers.Classifier;

class WekaClassifier {

	public static double classify(Object[] i)
		    throws Exception {

		    double p = Double.NaN;
		    p = WekaClassifier.N54661bf40(i);
		    return p;
		  }
		  static double N54661bf40(Object []i) {
		    double p = Double.NaN;
		    if (i[64] == null) {
		      p = 1;
		    } else if (((Double) i[64]).doubleValue() <= 5.931448) {
		    p = WekaClassifier.N7bb55b751(i);
		    } else if (((Double) i[64]).doubleValue() > 5.931448) {
		    p = WekaClassifier.N3f879dc62(i);
		    } 
		    return p;
		  }
		  static double N7bb55b751(Object []i) {
		    double p = Double.NaN;
		    if (i[0] == null) {
		      p = 0;
		    } else if (((Double) i[0]).doubleValue() <= 56.45699) {
		      p = 0;
		    } else if (((Double) i[0]).doubleValue() > 56.45699) {
		      p = 1;
		    } 
		    return p;
		  }
		  static double N3f879dc62(Object []i) {
		    double p = Double.NaN;
		    if (i[64] == null) {
		      p = 2;
		    } else if (((Double) i[64]).doubleValue() <= 26.78291) {
		    p = WekaClassifier.N608752bf3(i);
		    } else if (((Double) i[64]).doubleValue() > 26.78291) {
		      p = 3;
		    } 
		    return p;
		  }
		  static double N608752bf3(Object []i) {
		    double p = Double.NaN;
		    if (i[1] == null) {
		      p = 2;
		    } else if (((Double) i[1]).doubleValue() <= 141.8736) {
		      p = 2;
		    } else if (((Double) i[1]).doubleValue() > 141.8736) {
		    p = WekaClassifier.N5449ab1b4(i);
		    } 
		    return p;
		  }
		  static double N5449ab1b4(Object []i) {
		    double p = Double.NaN;
		    if (i[3] == null) {
		      p = 2;
		    } else if (((Double) i[3]).doubleValue() <= 54.22886) {
		      p = 2;
		    } else if (((Double) i[3]).doubleValue() > 54.22886) {
		      p = 3;
		    } 
		    return p;
		  }
}
