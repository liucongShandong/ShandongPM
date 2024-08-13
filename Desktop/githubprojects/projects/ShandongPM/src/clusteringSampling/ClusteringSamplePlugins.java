package clusteringSampling;


/*
 * A fast algorithm to sample log. 
 * The original idea is referred to "The automatic creation of literature abstracts". 
 * 
 * rank a trace by its significance;
 * the significance of a trace is determined by the combination of its activity significance and dfr significance. 
 * ��־�����Բ���
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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


public class ClusteringSamplePlugins {
	@Plugin(
			name = "Clustering-based Event Log Sampling",// plugin name
			returnLabels = {"Sample Log"}, //return labels
			returnTypes = {XLog.class},//return class
			parameterLabels = {"Orginal Event Log"},
			userAccessible = true,
			help = "This plugin aims to the orginal log ,returns a sample log  by clustering-based sampling methods." 
			)
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "XXX", 
	        email = "XXX"
	        )
	@PluginVariant(
			variantLabel = "Sampling Big Event Log, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public static XLog GraphSampling(UIPluginContext context,XLog originalLog) throws UserCancelledException
	{
		//UI
		//select two types of event logs, lifecycle event log and normal event log.  
		String [] GraphSamplingTechnique = new String[4];
		GraphSamplingTechnique[0]="K-means";
		GraphSamplingTechnique[1]="ActiTrac";
		GraphSamplingTechnique[2]="Guide Miner Tree";
		String selectedTechnique =ProMUIHelper.queryForObject(context, "Select the clustering techniques", GraphSamplingTechnique);
		context.log("The selected graph sampling technique is: "+selectedTechnique, MessageLevel.NORMAL);	
		System.out.println("Selected selected graph sampling technique is: "+selectedTechnique);
		
		//select two types of event logs, lifecycle event log and normal event log.  
		String [] logType = new String[2];
		logType[0]="LogRank-based Sampling";
		logType[1]="LogRank+-based Sampling";
		String selectedLogType =ProMUIHelper.queryForObject(context, "Select the sampling techniques", logType);
		context.log("The selected log type is: "+selectedLogType, MessageLevel.NORMAL);	
		System.out.println("Selected log type is: "+selectedLogType);
				
		

		
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
		for(int i=0;i<TraceIdList.size();i++) {
			System.out.println("TraceIdList"+i+":"+TraceIdList.get(i));
			System.out.println(TraceID2Trace.get(TraceIdList.get(i)));
		}
		//trace to activity set
		HashMap<String, HashSet<String>> traceIDToActivitySet = new HashMap<>();
		
		//trace to direct-follow relation set
		HashMap<String, HashSet<String>> traceIDToDFRSet = new HashMap<>();
		for(XTrace trace:originalLog)
		{
			HashSet<String> activitySet = new HashSet<>();
			HashSet<String> dfrSet = new HashSet<>();
			for(int i =0;i<trace.size();i++)
			{
				//add activity
				activitySet.add(XConceptExtension.instance().extractName(trace.get(i)));
			}
			for(int i =0;i<trace.size()-1;i++)
			{
				//add directly follow pair
				dfrSet.add(XConceptExtension.instance().extractName(trace.get(i))+","+XConceptExtension.instance().extractName(trace.get(i+1)));
			}
			traceIDToActivitySet.put(XConceptExtension.instance().extractName(trace), activitySet);
			traceIDToDFRSet.put(XConceptExtension.instance().extractName(trace), dfrSet);
		}
		
		//direct-follow relation set of the log
		HashSet<String> dfrSetLog = new HashSet<>();
		for(String traceID: traceIDToDFRSet.keySet())
		{
			dfrSetLog.addAll(traceIDToDFRSet.get(traceID));
		}
		//�õ���־�Ŀ�ʼ-�������� ������־�켣
		//trace to direct-follow relation set
//		HashSet<String> initialSet=
//		HashMap<String, HashSet<String>> traceIDToDFRSet = new HashMap<>();
		for(XTrace trace:originalLog)
		{
			HashSet<String> activitySet = new HashSet<>();
			HashSet<String> dfrSet = new HashSet<>();
			for(int i =0;i<trace.size();i++)
			{
				//add activity
				activitySet.add(XConceptExtension.instance().extractName(trace.get(i)));
			}
		}
		 /**
		  * 0.������
		  * ��1�����ڳ��ȵ�
		  * ��2������Ƶ�ʵ�
		  */
		 /*****************************����1�����ڳ��ȵ�******************/
		if(selectedTechnique.equals("Trace Length-based Graph Sampling"))
		{
			HashMap<XTrace,String> traceLabel=new HashMap<>();
			HashSet<String> traceVariantLabel=new HashSet<>();
			HashMap<XTrace,Integer> traceLength=new HashMap<>();
			//step0:���ÿ���켣��Ӧ�ĳ���,�Թ켣���Ƚ��д洢
			for (XTrace trace: originalLog)	{   ///all trace size!=0
				String mergeTransition="";
				for(XEvent event: trace){  
					String transition = event.getAttributes().get("concept:name").toString();       //Resource
					mergeTransition=mergeTransition+transition;
				}
				traceVariantLabel.add(mergeTransition);
				traceLabel.put(trace, mergeTransition);
				traceLength.put(trace, mergeTransition.length());
			}
			
			//step1-0:��õ�һ���� variant
			for(XTrace trace:originalLog) {
				String m1=traceLabel.get(trace);
				if(traceVariantLabel.contains(m1)) {
					traceVariantLabel.remove(m1);
				}else {
					traceLength.remove(trace);//ɾ���켣��ͬ�Ĺ켣
				}
			}
			//step2-1:���򣬽���   ����ֵ�Լ����� traceFrequency
		      List<Map.Entry<XTrace,Integer>> list1 = new ArrayList<Map.Entry<XTrace,Integer>>(traceLength.entrySet());
		      list1.sort(new Comparator<Map.Entry<XTrace,Integer>>() {
		            @Override
		            public int compare(Map.Entry<XTrace,Integer> o1, Map.Entry<XTrace,Integer> o2) {
		                return o2.getValue().compareTo(o1.getValue());
		            }
		       });
		       //step3:���
		       for (int i = 0; i < list1.size(); i++) {
		           System.out.println("��i�Σ� " + i);
		           System.out.println(list1.get(i).getValue()+ ": " + list1.get(i).getKey() ); 
		           System.out.println( "name: " + XConceptExtension.instance().extractName(list1.get(i).getKey()));
		           System.out.println( "dfrSet: " + traceIDToDFRSet.get(XConceptExtension.instance().extractName(list1.get(i).getKey())));
		           System.out.println( "�ܵ�dfrSetLog: " + dfrSetLog);
		           //ȡ�
		         if(dfrSetLog.removeAll(traceIDToDFRSet.get(XConceptExtension.instance().extractName(list1.get(i).getKey())))) {
						sampleLog.add(list1.get(i).getKey());
				 }
	//	          System.out.println( "�������dfrSetLog: " + dfrSetLog);
				  if(dfrSetLog.size()==0) break;	  
		        }
		}
		/********************************����2�����ڹ켣Ƶ����******************************/
		if(selectedTechnique.equals("Trace Frequency-based Graph Sampling")) {
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
//			 for (XTrace key : traceFrequency.keySet()){
//					System.out.println("traceFrequency key: "+ XConceptExtension.instance().extractName(key) + "; traceFrequency value: " + traceFrequency.get(key));
//			 }
			//step2-1:���򣬽���   ����ֵ�Լ����� traceFrequency
		      List<Map.Entry<XTrace,Integer>> list1 = new ArrayList<Map.Entry<XTrace,Integer>>(traceFrequency.entrySet());
		      list1.sort(new Comparator<Map.Entry<XTrace,Integer>>() {
		            @Override
		            public int compare(Map.Entry<XTrace,Integer> o1, Map.Entry<XTrace,Integer> o2) {
		                return o2.getValue().compareTo(o1.getValue());
		            }
		       });
		       //step3:���
		       for (int i = 0; i < list1.size(); i++) {
		           System.out.println("��i�Σ� " + i);
		           System.out.println(list1.get(i).getValue()+ ": " + list1.get(i).getKey() ); 
		           System.out.println( "name: " + XConceptExtension.instance().extractName(list1.get(i).getKey()));
		           System.out.println( "dfrSet: " + traceIDToDFRSet.get(XConceptExtension.instance().extractName(list1.get(i).getKey())));
		           System.out.println( "�ܵ�dfrSetLog: " + dfrSetLog);
		           //ȡ�
		         if(dfrSetLog.removeAll(traceIDToDFRSet.get(XConceptExtension.instance().extractName(list1.get(i).getKey())))) {
						sampleLog.add(list1.get(i).getKey());
				 }
//		          System.out.println( "�������dfrSetLog: " + dfrSetLog);
				  if(dfrSetLog.size()==0) break;	  
		        }
		}	
		
		/**
		  * ����2.������ �������� �в�ȷ����
		  * dfrSetLog:��ʾ��־�����е�ֱ�Ӹ�����ϵ��    HashSet<String>
		  * traceIDToDFRSet ��key���켣����value:ֱ�Ӹ��� HashMap<String, HashSet<String>>
		  */
		HashSet<String> StartSet = new HashSet<>();
		HashSet<String> EndSet = new HashSet<>();
		HashSet<String> StartAndEndSet = new HashSet<>();
		for(XTrace trace:originalLog)
		{
//			HashSet<String> activitySet = new HashSet<>();
//			HashSet<String> dfrSet = new HashSet<>();
			StartSet.add(XConceptExtension.instance().extractName(trace.get(0)));
			EndSet.add(XConceptExtension.instance().extractName(trace.get(trace.size()-1)));
			StartAndEndSet.add(XConceptExtension.instance().extractName(trace.get(0))+","+
			XConceptExtension.instance().extractName(trace.get(trace.size()-1)));
		}
		System.out.println("StartSet-------------------------->"+StartSet);
		System.out.println("EndSet---------------------------->"+EndSet);
		System.out.println("StartSet.size()--------------+++++++++++>"+StartSet.size());
		System.out.println("EndSet.size()--------------+++++++++++>"+EndSet.size());
		if(selectedTechnique.equals("Brute Force Graph Sampling")) {
			 
//			 for(int i=0;i<TraceIdList.size();i++) {
////				 System.out.println("��i�α���:"+i+"dfrSetLog:"+dfrSetLog);
//				 //� dfrSetLog���¹켣�н�����ô������뵽������־��
//				 if(dfrSetLog.removeAll(traceIDToDFRSet.get(TraceIdList.get(i)))) {
//					 sampleLog.add(TraceID2Trace.get(TraceIdList.get(i)));
//				 }
//				 if(dfrSetLog.size()==0) break;	 
//				 
//			 }
			 
			 
//			 for(XTrace trace:originalLog)
//				{
//					
//					if(dfrSetLog.removeAll(traceIDToDFRSet.get(XConceptExtension.instance().extractName(trace))))
//					{
//						sampleLog.add(trace);
//					}
//					//dfrSetLog.remove(traceIDToDFRSet);
//					//System.out.println("//////dfrSetLog//////////");	
//					 if(dfrSetLog.size()==0) break;	 
//				}
//			System.out.println("/////��һ��//////");
			for(XTrace trace:originalLog)
			{	
				System.out.println("////////////////////////////");
				String StartEvent = XConceptExtension.instance().extractName(trace.get(0));
				String EndEvent = XConceptExtension.instance().extractName(trace.get(trace.size()-1));
				String StartAndEndEvent = StartEvent+","+EndEvent;
				System.out.println("11111111111111111111");
//				if((StartAndEndSet.size() != 0) || (dfrSetLog.size() !=0)){
//					int flag1=0;
//					int flag2=0;
//					System.out.println("222����ǰ��2222");
//					System.out.println("StartAndEndEvent��"+StartAndEndEvent);
//					System.out.println("StartAndEndSet��"+StartAndEndSet);
//					System.out.println("dfrSetLog��"+dfrSetLog);
//					if(StartAndEndSet.remove(StartAndEndEvent)) {
//						flag1=1;
//					}
//					if(dfrSetLog.removeAll(traceIDToDFRSet.get(XConceptExtension.instance().extractName(trace))))
//					{
//						flag2=1;
//					}
//					if((flag1 ==1) || (flag2 ==1)) {
//						System.out.println("true!!!!!!!!!!!!!!!");
//						sampleLog.add(trace);
//					}
//				}else {
//					System.out.println("333333333333333333333333");
//					break;
//				}
//				System.out.println("******************************");
				if((StartSet.size()!=0)||(EndSet.size()!=0)||(dfrSetLog.size()!=0)) {
					
					int flag1=0;
					int flag2=0;
					int flag3=0;
					System.out.println("222����ǰ��2222");
					System.out.println("StartSet��"+StartSet);
					System.out.println("EndSet��"+EndSet);
					System.out.println("StartEvent:"+StartEvent);
					System.out.println("EndEvent:"+EndEvent);
					if(StartSet.remove(StartEvent)) {
						flag1=1;
					}
					if(EndSet.remove(EndEvent))
					{
						flag2=1;
					}
					if(dfrSetLog.removeAll(traceIDToDFRSet.get(XConceptExtension.instance().extractName(trace))))
					{
						flag3=1;
					}
					if((flag1==1)||(flag2==1)||(flag3==1))
					{
						System.out.println("true!!!!!!!!!!!!!!!");
						sampleLog.add(trace);
					}
					System.out.println("StartSet.size():"+StartSet.size());
					System.out.println("EndSet.size():"+EndSet.size());
					System.out.println("dfrSetLog.size():"+dfrSetLog.size());
				}else{
					break;
				}
				System.out.println("******************************");
			}
		}
		
		/**
		 * ����3.���ϸ����㷨
		 */	
		if(selectedTechnique.equals("Simple Set covering Graph Sampling")) {

//			 �����㲥��̨,���뵽Map
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
	        //��ʣ��ĵ�����Ϊ0ʱ,����ѡ��
	        while (allAreas.size() != 0) {
	            //ָ���ÿ�
	            maxKey = null;
	            //�������еĵ���,�ҳ����Ž�
	            for (String key : broadcasts.keySet()) {
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
	            	//��Ź켣
	            	traceSet.add(TraceID2Trace.get(maxKey));
	            	//if()
	                selects.add(maxKey);
	                //��allAreas���Ƴ���ѡ���
	                allAreas.removeAll(broadcasts.get(maxKey));
	            }
	        }
	        //�Ƴ���һ���㷨�еĿ�ʼ�¼��ͽ����¼�
	      //��ǿforѭ��
	        for(XTrace trace : traceSet){
	        	String StartEvent = XConceptExtension.instance().extractName(trace.get(0));
				String EndEvent = XConceptExtension.instance().extractName(trace.get(trace.size()-1));
				StartSet.remove(StartEvent);
				EndSet.remove(EndEvent);
	        }
	        
			for(XTrace trace:originalLog)
			{	
				if(traceSet.contains(trace)) {
					continue;
				}
				System.out.println("////////////////////////////");
				String StartEvent = XConceptExtension.instance().extractName(trace.get(0));
				String EndEvent = XConceptExtension.instance().extractName(trace.get(trace.size()-1));
				System.out.println("11111111111111111111");
				if((StartSet.size()!=0)||(EndSet.size()!=0)) {
					
					int flag1=0;
					int flag2=0;
					System.out.println("222����ǰ��2222");
					System.out.println("StartSet��"+StartSet);
					System.out.println("EndSet��"+EndSet);
					System.out.println("StartEvent:"+StartEvent);
					System.out.println("EndEvent:"+EndEvent);
					if(StartSet.remove(StartEvent)) {
						flag1=1;
					}
					if(EndSet.remove(EndEvent))
					{
						flag2=1;
					}
					if((flag1==1)||(flag2==1))
					{
						System.out.println("true!!!!!!!!!!!!!!!");
						sampleLog.add(trace);
					}
					System.out.println("StartSet.size():"+StartSet.size());
					System.out.println("EndSet.size():"+EndSet.size());
					System.out.println("dfrSetLog.size():"+dfrSetLog.size());
				}else{
					break;
				}
				System.out.println("******************************");
			}
		}
		
		 /**
		  * 2,��������		������
		  * ���������� ���������������־�Ĺ켣��ֱ�Ӹ�����  ��ֵ��Ӧʲô
		  * ÿ����Ʒ�������ԣ���Ʒ��Ӧ�켣��������Ӧ�켣�е�ֱ�Ӹ����ϵ����ֵ��Ӧ����
		  * dfrSetLog:��ʾ��־�����е�ֱ�Ӹ�����ϵ�� ��С���൱������ HashSet<String>
		  * traceIDToDFRSet ��key���켣����value:ֱ�Ӹ��� HashMap<String, HashSet<String>>
		  * �������ڼ�ֵ ��һ��
		  * �൱��һ��װ����
		  * 1���켣�������ֱ�Ӹ��� һ��װ����ô��ֱ�Ӹ���
		  * ����ֵ��Ȩ����ʱ����
		  */
//		 int V= dfrSetLog.size();//��ʼ��������������������Ϊ�����¼���־��ֱ�Ӹ�����ϵ��
//		 int N=TraceIdList.size();//��ʼ������ĸ�����������Ϊ�켣�ĸ�������������sh�Ĺ켣�������ظ�
//		 //���ڴ洢ÿ��������������±��1��ʼ
//		 //����Ҫ��ʼ��ÿ��������������ֵ
//		 //�����Ļ���ÿ���켣������ֱ�Ӹ�����
//		 //��ʼ���������ֵ
//		 int[] weight= {0};
//		 int[] value= {0};
//		 for(int i=0;i<N+1;i++)//��һ��������
//		 {
//			 weight[i+1]=traceIDToDFRSet.get(TraceIdList.get(0)).size();
//			 value[i+1]=traceIDToDFRSet.get(TraceIdList.get(0)).size();
//			 System.out.println(" weight[i+1]:"+i+" "+weight[i+1]);
//		 }
//		 System.out.println(" value: "+value);
//		 //�������͸���ԭ����V��value
//		 //������Ļ��Ͳ�����
//		 //VΪ�
//		 //V=V-dfrSetLog�����Ĺ켣��ֱ�Ӹ����Ľ����Ĵ�С
//		 //value=dfrSetLog��ù켣��ֱ�Ӹ����Ľ����Ĵ�С
//		 for(int i=0;i<N+1;i++) {
//			 boolean notContains1=dfrSetLog.removeAll(traceIDToDFRSet.get(TraceIdList.get(i)));
////			 N=N-dfrSetLog.retainAll(traceIDToDFRSet.get(TraceIdList.get(0))).size();
//			 System.out.println(" notContains1: "+notContains1);
//			 N=dfrSetLog.size();
//			 HashSet<String> newDfrSetLog=dfrSetLog;
//			 boolean isContains=newDfrSetLog.retainAll(traceIDToDFRSet.get(TraceIdList.get(i)));
//			 System.out.println(isContains);
//			 value[i+1]=newDfrSetLog.size();
//		 }
//		    
		//return the sample log.
		return sampleLog;
//		return "";	
		}                                                                                                                                                                                          
	
}
