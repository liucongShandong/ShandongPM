package equalscale.SampleEvaluation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;


@Plugin(
		name = "111111Sample Evalution Plugin",// plugin name
		
		returnLabels = {"Final results"}, //return labels
		returnTypes = {String.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = { "Name of your first original Log", "Name of your second Sample Log" },
		userAccessible = true,
		help = "This plugin aims to sample an input large-scale example log and returns a small sample log by measuring the significance of traces." 
		)
public class SampleEvalutionPlugin {

	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl"
	        )
	@PluginVariant(
			variantLabel = "Merge two Event Log, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0,1}
			)
	public static String SimRankSamplingTechnique(UIPluginContext context,XLog OriginalLog, XLog SampleLog) throws UserCancelledException
	{
		//����˼��:
		//��һ�����õ�ֱ�Ӹ�����ϵ
		//����ָ�꣺coverage:������ ��ֱ�Ӹ�������/�¼���־�е�ֱ�Ӹ�����ϵ��������������Ϊ
		//����ָ��2������ȵ���Ԥ�ڵĲ�����Ϊ��ʵ�ʵĲ�����Ϊ֮������
		//           ������ eΪԤ�ڵ���Ϊ��sΪ��������Ϊ
		//			  ��һ��ƽ���������(NAME):����Ҫ�þ���ֱ�����б�����Ϳ��Ա�ʾ
		//			  �Գ�ƽ�����԰ٷֱ����(sMAPE):��Ϊ��Ƿ�������ܵ������صĳͷ�
		/*
		 * �㷨���裺
		 * 1.�����־�еı���
		 * �ҵ�ԭʼ��־�Ͳ�����־��ֱ�Ӹ�����ϵ��������־���Լ���������hashmap���� ������
		 * 2.e����Ƶ�Σ�
		 * 
		 * 
		 */
		//set the sampling ratio
		double SampleRatio = ProMUIHelper.queryForDouble(context, "Select the sampling ratios", 0, 1,	0.3);		
		context.log("Sampling Ratio is: "+SampleRatio, MessageLevel.NORMAL);	
		//���������裬����ΪSampleRatio,Ĭ��Ϊ30%
//		double SampleRatio=0.3;
		
		QualityMetrics qualityMetrics = new QualityMetrics();
//		HashMap<XTrace,Integer> traceFrequency_orginalLog=GetVariantLog(OriginalLog);
//		HashMap<XTrace,Integer> traceFrequency_sampleLog=GetVariantLog(SampleLog);
		
		
		HashMap<String,Integer> DfrNumber_original=getDfrNumber(OriginalLog);
		HashMap<String,Integer> DfrNumber_sample=getDfrNumber(SampleLog);
		
		double coverage=(double)DfrNumber_sample.size()/DfrNumber_original.size();
		System.out.println("DfrNumber_original.size(): " +DfrNumber_original.size());
		System.out.println("DfrNumber_sample.size(): " +DfrNumber_sample.size());
		System.out.println("coverage(������) = " +coverage);
		qualityMetrics.setCoverage(coverage);
		
	
		HashMap<String,Double> DfrNumber_except_original=new HashMap<>();
		Map<String, Integer> map0 = DfrNumber_original;
		for(Map.Entry<String, Integer> entry : map0.entrySet()){
//			System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
			DfrNumber_except_original.put(entry.getKey(), entry.getValue()*SampleRatio);
		}
		
		
		
		
		//1.����NAME
//		System.out.println("ԭʼ��־Ϊ:\n");
		Map<String, Double> map1 = DfrNumber_except_original;
		double fenzi=0;
		double fenmu=0;
		for(Map.Entry<String, Double> entry : map1.entrySet()){
//			System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
			fenmu+= entry.getValue();
			if(DfrNumber_sample.containsKey(entry.getKey())) {
//				System.out.println("DfrNumber_sample.get(entry.getKey()):"+DfrNumber_sample.get(entry.getKey()));
				fenzi+=Math.abs(entry.getValue()-(double)DfrNumber_sample.get(entry.getKey()));
			}else {
//				System.out.println("entry.getValue():"+entry.getValue());
				fenzi+=entry.getValue();
			}
		}

		
		System.out.println("fenzi=" +fenzi+ ", fenmu = " + fenmu);
		double NAME_value=fenzi/fenmu;
		System.out.println("NAME_value(ָ��1) = " + NAME_value);
		qualityMetrics.setNAME(NAME_value);
		
		
		//2.�Գ�ƽ����� sMAPE
		Map<String, Double> map2 = DfrNumber_except_original;

		double abs_sum=0;
		for(Map.Entry<String, Double> entry : map2.entrySet()){
//			fenmu+= entry.getValue();
			double fenzi1=0;
			double fenmu1=0;
			if(DfrNumber_sample.containsKey(entry.getKey())) {
				fenzi1+=Math.abs(entry.getValue()-(double)DfrNumber_sample.get(entry.getKey()));
				fenmu1+=entry.getValue()+(double)DfrNumber_sample.get(entry.getKey());
				abs_sum+=fenzi1/fenmu1;
			}else {
				abs_sum+=1;
			}
		}
		double sMAPE_value=abs_sum/ DfrNumber_except_original.size();
		System.out.println("sMAPE_value(ָ��2) = " + sMAPE_value);
		qualityMetrics.setSMAPE(sMAPE_value);
		
		return VisualizeQualityMetrics.visualizeQualityMetrics(qualityMetrics);
	}
	/**
	 * ��ñ�����־
	 * @param OriginalLog
	 * @return
	 */
	public static HashMap<XTrace,Integer> GetVariantLog(XLog OriginalLog) {
		HashMap<XTrace,String> compareString=new HashMap<>();
		HashSet<String> compareString1=new HashSet<>();
		for (XTrace trace: OriginalLog)	{   ///all trace size!=0
			String mergeTransition="";
			for(XEvent event: trace){  
				String transition = event.getAttributes().get("concept:name").toString();       //Resource
				mergeTransition=mergeTransition+transition;
			}
			compareString1.add(mergeTransition);
			compareString.put(trace, mergeTransition);
		}
        //step1:�Թ켣Ƶ�����м���
		HashMap<XTrace,Integer> traceFrequency=new HashMap<>();
		for(XTrace trace1:OriginalLog)
		{
			int count=0;
			for(XTrace trace2:OriginalLog)
			{
				if(compareString.get(trace1).equals(compareString.get(trace2))) {
					count++;
				}
			}
			traceFrequency.put(trace1, count);
		}
		//step2-0:��õ�һ���� variant
		for(XTrace trace:OriginalLog) {
			String m1=compareString.get(trace);
			if(compareString1.contains(m1)) {//hashset
				compareString1.remove(m1);
			}else {
				traceFrequency.remove(trace);//ɾ���켣��ͬ�Ĺ켣
			}
		}
		return traceFrequency;
	}
	/**
	 * ������־��ֱ�Ӹ����Լ�Ƶ��
	 * @param OriginalLog
	 * @return
	 */
	public static HashMap<String,Integer> getDfrNumber(XLog OriginalLog) {
		//trace to direct-follow relation set
		HashMap<String, HashSet<String>> traceIDToDFRSet = new HashMap<>();
		for(XTrace trace:OriginalLog)
		{
			HashSet<String> dfrSet = new HashSet<>();
			for(int i =0;i<trace.size()-1;i++)
			{
				//add activity
//						activitySet.add(XConceptExtension.instance().extractName(trace.get(i)));
				//add directly follow pair
				dfrSet.add(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1)));
			}
			traceIDToDFRSet.put(XConceptExtension.instance().extractName(trace), dfrSet);
		}
		
		//direct-follow relation set of the log
		HashSet<String> dfrSetLog = new HashSet<>();
		for(String traceID: traceIDToDFRSet.keySet())
		{
			dfrSetLog.addAll(traceIDToDFRSet.get(traceID));
		}
		//direct-follow relation to significance. the number of traces contain the drf divided by the total number of traces. 
		HashMap<String, Integer> dfrNumber = new HashMap<>();
		for(String dfr: dfrSetLog)
		{
			//count the number of traces that contains dfr
			int count =0;
			for(XTrace trace: OriginalLog)
			{
				for(int i =0;i<trace.size()-1;i++)
				{
					if(dfr.equals(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1))))
					{
						count++;
						break;
					}
				}
			}
			dfrNumber.put(dfr, count);
		}
		return dfrNumber;
	}
	
	/**
	 * ����켣��ֱ�Ӹ����Լ�Ƶ��
	 * @param OriginalLog
	 * @return
	 */
	public static HashSet<String> getDfrNumber(XTrace trace) {
		//trace to direct-follow relation set
		HashMap<String, HashSet<String>> traceIDToDFRSet = new HashMap<>();
		HashSet<String> dfrSet = new HashSet<>();
		for(int i =0;i<trace.size()-1;i++)
		{
			//add activity
//			activitySet.add(XConceptExtension.instance().extractName(trace.get(i)));
			//add directly follow pair
			dfrSet.add(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1)));
		}
        return dfrSet;
	}
}
