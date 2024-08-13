package equalscale.SampleMethods;

/*
 * A fast algorithm to sample log. 
 * The original idea is referred to "The automatic creation of literature abstracts". 
 * 
 * rank a trace by its significance;
 * the significance of a trace is determined by the combination of its activity significance and dfr significance. 
 * ��־�����Բ���
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;

import equalscale.SampleEvaluation.SampleEvalutionPlugin;




public class FastSetCoverMethodsPlus {
	@Plugin(
			name = " (Fasted set cover Plus)sampling Plugin",// plugin name
			returnLabels = {"Sample Log"}, //return labels
			returnTypes = {XLog.class},//return class
			parameterLabels = {"Orginal Event Log"},
			userAccessible = true,
			help = "This plugin aims to the orginal  log and sample log,returns a similarity  value by measuring the log similarity." 
			)
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl"
	        )
	@PluginVariant(
			variantLabel = "Sampling Big Event Log, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	
	public static XLog DijkstraPlusSampling(UIPluginContext context, XLog originalLog) throws UserCancelledException
	{
		
		double startTime_total=0;
		double endTime_total=0;
		//set the sampling ratio
		double samplingRatio = ProMUIHelper.queryForDouble(context, "Select the sampling ratios", 0, 1,	0.3);		
		context.log("Interface Sampling Ratio is: "+samplingRatio, MessageLevel.NORMAL);	
		startTime_total=System.currentTimeMillis();
		XLog log=DijkstraPlus(originalLog,samplingRatio);
		endTime_total=System.currentTimeMillis();
		
		System.out.println("***********************");
		System.out.println("Set Cover Time:"+(endTime_total-startTime_total));
		System.out.println("***********************");
		context.log("Set Cover Time:"+(endTime_total-startTime_total));	
		return log;
	}
	   //������־�����������־
		public static XLog DijkstraPlus(XLog originalLog, double SampleRatio) {
			
			XLog sampleLog = new XLogImpl(originalLog.getAttributes());
			int org_size=originalLog.size();

			/**
			 * Ԥ������ȡ�켣���弰��Ƶ��
			 * 
			 */
			System.out.println("before originalLog.size():"+originalLog.size());
			sampleLog= PreprocessLog(originalLog,SampleRatio);
			System.out.println("After originalLog.size():"+originalLog.size());
			System.out.println("After sampleLog.size():"+sampleLog.size());
			
			/**
			 * 1.����ԭʼ��־������ֵ
			 */
			
			//����dfr������ֵ
			HashMap<String,Integer> DfrNumber_original=SampleEvalutionPlugin.getDfrNumber(originalLog);
			System.out.println("DfrNumber_original:"+DfrNumber_original);
			//���������裬����ΪSampleRatio,Ĭ��Ϊ30%
//			double SampleRatio=0.1;
			HashMap<String,Double> DfrNumber_except_original=new HashMap<>();
			HashMap<String,Double> DfrNumber_except_org=new HashMap<>();
			Map<String, Integer> map0 = DfrNumber_original;
			for(Map.Entry<String, Integer> entry : map0.entrySet()){
				DfrNumber_except_original.put(entry.getKey(), entry.getValue()*SampleRatio);
				DfrNumber_except_org.put(entry.getKey(), entry.getValue()*SampleRatio);
//				System.out.println("�켣���� " + entry.getKey()+"ֵ�� " + entry.getValue()*SampleRatio);
			}
			
			/**
			 * 2.����ֵС��1�ļ���
			 */
			
			HashMap<String,Double> DfrNumber_except_original0=new HashMap<>();//����dfֵ����1�ļ���
			HashMap<String,Double> DfrNumber_except_original1=new HashMap<>();//����dfֵС��1�ļ���
			Map<String, Double> map1 = DfrNumber_except_original;
			for(Map.Entry<String, Double> entry : map1.entrySet()){
				if(entry.getValue()>0 && entry.getValue()<1) {
					DfrNumber_except_original1.put(entry.getKey(),entry.getValue());
				}else if(entry.getValue()>1) {
					DfrNumber_except_original0.put(entry.getKey(),entry.getValue());
				}
			}
			
			/**
			 * ��logrank++̰���㷨���Թ켣����ѡȡ��ֱ�������dfƵ�ζ�����1��ֵ������10
			 */
			//����˵���������֮��������־�Ĵ�СӦ�ò�����ԭʼ��־�Ĵ�С*����ֵ
			//ѡ����Ե�ѡȡ  128���켣
			//ѭ��
			int orgSize=(int) (org_size*SampleRatio-sampleLog.size());
			int count0=1;
			int flag=1;
			int threnold=128;
			while(count0 <=orgSize-threnold && flag==1) {
				double allCurrentCost001=CurrentCost(DfrNumber_except_org,sampleLog);
				System.out.println("֮ǰ��ֵΪ"+allCurrentCost001);
				XLog sampleLog2=SigRankSamplingTechnique(DfrNumber_except_original0,originalLog,threnold);//����logrank++��
				for(XTrace trace:sampleLog2) {
					originalLog.remove(trace);
					sampleLog.add(trace);
				}
				double allCurrentCost002=CurrentCost(DfrNumber_except_org,sampleLog);
				System.out.println("֮���ֵΪ"+allCurrentCost002);
				if(allCurrentCost001 < allCurrentCost002) {
					flag=0;
				}
				System.out.println("originalLog.size:"+originalLog.size());
				//����dfr������ֵ
				HashMap<String,Integer> DfrNumber_original1=SampleEvalutionPlugin.getDfrNumber(originalLog);
				
				Map<String, Double> map3 = DfrNumber_except_original;
				Map<String, Integer> map00 = DfrNumber_original1;
				for(Map.Entry<String, Integer> entry : map00.entrySet()){
					DfrNumber_except_original.put(entry.getKey(), entry.getValue()*SampleRatio);
				}
				
				System.out.println("***************************");
				System.out.println("DfrNumber_except_original:"+DfrNumber_except_original);
				DfrNumber_except_original0.clear();//��������ֵ����1��df����  ���
				for(Map.Entry<String, Double> entry : map3.entrySet()){
					if(entry.getValue()>0 &&entry.getValue()<1) {
						DfrNumber_except_original1.put(entry.getKey(),entry.getValue());
					}else if(entry.getValue()>= 1) {
						DfrNumber_except_original0.put(entry.getKey(),entry.getValue());
					}
				}
				System.out.println("originalLog:"+originalLog.size());
				Map<String, Double> map4 = DfrNumber_except_original0;
				for(Map.Entry<String, Double> entry : map4.entrySet()){//ȷ��dfƵ�ζ�С��10������10��ѭ��
					if(entry.getValue()>10) {
						flag =flag | 1;
					}
				}
				System.out.println("ѭ����"+(count0-1)/128+"��");
				count0+=threnold;	
			}
			
			double allCurrentCost00=CurrentCost(DfrNumber_except_org,sampleLog);
			System.out.println("����nameֵΪ:" +allCurrentCost00);
			return sampleLog;
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
				//add directly follow pair
				dfrSet.add(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1)));
			}
	        return dfrSet;
		}
		public static XLog SigRankSamplingTechnique(HashMap<String,Double> DfrNumber_except_original0,XLog originalLog, int topN)
		{
			XLog sampleLog = new XLogImpl(originalLog.getAttributes());
			//trace to significance	
			HashMap<String, Double> traceToSignificance = new HashMap<>();
			for(XTrace trace: originalLog)
			{
				HashSet<String> trace_DFR=getDfrNumber(trace);
				Iterator<String> it = trace_DFR.iterator();
				double traceNumber=0.0;
				while (it.hasNext()) {
				  String str = it.next();
				  if(DfrNumber_except_original0.containsKey(str)) {
					  traceNumber+=DfrNumber_except_original0.get(str);
				  }else {
					  traceNumber+=0.0;
				  }  
				}
//				System.out.println("�켣����"+XConceptExtension.instance().extractName(trace)+"  ��Ӧֵ��"+traceNumber);
				traceToSignificance.put(XConceptExtension.instance().extractName(trace), traceNumber);
			}
			//order traces based on the weight
			HashSet<String> sampleTraceNameSet=SortingHashMapByValues.sortMapByValues(traceToSignificance,topN);
			
			System.out.println("Sample Trace Names: "+sampleTraceNameSet);
			//construct the sample log based on the selected top n traces. 
			for(XTrace trace: originalLog)
			{
				if(sampleTraceNameSet.contains(trace.getAttributes().get("concept:name").toString()))
				{
					sampleLog.add(trace);
				}
			}
			
			//return the sample log. 
			return sampleLog;	
		}
		
		
		
		
		
		
		
		
		
	    public static XLog PreprocessLog(XLog originalLog,double SampleRatio) {
	    	XLog sampleLog = new XLogImpl(originalLog.getAttributes());
//			****************************************��ù켣����******************************
			HashMap<XTrace,String> compareString=new HashMap<>();
			HashSet<String> compareString1=new HashSet<>();
			for (XTrace trace: originalLog)	{   ///all trace size!=0
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
			for(XTrace trace1:originalLog)
			{
				int count=0;
				for(XTrace trace2:originalLog)
				{
					if(compareString.get(trace1).equals(compareString.get(trace2))) {
						count++;
					}
				}
				traceFrequency.put(trace1, count);
			}
			//step2-0:��õ�һ���� variant
			for(XTrace trace:originalLog) {
				String m1=compareString.get(trace);
				if(compareString1.contains(m1)) {//hashset
					compareString1.remove(m1);
				}else {
					traceFrequency.remove(trace);//ɾ���켣��ͬ�Ĺ켣
				}
			}
			//traceFrequency�б�����ǹ켣�����Լ�Ƶ��
			/********************************************************************************************/
			Map<XTrace, Integer> map = traceFrequency;
			XLog VariantLog = new XLogImpl(originalLog.getAttributes());
			HashMap<XTrace,Integer> new_traceSet=new HashMap<>();
			for(Map.Entry<XTrace, Integer> entry : map.entrySet()){
				VariantLog.add(entry.getKey());
				new_traceSet.put(entry.getKey(), (int)Math.floor(entry.getValue()*SampleRatio));
			}
			Map<XTrace, Integer> map1 = new_traceSet;
			for(Map.Entry<XTrace, Integer> entry : map1.entrySet()){
				if(entry.getValue()>0) {
					for(int i=0;i <entry.getValue();i++) {
						sampleLog.add(entry.getKey());
						
					}
				}
			}	
			//
			HashMap<String,Integer> compareString_samplelog=new HashMap<>();
			for (XTrace trace: sampleLog)	{   ///������־������
				String mergeTransition="";
				for(XEvent event: trace){  
					String transition = event.getAttributes().get("concept:name").toString();       //Resource
					mergeTransition=mergeTransition+transition;
				}
				if(compareString_samplelog.containsKey(mergeTransition)) {
					compareString_samplelog.put(mergeTransition, compareString_samplelog.get(mergeTransition)+1);
				}else {
					compareString_samplelog.put(mergeTransition, 1);
				}
			}
			
			XLog sampleLog1 = new XLogImpl(originalLog.getAttributes());
			for (XTrace trace: originalLog)	{   ///all trace size!=0
				String mergeTransition="";
				for(XEvent event: trace){  
					String transition = event.getAttributes().get("concept:name").toString();       //Resource
					mergeTransition=mergeTransition+transition;
				}
				if(compareString_samplelog.containsKey(mergeTransition) && compareString_samplelog.get(mergeTransition)>0) {
					sampleLog1.add(trace);
					compareString_samplelog.put(mergeTransition,compareString_samplelog.get(mergeTransition)-1);
				}
			}	
			for(XTrace trace: sampleLog1) {
				originalLog.remove(trace);
			}
			
	        return sampleLog1;
	    }
		/**
	     * ����Ƿ�ȫ��������(ֻҪ��һ����δ����������false)
	     *
	     * @return boolean
	     */
	    public static boolean isMinVisited(HashMap<String,Double> StatusVisited) {
	    	Map<String, Double> map4 =StatusVisited ;
			for(Entry<String, Double> entry : map4.entrySet()){
				if(entry.getValue()>1) {
					return false;
				}
			}
	        return true;
	    }
		public static XLog SetCover(XLog originalLog,HashMap<String,Double> DfrNumber_except_original0)
		{	
			//create a new log with the same log-level attributes. 
			XLog sampleLog = new XLogImpl(originalLog.getAttributes());
			//keep an ordered list of traces names. 
			ArrayList<String> TraceIdList = new ArrayList<>();
			
			//convert the log to a map, the key is the name of the trace, and the value is the trace. 
			HashMap<String, XTrace> TraceID2Trace = new HashMap<>();
			for(XTrace trace: originalLog)
			{
				TraceIdList.add(trace.getAttributes().get("concept:name").toString());
				TraceID2Trace.put(trace.getAttributes().get("concept:name").toString(), trace);
			}
//			for(int i=0;i<TraceIdList.size();i++) {
//				System.out.println("TraceIdList"+i+":"+TraceIdList.get(i));
//				System.out.println(TraceID2Trace.get(TraceIdList.get(i)));
//			}
			
			//trace to direct-follow relation set
			HashMap<String, HashSet<String>> traceIDToDFRSet = new HashMap<>();
			for(XTrace trace:originalLog)
			{
				HashSet<String> dfrSet = new HashSet<>();
				for(int i =0;i<trace.size()-1;i++)
				{
					//add directly follow pair
					dfrSet.add(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1)));
				}
				traceIDToDFRSet.put(XConceptExtension.instance().extractName(trace), dfrSet);
			}
			
			//direct-follow relation set of the log
			HashSet<String> dfrSetLog = new HashSet<>();
			Map<String, Double> map1 = DfrNumber_except_original0;
			for(Map.Entry<String, Double> entry : map1.entrySet()){
				if(entry.getValue()>1) {
					dfrSetLog.add(entry.getKey());
				}
				
			}
			

//			System.out.println("--------��ǿforѭ������---------");
//			for (String item : dfrSetLog) {
//				System.out.println(item+",");
//			}

//				 �����㲥��̨,���뵽Map
	        HashMap<String,HashSet<String>> broadcasts = new HashMap<String, HashSet<String>>();
	        broadcasts=traceIDToDFRSet;//������еĹ켣
	        //allAreas ������еĵ���
	        HashSet<String> allAreas = new HashSet<String>();
	        allAreas=dfrSetLog;
	        //��ѡ��ĵ���
	        ArrayList<String> selects = new ArrayList<String>();
	        //��ʱ����,�����ʱѡ��ĵ��������ĵ���
	        HashSet<String> tempSet = new HashSet<String>();
	        //��Ź켣
	        HashSet<XTrace> traceSet = new HashSet<XTrace>();
	        //ָ�������Ž��ָ��
	        String maxKey = null;
//	        System.out.println("232323 ");
	        //��ʣ��ĵ�����Ϊ0ʱ,����ѡ��
	        int num=allAreas.size();
	        while (allAreas.size() != 0 && num > 0) {
	        	
	            //ָ���ÿ�
	            maxKey = null;
	            //�������еĵ���,�ҳ����Ž�
//	           int count=0;
	            for (String key : broadcasts.keySet()) {
//	            	count++;
//	            	if(count == broadcasts.keySet().size()) break;
	                //��ʱ�����ÿ�
	                tempSet.clear();
	                //��broadcast��һ��value��ȡ�����е���,����tempSet
	                HashSet<String> areas = broadcasts.get(key);
	                tempSet.addAll(areas);
	                //��tempSet��ʣ��δѡ�������Ӽ�����ֵ��tempSet
	                tempSet.retainAll(allAreas);
	                //���tempSet��Ϊ0,��maxKeyΪ�ջ��ߵ�ǰtempSet�ĵ�����������maxKey�ĵ�������ʱ,����maxKey
	                if (tempSet.size() > 0 && (maxKey == null || tempSet.size() > broadcasts.get(maxKey).size())) {
	                    maxKey = key;
	                }
	            }
	            //���maxKey��Ϊ��
	            if(maxKey != null){
	                //ѭ������ʱ,maxKey�Ǳ������Ž�
	                //����ѡ��ĵ���
	            	//TraceID2Trace
	            	sampleLog.add(TraceID2Trace.get(maxKey));
	            	originalLog.remove(TraceID2Trace.get(maxKey));
	            	//��Ź켣
	            	traceSet.add(TraceID2Trace.get(maxKey));
	            	//if()
	                selects.add(maxKey);
	                //��allAreas���Ƴ���ѡ���
	                allAreas.removeAll(broadcasts.get(maxKey));
	            }
	            num--;
	        }
//	     System.out.println("originalLog.size():"+originalLog.size());	 
//	     System.out.println("sampleLog.size():"+sampleLog.size());	    
		//return the sample log.
		return sampleLog;

		}	            
		/**
	     * ����Ƿ�ȫ��������(ֻҪ��һ����δ����������false)
	     *
	     * @return boolean
	     */
	    public static boolean isAllVisited(HashMap<XTrace,Boolean> StatusVisited) {
	    	Map<XTrace, Boolean> map4 =StatusVisited ;
			for(Entry<XTrace, Boolean> entry : map4.entrySet()){
				if(!entry.getValue()) {
					return false;
				}
			}
	        return true;
	    }
		/**
		 * 
		 * @param originalLog
		 * @param sampleLog
		 * @return ��ǰ���۵�ֵ
		 */
		public static double CurrentCost(HashMap<String,Double> DfrNumber_except_original,XLog sampleLog) {
			HashMap<String,Integer> DfrNumber_sample=SampleEvalutionPlugin.getDfrNumber(sampleLog);
			//2.�Գ�ƽ����� sMAPE
			Map<String, Double> map2 = DfrNumber_except_original;

			double abs_sum=0;
			for(Map.Entry<String, Double> entry : map2.entrySet()){
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
//			System.out.println("sMAPE_value(ָ��2) = " + sMAPE_value);
			return sMAPE_value;
		}
		
		
		/**
		 * 
		 * @param originalLog
		 * @param sampleLog
		 * @return ��ǰ���۵�ֵ
		 */
		public static double CurrentCost(HashMap<String,Double> DfrNumber_except_original,XLog sampleLog,XTrace trace) {
			XLog sampleLog2 = new XLogImpl(sampleLog.getAttributes());
			sampleLog2=(XLog) sampleLog.clone();//����Ŀ�¡
			sampleLog2.add(trace);
			HashMap<String,Integer> DfrNumber_sample=SampleEvalutionPlugin.getDfrNumber(sampleLog2);


			//2.�Գ�ƽ����� sMAPE
			Map<String, Double> map2 = DfrNumber_except_original;

			double abs_sum=0;
			for(Map.Entry<String, Double> entry : map2.entrySet()){
//				fenmu+= entry.getValue();
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
//			System.out.println("sMAPE_value(ָ��2) = " + sMAPE_value);
			return sMAPE_value;
		}
		
		/************************************************�����㷨***************************************/
	    // Map��valueֵ��������
	    public static <K, V extends Comparable<? super V>> Map<K, V> sortDescend(Map<K, V> map) {
	        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
	        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
	            @Override
	            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
	                int compare = (o1.getValue()).compareTo(o2.getValue());
	                return -compare;
	            }
	        });
	 
	        Map<K, V> returnMap = new LinkedHashMap<K, V>();
	        for (Map.Entry<K, V> entry : list) {
	            returnMap.put(entry.getKey(), entry.getValue());
	        }
	        return returnMap;
	    }
	 
	    // Map��valueֵ��������
	    public static <K, V extends Comparable<? super V>> Map<K, V> sortAscend(Map<K, V> map) {
	        List<Map.Entry<K, V>> list = new ArrayList<Map.Entry<K, V>>(map.entrySet());
	        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
	            @Override
	            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
	                int compare = (o1.getValue()).compareTo(o2.getValue());
	                return compare;
	            }
	        });
	 
	        Map<K, V> returnMap = new LinkedHashMap<K, V>();
	        for (Map.Entry<K, V> entry : list) {
	            returnMap.put(entry.getKey(), entry.getValue());
	        }
	        return returnMap;
	    }
                                                                                                                                                                           
	
}
