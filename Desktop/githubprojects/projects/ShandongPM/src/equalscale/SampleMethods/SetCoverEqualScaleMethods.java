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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.deckfour.xes.extension.std.XConceptExtension;
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


public class SetCoverEqualScaleMethods {
	@Plugin(
			name = " (set cover)sampling Plugin",// plugin name
//			name="Similarity-based  Sample Log Evaluation Method Plugin",
			returnLabels = {"Sample Log"}, //return labels
			returnTypes = {XLog.class},//return class
//			returnLabels = {"Similarity Value"}, //return labels
//			returnTypes = {String.class},//return class
			//input parameter labels, corresponding with the second parameter of main function
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
			double currentCost=0.0;//��ǰ����
			HashMap<XTrace,Boolean> StatusVisited=new HashMap<>();//����״̬
			HashMap<XTrace,Double> traceWeight=new HashMap<>();//Ȩ��ϵ��
			
			
			XLog newOriginalLog = new XLogImpl(originalLog.getAttributes());
			newOriginalLog=(XLog) originalLog.clone();//����Ŀ�¡
			
			
			
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
			System.out.println("ԭʼ��DfrNumber_except_original:"+DfrNumber_except_original);
			System.out.println("ԭʼ��DfrNumber_except_original0:"+DfrNumber_except_original0);
			System.out.println("ԭʼ��DfrNumber_except_original1:"+DfrNumber_except_original1);
			System.out.println("ԭʼ��DfrNumber_except_original0.size():"+DfrNumber_except_original0.size());
			System.out.println("originalLog.size():"+originalLog.size());
			int orgSize=originalLog.size();
			int count=1;
			while(!isMinVisited(DfrNumber_except_original0) && count <=orgSize*SampleRatio) {
				
				
				Map<String, Double> map3 = DfrNumber_except_original;
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
				System.out.println("��"+count+"��ѭ��:");
				System.out.println("DfrNumber_except_original0:"+DfrNumber_except_original0);
				System.out.println("DfrNumber_except_original1:"+DfrNumber_except_original1);
				
				XLog log = new XLogImpl(originalLog.getAttributes());
				log=SetCover(originalLog,DfrNumber_except_original0);//���ϸ����㷨
				//����ж�����Ϊ�������ּ���Ϊ1���������������Ҫ�����趨�� 
				double allCurrentCost1=0.0;
				if(DfrNumber_except_original0.size()<2 ) {
					allCurrentCost1=CurrentCost(DfrNumber_except_org,sampleLog);
					System.out.println("��ǰ�Ļ���ֵΪ:" +allCurrentCost1);
				}//���������ж�
				
				
				//���������˼��ԭʼ��־������������df����1�ļ���
				//���Ϊ����������־
				//���켣��ӵ�������־��
				System.out.println("��"+count+"�μ���Ĺ켣:");
				for(XTrace trace:log)
				{	
					System.out.print(" " +trace.getAttributes().get("concept:name").toString());
					sampleLog.add(trace);
				}
				double allCurrentCost2=0.0;
				if(DfrNumber_except_original0.size()<2 ) {
					allCurrentCost2=CurrentCost(DfrNumber_except_org,sampleLog);
					System.out.println("����nameֵΪ:" +allCurrentCost2);
					
				}//���������ж�
				if(allCurrentCost1 < allCurrentCost2) {
					for(XTrace trace:log)
					{	
						sampleLog.remove(trace);
					}
					break;
				}
//				//��������1����
//				Map<String, Double> map2 = DfrNumber_except_original0;
//				for(Map.Entry<String, Double> entry : map2.entrySet()){
////					DfrNumber_except_original0.clear();
//					if(entry.getValue()-count>1) {
//						DfrNumber_except_original0.put(entry.getKey(),entry.getValue()-count);//��ʵ�е��ظ���
////						DfrNumber_except_original.put(entry.getKey(), entry.getValue()-1);
//						number++;
//					}
//					else {
//						DfrNumber_except_original0.put(entry.getKey(),(double) -1000);
////						DfrNumber_except_original.remove(entry.getKey());
//					}
//				}
				System.out.println("2222DfrNumber_except_original0.size():"+DfrNumber_except_original0.size());

				
				//����������־������dfֵ����
				DfrNumber_except_original.clear();
				//����dfr������ֵ
				HashMap<String,Integer> DfrNumber_originalNew=SampleEvalutionPlugin.getDfrNumber(originalLog);
				System.out.println("2222originalLog.size():"+originalLog.size());
				//���������裬����ΪSampleRatio,Ĭ��Ϊ30%
				Map<String, Integer> map4 = DfrNumber_originalNew;
				
				
				System.out.println("2222DfrNumber_originalNew:"+DfrNumber_originalNew);
				for(Map.Entry<String, Integer> entry : map4.entrySet()){
					if(entry.getValue()*SampleRatio-count>0) {
    					DfrNumber_except_original.put(entry.getKey(), entry.getValue()*SampleRatio-count);
					}else {
						DfrNumber_except_original.put(entry.getKey(), (double) -1000);
					}
					
				}
				System.out.println("Finsh update:DfrNumber_except_original:"+DfrNumber_except_original);
				count++;
			}
			//�����һ����ʲô��˼��
		    System.out.println("sampleLog size�� " + sampleLog.size());
			double allCurrentCost00=CurrentCost(DfrNumber_except_org,sampleLog);
			System.out.println("����nameֵΪ:" +allCurrentCost00);
			/*XLog log11 = new XLogImpl(originalLog.getAttributes());
			log11=SetCover(originalLog,DfrNumber_except_original1);
			for(XTrace trace:log11)
			{	
				sampleLog.add(trace);
			}
			System.out.println("111111sampleLog1 size�� " + sampleLog.size());
			double allCurrentCost3=CurrentCost(DfrNumber_except_org,sampleLog);
			System.out.println("111111����nameֵΪ:" +allCurrentCost3);
			
			HashMap<String,Double> DfrNumber_except_original1_new=new HashMap<>();//����dfֵ����1�ļ���
			System.out.println("DfrNumber_except_original1:" +DfrNumber_except_original1);
			System.out.println("DfrNumber_except_original1.size():" +DfrNumber_except_original1.size());*/
			
//			Map<String, Double> map5 = DfrNumber_except_original1;
//			for(Map.Entry<String, Double> entry : map1.entrySet()){
//				if(entry.getValue()>0 && entry.getValue()<1) {
//					DfrNumber_except_original1.put(entry.getKey(),entry.getValue());
//				}else if(entry.getValue()>1) {
//					DfrNumber_except_original0.put(entry.getKey(),entry.getValue());
//				}
//			}
	/*
			//1.��ʼ����ϵ�������켣Ȩ�ص�һ�γ�ʼ��traceWeight,���켣״̬������Ϊδ����
			for (XTrace trace: originalLog)	{   ///all trace size!=0
				XLog sampleLog1 = new XLogImpl(originalLog.getAttributes());
				sampleLog1=(XLog) sampleLog.clone();//����Ŀ�¡
				sampleLog1.add(trace);
				currentCost=CurrentCost(DfrNumber_except_original1,sampleLog1);//��ǰԽСԽ��
				StatusVisited.put(trace, false);//��ʼ��ȫ���Ĺ켣��Ϊδ����״̬
				traceWeight.put(trace, currentCost);
			}		
			//2.ѡ����С��mseֵ��Ϊ��ʼ��
			Map<XTrace, Double> map3 = sortAscend(traceWeight);
//			int count=0;
			XTrace FirstTrace=null;
			for(Entry<XTrace, Double> entry : map3.entrySet()){
//				System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
//				if(count ==0) {
					sampleLog.add(entry.getKey());//����Сֵ��ӵ�������־��
					FirstTrace=entry.getKey();
					System.out.println("��ѡ��ĵ�һ���켣 Ϊ= " + entry.getKey().getAttributes().get("concept:name").toString());
//					System.out.println("NAMPEֵΪ�� " + CurrentCost(DfrNumber_except_original,sampleLog));		
					break;
//				}
			}

//			
			//˼·������
			
			//trace_pearson���� key���켣��value��weightֵ
			//status key:trace  value: boolean
			//������һ��weight hashmap  key:trace value:name(double)
			//path:��¼ǰ���� trace   trace
			//�ж�������weight.get(trace0).value + currentcost(tarce1) <= weight.get(trace1)
			
			
			//4.����
			//���ʶ�Ĺ켣Ϊ��һ���켣
			XTrace trace00=FirstTrace;
			double PrecurrentCost=currentCost;//��ǰ����
			boolean flag=true;
			
			
//			Map<XTrace, Double> textmap3 = traceWeight;
			
			
			while(!isAllVisited(StatusVisited) && flag) {//��ǰ������״̬�������ʵ����������ֵС��һ����ֵ(0.01)����ֹѭ��	
//			while(flag) {//��ǰ������״̬�������ʵ����������ֵС��һ����ֵ(0.01)����ֹѭ��	
				// ������ڵ�����Ϊ�ѷ���
				StatusVisited.put(trace00, true);
	            // �鿴�ڽӾ�������ָ���ڵ��ڽӵĽڵ�
//		     	int value=0;
	            for (XTrace trace:originalLog) {
	            	if(!StatusVisited.get(trace)) {
	                    // ���ܵ���·��Ȩֵ: ���ʼ��ָ����㵽������㵽�ýڵ��·��Ȩֵ�ܺ�
	                    double newWeight;//�켣����Ȩ��
	                    // ����ڵ�δ����, �����ڽӽڵ�  д����
	                    newWeight=CurrentCost(DfrNumber_except_original1,sampleLog,trace);
	                    if(newWeight<traceWeight.get(trace)) {//������������㣿
	                    	// ����¸ýڵ����С·��ֵ, ���¸ýڵ��ǰ��Ϊ�������
	                    	traceWeight.put(trace,newWeight);   //����Ȩ��
	                    	
	                    }
	            	}
	            }
	          //1.ѡ����һ����ֵ��С�Ľڵ���δ�����ʹ���,��ʱ�켣Ȩ����Ҫ����
	    		Map<XTrace, Double> map4 = sortAscend(traceWeight);
	    		int count1=0;
	    		for(Entry<XTrace, Double> entry : map4.entrySet()){
	    			if(count1 ==0 && !StatusVisited.get(entry.getKey()) ) {
	    				sampleLog.add(entry.getKey());//����������־
	    				trace00=entry.getKey();
	    				originalLog.remove(entry.getKey());//�������켣�Ƴ�
	    				System.out.println("*******************************");
	    				System.out.println("��ѡ��Ĺ켣 Ϊ= " + entry.getKey().getAttributes().get("concept:name").toString());
//	    				System.out.println("NAMPEֵΪ�� " + CurrentCost(DfrNumber_except_original,sampleLog));		
	    				break;
	    			}
	    		}
	    		double allCurrentCost=CurrentCost(DfrNumber_except_original1,sampleLog);
	    		double allCurrentCost2=CurrentCost(DfrNumber_except_org,sampleLog);
	    		System.out.println("��ǰֵ allCurrentCost:" +allCurrentCost);
	    		System.out.println("��ǰֵ PrecurrentCost:" +PrecurrentCost);
	    		System.out.println("������ allCurrentCost:" +allCurrentCost2);
//	    		if(PrecurrentCost > allCurrentCost && allCurrentCost >0.1) {
	    		if( PrecurrentCost > allCurrentCost) {//����ֲ����Ž�   �ж������ǲ���Ҫ��һ�£����ǵ����ݼ���
	    			PrecurrentCost=allCurrentCost;
	    			System.out.println("111��ǰֵ���ڵ�ǰֵ ����" );	
	    		}else {
	    			flag=false;
	    			System.out.println("����nameֵΪ:" +allCurrentCost);
	    			System.out.println("��ǰֵС�ڵ�ǰֵ ������" );	
	    		}
	    		
	    		
	            // �������from����Ϊ: weights��������ֵ��С�Ĳ���δ���ʵĽڵ�
	    		//����Ȩ��
	    		for (XTrace trace: originalLog)	{   ///all trace size!=0
	    			if(!StatusVisited.containsKey(trace)) {
		    			XLog sampleLog3 = new XLogImpl(originalLog.getAttributes());
		    			sampleLog3=(XLog) sampleLog.clone();//����Ŀ�¡
		    			sampleLog3.add(trace);
		    			currentCost=CurrentCost(DfrNumber_except_original1,sampleLog3);//��ǰԽСԽ��
		    			traceWeight.put(trace, currentCost);
	    			}
	    		}
//	    		countValue++;
			}
		*/
//			return sampleLog;
			return sampleLog;
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
