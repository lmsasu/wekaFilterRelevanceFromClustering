/**
 * Implements a Weka filter which computes the relevance of each instance in a data set, based n a clustering step
 */
package weka.filters.unsupervised.instance;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import weka.clusterers.XMeans;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Capabilities.Capability;
import weka.filters.Filter;
import weka.filters.SimpleBatchFilter;
import weka.filters.unsupervised.attribute.Remove;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;

/**
 * @author Lucian Sasu lmsasu &lt;at&gt; yahoo dot com
 */
public class RelevanceClusteringClosestCentrHighRel extends SimpleBatchFilter {

	/**
	 * for serialization purposes
	 */
	private static final long serialVersionUID = 3L;

	public static enum RelevanceFunctionModifier {
		IDENTICAL("Identical"), LOG("Logarithm"), SIGMOID("Sigmoid"), EXP("Exp");

		private final String m_stringVal;

		RelevanceFunctionModifier(String name) {
			m_stringVal = name;
		}

		@Override
		public String toString() {
			return m_stringVal;
		}
	}
	
	public static enum ClosestCentroidImpact {
		ClosestCentroidHighRelevance("HighRelevance"), ClosestCentroidLowRelevance("LowRelevance");

		private final String m_stringVal;

		ClosestCentroidImpact(String name) {
			m_stringVal = name;
		}

		@Override
		public String toString() {
			return m_stringVal;
		}
	}

	public static final Tag[] FUNCTION_MODIFIERS_SELECTION = { new Tag(RelevanceFunctionModifier.IDENTICAL.ordinal(), "Identical"),
			new Tag(RelevanceFunctionModifier.LOG.ordinal(), "Logarithm"),
			new Tag(RelevanceFunctionModifier.SIGMOID.ordinal(), "Sigmoid"),
			new Tag(RelevanceFunctionModifier.EXP.ordinal(), "Exp"), };
	
	public static final Tag[] CLOSEST_CENTROID_IMPACT_SELECTION = { 
			new Tag(ClosestCentroidImpact.ClosestCentroidHighRelevance.ordinal(), "HighRelevance"),
			new Tag(ClosestCentroidImpact.ClosestCentroidLowRelevance.ordinal(), "LowRelevance"),
			};

	// to avoid division by zero
	final static double epsilon = 1e-3;

	protected RelevanceFunctionModifier m_relevanceFunctionModifier = RelevanceFunctionModifier.IDENTICAL;
	protected ClosestCentroidImpact m_closestCentroidImpact = ClosestCentroidImpact.ClosestCentroidHighRelevance;

	/*
	 * (non-Javadoc)
	 * 
	 * @see weka.filters.SimpleFilter#determineOutputFormat(weka.core.Instances)
	 */
	@Override
	protected Instances determineOutputFormat(Instances arg0) throws Exception {
		return arg0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see weka.filters.SimpleFilter#globalInfo()
	 */
	@Override
	public String globalInfo() {
		return "Computes relevance scores for data instances, based on clustering";
	}

	/**
	 * Gets the current settings of the filter.
	 * 
	 * @return an array of strings suitable for passing to setOptions
	 */
	@Override
	public String[] getOptions() {
		ArrayList<String> options = new ArrayList<String>();

		options.add("-F");
		options.add(getRelevanceFunctionModifier().toString());
		
		options.add("-C");
		options.add(getClosestCentroidImpact().toString());

		return options.toArray(new String[1]);
	}

	/**
	 * Parses a given list of options.
	 * <p/>
	 * 
	 * <!-- options-start --> Valid options are:
	 * <p/>
	 * 
	 * <pre>
	 * -F &lt;Identical | Logarithm | Sigmoid | Exp&gt;
	 *  Aggregation function for numeric attributes.
	 *  (default: Identical).
	 * </pre>
	 * 
	 * 	<pre>
	 * -C &lt;HighRelevance | LowRelevance&gt;
	 *  The impact the closest centroid has on an instance.
	 *  (default: HighRelevance).
	 * </pre>
	 * 
	 * <!-- options-end -->
	 * 
	 * @param options
	 *            the list of options as an array of strings
	 * @throws Exception
	 *             if an option is not supported
	 */
	@Override
	public void setOptions(String[] options) throws Exception {

		String functionModifier = Utils.getOption('F', options);
		if (functionModifier.length() != 0) {

			RelevanceFunctionModifier selected = null;
			for (RelevanceFunctionModifier n : RelevanceFunctionModifier.values()) {
				if (n.toString().equalsIgnoreCase(functionModifier)) {
					selected = n;
				}
			}
			if (selected == null) {
				throw new Exception("Unknown function type: " + functionModifier);
			} else {
				setRelevanceFunctionModifier(new SelectedTag(selected.ordinal(), FUNCTION_MODIFIERS_SELECTION));
			}
		}
		
		String closestCentroidImpact = Utils.getOption('C', options);
		if (closestCentroidImpact.length() != 0) {

			ClosestCentroidImpact selected = null;
			for (ClosestCentroidImpact n : ClosestCentroidImpact.values()) {
				if (n.toString().equalsIgnoreCase(closestCentroidImpact)) {
					selected = n;
				}
			}
			if (selected == null) {
				throw new Exception("Unknown impact type: " + closestCentroidImpact);
			} else {
				setClosestCentroidImpact(new SelectedTag(selected.ordinal(), CLOSEST_CENTROID_IMPACT_SELECTION));
			}
		}

		Utils.checkForRemainingOptions(options);
	}

	/**
	 * Returns the revision string.
	 * 
	 * @return the revision
	 */
	@Override
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 2 $");
	}

	@Override
	/**
	 * Gets the capabilities; useful for Weka environment
	 */
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.enableAllAttributes();
		result.enableAllClasses();
		result.enable(Capability.NO_CLASS);
		return result;
	}

	public void setRelevanceFunctionModifier(SelectedTag tag) {
		int ordinal = tag.getSelectedTag().getID();

		for (RelevanceFunctionModifier n : RelevanceFunctionModifier.values()) {
			if (n.ordinal() == ordinal) {
				m_relevanceFunctionModifier = n;
				break;
			}
		}
	}

	public SelectedTag getRelevanceFunctionModifier() {
		return new SelectedTag(m_relevanceFunctionModifier.ordinal(), FUNCTION_MODIFIERS_SELECTION);
	}

	public String relevanceFunctionModifierTipText() {
		return "The type of function to be applied over the relevance values";
	}
	
	public void setClosestCentroidImpact(SelectedTag tag) {
		int ordinal = tag.getSelectedTag().getID();

		for (ClosestCentroidImpact n : ClosestCentroidImpact.values()) {
			if (n.ordinal() == ordinal) {
				m_closestCentroidImpact = n;
				break;
			}
		}
	}

	public SelectedTag getClosestCentroidImpact() {
		return new SelectedTag(m_closestCentroidImpact.ordinal(), CLOSEST_CENTROID_IMPACT_SELECTION);
	}

	public String closestCentroidImpactTipText() {
		return "How the distance to the closest centroid contributes to the relevance computation";
	}

	/**
	 * Returns an enumeration describing the available options.
	 * 
	 * @return an enumeration of all the available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		Vector<Option> newVector = new Vector<Option>();

		newVector.add(new Option("\tFunction modifier." + "\n\t(default: Identical).", "F", 1,
				"-F <Identical | Logarithm | Sigmoid | Exp>"));
		
		newVector.add(new Option("\tClosest centroid impact." + "\n\t(default: HighRelevance).", "C", 1,
				"-F <HighRelevance | LowRelevance>"));

		return newVector.elements();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see weka.filters.SimpleFilter#process(weka.core.Instances)
	 */
	@Override
	protected Instances process(Instances instances) throws Exception {

		if (instances.numInstances() == 1) {
			return instances;// nothing to cluster
		} else {
			// cluster through xmeans and get the centroids

			Instances trainWOClasses = removeClass(instances);
			// cluster
			Instances centers = getCentroids(trainWOClasses);
			
			double minRelevance = Double.POSITIVE_INFINITY;

			for (int i = 0; i < instances.numInstances(); i++) {
				double[] distances = computeDistances(trainWOClasses.get(i), centers);
				double minDistance = getMin(distances);
				double relevance = computeRelevance(minDistance);
				relevance = applyFunction(relevance);
				if (relevance < 0)
				{
					minRelevance = Math.min(minRelevance, relevance);
				}
				instances.get(i).setWeight(relevance);
			}
			
			if (minRelevance <= 0)
			{
				System.err.println("minRelevance= " + minRelevance);
				//we shift all relevances above 1.0
				for (int i = 0; i < instances.numInstances(); i++) {
					instances.get(i).setWeight(instances.get(i).weight() - minRelevance + 1.0);
				}
			}
		}

		return instances;
	}

	private double computeRelevance(double minDistance) throws Exception {
		if (m_closestCentroidImpact == ClosestCentroidImpact.ClosestCentroidHighRelevance)
		{
			return 1.0 / (epsilon + minDistance);
		}
		if (m_closestCentroidImpact == ClosestCentroidImpact.ClosestCentroidLowRelevance)
		{
			return minDistance;
		} 
		throw new Exception("Unknown strategy: " + m_closestCentroidImpact.toString());
	}

	private double applyFunction(double relevance) throws Exception {
		if (m_relevanceFunctionModifier == RelevanceFunctionModifier.IDENTICAL)
		{
			return relevance;
		}
		if (m_relevanceFunctionModifier == RelevanceFunctionModifier.EXP)
		{
			return Math.exp(relevance);
		}
		if (m_relevanceFunctionModifier == RelevanceFunctionModifier.LOG)
		{
			return Math.log(relevance);
		}
		if (m_relevanceFunctionModifier == RelevanceFunctionModifier.SIGMOID)
		{
			return sigmoid(relevance);
		}
		throw new Exception("Unknown relevance function modifier: " + m_relevanceFunctionModifier.toString());
	}

	private static double sigmoid(double relevance) {
		return 1.0 / (1 + Math.exp(-relevance));
	}

	/**
	 * The current relevances are changed to be at least 1
	 * 
	 * @param instances
	 *            the set of instance whose weights (relevances) are to be
	 *            updated
	 */
	private void changeRelevancesToMin1(Instances instances) {
		double minWeight = Double.POSITIVE_INFINITY;
		for (Instance instance : instances) {
			minWeight = Math.min(minWeight, instance.weight());
		}
		for (Instance instance : instances) {
			instance.setWeight(instance.weight() / (epsilon + minWeight));
		}
	}

	/***
	 * Return the minimum value form a vector
	 * 
	 * @param values
	 *            a vector of double values
	 * @return the minimum value within the vector
	 */
	private static double getMin(final double[] values) {
		double min = values[0];
		for (int i = 1; i < values.length; i++) {
			min = Math.min(min, values[i]);
		}
		return min;
	}

	/***
	 * Computes the distance between an instance an the cluster centroids
	 * 
	 * @param instance
	 *            the data row (class missing) for which the distance to
	 *            centroids must be computed
	 * @param centroids
	 *            the centroids as computed in the clustering step
	 * @return a vector of double value containing the distances from instance
	 *         to all centroids
	 */
	private static double[] computeDistances(Instance instance, Instances centroids) {
		double[] distances = new double[centroids.numInstances()];

		for (int i = 0; i < centroids.numInstances(); i++) {
			Instance centroid = centroids.get(i);
			distances[i] = computeDistance(instance, centroid);
		}

		return distances;
	}

	/***
	 * Computes the distance between an instance and a centroid
	 * 
	 * @param instance
	 *            a data row (class ommited) for which the distance to the given
	 *            centroid must be computed
	 * @param centroid
	 *            a cluster centroid
	 * @return a double value
	 */
	private static double computeDistance(Instance instance, Instance centroid) {
		double sum = 0.0;
		for (int i = 0; i < instance.numAttributes(); i++) {
			sum += Math.pow(instance.value(i) - centroid.value(i), 2);
		}
		return Math.sqrt(sum);
	}

	/***
	 * Applies a clustering algorithm for a data set
	 * 
	 * @param instances
	 *            the instances to be clustered
	 * @return a set of centroids
	 * @throws Exception
	 */
	private static Instances getCentroids(Instances instances) throws Exception {
		XMeans clusterer = new XMeans();
		clusterer.setMaxNumClusters(1000);// TODO: expose as hyperparameter
		clusterer.setMinNumClusters(2);// TODO: expose as hyperparameter
		clusterer.setMaxIterations(1000);// TODO: expose as hyperparameter
		clusterer.buildClusterer(instances);

		Instances centroids = clusterer.getClusterCenters();
		return centroids;
	}

	/**
	 * Removes the class from an instance
	 * 
	 * @param instances
	 *            a set of weka instances with classes
	 * @return a set of weka instances without classes
	 * @throws Exception
	 *             by weka internals
	 */
	private static Instances removeClass(Instances instances) throws Exception {
		Instances instancesWOClasses = new Instances(instances);
		Remove remove = new Remove();
		System.out.println("class index to be removed: " + (1 + instancesWOClasses.classIndex()));
		System.out.println("Instances count: " + instances.numInstances());
		remove.setAttributeIndices(1 + instancesWOClasses.classIndex() + "");
		remove.setInputFormat(instancesWOClasses);
		instancesWOClasses = Filter.useFilter(instancesWOClasses, remove);
		return instancesWOClasses;
	}
}
