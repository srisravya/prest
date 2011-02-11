package predictor;

import java.util.Date;
import java.text.DateFormat;

import weka.classifiers.*;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.j48.*;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.*;

public class WekaRunner
{

	private static String findPredResultPath(String trainPath)
	{
		String cut = trainPath.substring(0, trainPath.lastIndexOf(File.separator));
		return cut + File.separator;
	}

	private static int writeToFile(String folder, String fileNames, String nowStr, String data)
	{
		nowStr = nowStr.replaceAll(" ", "-");
		nowStr = nowStr.replaceAll(":", ".");
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(folder + "results-"+ fileNames  + "-" + nowStr + ".res"));
			writer.write(data);
			writer.flush();
			writer.close();

		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		return 1;
	}

	public static String runWeka(String trainPath, String testPath, String algorithm, String preProcess, String CrossValidate,
			String logFilter)
	{
		String fileNames = trainPath.substring(trainPath.lastIndexOf(File.separator) + 1) + "-"
		                   + trainPath.substring(testPath.lastIndexOf(File.separator) +  1);
		String output = "";
		output += "train file: " + trainPath + "\n";
		output += "test file: " + testPath + "\n";
		try
		{
			Date now = new Date();
			DateFormat df = DateFormat.getDateTimeInstance();
			String nowStr = df.format(now);
			//first load training set 
			Instances trainData = new Instances(new BufferedReader(new FileReader(trainPath)));
			// setting class attribute
			trainData.setClassIndex(trainData.numAttributes() - 1);

			//first load test set 
			//note: if cross validation is to be done than it is not used.
			Instances testData = new Instances(new BufferedReader(new FileReader(testPath)));

			//normalize data if option selected
			if (preProcess == "Normalize")
			{
				trainData = Filter.useFilter(trainData, new Normalize());
				testData = Filter.useFilter(testData, new Normalize());
			}

			// setting class attribute
			testData.setClassIndex(testData.numAttributes() - 1);
			Classifier cls = null;

			//choose your algorithm
			if (algorithm == "Naive Bayes")
			{
				cls = new NaiveBayes();
			}
			else if (algorithm == "J48")
			{
				cls = new J48();
			}

			cls.buildClassifier(trainData);

			Evaluation eval = new Evaluation(trainData);

			//if cross validate is selected use cross validation else use test data
			if (CrossValidate == "true")
			{
				eval.crossValidateModel(cls, trainData, 10);
			}
			else
			{
				eval.evaluateModel(cls, testData);
			}

			//show output on screen
			output += "Experiment Results\n" + nowStr + "\n\n" + eval.toClassDetailsString() + eval.toMatrixString() + "\n\n";

			// output the ID, actual value and predicted value for each instance
			for (int i = 0; i < testData.numInstances(); i++)
			{
				double pred = cls.classifyInstance(testData.instance(i));
				output += ("ID: " + testData.instance(i).value(0));
				output += (", actual: " + testData.classAttribute().value((int) testData.instance(i).classValue()));
				output += (", predicted: " + testData.classAttribute().value((int) pred) + "\n");
			}
			writeToFile(findPredResultPath(trainPath), fileNames,  nowStr, output);

		}
		catch (Exception e)
		{
			//should be extended to handle other exceptions
			output = "error in parsing, examine your datasets...";
		}

		return output;
	}
}
